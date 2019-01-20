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
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class XMLWrap extends HttpServlet
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
        
        out.println ("usage: xmlwrap <node> [<node>...]");
        out.println ();
        out.println ("Wraps the input stream line-by-line in the given xml node(s).");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?all       - skip line-by-line mode and wrap it all at once");
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
        
            final PrintWriter out = resp.getWriter ();
            final List<String> args = params.get (null);

            if (args==null || args.size ()<1)
            {
                usage (resp);
                Shell.exit (req, context, 1);
                return;
            }
            
            final boolean all = params.containsKey ("all");
            
            for (int i=0; i < args.size () - (all?0:1); i++)
            {
                startMe(args, i, out);
                if (i < args.size () - 1) out.println ();
            }
            
            if (req.getInputStream () != null)
            {
                BufferedReader in = new BufferedReader (new InputStreamReader (req.getInputStream ()));
                
                for (String line; (line = in.readLine ())!=null; )
                {
                    if (!all)
                    {
                        startMe (args, args.size() - 1, out);
                    }
                    
                    out.print (Utils.toXML (line));
                    
                    if (!all)
                    {
                        stopMe (args, args.size() - 1, out);
                    }
                    else
                    {
                        out.println ();
                    }
                }
            }
            
            for (int i = args.size () - (all?1:2); i >= 0; i--)
            {
                for (int j=0; j<i; j++) out.print ("    ");
                stopMe (args, i, out);
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
    
    void startMe(final List<String> args, final int index, final PrintWriter out)
    {
        for (int j=0; j<index; j++) out.print ("    ");
        final String name = args.get (index);
        final String tag = Utils.tagify (name);
        out.print ("<"+tag);
        if (! tag.equals (name)) out.print (" tag-name-orig=\""+name+"\"");
        out.print (">");
    }
    
    void stopMe(final List<String> args, final int index, final PrintWriter out)
    {
        final String name = args.get (index);
        final String tag = Utils.tagify (name);
        out.println ("</" + tag + ">");
    }
}
