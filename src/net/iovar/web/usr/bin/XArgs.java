/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.bin.shell.task.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

// 3rd-party imports:

/**
 * @author  shawn@lannocc.com
 */
public class XArgs extends HttpServlet
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
        
        out.println ("usage: xargs [options] <command> [args]");
        out.println ();
        out.println ("Build arguments from standard input and execute <command>.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?_=<name>  - the named option value(s) converted to anonymous argument(s)");
        out.println ("   ?--=<name> - the named option is converted to long-form anonymous name-value pair");
        out.println ("                (use * to convert all)");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[0]: command to execute
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
        
        final List<String> vals = params.get (null);
        if (vals==null || vals.size ()<1)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        final String resource = vals.remove (0);
        //final String resource = vals.get (0);
        
        final InputStream in = req.getInputStream ();
        //Utils.pipe (in, resp.getOutputStream ());

        final Session session = Sessions.get (req);
        /*
        if (session==null) 
        {
            Log.error ("unable to retrieve session");
            throw new IllegalArgumentException ("need a session to exec a call");
        }
        */
        
        final Map<String,List<String>> callParams;
        String type = req.getContentType ();
        if (type!=null)
        {
            final int semicolon = type.indexOf (';');
            if (semicolon > 0)
            {
                type = type.substring (0, semicolon);
            }
            type = type.toLowerCase ();
        }

        if ("application/x-www-form-urlencoded".equals (type))
        {
            Log.info ("x-www-form-urlencoded xargs processing");
            
            //Log.debug ("in: "+ (int) in.read ());
            callParams = Utils.getParams (Utils.toString (in));
        }
        else if ("text/xml".equals (type))
        {
            Log.info ("xml mode xargs processing");
            
            try
            {
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
                final DocumentBuilder builder = factory.newDocumentBuilder ();
                final Document doc = builder.parse (in);
                doc.getDocumentElement ().normalize ();
                
                callParams = new HashMap<String,List<String>> ();
                
                for (Node node = doc.getDocumentElement ().getFirstChild (); node != null; node = node.getNextSibling ())
                {
                    switch (node.getNodeType ())
                    {
                        case Node.ELEMENT_NODE:
                            final String name = node.getNodeName ();

                            List<String> cvals = callParams.get (name);
                            if (cvals==null)
                            {
                                cvals = new ArrayList<String> ();
                                callParams.put (name, cvals);
                            }

                            cvals.add (node.getTextContent ());
                            break;
                            
                        case Node.TEXT_NODE:
                            final String text = node.getTextContent ().trim ();
                            
                            if (! text.isEmpty ())
                            {
                                List<String> anons = callParams.get (null);
                                if (anons==null)
                                {
                                    anons = new ArrayList<String> ();
                                    callParams.put (null, anons);
                                }

                                anons.add (node.getTextContent ());
                            }
                            break;
                    }
                }
            }
            catch (final ParserConfigurationException e)
            {
                Log.error (e);
                resp.getWriter ().println ("xargs: "+e);
                Shell.exit (req, context, 2);
                return;
            }
            catch (final SAXException e)
            {
                Log.error (e);
                resp.getWriter ().println ("xargs: "+e);
                Shell.exit (req, context, 3);
                return;
            }
        }
        else
        {
            Log.info ("default xargs processing");
            
            callParams = new HashMap<String,List<String>> ();
            final List<String> args = new ArrayList<String> ();
            
            // FIXME: do this more effeciently, possibly utilize bin.shell.task package for parsing
            StringBuffer arg = new StringBuffer ();
            
            for (int c; (c = in.read ())>=0; )
            {
                if (' '==c || '\t'==c || '\r'==c || '\n'==c)
                {
                    if (arg.length ()>0)
                    {
                        args.add (arg.toString ());
                        arg = new StringBuffer ();
                    }
                }
                else
                {
                    arg.append ((char) c);
                }
            }
            
            if (arg.length ()>0)
            {
                args.add (arg.toString ());
            }
            
            if (! args.isEmpty ())
            {
                callParams.put (null, args);
            }
        }

        if (vals.size ()>0)
        {
            Log.debug ("passed-in arguments to add: "+vals);

            List<String> anons = callParams.get (null);
            if (anons==null)
            {
                anons = new ArrayList<String> ();
                callParams.put (null, anons);
            }
            anons.addAll (0, vals);
        }
        
        List<String> converts = params.get ("_");
        if (converts!=null)
        {
            Log.debug ("parameter values to convert: "+converts);

            List<String> anons = callParams.get (null);
            if (anons==null)
            {
                anons = new ArrayList<String> ();
                callParams.put (null, anons);
            }

            for (final String convert : converts)
            {
                Log.debug ("converting: "+convert);
                final List<String> cvals = callParams.remove (convert);
                if (cvals!=null && cvals.size ()>0)
                {
                    Log.debug ("adding anon args: "+cvals);
                    anons.addAll (cvals);
                }
            }
        }
        
        converts = params.get ("--");
        if (converts != null)
        {
            Log.debug ("parameter name-value pairs to convert to long-form: "+converts);
            
            List<String> anons = callParams.get (null);
            if (anons==null)
            {
                anons = new ArrayList<String> ();
                callParams.put (null, anons);
            }
            
            if (converts.size ()==1 && "*".equals (converts.get (0)))
            {
                converts = new ArrayList (callParams.keySet ());
                converts.remove (null);
            }
            
            for (final String convert : converts)
            {
                Log.debug ("converting: "+convert);
                final List<String> cvals = callParams.remove (convert);
                if (cvals!=null && cvals.size ()>0)
                {
                    for (final String cval : cvals)
                    {
                        Log.debug ("adding anon arg: --"+convert+"="+cval);
                        anons.add ("--"+convert+"="+cval);
                    }
                }
            }
        }

        final Return r = Shell.exec (resource, callParams, new TaskData (session, context, req.getRemoteUser (), req.getSession ()));
        resp.setContentType (r.type);
        Utils.pipe (r.data, resp.getOutputStream ());

        Shell.exit (req, context, r.status.affirmative () ? 0 : 99);
    }
}
