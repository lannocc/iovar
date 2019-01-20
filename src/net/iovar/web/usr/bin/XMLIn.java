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
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

// 3rd-party imports:

/**
 * Pull out a node text value from XML tree.
 *
 * @author  shawn@lannocc.com
 */
public class XMLIn extends HttpServlet
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
        
        out.println ("usage: xmlin [options] [<node>...]");
        out.println ();
        out.println ("Pull out a node text value from xml tree passed as input.");
        out.println ("If no node specified, the text value of the root node is returned.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help  - display this help screen");
    }
    
    /**
     * Execute.
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
        
        final List<String> nodes = params.get (null);
        
        /*
        if (nodes==null || nodes.size ()<1)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        */
        
        final InputStream in = req.getInputStream ();
        if (in==null)
        {
            Log.error ("no data");
            resp.getWriter ().println ("xmlin: nothing to do");
            Shell.exit (req, context, 2);
            return;
        }
        
        try
        {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
            //factory.setNamespaceAware (true);
            final DocumentBuilder parser = factory.newDocumentBuilder ();
            
            final Document xml = parser.parse (in);
            
            boolean fail = false;
            Node node = xml.getDocumentElement ();
            
            if (nodes==null || nodes.isEmpty ())
            {
                // no-op: just use the root node
            }
            else
            {
                do
                {
                    final String name = nodes.remove (0);

                    while (node!=null && ! node.getNodeName ().equals (name))
                    {
                        node = node.getNextSibling ();
                    }

                    if (node==null)
                    {
                        // no match
                        fail = true;
                        break;
                    }

                    if (nodes.isEmpty ())
                    {
                        // all done
                        break;
                    }
                }
                while ( (node = node.getFirstChild ()) != null);

                if (fail || ! nodes.isEmpty ())
                {
                    Log.warn ("no match");
                    resp.getWriter ().println ("xmlin: no match");
                    Shell.exit (req, context, 3);
                    return;
                }
            }
            
            // print matching node text content
            final String text = node.getTextContent ();
            if (text != null) resp.getWriter ().print (text);
            
            Shell.exit (req, context, 0);
        }
        catch (final ParserConfigurationException e)
        {
            Log.error (e);
            resp.getWriter ().println ("xmlin: "+e);
            Shell.exit (req, context, 4);
        }
        catch (final SAXException e)
        {
            Log.error (e);
            resp.getWriter ().println ("xmlin: "+e);
            Shell.exit (req, context, 5);
        }
        catch (final Exception e)
        {
            Log.fatal (e.toString ());
            StackTraceElement[] elems = e.getStackTrace ();
            for (StackTraceElement elem : elems)
            {
                Log.fatal (elem.toString ());
            }
            
            resp.getWriter ().println ("xmlin: "+e);
            Shell.exit (req, context, 6);
        }
    }
}
