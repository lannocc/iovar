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

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
class Literal implements Graph, Text
{
    char c;
    
    Literal ()
    {
    }
    
    public Literal (final char c)
    {
        this.c = c;
    }
    
    public boolean assemble (final GraphReader in) throws IOException
    {
        int c = in.pop ();
        if (c<0) return false;
        
        this.c = (char) c;
        return true;
    }
    
    public String toString ()
    {
        return value ();
    }
    
    public String value ()
    {
        return String.valueOf (c);
    }
}
