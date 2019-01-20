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
 * @author  shawn@lannocc.com
 */
public class Form2XML extends HttpServlet
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
        
        out.println ("usage: form2xml [options] <root>");
        out.println ();
        out.println ("Build XML document type <root> with node/value pairs from standard input.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help  - display this help screen");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[0]: root node name
     */
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
//Log.fatal ("input: "+Utils.toString (req.getInputStream()));
        final ServletContext context = getServletContext ();
        final String query = req.getQueryString ();
        final Map<String,List<String>> params = Utils.getParams (query);
        
        if (params.containsKey ("help"))
        {
            usage (resp);
            Shell.exit (req, context, 0);
            return;
        }
        
        final List<String> vals = params.remove (null);
        if (vals==null || vals.size ()!=1)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        final String root = vals.get (0);
        
        final InputStream in = req.getInputStream ();
        final String type = req.getContentType ();
        Log.debug ("input data content type: "+type);
        
        if ("application/x-www-form-urlencoded".equalsIgnoreCase (type) || (type!=null && type.startsWith("application/x-www-form-urlencoded;")))
        {
            Log.debug ("standard form processing");
//Log.fatal ("standard processing");
            
            resp.setContentType ("text/xml");
            final PrintWriter out = resp.getWriter ();
            out.println ("<?xml version=\"1.0\"?>");
            out.println ("<"+root+">");
            
/*
BufferedReader something = new BufferedReader (new InputStreamReader (in));
for (String line; (line = something.readLine ())!=null; )
{
out.println(line);
}
*/
            
            // FIXME #17
            /* Tomcat apparently already handles the request body when it is
             * application/x-www-form-urlencoded. */
            //  12/7/16 - Changed this to read from InputStream and that works for CatTamboo site
            //              - verified working under /$/bin
            //              - verified working under /app
            //   2/8/17 - Modified Sessions.java to NOT use getParameter and this works for Thompsons site
            for (final Map.Entry<String,List<String>> node : Utils.getParams (Utils.toString (in)).entrySet ())
//            for (final Map.Entry<String,String[]> node : req.getParameterMap ().entrySet ())
            {
                Log.debug ("param: "+node);
//Log.fatal ("param: "+node);
                
                // FIXME: shouldn't have to do this...
                /*
                if (Shell.EXT_PARAM_SESSION.equals (node))
                {
                    continue;
                }
                */
                
                final String name = node.getKey ();
                final String tag = Utils.tagify (name);
                
                for (final String val : node.getValue ())
                {
                    out.println ("    <"+tag+(tag!=null ? " tag-name-orig=\""+name+"\"" : "")+">"+Utils.toXML (val)+"</"+tag+">");
                }
            }
            
            out.println ("</"+root+">");
            
            Shell.exit (req, context, 0);
        }
        else if (type!=null && type.startsWith ("multipart/form-data; boundary="))
        {
            Log.debug ("multipart form processing");
//Log.fatal ("multipart processing");
        
            resp.setContentType ("text/xml");
            final PrintWriter out = resp.getWriter ();
            out.println ("<?xml version=\"1.0\"?>");
            out.println ("<"+root+">");
            
            final String boundary = "--"+type.substring (30);
            Log.debug ("boundary: "+boundary);

            // read (and ignore) until first boundary (preamble)
            final OutputStream dummy = new OutputStream ()
            {
                public void write (int b)
                {
                    // no-op
                }
            };
            Utils.pipe (in, dummy, boundary);
            Utils.pipe (in, dummy, "\r\n");
        
            while (true)
            {
                Log.debug ("in new multipart segment");

                // get headers: Content-Disposition is required
                final Map<String, String> headers = FormFile.getHeaders (in);
                if (headers==null) break;
                else if (headers.size ()==1 && headers.containsKey ("--") && headers.get ("--")==null) break;
                else if (headers.size ()==0)
                {
                    continue;
                }

                final String disposition = headers.get ("Content-Disposition");

                if (disposition==null)
                {
                    Log.error ("expecting 'Content-Disposition' header in the input data");
                    out.println ("form2xml: expecting 'Content-Disposition' header in the input data");
                    Shell.exit (req, context, 4);
                    return;
                }
                if (! disposition.startsWith ("form-data; name=\""))
                {
                    Log.error ("expecting form-data disposition including a named parameter");
                    out.println ("form2xml: expecting form-data disposition including a named paramter");
                    Shell.exit (req, context, 5);
                    return;
                }

                // get field name
                final String fname;
                {
                    int quote = disposition.indexOf ('"', 17);
                    if (quote<0)
                    {
                        Log.error ("unterminated form field name in Content-Disposition header");
                        out.println ("form2xml: unterminated form field name in Content-Disposition header");
                        Shell.exit (req, context, 6);
                        return;
                    }
                    fname = disposition.substring (17, quote);
                }
                Log.debug ("field name: "+fname);
                
                final String tag = Utils.tagify (fname);
                out.print ("    <"+tag+(tag!=null ? " tag-name-orig=\""+fname+"\"" : "")+">");
                
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream ();
                Utils.pipe (in, buffer, "\r\n"+boundary);
                out.write (Utils.toXML (new String (buffer.toByteArray ())));
                
                out.println ("</"+tag+">");
            }
            
            out.println ("</"+root+">");
            
            Shell.exit (req, context, 0);
        }
        else
        {
            Log.error ("unsupported content type: "+type);
            resp.getWriter ().println ("form2xml: unsupported content type: "+type);
            Shell.exit (req, context, 2);
            return;
        }
    }
}
