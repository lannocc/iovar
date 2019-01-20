/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Strip all but final path segment.
 *
 * @author  shawn@lannocc.com
 */
public class Basename extends HttpServlet
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
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[*]: resource name to strip all but final path segment from
     */
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        Log.debug ("begin basename POST");
        
        final ServletContext context = getServletContext ();
        final String query = req.getQueryString ();
        final PrintWriter out = resp.getWriter ();
        
        final Map<String,List<String>> params = Utils.getParams (query);
        final List<String> resources = params.get (null);
        
        if (resources!=null) for (final String resource : resources)
        {
            final int last = resource.lastIndexOf ('/');
            if (last<0) out.println (resource);
            else out.println (resource.substring (last+1));
        }
    }
}
