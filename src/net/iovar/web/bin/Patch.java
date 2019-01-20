/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import java.util.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Patch (partially modify) resource.
 *
 * @author  shawn@lannocc.com
 */
public class Patch extends HttpServlet
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
        
        out.println ("usage: patch <resource> [argument]...");
        out.println ();
        out.println ("Partially modifies resource, with data from stdin.");
        out.println ("  - any named parameters are automatically passed in");
        out.println ("  - argument is optional anonymous argument value to be passed in");
    }
    
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
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
        
        if (resources==null || resources.size ()!=1)
        {
            usage (resp);
            Shell.exit (req, context, 2);
            return;
        }
        
        final String resource = resources.remove (0);

        try
        {
            final String contentType = req.getContentType ();
            Log.debug ("request data contentType: "+contentType);
            
            Utils.pipe (Transport.handler (resource, params, context, req.getSession ()).patch (req.getInputStream (), contentType), resp.getOutputStream ());
        }
        catch (final Exception e)
        {
            Log.error (e);
            resp.getWriter ().println ("post: "+e);
            Shell.exit (req, context, 3);
        }
    }
}
