/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.bin.shell.task.Return;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;

// java imports:
import java.io.*;
import java.util.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Bring a backgrounded job to the foregound.
 *
 * @author  shawn@lannocc.com
 */
public class Foreground extends HttpServlet
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
        
        out.println ("usage: fg <job>");
        out.println ();
        out.println ("Bring job to foreground.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help          - display this help screen");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[0]: job to foreground
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
        
        final String jobId = resources.get (0);
        final Job job; try
        {
            job = Job.get (Integer.valueOf (jobId), context);
        }
        catch (final IllegalArgumentException e)
        {
            resp.getWriter ().println ("fg: not a number: "+jobId);
            Shell.exit (req, context, 2);
            return;
        }
        
        Log.debug ("job: "+job);
        if (job==null)
        {
            resp.getWriter ().println ("fg: job doesn't exist: "+jobId);
            Shell.exit (req, context, 3);
            return;
        }
        
        try
        {
            final Return r = job.join (context);
            Utils.pipe (r.data, resp.getOutputStream ());
            Shell.exit (req, context, r.status.affirmative () ? 0 : 99);
        }
        catch (final InterruptedException e)
        {
            Log.warn (e);
            resp.getWriter ().println ("fg: interrupted: "+e);
            Shell.exit (req, context, 4);
        }
    }
}
