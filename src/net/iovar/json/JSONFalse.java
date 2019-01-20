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
public class JSONFalse implements Graph, Value
{
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('f' != in.peek (1)) return false;
        if ('a' != in.peek (2)) return false;
        if ('l' != in.peek (3)) return false;
        if ('s' != in.peek (4)) return false;
        if ('e' != in.peek (5)) return false;
        for (int i=0; i<5; i++) in.discard ();
        return true;
    }
    
    public String toString ()
    {
        return "false";
    }
}
