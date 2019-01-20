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

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class JSONValue implements Graph
{
    Value val;
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if (set (in, new JSONMap ())) return true;
        if (set (in, new JSONArray ())) return true;
        if (set (in, new JSONString ())) return true;
        if (set (in, new JSONTrue ())) return true;
        if (set (in, new JSONFalse ())) return true;
        if (set (in, new JSONNull ())) return true;
        if (set (in, new JSONNumber ())) return true;
        
        // no match
        return false;
    }
    
    boolean set (final GraphReader in, final Value test) throws GraphException, IOException
    {
        if (test.assemble (in))
        {
            this.val = test;
            return true;
        }
        
        return false;
    }
    
    public Value value ()
    {
        return val;
    }
    
    public String toString ()
    {
        return val.toString ();
    }
}
