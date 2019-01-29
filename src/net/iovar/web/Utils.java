/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web;

// local imports:
import net.iovar.web.bin.shell.task.Return;
import net.iovar.web.bin.shell.task.Status;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:
import com.thoughtworks.xstream.*;

/**
 * FIXME: take advantage of Apache Commons IOUtils for most of the I/O stuff.
 *
 * @author  shawn@lannocc.com
 */
public class Utils
{
    public static final String ENCODING = "UTF-8";
    
    public static final String INIT_PARAM_LOOPBACK = "iovar.loopback";
    
    // FIXME: is xstream thread-safe?
    public static final XStream xstream;
    
    static
    {
        xstream = new XStream ();
        xstream.aliasPackage ("iotask", "net.iovar.web.bin.shell.task");
    }
    
    /**
     * @deprecated
     */
    public static HttpURLConnection loopback (final ServletContext context, String path) throws MalformedURLException, IOException
    {
        final String root = (String) context.getInitParameter (INIT_PARAM_LOOPBACK);
        Log.debug ("root: "+root);
        
        final URL url = new URL ("http://"+root+"/"+context.getContextPath ()+'/'+path);
        Log.debug ("url: "+url);
        
        if (!"http".equals (url.getProtocol ()))
        {
            throw new MalformedURLException ("http protocol required: "+url);
        }
        
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection ();
        conn.setInstanceFollowRedirects (false);
        return conn;
    }
    
    public static String toString (final InputStream in) throws IOException
    {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
        pipe (in, bytes);
        return bytes.toString ();
    }
    
    public static Map<String,List<String>> getParams (final String query)
    {
        Map<String,List<String>> params = new HashMap<String,List<String>> ();
        
        if (query!=null) for (final StringTokenizer stp = new StringTokenizer (query, "&"); stp.hasMoreTokens (); ) try
        {
            final String pair = stp.nextToken ();
            final int equals = pair.indexOf ('=');
            
            if (equals<0)
            {
                params.put (URLDecoder.decode (pair, ENCODING), null);
            }
            else if (equals==0)
            {
                List<String> vals = params.get (null); if (vals==null)
                {
                    vals = new ArrayList<String> ();
                    params.put (null, vals);
                }
                vals.add (URLDecoder.decode (pair.substring(1), ENCODING));
            }
            else
            {
                final String key = URLDecoder.decode (pair.substring (0, equals), ENCODING);
                final String val = URLDecoder.decode (pair.substring (equals+1), ENCODING);
                
                List<String> vals = params.get (key); if (vals==null)
                {
                    vals = new ArrayList<String> ();
                    params.put (key, vals);
                }
                vals.add (val);
            }
        }
        catch (final UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException ("unsupported URL encoding: "+e);
        }
        
        return params;
    }
    
    public static String buildQuery (final Map<String,List<String>> params)
    {
        final StringBuilder sb = new StringBuilder (); try
        {
            String sep = "";

            for (final Map.Entry<String,List<String>> entry : params.entrySet ())
            {
                final String name = entry.getKey ();
                final List<String> vals = entry.getValue ();
                
                if (vals==null)
                {
                    if (name!=null)
                    {
                        sb.append (sep).append (URLEncoder.encode (name, ENCODING));
                        sep = "&";
                    }
                }
                else for (final String val : vals)
                {
                    sb.append (sep);
                    if (name!=null) sb.append (URLEncoder.encode (name, ENCODING));
                    sb.append ('=');
                    if (val!=null) sb.append (URLEncoder.encode (val, ENCODING));
                    sep = "&";
                }
            }
        }
        catch (final UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException ("unsupported URL encoding: "+e);
        }
        
        Log.debug ("built query string: "+sb);
        return sb.toString ();
    }
    
