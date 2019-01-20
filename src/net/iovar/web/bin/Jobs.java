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
import net.iovar.web.lib.*;

// java imports:
import java.io.*;
import java.util.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * List backgrounded jobs.
 *
 * @author  shawn@lannocc.com
 */
public class Jobs extends HttpServlet
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
        
        out.println ("usage: jobs");
        out.println ();
        out.println ("List jobs that have been backgrounded.");
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
        
        if (resources!=null && resources.size ()!=0)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final PrintWriter out = resp.getWriter ();
        final Map<Integer,Job> jobs = Job.getJobs (context);
        
        if (jobs!=null)
        {
            for (final Job job : jobs.values ())
            {
                out.println (job);
            }
        }
        
        Shell.exit (req, context, 0);
    }
}
