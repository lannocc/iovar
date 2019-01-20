/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
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
 * Stream-EDit: filter input stream
 * 
 * @author  shawn@lannocc.com
 */
public class Sed extends HttpServlet
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
        
        out.println ("usage: sed [resource--FIXME]");
        out.println ();
        out.println ("Filters input from stdin or given resource.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?filter=   - the filter expression to use (REQUIRED)");
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
        
        final List<String> filters = params.get ("filter");
        if (filters==null || filters.isEmpty ())
        {
            resp.getWriter ().println ("sed: you must specify a filter expression");
            Shell.exit (req, context, 1);
            return;
        }
        
        //String text = Utils.toString (req.getInputStream ());
        final BufferedReader in = new BufferedReader (new InputStreamReader (req.getInputStream ()));
        final StringBuffer result = new StringBuffer ();
        String sep = "";

        for (String text; (text = in.readLine ())!=null; )
        {
            for (String filter : filters)
            {
                if (! filter.startsWith ("s/"))
                {
                    resp.getWriter ().println ("sed: expecting substitute command for filter expression, e.g.: s/foo/bar/");
                    Shell.exit (req, context, 2);
                    return;
                }
                /* FIXME: also allow non-global replace... disallowing for now
                 * so that existing scripts will error and be fixed. */
                if (! filter.endsWith ("/g") || filter.length ()<4)
                {
                    resp.getWriter ().println ("sed: incomplete substitute command for filter expression, e.g.: s/foo/bar/");
                    Shell.exit (req, context, 3);
                    return;
                }
                filter = filter.substring (2, filter.length ()-2 /* FIXME */);
                
                int split = 0;
                boolean found = false;
                boolean escaped = false;
                
                for (char c; split < filter.length (); split++)
                {
                    c = filter.charAt (split);
                    
                    if (! escaped)
                    {
                        if ('\\'==c)
                        {
                            escaped = true;
                        }
                        else if ('/'==c)
                        {
                            found = true;
                            break;
                        }
                    }
                    else
                    {
                        escaped = false;
                    }
                }
                
                if (!found)
                {
                    resp.getWriter ().println ("sed: [2] incomplete substitute command for filter expression, e.g.: s/foo/bar/");
                    Shell.exit (req, context, 4);
                    return;
                }

                text = text.replaceAll (filter.substring (0, split), filter.substring (split+1));
            }
            
            result.append (sep).append (text);
            sep = "\n";
        }
        
        
        resp.getWriter ().write (result.toString ());
        
        Shell.exit (req, context, 0);
    }
}
