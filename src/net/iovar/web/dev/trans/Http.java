/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev.trans;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.bin.shell.task.Return;
import net.iovar.web.dev.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Set;

// 3rd-party imports:
//import me.idfree.lib.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;

/**
 *
 * @author  shawn@lannocc.com
 */
public class Http extends Transport
{
    /**
     * Default constructor is necessary and used for servlet installation only.
     */
    public Http ()
    {
        // no-op
    }
    
    /*
    public Http (final String path)
    {
        this (path, null);
    }
    
    public Http (final String path, final Map<String,List<String>> params)
    {
        setPath (path);
        setParams (params);
    }
    */
    
    public Http (final Local local)
    {
        setContext (local.getContext ());

        final String root = (String) context.getInitParameter (Utils.INIT_PARAM_LOOPBACK);
        Log.debug ("root: "+root);
        
        setPath ("//"+root+"/"+context.getContextPath ()+"/"+local.getPath ());
        setParams (local.getParams ());
        setSession (local.getSession ());
        
        // SECURITY: for local loopback we use the user's session
        if (htsession!=null)
        {
            if (params==null) params = new HashMap<String,List<String>> ();
            List<String> sessparam = params.get (Shell.EXT_PARAM_SESSION);
            if (sessparam==null)
            {
                sessparam = new ArrayList<String> ();
                final String id = Sessions.getIdFromSession (htsession);

                if (id!=null)
                {
                    sessparam.add (id);
                    Log.info ("[HTSESSION] added session parameter ("+ sessparam + "): "+local);
                    params.put (Shell.EXT_PARAM_SESSION, sessparam);
                }
                else
                {
                    Log.warn ("[HTSESSION] have htsession but no existing shell session: "+local);
                }
            }
            else
            {
                Log.info ("[HTSESSION] already have a session parameter, keeping it (" + sessparam + "): "+local);
            }
        }
        else
        {
            Log.warn ("[HTSESSION] no htsession for loopback: "+local);
        }
    }
    
    protected void setPath (final String path)
    {
        this.path = path.replaceAll (" ", "%20");
    }
    
    protected URL getURL () throws MalformedURLException
    {
        final String query = params!=null ? ("?"+Utils.buildQuery (params)) : "";
        return new URL (new URL ("http", "", ""), path+query);
    }
    
    protected CookieStore getCookies ()
    {
        if (htsession == null)
        {
            Log.warn ("SECURITY: no http session");
        }
        final String key = Http.class.getName () + (htsession!=null ? htsession.getId () : "");
        
        CookieStore cookies = (CookieStore) context.getAttribute (key);
        if (cookies==null)
        {
            Log.debug ("creating new cookie store");
            cookies = new BasicCookieStore ();
            context.setAttribute (key, cookies);
        }
        
        Log.debug ("cookie store: "+cookies);
        return cookies;
    }
    
