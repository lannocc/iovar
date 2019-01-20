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
 * Return HTTP status code and optional message.
 *
 * @author  shawn@lannocc.com
 *
 * @see     Exit
 */
public class Status extends HttpServlet
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
        
        out.println ("usage: status <http-code> [<message>]");
        out.println ();
        out.println ("Sets HTTP status to the specified response code and optional message.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[0]: HTTP status code
     *  arg[1]: (optional) message
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
        
        final List<String> vals = params.get (null);
        if (vals==null || vals.size ()<1 || vals.size ()>2)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        try
        {
            final int status = Integer.parseInt (vals.get (0));
            if (status < 0) set (resp, resp.SC_BAD_REQUEST, "not a valid http status code: "+status); // FIXME: throw IllegalArgumentException instead (to differentiate status here vs status passed in)
            else set (resp, status, vals.size ()>1 ? vals.get (1) : null);
        }
        catch (final NumberFormatException e)
        {
            set (resp, resp.SC_BAD_REQUEST, "not a number: "+vals.get (0));
        }
    }
    
    public static void set (final HttpServletResponse resp, final int status) throws IOException
    {
        set (resp, status, null);
    }
    
    public static void set (final HttpServletResponse resp, final int status, final String message) throws IOException
    {
        Log.debug ("returning status: "+status+" - "+message);

        resp.setStatus (status);
        //resp.getWriter ().println (status);
        if (message!=null) resp.getWriter ().println (message);
    }
}
