/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class Canonical extends HttpServlet
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
        
        out.println ("usage: canon [<canonical> <redirect>]");
        out.println ();
        out.println ("Display the canonical URL redirect map or add a new entry.");
        out.println ("Requests for <canonical> are redirected to <redirect>.");
        out.println ("When no arguments are entered, the current mappings are displayed.");
        out.println ();
        // FIXME: don't really want options here... this is temporary until useful buffer support
        //      (needed for cattamboo /bin/view)
        out.println ("Options:");
        out.println ("   ?help - display this help screen");
    }
    
    /**
     * Execute.
     */
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        final ServletContext context = getServletContext ();
        final String query = req.getQueryString ();
        
        if (query==null)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final Map<String,List<String>> params = Utils.getParams (query);
        final List<String> resources = params.get (null);
        
        if (params.containsKey ("help"))
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }

        if (resources!=null && resources.size ()!=2)
        {
            usage (resp);
            Shell.exit (req, context, 2);
            return;
        }
        
        if (resources==null || resources.isEmpty ())
        {
            list (req, resp);
        }
        else
        {
            final String canon = resources.get (0);
            final String redirect = resources.get (1);

            if (canon==null || redirect==null)
            {
                usage (resp);
                Shell.exit (req, context, 3);
                return;
            }

            Default.addCanon (context, canon, redirect);

            Shell.exit (req, context, 0);
        }
    }
    
    void list (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        
        resp.setContentType ("text/plain");
        final PrintWriter out = resp.getWriter ();
        
        final Map<String,String> canon = Default.getCanon (context);
        
        if (canon!=null)
        {
            for (final Map.Entry<String,String> entry : canon.entrySet ())
            {
                out.println (entry.getKey ()+":"+entry.getValue());
            }
        }
        
        Shell.exit (req, context, 0);
    }
}
