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
 * Sleep for a number of seconds.
 *
 * @author  shawn@lannocc.com
 */
public class Sleep extends HttpServlet
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
        
        out.println ("usage: sleep <seconds>");
        out.println ();
        out.println ("Sleep for a number of seconds.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help          - display this help screen");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[0]: number of seconds to sleep
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
        
        if (resources==null || resources.size ()!=1)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final String secs = resources.get (0);
        try
        {
            Thread.sleep (Integer.valueOf (secs)*1000);
        }
        catch (final IllegalArgumentException e)
        {
            Log.warn (e);
            resp.getWriter ().println ("sleep: not a number: "+secs);
            Shell.exit (req, context, 2);
            return;
        }
        catch (final InterruptedException e)
        {
            Log.warn (e);
            resp.getWriter ().println ("sleep: interrupted: "+e);
            Shell.exit (req, context, 2);
            return;
        }
        
        Shell.exit (req, context, 0);
    }
}
