/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
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
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Output shell variable contents.
 * 
 * FIXME: `set` and `print` should also preserve content-type and other headers.
 *
 * @author  shawn@lannocc.com
 */
public class Print extends HttpServlet
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
        
        out.println ("usage: print <name>");
        out.println ();
        out.println ("Output the contents of shell variable called <name>.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
    }
    
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        try
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
                throw new IllegalArgumentException ("need a session to print a variable");
            }
            
            final List<String> vals = params.get (null);
            if (vals==null || vals.size () != 1)
            {
                usage (resp);
                Shell.exit (req, context, 1);
                return;
            }
            
            final String name = vals.get (0);
            final PrintWriter out = resp.getWriter ();
            
            final String val = shell.get (name);
            if (val!=null)
            {
                out.print (val);
                out.flush ();
            }
        }
        catch (final Exception e)
        {
            Log.fatal (e);
            StackTraceElement[] elems = e.getStackTrace ();
            for (StackTraceElement elem : elems)
            {
                Log.fatal (elem.toString ());
            }
        }
    }
}