    public InputStream get () throws IOException
    {
        final URL url = getURL ();
        Log.debug ("GET "+split (url));
        
        /*
        final HttpURLConnection http = (HttpURLConnection) url.openConnection ();
        http.setInstanceFollowRedirects (false);
        http.setRequestMethod ("GET");
        
        return http.getInputStream ();
        //return inputErrorCheck (http);
        */
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream (); // FIXME: #11
        final HttpClientBuilder htbuild = HttpClientBuilder.create ();
        htbuild.setDefaultCookieStore (getCookies ());
        
        final CloseableHttpClient http = htbuild.build (); try
        {
            http.execute (new HttpGet (url.toURI ()), new ResponseHandler ()
            {
                public Object handleResponse (final HttpResponse resp) throws IOException
                {
                    final StatusLine status = resp.getStatusLine ();
                    if (status.getStatusCode ()<200 || status.getStatusCode ()>=300)
                    {
                        throw new IOException ("non-ok status received: "+status);
                    }
                    
                    final HttpEntity entity = resp.getEntity ();
                    entity.writeTo (out);
                    
                    return null;
                }
            });
        }
        catch (final URISyntaxException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
        finally
        {
            http.close ();
        }
        
        return new ByteArrayInputStream (out.toByteArray ()); // FIXME: #11
    }
    
    /*
    static InputStream inputErrorCheck (final HttpURLConnection http) throws IOException
    {
        final InputStream in = http.getInputStream ();
        final InputStream err = http.getErrorStream ();
        
        if (err!=null || http.getResponseCode () < 200 || http.getResponseCode () >= 300)
        {
            throw new IOException ("status "+http.getResponseCode ());
        }
        
        return in;
    }
    */
    
    public InputStream put (final InputStream data) throws IOException
    {
        final URL url = getURL ();
        Log.debug ("PUT "+split (url));
        
        /*
        final HttpURLConnection http = (HttpURLConnection) url.openConnection ();
        http.setInstanceFollowRedirects (false);
        http.setRequestMethod ("PUT");
        http.setDoOutput (true);
        
        try
        {
            final OutputStream out = http.getOutputStream ();
            Utils.pipe (data, out);
        }
        catch (final IllegalArgumentException e)
        {
            Log.error ("cannot put HTTP resource `"+path+"': bad syntax: "+e.getMessage ());
            //setStatus (HttpServletResponse.SC_BAD_REQUEST);
//            Utils.status (resp, resp.SC_BAD_REQUEST, "cannot put HTTP resource `"+path+"': bad syntax: "+e.getMessage ());
        }
        catch (final UnknownHostException e)
        {
            Log.error ("cannot put HTTP resource `"+path+"': unknown host: "+e.getMessage ());
            //setStatus (HttpServletResponse.SC_NOT_FOUND);
//            Utils.status (resp, resp.SC_NOT_FOUND, "cannot put HTTP resource `"+path+"': unknown host: "+e.getMessage ());
        }
        catch (final IOException e)
        {
            Log.error ("cannot put HTTP resource `"+path+"': IOException: "+e.getMessage ());
            //setStatus (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            final String error = Utils.getError (http);
//            StringBuffer details = new StringBuffer ("server returned: "+http.getResponseMessage ());
//            if (error!=null) details.append ("\n").append (error);
            
//            Utils.status (resp, http.getResponseCode (), "cannot put HTTP resource `"+path+"': "+details.toString ());
        }
        
        return http.getInputStream ();
        */
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream (); // FIXME: #11
        final HttpClientBuilder htbuild = HttpClientBuilder.create ();
        htbuild.setDefaultCookieStore (getCookies ());
        
        final CloseableHttpClient http = htbuild.build (); try
        {
            final HttpPut put = new HttpPut (url.toURI ());
            put.setEntity (new InputStreamEntity (data));
            
            http.execute (put, new ResponseHandler ()
            {
                public Object handleResponse (final HttpResponse resp) throws IOException
                {
                    final StatusLine status = resp.getStatusLine ();
                    if (status.getStatusCode ()<200 || status.getStatusCode ()>=300)
                    {
                        throw new IOException ("non-ok status received: "+status);
                    }
                    
                    final HttpEntity entity = resp.getEntity ();
                    entity.writeTo (out);
                    
                    return null;
                }
            });
        }
        catch (final URISyntaxException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
        finally
        {
            http.close ();
        }
        
        return new ByteArrayInputStream (out.toByteArray ()); // FIXME: #11
    }
    
    public InputStream patch (final InputStream data, final String contentType) throws IOException
    {
        final URL url = getURL ();
        Log.debug ("PATCH "+split (url));
        
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream (); // FIXME: #11
        final HttpClientBuilder htbuild = HttpClientBuilder.create ();
        htbuild.setDefaultCookieStore (getCookies ());
        
        final CloseableHttpClient http = htbuild.build ();
        try
        {
            final HttpPatch patch = new HttpPatch (url.toURI ());
            patch.setEntity (new InputStreamEntity (data));
            
            http.execute (patch, new ResponseHandler ()
            {
                public Object handleResponse (final HttpResponse resp) throws IOException
                {
                    final StatusLine status = resp.getStatusLine ();
                    if (status.getStatusCode ()<200 || status.getStatusCode ()>=300)
                    {
                        throw new IOException ("non-ok status received: "+status);
                    }
                    
                    final HttpEntity entity = resp.getEntity ();
                    entity.writeTo (bytes);
                    
                    return null;
                }
            });
        }
        catch (final URISyntaxException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
        finally
        {
            http.close ();
        }
        
        return new ByteArrayInputStream (bytes.toByteArray ()); // FIXME: #11
    }
    
    public InputStream delete () throws IOException
    {
        final URL url = getURL ();
        Log.debug ("DELETE "+split (url));
        
        /*
        final HttpURLConnection http = (HttpURLConnection) url.openConnection ();
        http.setInstanceFollowRedirects (false);
        http.setRequestMethod ("DELETE");
        
        return http.getInputStream ();
        */
        
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream (); // FIXME: #11
        final HttpClientBuilder htbuild = HttpClientBuilder.create ();
        htbuild.setDefaultCookieStore (getCookies ());
        
        final CloseableHttpClient http = htbuild.build ();
        try
        {
            http.execute (new HttpDelete (url.toURI ()), new ResponseHandler ()
            {
                public Object handleResponse (final HttpResponse resp) throws IOException
                {
                    final StatusLine status = resp.getStatusLine ();
                    if (status.getStatusCode ()<200 || status.getStatusCode ()>=300)
                    {
                        throw new IOException ("non-ok status received: "+status);
                    }
                    
                    final HttpEntity entity = resp.getEntity ();
                    entity.writeTo (bytes);
                    
                    return null;
                }
            });
        }
        catch (final URISyntaxException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
        finally
        {
            http.close ();
        }
        
        return new ByteArrayInputStream (bytes.toByteArray ()); // FIXME: #11
    }
    
    public Return post (final InputStream data, final String contentType) throws IOException
    {
        final URL url = getURL ();
        Log.debug ("POST "+split (url));
        
/*
BufferedReader something = new BufferedReader (new InputStreamReader (data));
for (String line; (line = something.readLine ())!=null; )
{
    Log.fatal ("BLAH: "+line);
}
*/
        
        /*
        final HttpURLConnection http = (HttpURLConnection) url.openConnection ();
        http.setInstanceFollowRedirects (false);
        http.setRequestMethod ("POST");
        http.setDoOutput (true);
        
        // can't use null content type because then Tomcat sets it (incorrectly)
        // to application/x-www-form-urlencoded
        http.setRequestProperty("Content-Type", contentType!=null ? contentType : "");
        
        try
        {
            final OutputStream out = http.getOutputStream ();
            Log.debug ("data: "+data);
            if (data!=null) Utils.pipe (data, out);
            out.close ();
        }
        catch (final IllegalArgumentException e)
        {
            Log.error ("cannot exec HTTP resource `"+path+"': bad syntax: "+e.getMessage ());
            //setStatus (HttpServletResponse.SC_BAD_REQUEST);
//            Utils.status (resp, resp.SC_BAD_REQUEST, "cannot exec HTTP resource `"+path+"': bad syntax: "+e.getMessage ());
        }
        catch (final UnknownHostException e)
        {
            Log.error ("cannot exec HTTP resource `"+path+"': unknown host: "+e.getMessage ());
            //setStatus (HttpServletResponse.SC_NOT_FOUND);
//            Utils.status (resp, resp.SC_NOT_FOUND, "cannot exec HTTP resource `"+path+"': unknown host: "+e.getMessage ());
        }
        catch (final FileNotFoundException e)
        {
            Log.error ("cannot exec HTTP resource `"+path+"': not found: "+url.getFile ());
            //setStatus (HttpServletResponse.SC_NOT_FOUND);
//            Utils.status (resp, resp.SC_NOT_FOUND, "cannot exec HTTP resource `"+path+"': not found: "+url.getFile ());
        }
        
        return new Return (http.getContentType (), http.getHeaderField ("Content-Disposition"), http.getInputStream (), new Http.Status (http));
        */
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream (); // FIXME: #11
        final HttpClientBuilder htbuild = HttpClientBuilder.create ();
        htbuild.setDefaultCookieStore (getCookies ());
        
        final CloseableHttpClient http = htbuild.build (); try
        {
            final HttpPost post = new HttpPost (url.toURI ());
            
            if (data!=null)
            {
                final InputStreamEntity entity = new InputStreamEntity (data);

                // can't use null content type because then Tomcat sets it (incorrectly)
                // to application/x-www-form-urlencoded
                entity.setContentType (contentType!=null ? contentType : "");
                post.setEntity (entity);
            }
            
            final PostResponseHandler resp = new PostResponseHandler (out);
            http.execute (post, resp);
            
            return new Return (resp.contentType, resp.contentDisposition, resp.allowOrigin, new ByteArrayInputStream (out.toByteArray ()), resp.status); // FIXME: #11
        }
        catch (final URISyntaxException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
        finally
        {
            http.close ();
        }
    }
    
    static class PostResponseHandler implements ResponseHandler
    {
        final OutputStream out;
        
        // response data
        String contentType;
        String contentDisposition;
        String allowOrigin;
        Status status;
        
        PostResponseHandler (final OutputStream out)
        {
            this.out = out;
        }
        
        public Object handleResponse (final HttpResponse resp) throws IOException
        {
            final StatusLine status = resp.getStatusLine ();
            if (status.getStatusCode ()<200 || status.getStatusCode ()>=300)
            {
                if (status.getStatusCode () == 301) // Moved Permamently
                {
                    throw new IOException ("301 moved permanently: " + resp.getLastHeader("Location"));
                }
                else if (status.getStatusCode () == 302) // Found
                {
                    throw new IOException ("302 found: " + resp.getLastHeader("Location"));
                }
                else
                {
                    throw new IOException ("non-ok status received: "+status);
                }
            }

            final Header contentType = resp.getFirstHeader ("Content-Type");
            this.contentType = contentType!=null ? contentType.getValue () : null;
            
            final Header contentDisposition = resp.getFirstHeader ("Content-Disposition");
            this.contentDisposition = contentDisposition!=null ? contentDisposition.getValue () : null;
            
            final Header allowOrigin = resp.getFirstHeader ("Access-Control-Allow-Origin");
            this.allowOrigin = allowOrigin!=null ? allowOrigin.getValue () : null;
            
            this.status = new Status (resp);

            final HttpEntity entity = resp.getEntity ();
            entity.writeTo (out);

            return null;
        }
    }
    
    public boolean exists () throws IOException
    {
        final URL url = new URL (new URL ("http", "", ""), path);
        Log.debug ("exists? "+split (url));
        
        /*
        final HttpURLConnection http = (HttpURLConnection) url.openConnection ();
        http.setInstanceFollowRedirects (false);
        http.setRequestMethod ("HEAD");
        
        try
        {
            http.getInputStream ().close ();
        }
        catch (final FileNotFoundException e)
        {
            return false;
        }
        
        return true;
        */
        
        final HttpClientBuilder htbuild = HttpClientBuilder.create ();
        htbuild.setRedirectStrategy (new DefaultRedirectStrategy ()); // FIXME: #12
        htbuild.setDefaultCookieStore (getCookies ());
        
        final CloseableHttpClient http = htbuild.build ();
        try
        {
            HeadResponseHandler resp = new HeadResponseHandler ();
            Log.debug ("   final uri: "+url.toURI ());
            http.execute (new HttpHead (url.toURI ()), resp);
            
            return resp.exists;
        }
        catch (final URISyntaxException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
        finally
        {
            http.close ();
        }
    }
    
    public Boolean directory ()
    {
        // FIXME: we currently make no determination
        return null;
    }
    
    public Boolean executable ()
    {
        // FIXME: we currently make no determination
        return null;
    }
 
    /*
    public List<Legend> list (final User user) throws IOException
    {
        final List<Legend> entries = new ArrayList<Legend> ();
        // FIXME
        return entries;
    }
    */
    
    public Set<String> list (final boolean all, final boolean recurse) throws IOException
    {
        // FIXME: not yet implemented
        return null;
    }
    
    static class HeadResponseHandler implements ResponseHandler
    {
        boolean exists;
        
        public Object handleResponse (final HttpResponse resp) throws IOException
        {
            final StatusLine status = resp.getStatusLine ();
            if (status.getStatusCode ()<200 || status.getStatusCode ()>=300)
            {
                //Log.error ("non-ok status received from HTTP HEAD: "+status);
                //throw new IOException ("non-ok status received: "+status);
                exists = false;
            }
            else
            {
                exists = true;
            }

            return null;
        }
    }
    
    String split (final URL url)
    {
        final StringBuffer sb = new StringBuffer ();
        sb.append (url.getUserInfo ());
        sb.append (" @ ").append (url.getProtocol ());
        sb.append (" : ").append (url.getHost ());
        sb.append (" :").append (url.getPort ());
        sb.append (" ").append (url.getPath ());
        sb.append (" ? ").append (url.getQuery ());
        return sb.toString ();
    }
    
    public static class Status extends net.iovar.web.bin.shell.task.Status
    {
        final Map<String,List<String>> headers;
        final int code;
        
        Status (final HttpURLConnection http) throws IOException
        {
            this.headers = http.getHeaderFields ();
            this.code = http.getResponseCode ();
        }
        
        Status (final HttpResponse resp)
        {
            this.headers = new HashMap<String,List<String>> ();
            
            for (final Header header : resp.getAllHeaders ())
            {
                List<String> vals = this.headers.get (header.getName ());
                if (vals==null)
                {
                    vals = new ArrayList<String> ();
                    this.headers.put (header.getName (), vals);
                }
                vals.add (header.getValue ());
            }
            
            this.code = resp.getStatusLine ().getStatusCode ();
        }
        
        public boolean affirmative ()
        {
            return (200 <= code && code < 300);
        }
        
        public boolean isRedirect ()
        {
            return (300 <= code && code < 400);
        }
        
        public Map<String,List<String>> getHeaders ()
        {
            return headers;
        }
        
        public String toString ()
        {
            return String.valueOf (code);
        }
    }
}
