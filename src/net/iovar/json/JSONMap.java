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
public class JSONMap implements Graph, Value
{
    final List<Entry> entries;
    
    public JSONMap ()
    {
        entries = new ArrayList<Entry> ();
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('{' != in.peek ()) return false;
        in.discard ();
        
        // re-usable objects for testing
        final Whitespace white = new Whitespace ();
        
        while (true)
        {
            white.assemble (in);

            final JSONString key = new JSONString ();
            if (! key.assemble (in)) break;

            white.assemble (in);

            if (':' != in.pop ())
            {
                throw new GraphException ("missing colon separator after key: "+key);
            }

            white.assemble (in);

            final JSONValue val = new JSONValue ();
            if (! val.assemble (in))
            {
                throw new GraphException ("missing map value after key: "+key);
            }
            
            entries.add (new Entry (key, val));
            
            white.assemble (in);
            if (',' != in.peek ()) break;
            in.discard ();
        }
        
        if (entries.isEmpty ())
        {
            throw new GraphException ("at least one key-value pair required for a map");
        }
        
        white.assemble (in);
        
        if ('}' != in.pop ())
        {
            throw new GraphException ("missing closing brace");
        }

        return true;
    }
    
    public JSONValue get (final String key)
    {
        for (final Entry entry : entries)
        {
            if (entry.key.equals (key))
            {
                return entry.val;
            }
        }
        
        return null;
    }
    
    public String toString ()
    {
        final StringBuilder str = new StringBuilder ("{ ");
        String sep = "";
        
        for (final Entry entry : entries)
        {
            str.append (sep).append (entry);
            sep = ", ";
        }
        
        str.append (" }");
        return str.toString ();
    }
    
    static class Entry
    {
        final JSONString key;
        final JSONValue val;
        
        Entry (final JSONString key, final JSONValue val)
        {
            this.key = key;
            this.val = val;
        }
    
        public String toString ()
        {
            return key+" : "+val;
        }
    }
}
