/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.json;

// local imports:
import net.iovar.parse.*;

// java imports:
import java.io.*;
import java.util.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class JSONArray implements Graph, Value
{
    List<JSONValue> elements;
    
    JSONArray ()
    {
        this.elements = new ArrayList<JSONValue> ();
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('[' != in.peek ()) return false;
        in.discard ();
        
        final Whitespace white = new Whitespace ();
        white.assemble (in);
        
        if (! add (in, new JSONValue ()))
        {
            throw new GraphException ("empty array");
        }
        
        while (true)
        {
            white.assemble (in);
            if (',' != in.peek ()) break;
            in.discard ();
            white.assemble (in);
        
            if (! add (in, new JSONValue ()))
            {
                throw new GraphException ("missing array value");
            }
        }
        
        if (elements.isEmpty ())
        {
            throw new GraphException ("at least one value is required for array");
        }
        
        if (']' != in.pop ())
        {
            throw new GraphException ("missing closing bracket for array");
        }
        
        return true;
    }
    
    boolean add (final GraphReader in, final JSONValue test) throws GraphException, IOException
    {
        if (test.assemble (in))
        {
            elements.add (test);
            return true;
        }
        
        return false;
    }
    
    public JSONValue get (final int index)
    {
        return elements.get (index);
    }
    
    public String toString ()
    {
        final StringBuilder str = new StringBuilder ("[ ");
        String sep = "";
        
        for (final JSONValue element : elements)
        {
            str.append (sep).append (element);
            sep = ", ";
        }
        
        str.append (" ]");
        return str.toString ();
    }
}
