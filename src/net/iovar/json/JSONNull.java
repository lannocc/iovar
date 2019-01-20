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
public class JSONNull implements Graph, Value
{
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('n' != in.peek (1)) return false;
        if ('u' != in.peek (2)) return false;
        if ('l' != in.peek (3)) return false;
        if ('l' != in.peek (4)) return false;
        for (int i=0; i<4; i++) in.discard ();
        return true;
    }
    
    public String toString ()
    {
        return "null";
    }
}
