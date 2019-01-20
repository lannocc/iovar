/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev;

// local imports:
import net.iovar.web.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Generic temporary buffer device.
 * 
 * On POST (exec) without trailing slash this will send standard input to a new
 * buffer device and output the name of this resource.
 * 
 * Once a buffer has been created it is the user's responsibility to remove the
 * resource when finished.
 * 
 * FIXME: presently all buffers are stored in memory, and leakage is easy
 *
 * @author  shawn@lannocc.com
 */
public class Buffer extends HttpServlet
{
    protected void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doHead (this, req, resp);
    }
    
    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final String path = req.getPathInfo ();
        final ServletContext context = getServletContext ();
        Map<String,byte[]> buffers = (Map<String,byte[]>) context.getAttribute (Buffer.class.getName ());
        
        if (path==null)
        {
            Utils.doGet (this, req, resp);
        }
        else if ("/".equals (path))
        {
            resp.setContentType ("text/xml");
            final PrintWriter out = resp.getWriter ();
            
            out.println ("<?xml version=\"1.0\"?>");
            out.println ("<buffer-list xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
            
            if (buffers!=null)
            {
                for (final String key : buffers.keySet ())
                {
                    out.println ("    <buffer id=\""+key+"\" xlink:href=\""+key+"\"/>");
                }
            }
            
            out.println ("</buffer-list>");
        }
        else
        {
            if (buffers==null)
            {
                Log.error ("no buffers");
                throw new FileNotFoundException ("no buffers");
            }
            
            final String key = path.substring (1);
            final byte[] bytes = buffers.get (key);
            
            if (bytes==null)
            {
                Log.error ("buffer not found: "+key);
                throw new FileNotFoundException ("buffer not found: "+key);
            }
            
            Utils.pipe (new ByteArrayInputStream (bytes), resp.getOutputStream ());
        }
    }
    
    protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final String path = req.getPathInfo ();
       
        if (path==null)
        {
            Utils.doPut (this, req, resp);
        }
        else
        {
            // FIXME
            Log.fatal ("doPut not fully implemented");
            throw new RuntimeException ("doPut not fully implemented");
        }
    }
    
    protected void doDelete (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final String path = req.getPathInfo ();
        final ServletContext context = getServletContext ();
        Map<String,byte[]> buffers = (Map<String,byte[]>) context.getAttribute (Buffer.class.getName ());
        
        if (path==null)
        {
            // FIXME:
            //Utils.doDelete (this, req, resp);
            Log.fatal ("doDelete not fully implemented");
            throw new RuntimeException ("doDelete not fully implemented");
        }
        else if ("/".equals (path))
        {
            // FIXME
            Log.fatal ("doDelete not fully implemented");
            throw new RuntimeException ("doDelete not fully implemented");
        }
        else
        {
            if (buffers==null)
            {
                Log.error ("no buffers");
                throw new FileNotFoundException ("no buffers");
            }
            
            final String key = path.substring (1);
            final byte[] bytes = buffers.remove (key);
            
            if (bytes==null)
            {
                Log.error ("buffer not found: "+key);
                throw new FileNotFoundException ("buffer not found: "+key);
            }
        }
    }
    
    /**
     * Execute.
     */
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final String path = req.getPathInfo ();
        final ServletContext context = getServletContext ();

        if (! "/".equals (path))
        {
            Log.error ("unsupported path for POST: "+path);
            throw new RuntimeException ("unsupported path for POST: "+path);
        }
        
        Map<String,byte[]> buffers = (Map<String,byte[]>) context.getAttribute (Buffer.class.getName ());
        if (buffers==null)
        {
            buffers = new HashMap<String,byte[]> ();
            context.setAttribute (Buffer.class.getName (), buffers);
        }
        
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
        final String key = String.valueOf (System.identityHashCode (bytes));
        
        if (buffers.containsKey (key))
        {
            Log.fatal ("buffer key collision: "+key);
            throw new RuntimeException ("buffer key collision: "+key);
        }
        
        Utils.pipe (req.getInputStream (), bytes);
        buffers.put (key, bytes.toByteArray ());
        
        resp.getWriter ().print (key);
    }
}

