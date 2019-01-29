/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:
//import me.idfree.dev.*;
//import me.idfree.lib.*;

/**
 *
 * @author  shawn@lannocc.com
 */
public class List extends HttpServlet
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
        
        out.println ("usage: ls <path>...");
        out.println ();
        out.println ("List the entries at the given path(s).");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?all       - also list hidden files (those starting with dot)");
        out.println ("   ?recurse   - recursively list subdirectories too");
    }
    
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        final String query = req.getQueryString ();
        final Map<String,java.util.List<String>> params = Utils.getParams (query);

        if (params.containsKey ("help"))
        {
            usage (resp);
            Shell.exit (req, context, 0);
            return;
        }
        
        final java.util.List<String> args = params.get (null);
        if (args==null || args.size() < 1)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final boolean all = params.containsKey ("all");
        final boolean recurse = params.containsKey ("recurse");
        
        final PrintWriter out = resp.getWriter (); try
        {
            for (final String resource : args)
            {
                final Transport t = Transport.handler (resource, context, req.getSession ());

                /*
                User user; try
                {
                    user = Authentication.getUser (context, req.getSession (), session);
                }
                catch (final Authentication.NotLoggedInException e)
                {
                    user = null;
                }

                Utils.xstream.toXML (t.list (user), out);
                */
                
                for (final String entry : t.list (all, recurse))
                {
                    out.println (entry);
                }
            }
        }
        finally
        {
            out.close ();
        }

        Shell.exit (req, context, 0);
    }
}
