/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2014 Lannocc Technologies
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
 * Out goes the in; fetch resources and output together, in order.
 * This will output the input stream followed by any number of named resources.
 *
 * @author  shawn@lannocc.com
 */
public class Cat extends HttpServlet
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
        
        out.println ("usage: cat [options] [resource]...");
        out.println ();
        out.println ("Concatenate resources to output.");
        out.println ("By default, any resources are displayed after any input.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help          - display this help screen");
        out.println ("   ?type=<mime>   - set output MIME type (e.g. text/plain)");
        out.println ("   ?disposition=  - set content-disposition header");
        out.println ("   ?allow-origin= - set Access-Control-Allow-Origin header (requires CorsFilter in web.xml)");
        out.println ("   ?first         - display resources before any input");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[...]: optional resource to display
     * 
     * Exit code is set to the number of problems encountered while processing.
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
        
        if (params.containsKey ("type"))
        {
            final String type = params.get ("type").get (0);
            Log.debug ("setting content-type: "+type);
            resp.setContentType (type);
        }
        
        if (params.containsKey ("disposition"))
        {
            final String disposition = params.get ("disposition").get (0);
            Log.debug ("setting content-disposition: "+disposition);
            resp.setHeader ("Content-Disposition", disposition);
        }
        
        /*
         * NOTE: this requires CorsFilter enabled in web.xml to allow this header...
         */
        if (params.containsKey ("allow-origin"))
        {
            final String origin = params.get ("allow-origin").get (0);
            Log.debug ("setting Access-Control-Allow-Origin: "+origin);
            resp.setHeader ("Access-Control-Allow-Origin", origin);
        }
        
        final boolean first = params.containsKey ("first");

        final OutputStream out = resp.getOutputStream ();
        int errors = 0;
        final InputStream in = req.getInputStream ();
        
        if (! first) try
        {
            Utils.pipe (in, out);
            out.flush ();
        }
        finally
        {
            in.close ();
        }
        
        final List<String> resources = params.get (null);

        if (resources!=null) for (final String resource : resources)
        {
            InputStream i2; try
            {
                i2 = Transport.handler (resource, context, req.getSession ()).get ();
            }
            catch (final IOException e)
            {
                Log.warn (e);
                errors++;
                out.write (("cat: "+e+'\n').getBytes());
                continue;
            }

            Utils.pipe (i2, out);
        }
        
        if (first) try
        {
            Utils.pipe (in, out);
            out.flush ();
        }
        finally
        {
            in.close ();
        }
        
        Shell.exit (req, context, errors);
    }
}
