/*
 * Copyright (C) 2018-2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;
import net.iovar.web.dev.trans.*;
import net.iovar.web.dev.trans.File;

// java imports:
import java.io.*;
import java.util.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Translate a transport path such as file: or res: to a local: path.
 *
 * @author  shawn@lannocc.com
 */
public class Localize extends HttpServlet
{
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
        
        out.println ("usage: local <path>");
        out.println ();
        out.println ("Translate transport <path> such as file: or res: to a local: path.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
    }
    
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        try
        {
            final ServletContext context = getServletContext ();
            final String query = req.getQueryString ();
            final Map<String,List<String>> params = Utils.getParams (query);

            if (params.containsKey ("help"))
            {
                usage (resp);
                Shell.exit (req, context, 0);
                return;
            }
            
            final List<String> vals = params.get (null);
            if (vals==null || vals.size () != 1)
            {
                usage (resp);
                Shell.exit (req, context, 1);
                return;
            }
            
            final Transport res = Transport.handler (vals.get (0), context, null);
            final PrintWriter out = resp.getWriter ();
            
            if (res instanceof Local || res instanceof Resource)
            {
                out.print (res.getPath ());
            }
            else if (res instanceof File)
            {
                final String root = new java.io.File (context.getRealPath ("/")).getCanonicalPath ();
                final String file = new java.io.File (res.getPath ()).getCanonicalPath ();
                
                // FIXME SECURITY: this is not safe... special characters must be quoted
                if (file.startsWith (root))
                {
                    String local = file.substring (root.length ());
                    while (local.startsWith ("/"))
                    {
                        local = local.substring (1);
                    }
                    
                    out.print ("/" + local);
                }
                else
                {
                    Shell.exit (req, context, 2);
                }
            }
            else
            {
                throw new IllegalArgumentException ("unsupported transport type: " + res);
            }
            
            out.flush ();
        }
        catch (final Exception e)
        {
            Log.fatal (e);
            StackTraceElement[] elems = e.getStackTrace ();
            for (StackTraceElement elem : elems)
            {
                Log.fatal (elem.toString ());
            }
        }
    }
}
