/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev;

// local imports:
import net.iovar.web.*;
import net.iovar.web.lib.*;
import net.iovar.web.dev.trans.*;
import net.iovar.web.bin.shell.task.Return;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:
//import me.idfree.lib.*;

/**
 *
 * @author  shawn@lannocc.com
 */
public abstract class Transport extends HttpServlet
{
    protected ServletContext context;
    private String method;
    protected String path;
    protected Map<String,List<String>> params;
    protected HttpSession htsession; // FIXME: this is to track the user's session for loopback; find a better way...
    //int status = 0; // NOT HTTP status! Use <=0 for success or >0 for failure
    
    protected void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doHead (this, req, resp);
    }
    
    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doGet (this, req, resp);
    }
    
    protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doPut (this, req, resp);
    }
    
    public void usage (final HttpServletResponse resp) throws IOException
    {
        Log.debug ("displaying usage");
        
        resp.setContentType ("text/plain");
        final PrintWriter out = resp.getWriter ();
        
        out.println ("Transport handler. Outputs the Java class name on invocation.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help          - display this help screen");
    }
    
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        resp.getWriter ().println (this.getClass ().getName ());
    }
    
    protected void setContext (final ServletContext context)
    {
        this.context = context;
    }
    
    public ServletContext getContext ()
    {
        return context;
    }
    
    protected void setPath (final String path)
    {
        this.path = path;
    }
    
    public String getPath ()
    {
        return path;
    }
    
    protected void setParams (final Map<String,List<String>> params)
    {
        this.params = params;
    }
    
    public Map<String,List<String>> getParams ()
    {
        return params;
    }
    
    protected void setSession (final HttpSession htsession)
    {
        this.htsession = htsession;
    }
    
    public HttpSession getSession ()
    {
        return htsession;
    }
    
    public abstract InputStream get () throws IOException;
    
    public abstract InputStream put (final InputStream data) throws IOException;
    
    public abstract InputStream patch (final InputStream data, final String contentType) throws IOException;
    
    public abstract InputStream delete () throws IOException;
    
    public abstract Return post (final InputStream data, final String contentType) throws IOException;
    
    public abstract boolean exists () throws IOException;
    
    /**
     * @return  <code>null</code> if possibly true or no determination made
     */
    public abstract Boolean directory () throws IOException;
        
    /**
     * @return  <code>null</code> if possibly true or no determination made
     */
    public abstract Boolean executable () throws IOException;
    
    //public abstract List<Legend> list (final User user) throws IOException;
    
    /**
     * @deprecated not sure this ever really was used so now it does nothing
     */
    protected void setStatus (final int status)
    {
        //this.status = status;
    }
    
    /*
    public int getStatus ()
    {
        return status;
    }
    */
    
    public String toString ()
    {
        return super.toString ()+" [path="+path+" params="+params+"]";
    }
    
    public String toPathString ()
    {
        return (method!=null ? (method+":") : "") + path;
    }
    
    public static Transport handler (final String resource, final ServletContext context, final HttpSession htsession) throws IOException
    {
        return handler (resource, null, context, htsession);
    }
    
    public static Transport handler (final String resource, final Map<String,List<String>> params, final ServletContext context, final HttpSession htsession) throws IOException
    {
        Log.debug ("params passed to handler (): "+params);
        
        // FIXME: make this safer / more robust
        
        final int colon = resource.indexOf (':');
        if (colon >0)
        {
            // FIXME: hardcoded reference to /dev/trans
            final Local handler = new Local ("/dev/trans/"+resource.substring (0, colon), context, params, htsession);
            
            if (! handler.exists ())
            {
                throw new IOException ("transport handler does not exist: "+handler);
            }
            
            final Return r = handler.post (null, null);
            if (r.data==null)
            {
                throw new IOException ("no data returned from "+handler+": "+r);
            }
            
            final BufferedReader data = new BufferedReader (new InputStreamReader (r.data));
            final String name; try 
            {
                name = data.readLine ();
            }
            finally
            {
                data.close ();
            }
                
            final Class cl; try
            {
                cl = Class.forName (name);
            }
            catch (final ClassNotFoundException e)
            {
                throw new IOException ("bad transport (class not found): "+name, e);
            }
            
            if (! Transport.class.isAssignableFrom (cl))
            {
                throw new IOException ("bad transport (not a subclass of "+Transport.class+": "+name);
            }
            
            try
            {
                Log.debug ("returning handler "+cl+" for: "+resource);
                final Transport t = (Transport) cl.newInstance ();
                t.setContext (context);
                t.method = resource.substring (0, colon);
                t.setPath (resource.substring (colon+1));
                t.setParams (params);
                t.setSession (htsession);
                Log.debug ("transport reference instantiated: "+t);
                return t;
            }
            catch (final InstantiationException e)
            {
                throw new IOException ("failed to instantiate transport instance: "+cl, e);
            }
            catch (final IllegalAccessException e)
            {
                throw new IOException ("illegal access while instantiating transport instance: "+cl, e);
            }
        }
        else
        {
            Log.debug ("(default) returning Local handler for: "+resource);
            final Local local = new Local (resource, context, params, htsession);
            ((Transport) local).method = "local";
            Log.debug ("transport reference instantiated: "+local);
            return local;
        }
    }
    
    /**
     * Use this handler when the resource string is more like an href and should be parsed for parameters
     */
    public static Transport handlerHref (final String href, final ServletContext context, final HttpSession htsession) throws IOException
    {
        Log.debug ("href: " + href);
        
        final String resource;
        final Map<String,List<String>> params;
        
        int q = href.indexOf ('?');
        if (q >= 0)
        {
            resource = href.substring (0, q);
            params = Utils.getParams (href.substring (q + 1));
        }
        else
        {
            resource = href;
            params = null;
        }
        
        Log.debug ("href resource: " + resource);
        return handler (resource, params, context, htsession);
    }
}
