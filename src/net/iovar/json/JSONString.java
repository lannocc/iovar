/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
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
public class JSONString implements Graph, Text, Value
{
    List<Text> items;
    
    JSONString ()
    {
        this.items = new ArrayList<Text> (30);
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('"' != in.peek ()) return false;
        in.discard ();
        
        // variables for testing
        Escaped escaped = new Escaped ();
        Literal literal = new Literal ();
        
        for (int c; (c = in.peek ()) >= 0; )
        {
            if ('"'==c)
            {
                in.discard ();
                return true;
            }
            
            if (escaped.assemble (in))
            {
                items.add (escaped);
                escaped = new Escaped ();
            }
            else if (literal.assemble (in))
            {
                items.add (literal);
                literal = new Literal ();
            }
            else
            {
                throw new GraphException ();
            }
        }
        
        throw new GraphException ("Missing closing double-quote: "+display ());
    }
    
    public String toString ()
    {
        return '"'+display ()+'"';
    }
    
    String display ()
    {
        final StringBuffer s = new StringBuffer ();
        
        for (final Text item : items)
        {
            s.append (item.toString ());
        }
        
        return s.toString ();
    }
    
    public String value ()
    {
        final StringBuilder s = new StringBuilder ();
        
        for (final Text item : items)
        {
            s.append (item.value ());
        }
        
        return s.toString ();
    }
    
    public boolean equals (final String s)
    {
        return display ().equals (s);
    }
    
    public static void main (final String[] args) throws Exception
    {
        final GraphReader in = new GraphReader (new InputStreamReader (System.in, java.nio.charset.StandardCharsets.UTF_8));
        
        final Graph json = new JSONString ();
        if (! json.assemble (in))
        {
            System.err.println ("did not assemble");
            return;
        }
        
        System.out.println (json);
    }
}
