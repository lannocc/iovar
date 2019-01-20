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
 * Display system uptime in user-friendly format.
 *
 * @author  shawn@lannocc.com
 */
public class Uptime extends HttpServlet
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
        
        out.println ("usage: uptime [options]");
        out.println ();
        out.println ("Display system uptime.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
    }
    
    /**
     * Execute.
     */
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
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
        
        long secs = net.iovar.web.proc.Uptime.get () / 1000;
        
        final long days = secs / (60 * 60 * 24);
        secs %= (60 * 60 * 24);
        
        final long hrs = secs / (60 * 60);
        secs %= (60 * 60);
        
        final long mins = secs / 60;
        secs %= 60;
        
        final PrintWriter out = resp.getWriter ();
        out.print ("up "+days+" day");
        if (days!=1) out.print ("s");
        out.print (" "+hrs+" hour");
        if (hrs!=1) out.print ("s");
        out.print (" "+mins+" minute");
        if (mins!=1) out.print ("s");
        out.println ();
        
        Shell.exit (req, context, 0);
    }
}
