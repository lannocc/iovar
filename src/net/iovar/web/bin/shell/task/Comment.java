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
class Comment implements Graph
{
    String val;
    
    public boolean assemble (final GraphReader in) throws IOException
    {
        if ('#' != in.peek ()) return false;
        in.discard ();
        
        final StringBuffer s = new StringBuffer ();
        
        for (int c; (c = in.peek ())>=0; s.append ((char) c))
        {
            in.discard ();
            
            if ('\n'==c)
            {
                break;
            }
        }
        
        val = s.toString ();
        
        return true;
    }
    
    String getVal ()
    {
        return val;
    }
    
    public String toString ()
    {
        return '#'+val;
    }
}
