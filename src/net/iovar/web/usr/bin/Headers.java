/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
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
 * Output just the HTTP headers from the request.
 *
 * @author  shawn@lannocc.com
 */
public class Headers extends HttpServlet
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
        
        out.println ("usage: headers");
        out.println ();
        out.println ("Output just the HTTP headers from the request.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help          - display this help screen");
    }
    
    /**
     * Execute.
     */
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
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
        
        final List<String> resources = params.get (null);
        
        if (resources!=null && !resources.isEmpty ())
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        resp.setContentType ("text/plain");
        final PrintWriter out = resp.getWriter ();
        
        for (final Enumeration names = req.getHeaderNames (); names.hasMoreElements (); )
        {
            final String name = (String) names.nextElement ();
            
            for (final Enumeration vals = req.getHeaders (name); vals.hasMoreElements (); )
            {
                final String val = (String) vals.nextElement ();
                out.print (name);
                out.print (": ");
                out.println (val);
            }
        }
        
        Shell.exit (req, context, 0);
    }
}
