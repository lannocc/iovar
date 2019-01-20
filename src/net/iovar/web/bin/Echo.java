/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.dev.*;
import net.iovar.web.bin.shell.*;

// java imports:
import java.io.*;
import java.util.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Output the arguments.
 *
 * @author  shawn@lannocc.com
 */
public class Echo extends HttpServlet
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
        
        out.println ("usage: echo [options] ...");
        out.println ();
        out.println ("Output the input stream followed by any arguments.");
        out.println ("A newline is automatically appended unless turned off in the options.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?noline    - do not output trailing newline");
        out.println ("   ?first     - display text arguments before any input");
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

            final boolean first = params.containsKey ("first");
            final List<String> vals = params.get (null);
            final PrintWriter out = resp.getWriter ();

            if (first)
            {
                if (vals != null)
                {
                    String sep = "";
                    for (final String val : vals)
                    {
                        out.print (sep);
                        out.print (val);
                        out.flush ();
                        sep = " ";
                    }
                }

                if (! params.containsKey ("noline")) out.println ();
            }

            final InputStream in = req.getInputStream (); try
            {
                //boolean space = in!=null && in.available ()>0; // FIXME: doesn't actually seem to work as expected
                if (Utils.pipe (in, out) > 0)
                {
                    //out.print (' ');
                    out.flush ();
                }
            }
            finally
            {
                in.close ();
            }

            if (!first)
            {
                if (vals != null)
                {
                    String sep = "";
                    for (final String val : vals)
                    {
                        out.print (sep);
                        out.print (val);
                        out.flush ();
                        sep = " ";
                    }
                }

                if (! params.containsKey ("noline")) out.println ();
            }

            out.flush ();
        }
        catch (Exception e)
        {
            StackTraceElement[] elems = e.getStackTrace ();
            for (StackTraceElement elem : elems)
            {
                Log.fatal (elem.toString ());
            }
        }
    }
}
