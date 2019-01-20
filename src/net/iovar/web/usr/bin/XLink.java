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
import net.iovar.xml.Copy;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

// 3rd-party imports:

/**
 * Follow xlink:href and embed.
 * 
 * @author  shawn@lannocc.com
 */
public class XLink extends HttpServlet
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
        
        out.println ("usage: xlink [<xmldoc>]");
        out.println ();
        out.println ("Follow xlink:href and embed. If <xmldoc> is omitted then standard input is used.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help  - display this help screen");
        out.println ("   ?quiet - do not add comments");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[0]: xmldoc
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
        if (vals!=null && vals.size ()>1)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final String resource;
        if (vals!=null && !vals.isEmpty ()) resource = vals.get (0);
        else resource = null;
        
        final InputSource in;
        if (resource==null)
        {
            final InputStream rin = req.getInputStream ();
            if (rin==null)
            {
                Log.error ("no data");
                resp.getWriter ().println ("xlink: nothing to do");
                Shell.exit (req, context, 2);
                return;
            }
            
            in = new InputSource (req.getInputStream ());
        }
        else
        {
            in = new InputSource (Transport.handlerHref (resource, context, req.getSession ()).get ());
        }
        
        try
        {
            final SAXParserFactory factory = SAXParserFactory.newInstance ();
            factory.setNamespaceAware (true);
            final SAXParser parser = factory.newSAXParser ();
            final XMLReader xml = parser.getXMLReader ();
            xml.setContentHandler (new Handler (factory, context, req.getSession (), resp.getWriter (), params.containsKey ("quiet")));
            xml.parse (in);
            
            Shell.exit (req, context, 0);
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
    
    static class Handler extends Copy
    {
        final SAXParserFactory factory;
        final ServletContext context;
        final HttpSession htsession;
        final boolean quiet;
        
        int linkNest = 0;
        
        Handler (final SAXParserFactory factory, final ServletContext context, final HttpSession htsession, final Writer out, final boolean quiet)
        {
            super (out);
            this.factory = factory;
            this.context = context;
            this.htsession = htsession;
            this.quiet = quiet;
        }
        
        public void startElement (final String uri, final String name, final String qname, final Attributes attrs) throws SAXException
        {
            if (linkNest>0)
            {
                linkNest++;
                return;
            }
            
            // FIXME: just a hack, not actually checking against XLINK namespace URI yet...
            
            String href = null;
            String show = null;
            String actuate = null;
            
            if (attrs!=null)
            {
                for (int i=0; i<attrs.getLength (); i++)
                {
                    if ("xlink:href".equals (attrs.getQName (i)))
                    {
                        href = attrs.getValue (i);
                    }
                    else if ("xlink:show".equals (attrs.getQName (i)))
                    {
                        show = attrs.getValue (i);
                    }
                    else if ("xlink:actuate".equals (attrs.getQName (i)))
                    {
                        actuate = attrs.getValue (i);
                    }
                }
            }
            
            if (href!=null && (show==null || "embed".equals (show)) && (actuate==null || "onLoad".equals (actuate)))
            {
                Log.info ("embedding: "+href);
                linkNest++;
                
                //super.startElement (uri, name, qname, attrs);
                
                try
                {
                    if (!quiet) out.write ("<!-- BEGIN XLINK EMBED: "+Utils.toXML (href)+" -->");
                    
                    final SAXParser parser = factory.newSAXParser ();
                    final XMLReader xml = parser.getXMLReader ();
                    xml.setContentHandler (new ChildCopy (out, attrs));

                    try
                    {
                        xml.parse (new InputSource (Transport.handlerHref (href, context, htsession).get ()));
                    }
                    catch (final SAXParseException e)
                    {
                        Log.info ("xlink target is not an XML document... embedding in current tag");
                        out.append ("<"+qname);
                        
                        if (attrs!=null && attrs.getLength ()>0)
                        {
                            for (int i=0; i < attrs.getLength (); i++)
                            {
                                final String an = attrs.getQName (i);
                                if (! ("xlink:href".equals (an) || "xlink:show".equals (an) || "xlink:actuate".equals (an)))
                                {
                                    out.append (" "+an+"=\""+Utils.toXML (attrs.getValue (i))+"\"");
                                }
                            }
                        }
                        out.append (">");
                        Utils.pipe (Transport.handler (href, context, htsession).get (), out);
                        out.append ("</"+qname+">");
                    }
                }
                catch (final ParserConfigurationException e)
                {
                    throw new SAXException ("while creating parser for "+href, e);
                }
                catch (final IOException e)
                {
                    throw new SAXException ("while writing output or reading "+href, e);
                }
            }
            else
            {
                super.startElement (uri, name, qname, attrs);
            }
        }
        
        public void endElement (final String uri, final String name, final String qname) throws SAXException
        {
            if (linkNest>0)
            {
                linkNest--;
                
                if (linkNest<1)
                {
                    try
                    {
                        if (!quiet) out.write ("<!-- END XLINK EMBED -->");
                    }
                    catch (final IOException e)
                    {
                        throw new SAXException (e);
                    }
                }
            }
            else
            {
                super.endElement (uri, name, qname);
            }
        }
        
        public void characters (final char[] ch, int start, int length) throws SAXException
        {
            if (linkNest<1)
            {
                super.characters (ch, start, length);
            }
        }
    }

    static class ChildCopy extends Copy
    {
        final Attributes attrs;
        boolean isRoot;

        ChildCopy (final Writer out, final Attributes attrs)
        {
            super (out);
            this.attrs = attrs;
            this.isRoot = true;
        }

        public void startDocument ()
        {
            // no-op
        }

        public void processingInstruction (final String target, final String data)
        {
            // no-op
        }

        public void midElement (final String uri, final String name, final String qname, final Attributes attrs) throws SAXException
        {
            try
            {
                if (isRoot)
                {
                    isRoot = false;

                    /* copy in non-xlink attributes from parent */

                    // FIXME: just a hack, not actually checking against XLINK namespace URI yet...
                    for (int i=0; i<this.attrs.getLength (); i++)
                    {
                        final String aname = this.attrs.getQName (i);

                        if (aname!=null && !aname.startsWith ("xlink:"))
                        {
                            out.append (" "+aname+"=\""+this.attrs.getValue (i)+"\"");
                        }
                    }
                    
                }
            }
            catch (final IOException e)
            {
                throw new SAXException (e);
            }
        }
    }
}
