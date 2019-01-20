/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class Form extends HttpServlet
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
        
        out.println ("usage: form [name=value] ...");
        out.println ();
        out.println ("Prepares application/x-www-form-urlencoded content from name-value pairs.");
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

            resp.setContentType("application/x-www-form-urlencoded");
            final PrintWriter out = resp.getWriter ();

            final List<String> pairs = params.get (null);
            if (pairs!=null)
            {
                String sep = "";
                for (final String pair : pairs)
                {
                    out.print (sep);
                    
                    int equalsign = pair.indexOf('=');
                    if (equalsign >= 0)
                    {
                        out.print(URLEncoder.encode(pair.substring(0, equalsign)));
                        out.print('=');
                        out.print(URLEncoder.encode(pair.substring(equalsign+1)));
                    }
                    else
                    {
                        out.print(URLEncoder.encode(pair));
                    }
                    sep = "&";
                }
            }
                    
            out.flush();
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