    public static String getError (final HttpURLConnection http) throws IOException
    {
        final InputStream err = http.getErrorStream (); if (err!=null) try
        {
            final StringWriter mout = new StringWriter (); try
            {
                final InputStreamReader errr = new InputStreamReader (err);
                for (int c; (c=errr.read ())>=0; mout.write (c));
            }
            finally
            {
                mout.close ();
            }

            return mout.toString ();
        }
        finally
        {
            err.close ();
        }
        else
        {
            return null;
        }
    }
    
    public static void pipe (final InputStream in, final OutputStream out) throws IOException
    {
        if (in!=null)
        {
            for (int b; (b = in.read ())>=0; out.write (b));
            out.flush ();
        }
    }
    
    public static void pipe (final InputStream in, final OutputStream out, final String until) throws IOException
    {
        if (in!=null)
        {
            final int len = until.length ();
            final LinkedList<Byte> queue = new LinkedList<Byte> ();
            boolean found = false;
            
            for (int b; (b = in.read ())>=0; )
            {
                queue.add (new Byte ((byte) b));
                
                if (queue.size () >= len) // queue is full (should never actually be larger than len, however)
                {
                    // does the queue match the <until> string ?
                    boolean match = true;
                    for (int i=0; i<len; i++)
                    {
                        if (((char) queue.get (i).byteValue ()) != until.charAt (i))
                        {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        found = true;
                        break;
                    }
                    
                    out.write (queue.removeFirst ());
                }
            }
            
            if (!found)
            {
                // empty anything left in the queue
                for (Byte b : queue) out.write (b);
            }
            
            out.flush ();
        }
    }
    
    public static int pipe (final InputStream in, final Writer out) throws IOException
    {
        int count = 0;
        
        if (in!=null)
        {
            for (int b; (b = in.read ())>=0; count++)
            {
                out.write (b);
            }
            out.flush ();
        }
        
        return count;
    }
    
    public static int pipe (final Reader in, final Writer out) throws IOException
    {
        int count = 0;
        
        if (in!=null)
        {
            for (int b; (b = in.read ())>=0; count++)
            {
                out.write (b);
            }
            out.flush ();
        }
        
        return count;
    }
    
    /** FIXME: not done */
    static class Pipe implements Runnable
    {
        public void run ()
        {
            
        }
    }
    
    public static Map<String,String> configMap (final InputStream config) throws IOException
    {
        final Map<String,String> map = new HashMap<String,String> ();
        final BufferedReader in = new BufferedReader (new InputStreamReader (config));
        
        for (String line; (line = in.readLine ())!=null; )
        {
            line = line.trim ();
            if (line.length ()<1) continue;
            
            final int equals = line.indexOf ('=');
            if (equals<0) throw new IllegalArgumentException ("missing equals sign (=) in config stream line");
            
            final String key = line.substring (0, equals).trim ();
            final String val = line.substring (equals+1).trim ();
            map.put (key, val);
        }
        
        return map;
    }
    
    public static Return error (final String msg)
    {
        return new Return (
                new ByteArrayInputStream (("iovar: "+msg+"\n").getBytes ()),
                new Status () {
                    public boolean affirmative () {
                        return false;
                    }
                }
         );
    }
    
    /**
     * Standard implementation of doHead (): currently does nothing (returns success).
     */
    public static void doHead (final HttpServlet servlet, final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        // no-op
    }
    
    /**
     * Standard implementation of doGet (): retrieve the file's source.
     */
    public static void doGet (final HttpServlet servlet, final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        // FIXME: hardcoded path to src folder
        resp.sendRedirect (req.getContextPath ()+"/src/"+servlet.getClass ().getName ().replace ('.', '/')+".java");
    }
    
    /**
     * Standard implementation of doPut (): update the file's source.
     */
    public static void doPut (final HttpServlet servlet, final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        // FIXME: hardcoded path to src folder
        resp.sendRedirect (req.getContextPath ()+"/src/"+servlet.getClass ().getName ().replace ('.', '/')+".java");
    }
    
    /**
     * Sanitize string to be used as an XML tag name
     */
    public static String tagify (String str)
    {
        // FIXME: not complete
        str = str.replace (' ', '-');
        str = str.replace ("(", "");
        str = str.replace (")", "");
        //str = str.replace ("*", "-star-");
        str = str.replace ("*", "");
        str = str.replace ("+", "-plus-");
        str = str.replace (".", "_");
        str = str.replace ("'", "_");
        str = str.replace ("\"", "_");

        while (str.startsWith ("_") || str.startsWith ("-"))
        {
            str = str.substring (1);
        }
        
        /*
        while (str.endsWith ("-"))
        {
            str = str.substring (0, str.length ()-1);
        }
        */
        
        return str;
    }
    
    /**
     * Sanitize string to be used as XML data
     */
    public static String toXML (String str)
    {
        if (str==null) return null;
        
        // FIXME: make sure this list is complete / to-spec
        str = str.replace ("&", "&amp;");
        str = str.replace ("<", "&lt;");
        str = str.replace (">", "&gt;");
        str = str.replace ("'", "&apos;");
        str = str.replace ("\"", "&quot;");
        
        return str;
    }
    
    public static String toXML (final char c)
    {
        // FIXME: make sure this list is complete / to-spec
        switch (c)
        {
            case '&': return "&amp;";
            case '<': return "&lt;";
            case '>': return "&gt;";
            case '\'': return "&apos;";
            case '"': return "&quot;";
            default: return String.valueOf (c);
        }
    }
    
    public static String getTrace (final Exception e)
    {
        final StringBuilder sb = new StringBuilder ();
        
        StackTraceElement[] elems = e.getStackTrace ();
        for (StackTraceElement elem : elems)
        {
            sb.append (elem.toString ());
            sb.append ("\n");
        }
        
        return sb.toString ();
    }
    
    public static String bytesToHex (final byte[] bytes)
    {
        final StringBuffer hexString = new StringBuffer();
        
        for (int i = 0; i < bytes.length; i++)
        {
            final String hex = Integer.toHexString (0xff & bytes[i]);
            if (hex.length() == 1) hexString.append ('0');
            hexString.append (hex);
        }
        
        return hexString.toString ();
    }
    
    public static byte[] hexToBytes (final String hex)
    {
        final int len = hex.length ();
        final byte[] bytes = new byte[len / 2];
        
        for (int i=0; i < len; i+=2)
        {
            bytes[i/2] = (byte) ((Character.digit (hex.charAt (i), 16) << 4)
                    + Character.digit (hex.charAt (i+1), 16));
        }
        
        return bytes;
    }

    /**
     * Usable by XSL stylesheets to easily include contents of another resource.
     * @since 1.1
     */
    public static String get (final String uri) throws IOException
    {
        final StringBuilder sb = new StringBuilder ();
        final BufferedReader in = new BufferedReader (new InputStreamReader (
                    Transport.handler (uri, net.iovar.web.sbin.Init.CONTEXT, null).get ()));
        try
        {
            for (String line; (line = in.readLine ()) != null; )
            {
                sb.append (line).append ('\n');
            }
        }
        finally
        {
            in.close ();
        }

        return sb.toString ();
    }
    
    /**
     * Get the relative path of a file relative to a potential parent directory.
     * Returns null if the file is not a child of that parent location.
     * @since 1.1
     */
    public static String getLocalPath (final File parentDir, final File childFile)
    {
        final File parent = childFile.getParentFile ();
        
        if (parent == null)
        {
            return null;
        }
        else if (parent.equals (parentDir))
        {
            return "/" + childFile.getName ();
        }
        else
        {
            final String local = getLocalPath (parentDir, parent);
            if (local == null) return null;
            else return local + "/" + childFile.getName ();
        }
    }



    
    /**
     * For testing
     */
    public static void main (final String[] args) throws Exception
    {
        String hex = bytesToHex ("just a test".getBytes ());
        byte[] bytes = hexToBytes (hex);
        
        System.out.println (new String (bytes));
    }
}
