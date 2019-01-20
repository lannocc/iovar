/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.Shell;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Output end of content.
 *
 * @author  shawn@lannocc.com
 */
public class Tail extends HttpServlet
{
    public static final int COUNT = 10;
    
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
        
        out.println ("usage: tail [options] [resource]");
        out.println ();
        out.println ("Output end of stdin and optionally specified resource.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?lines=<n> - output the last <n> lines (default is "+COUNT+")");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[*]: resource to output (if omitted then stdin is used)
     */
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        Log.debug ("begin tail POST");
        
        final ServletContext context = getServletContext ();
        final String query = req.getQueryString ();
        final Map<String,List<String>> params = Utils.getParams (query);
        
        if (params.containsKey ("help"))
        {
            usage (resp);
            Shell.exit (req, context, 0);
            return;
        }
        
        int count = COUNT;
        if (params.containsKey ("lines")) try
        {
            count = Integer.parseInt (params.get ("lines").get (0));
        }
        catch (final IllegalArgumentException e)
        {
            resp.getWriter ().println ("head: not a valid line count");
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final PrintWriter out = resp.getWriter ();
        final List<String> resources = params.get (null);
        
        tail (new BufferedReader (req.getReader ()), out, count);
        
        if (resources!=null) for (final String resource : resources)
        {
            tail (new BufferedReader (new InputStreamReader (Transport.handler (resource, context, req.getSession ()).get ())), out, count);
        }
    }
    
    void tail (final BufferedReader in, final PrintWriter out, final int count) throws IOException
    {
        if (count>0)
        {
            final List<String> buffer = new LinkedList<String> ();

            for (String line; (line = in.readLine ())!=null; )
            {
                buffer.add (line);
                if (buffer.size ()>count) buffer.remove (0);
            }

            for (final String line : buffer)
            {
                out.println (line);
            }
        }
        else if (count<0) // print all but the first <count> lines
        {
            int remaining = -count;
            for (String line; remaining > 0 && (line = in.readLine ())!=null; remaining--);
            for (String line; (line = in.readLine ())!=null; )
            {
                out.println (line);
            }
        }
    }
}
