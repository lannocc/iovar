/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.parse.*;

// java imports:
import java.io.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
class Whitespace implements Graph
{
    public boolean assemble (final GraphReader in) throws IOException
    {
        boolean found = false;
        
        for (int c; (c = in.peek ())>=0; in.discard ())
        {
            if (' '!=c && '\t'!=c)
            {
                break;
            }
            
            found = true;
        }
        
        return found;
    }
    
    public String toString ()
    {
        return " ";
    }
}
