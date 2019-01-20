/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2015 Lannocc Technologies
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
 * Set/view environment variables.
 *
 * @author  shawn@lannocc.com
 */
public class Set extends HttpServlet
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
        
        out.println ("usage: set [<name>[=<val>]]");
        out.println ();
        out.println ("When invoked without arguments, displays currently assigned variables.");
        out.println ("Otherwise define a variable named <name> and assign it <val> or from standard input.");
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
        
        final Session shell = Sessions.get (req);
        if (shell==null) 
        {
            Log.error ("unable to retrieve session");
            throw new IllegalArgumentException ("need a session to list environment");
        }
        
        final List<String> vals = params.get (null);
        if (vals==null || vals.isEmpty ())
        {
            environment (shell, resp);
            Shell.exit (req, context, 0);
            return;
        }
        
        final StringBuffer s = new StringBuffer ();
        final InputStream in = req.getInputStream ();
        if (in!=null)
        {
            final InputStreamReader reader = new InputStreamReader (in);
            for (int c; (c = reader.read ())>=0; s.append ((char) c));
        }

        for (final String var : vals)
        {
            int idx = var.indexOf ("=");
            String key = idx>=0? var.substring (0, idx): var;
            String val = idx>=0? var.substring (idx+1): null;
            if (val==null) val = s.toString ();
            
            // FIXME: re-evaluate this...
            shell.export (1, context, req.getSession (), key, val);
        }
        
        // FIXME: re-evaluate this...
        shell.saveUp (1, context, req.getSession ());
        
        Shell.exit (req, context, 0);
    }
    
    void environment (final Session shell, final HttpServletResponse resp) throws IOException
    {
        PrintWriter out = resp.getWriter ();
        Log.debug ("printing current environment for "+shell);
        
        for (final Map.Entry<String,String> entry: shell.getVariables ().entrySet ())
        {
            out.println (entry);
        }
        
        for (final Map.Entry<String,String> entry : shell.getLocals ().entrySet ())
        {
            out.println ("local "+entry);
        }
    }
}
