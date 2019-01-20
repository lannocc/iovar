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
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class XMLGet extends HttpServlet
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
        
        out.println ("usage: xmlget [ <node>... ]");
        out.println ();
        out.println ("Retrieve XML DOM tree from the session and send to stdout.");
        out.println ("Any arguments are node names representing the hieararchy to retrieve.");
        out.println ("By default, the retrieval key is the first node listed.");
        out.println ("The ?key option must be used if no arguments are given.");
        out.println ("Use the ?text option if you want node value.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?key=<key> - provide a key other than the default");
        out.println ("   ?text      - return node text value instead of DOM tree");
    }
    
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final PrintWriter out = resp.getWriter ();
        final ServletContext context = getServletContext ();
        
        try
        {
            final String query = req.getQueryString ();
            final Map<String,List<String>> params = Utils.getParams (query);

            if (params.containsKey ("help"))
            {
                usage (resp);
                Shell.exit (req, context, 0);
                return;
            }
        
            final List<String> args = params.get (null);
            
            String key = null;
            if (params.containsKey ("key"))
            {
                key = params.get ("key").get (0);
            }
            
            final List<String> nodes = new ArrayList<String> (args==null ? 0 : args.size () + 1);
            
            if (args!=null && ! args.isEmpty ())
            {
                for (final String node : args)
                {
                    nodes.add (node);
                }
            }
            else if (key==null)
            {
                usage (resp);
                Shell.exit (req, context, 1);
                return;
            }
            
            if (key==null && ! nodes.isEmpty ())
            {
                key = nodes.get (0);
            }
            
            final HttpSession session = req.getSession ();
            //final Map<String,Map<String,Object>> xenv = (Map<String,Map<String,Object>>) session.getAttribute (XMLSet.class.getName ());
            
            final Map<String,Document> xenv = (Map<String,Document>) session.getAttribute (XMLSet.class.getName ());
            if (xenv==null)
            {
                // no match
                Shell.exit (req, context, 2);
                return;
            }
            
            final Document doc = xenv.get (key);
            if (doc==null)
            {
                // no match
                Shell.exit (req, context, 3);
                return;
            }
            
            Node node = null;
            if (nodes.isEmpty ())
            {
                node = doc.getDocumentElement ();
            }
            else
            {
                tags: for (final String tag : nodes)
                {
                    if (node==null)
                    {
                        node = doc.getDocumentElement ();

                        if (!tag.equals (node.getNodeName ()))
                        {
                            // no match
                            Shell.exit (req, context, 4);
                            return;
                        }
                    }
                    else
                    {
                        for (node = node.getFirstChild (); node!=null; node = node.getNextSibling ())
                        {
                            if (tag.equals (node.getNodeName ()))
                            {
                                continue tags;
                            }
                        }

                        // no match
                        Shell.exit (req, context, 5);
                        return;
                    }
                }
            }
            
            // we have a winner!
            
            if (params.containsKey ("text"))
            {
                out.print (node.getTextContent ());
            }
            else
            {
                final TransformerFactory factory = TransformerFactory.newInstance ();
                final Transformer transformer = factory.newTransformer ();
                final DOMSource source = new DOMSource (node);
                final StreamResult result = new StreamResult (out);
                transformer.transform (source, result);            
            }
            
        }
        catch (Exception e)
        {
            Log.fatal (e.toString ());
            StackTraceElement[] elems = e.getStackTrace ();
            for (StackTraceElement elem : elems)
            {
                Log.fatal (elem.toString ());
            }
            
            out.println ("xmlget: "+e);
            Shell.exit (req, context, 99);
        }
    }
}
