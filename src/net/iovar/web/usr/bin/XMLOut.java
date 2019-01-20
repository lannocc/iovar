/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
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
 * Create simple XML tree and escape reserved symbols.
 *
 * @author  shawn@lannocc.com
 */
public class XMLOut extends HttpServlet
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
        
        out.println ("usage: xmlout [options] <node> [<node>...] <content>");
        out.println ();
        out.println ("Build simple XML tree.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help  - display this help screen");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[0-*]: xml node name (required at least once for root node)
     *  arg[n]: last parameter is node content
     */
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
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
        
        if (args==null || args.size ()<2)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        // FIXME: extremely hacky (many assumptions)... should actually run through a real XML parser
        BufferedReader in = null;
        
        // we read just a couple lines here
        if (req.getInputStream () != null)
        {
            in = new BufferedReader (new InputStreamReader (req.getInputStream ()));
            String line = in.readLine ();
            
            if (line!=null)
            {
                boolean xml = false;
                
                if ("<?xml version=\"1.0\"?>".equals (line))
                {
                    xml = true;
                    line = in.readLine ();
                }
                else if (line.startsWith ("<?xml version=\"1.0\""))
                {
                    final int end = line.indexOf ("?>");
                    if (end>0)
                    {
                        xml = true;
                        
                        line = line.substring (end+2);
                        if (line.length ()<1)
                        {
                            line = in.readLine ();
                        }
                    }
                }
                
                if (!xml)
                {
                    Log.error ("input does not appear to be xml: "+line);
                    out.println ("xmlout: input does not appear to be xml: "+line);
                    Shell.exit (req, context, 2);
                    return;
                }
                
                if (line==null)
                {
                    Log.error ("incomplete xml document passed as input: "+line);
                    out.println ("xmlout: incomplete xml document passed as input: "+line);
                    Shell.exit (req, context, 3);
                    return;
                }

                resp.setContentType ("text/xml");
                out.println ("<?xml version=\"1.0\"?>");
                out.println (line);

                if (("<"+args.get (0)+">").equals (line))
                {
                    args.remove (0);
                }
            }
            else
            {
                in.close ();
                in = null;
            }
        }
        
        if (in==null)
        {
            resp.setContentType ("text/xml");
            out.println ("<?xml version=\"1.0\"?>");
        }
        
        final int nodes = args.size () - 1;
        
        for (int i=0; i < nodes; i++)
        {
            for (int j=0; j<i; j++) out.print ("    ");
            final String name = args.get (i);
            final String tag = Utils.tagify (name);
            out.print ("<"+tag);
            if (! tag.equals (name)) out.print (" tag-name-orig=\""+name+"\"");
            out.print (">");
            if (i < nodes - 1) out.println ();
        }
        
        out.print (Utils.toXML (args.get (args.size () - 1)));
        if (args.size ()>1) out.println ("</"+Utils.tagify (args.get (args.size () - 2))+">");
        
        for (int i=nodes-2; i >= 0; i--)
        {
            for (int j=0; j<i; j++) out.print ("    ");
            out.println ("</"+Utils.tagify (args.get (i))+">");
        }
        
        // the rest of stdin gets appended here
        if (in!=null)
        {
            Utils.pipe (in, out);
        }
        
        out.flush ();
    }
}
