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
 * Convert XML tree to Java object.
 * 
 * @author  shawn@lannocc.com
 */
public class XML2Java extends HttpServlet
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
        
        out.println ("usage: xml2java");
        out.println ();
        out.println ("Convert XML tree passed as standard input into Java object.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
    }
    
    /**
     * Execute.
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
        
        /*
        if (params.size ()!=0)
        {
            resp.getWriter ().println ("xml2java: no arguments expected");
            Shell.exit (req, context, 1);
            return;
        }
        */
        
        /*
        if (!"text/xml".equals (req.getContentType ()))
        {
            resp.getWriter ().println ("xml2java: expected input of 'text/xml' content-type");
            Shell.exit (req, context, 2);
            return;
        }
        */
        
        
        try
        {
            final Object obj = Utils.xstream.fromXML (req.getInputStream ());
            resp.setContentType ("application/x-java-serialized-object");

            final ObjectOutputStream out = new ObjectOutputStream (resp.getOutputStream ()); try
            {
    //            out.writeObject (map (req.getInputStream ()));
                out.writeObject (obj);
            }
            finally
            {
                out.close ();
            }
        
            Shell.exit (req, context, 0);
        }
        catch (final Exception e)
        {
            Log.fatal ("uncaught exception", e);
            resp.getWriter ().println ("xml2java: uncaught exception: "+e);
            Shell.exit (req, context, 99);
        }
    }
    
    /*
    public static Map map (final InputStream in)
    {
        try
        {
            final SAXParserFactory factory = SAXParserFactory.newInstance ();
            factory.setNamespaceAware (true);
            final SAXParser parser = factory.newSAXParser ();
            final XMLReader xml = parser.getXMLReader ();
            
            final Handler handler = new Handler ();
            xml.setContentHandler (handler);
            xml.parse (new InputSource (in));
        }
        catch (final ParserConfigurationException e)
        {
            Log.error (e);
            resp.getWriter ().println ("xlink: "+e);
            Shell.exit (req, context, 3);
        }
        catch (final SAXException e)
        {
            Log.error (e);
            resp.getWriter ().println ("xlink: "+e);
            Shell.exit (req, context, 4);
        }
    }

    static class Handler extends DefaultHandler
    {
        final Map map;
        final List<String> stack;
        
        Map current;
        StringBuffer val;
        
        boolean ended;
        boolean chars;
        
        Handler ()
        {
            this.map = new HashMap ();
            this.stack = new ArrayList<String> ();
            
            this.current = this.map;
        }

        public void startElement (final String uri, final String name, final String qname, final Attributes attrs) throws SAXException
        {
            stack.add (0, qname);
            val = new StringBuffer ();
        }
    
        public void characters (final char[] ch, int start, int length) throws SAXException
        {
            for (final char c : ch)
            {
                val.append (c);
            }
        }

        public void endElement (final String uri, final String name, final String qname) throws SAXException
        {
            if (val.length ()>0)
            {
                current.put (stack.get (0), val.toString ());
            }
            
            stack.remove (0);
        }
    }
    */
}
