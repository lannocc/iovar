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
 * Test if specified resource exists (HTTP HEAD).
 *
 * @author  shawn@lannocc.com
 */
public class Exists extends HttpServlet
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
        
        out.println ("usage: exists [<resource>] [argument]...");
        out.println ();
        out.println ("Test if resource exists (HTTP HEAD). If <resource> is not specified then");
        out.println ("the resource name will come from standard input.");
        out.println ("  - any named parameters are automatically passed in");
        out.println ("  - argument is optional anonymous argument value to be passed in");
    }
    
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        final String query = req.getQueryString ();
        
        if (query==null)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final Map<String,List<String>> params = Utils.getParams (query);
        final List<String> resources = params.get (null);
        
        String resource = null;
        boolean output = false;
        
        if (resources==null || resources.size ()<1)
        {
            final BufferedReader in = req.getReader ();
            if (in!=null)
            {
                final StringBuffer sb = new StringBuffer ();
                for (int c; (c = in.read())>=0; sb.append ((char) c));
                resource = sb.toString ();
                output = true;
            }
        }
        else
        {
            resource = resources.remove (0);
        }
        
        
        if (resource==null || resource.isEmpty ())
        {
            usage (resp);
            Shell.exit (req, context, 2);
            return;
        }

        try
        {
            if (Transport.handler (resource, params, context, req.getSession ()).exists ())
            {
                if (output)
                {
                    resp.getWriter ().print (resource);
                }
                
                Shell.exit (req, context, 0);
            }
            else
            {
                Shell.exit (req, context, 99);
            }
        }
        catch (final IOException e)
        {
            Log.error (e);
            resp.getWriter ().println ("exists: "+e);
            Shell.exit (req, context, 4);
        }
    }
}
