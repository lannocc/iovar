/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.xml;

// local imports:
import net.iovar.web.*;

// java imports:
import java.io.*;
import java.util.*;
import org.xml.sax.*;

// 3rd-party imports:

/**
 * Copy XML source to destination.
 * 
 * @author  shawn@lannocc.com
 */
public class Copy implements ContentHandler
{
    final protected Writer out;
    final Map<String,String> mappings;
    
    public Copy (final Writer out)
    {
        this.out = out;
        this.mappings = new HashMap<String,String> ();
    }
    
    public void setDocumentLocator (final Locator locator)
    {
        // no-op
    }

    public void startDocument () throws SAXException
    {
        try
        {
            out.append ("<?xml version=\"1.0\"?>\n");
        }
        catch (final IOException e)
        {
            throw new SAXException (e);
        }
    }

    public void endDocument () throws SAXException
    {
    }
    
    public void processingInstruction (final String target, final String data) throws SAXException
    {
        try
        {
            out.append ("<?"+target+" "+data+"?>\n");
        }
        catch (final IOException e)
        {
            throw new SAXException (e);
        }
    }
    
    public void startPrefixMapping (final String prefix, final String uri) throws SAXException
    {
        mappings.put (prefix, uri);
    }
    
    public void endPrefixMapping (final String prefix)
    {
        // no-op
    }

    public void startElement (final String uri, final String name, final String qname, final Attributes attrs) throws SAXException
    {
        try
        {
            out.append ("<"+qname);
            
            for (final Map.Entry<String,String> mapping : mappings.entrySet ())
            {
                out.append (" xmlns:"+mapping.getKey ()+"=\""+mapping.getValue ()+"\"");
            }
            mappings.clear ();

            if (attrs!=null && attrs.getLength ()>0)
            {
                for (int i=0; i < attrs.getLength (); i++)
                {
                    out.append (" "+attrs.getQName (i)+"=\""+Utils.toXML (attrs.getValue (i))+"\"");
                }
            }

            midElement (uri, name, qname, attrs);

            out.append (">");
        }
        catch (final IOException e)
        {
            throw new SAXException (e);
        }
    }

    /**
     * Subclasses may implement this.
     */
    protected void midElement (final String uri, final String name, final String qname, final Attributes attrs) throws SAXException
    {
        // no-op
    }

    public void endElement (final String uri, final String name, final String qname) throws SAXException
    {
        try
        {
            out.append ("</"+qname+">");
        }
        catch (final IOException e)
        {
            throw new SAXException (e);
        }
    }
    
    public void characters (final char[] ch, int start, int length) throws SAXException
    {
        try
        {
            final int end = start+length;
            for (int i=start; i<end; i++)
            {
                out.write (Utils.toXML (ch[i]));
            }
        }
        catch (final IOException e)
        {
            throw new SAXException (e);
        }
    }
    
    public void ignorableWhitespace (final char[] ch, int start, int length) throws SAXException
    {
        // no-op
    }
    
    public void skippedEntity (final String name)
    {
        // no-op
    }
}
