/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell;

// local imports:
import net.iovar.web.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * @author  shawn@lannocc.com
 */
public class Which extends HttpServlet
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
        
        out.println ("usage: which [options] <command>");
        out.println ();
        out.println ("Outputs the full path of <command>.");
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
        
        final Session session = Sessions.get (req);
        if (session==null) 
        {
            Log.error ("unable to retrieve session");
            throw new IllegalArgumentException ("need a session for path information");
        }

        final List<String> vals = params.get (null);
        if (vals==null || vals.size ()!=1)
        {
            usage (resp);
            Shell.exit (req, getServletContext (), 1);
            return;
        }
        
        final PrintWriter out = resp.getWriter ();
        final String cmd = vals.get (0);
        
        final Transport t = reference (session, context, req.getRemoteUser (), cmd, null, req.getSession ());
        
        if (t!=null)
        {
            out.println (t.toPathString ());
        }
        else
        {
            out.println ("which: not found: "+cmd);
            Shell.exit (req, context, 3);
        }
    }
    
    public static Transport reference (final Session shell, final ServletContext context, final String user, final String cmd, Map<String,List<String>> params, final HttpSession htsession) throws IOException
    {
        // FIXME: SECURITY
        if (params==null) params = new HashMap<String,List<String>> ();
        params.put ("REMOTE_USER", Arrays.asList (new String[] { user }));
        
        // FIXME: this test is not exhaustive
        if (cmd.startsWith ("/") || cmd.contains (":"))
        {
            Log.debug ("cmd is absolute");
            final Transport t = Transport.handler (cmd, params, context, htsession);
            
            if (t.exists ()) return t;
            else return null;
        }
        else
        {
            Log.debug ("cmd is relative");
            final List<String> paths = shell.getPathList (); if (paths!=null)
            for (final String path : paths)
            {
                Log.debug ("checking for: "+path+"/"+cmd);
                final Transport t = Transport.handler (path+"/"+cmd, params, context, htsession);
                
                if (t.exists ())
                {
                    return t;
                }
            }
            
            return null;
        }
    }
}
