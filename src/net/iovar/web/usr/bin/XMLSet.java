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
import javax.xml.parsers.*;
import org.w3c.dom.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class XMLSet extends HttpServlet
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
        
        //out.println ("usage: xmlset [ <node>... [-] ]");
        out.println ("usage: xmlset [ <node>... ]");
        out.println ();
        out.println ("Store XML data from stdin to the session.");
        out.println ("Any arguments are node names representing the hieararchy under which to store the data:");
        out.println ("  - missing nodes are created automatically");
        //out.println ("  - use a dash (\"-\") as final argument and the last node will be the root node of the data");
        out.println ("By default, the storage key is the first node listed or the root node tag name of the input data.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?key=<key> - provide a key other than the default");
        out.println ("   ?replace   - clear any existing value before setting (do not append)");
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
            final InputStream in = req.getInputStream ();

            /*
            if (in==null || in.available ()<1)
            {
                usage (resp);
                Shell.exit (req, context, 1);
                return;
            }
            */
            
            String key = null;
            if (params.containsKey ("key"))
            {
                key = params.get ("key").get (0);
            }
            
            final List<String> nodes = new ArrayList<String> (args==null ? 0 : args.size () + 1);
            //boolean autoNode = false;
            
            if (args!=null) for (final String node : args)
            {
                /*
                if ("-".equals (node))
                {
                    autoNode = true;
                    break;
                }
                */
                
                nodes.add (node);
            }
            
            /*
            if (autoNode && nodes.isEmpty ())
            {
                usage (resp);
                Shell.exit (req, context, 2);
                return;
            }
            */
            
            if (key==null && ! nodes.isEmpty ())
            {
                key = nodes.get (0);
            }
            
            final HttpSession session = req.getSession ();
            //final Map<String,Map<String,Object>> xenv = (Map<String,Map<String,Object>>) session.getAttribute (XMLSet.class.getName ());
            
            Map<String,Document> xenv = (Map<String,Document>) session.getAttribute (XMLSet.class.getName ());
            if (xenv==null)
            {
                xenv = new HashMap<String,Document> ();
                session.setAttribute (XMLSet.class.getName (), xenv);
            }
            
            try
            {
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
                final DocumentBuilder builder = factory.newDocumentBuilder ();
                final Document doc = builder.parse (in);
                doc.getDocumentElement ().normalize ();
                
                if (key==null)
                {
                    key = doc.getDocumentElement ().getTagName ();
                }
                
                if (nodes.isEmpty () || params.containsKey ("replace")) // simple replace mode
                {
                    xenv.put (key, doc);
                }
                else // merge mode
                {
                    // FIXME: all this could probably be greatly simplified
                    //        but I've been up all night...
                    
                    Node node;
                    Document merging = xenv.get (key);
                    
                    if (merging==null)
                    {
                        merging = builder.newDocument ();
                        xenv.put (key, merging);
                        
                        node = merging;
                        
                        for (final String tag : nodes)
                        {
                            node = node.appendChild (merging.createElement (tag));
                        }
                    }
                    else
                    {
                        final Iterator<String> tags = nodes.iterator ();
                        String tag = tags.next ();
                        node = merging.getDocumentElement ();
                        
                        if (tag.equals (node.getNodeName ()))
                        {
                            tags: while (tags.hasNext ())
                            {
                                tag = tags.next ();

                                for (node = node.getFirstChild (); node != null; node = node.getNextSibling ())
                                {
                                    if (tag.equals (node.getNodeName ()))
                                    {
                                        continue tags;
                                    }
                                }
                                
                                node = node.appendChild (merging.createElement (tag));
                            }
                        }
                        else
                        {
                            node = node.appendChild (merging.createElement (tag));
                            while (tags.hasNext ())
                            {
                                node = node.appendChild (merging.createElement (tags.next ()));
                            }
                        }
                    }
                    
                    node.appendChild (merging.importNode (doc.getDocumentElement (), true));
                }

                Shell.exit (req, context, 0);
            }
            catch (final ParserConfigurationException e)
            {
                Log.error (e);
                resp.getWriter ().println ("xmlset: "+e);
                Shell.exit (req, context, 3);
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
            
            out.println ("xmlset: "+e);
            Shell.exit (req, context, 99);
        }
    }
}
