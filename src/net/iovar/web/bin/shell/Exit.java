/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell;

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
 * Set a simple numerical exit value.
 * Zero is generally considered a successful exit or <tt>true</tt> value.
 * 
 * Note that the protocol-specific status mechanism (if such exists) is always
 * interrogated first. The shell exit value defined here is only tested upon
 * successful protocol status (e.g. HTTP 200 OK).
 *
 * @author  shawn@lannocc.com
 *
 * @see     Status
 */
public class Exit extends HttpServlet
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
     *  arg[0]: numerical exit value
     */
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        
        final String query = req.getQueryString ();
        if (query==null)
        {
            usage (resp);
            return;
        }

        final Map<String,List<String>> params = Utils.getParams (query);
        final List<String> vals = params.get (null);
        
        if (vals==null || vals.size ()!=1)
        {
            usage (resp);
            return;
        }
        
        try
        {
            Shell.exit (req, context, Integer.parseInt (vals.get (0)));
        }
        catch (final NumberFormatException e)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "not a number: "+vals.get (0));
        }
    }
    
    public void usage (final HttpServletResponse resp) throws IOException
    {
        Log.debug ("displaying usage");
        
        resp.setContentType ("text/plain");
        final PrintWriter out = resp.getWriter ();
        
        out.println ("usage: exit <code>");
        
        Status.set (resp, resp.SC_BAD_REQUEST);
    }
}
