/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.parse.*;

// java imports:
import java.io.*;

// 3rd-party imports:

/**
 * Special comment that indicates a named interpreter (command) to parse the script.
 * Hash (#) followed by bang (!) followed by command. Ex: #!/bin/sh
 *
 * @author  shawn@lannocc.com
 */
class HashBang implements Graph
{
    String val;
    
    public boolean assemble (final GraphReader in) throws IOException
    {
        if ('#' != in.peek () || '!' != in.peek (2)) return false;
        in.discard ();
        in.discard ();
        
        final StringBuffer s = new StringBuffer ();
        
        for (int c; (c = in.pop ())>=0; s.append ((char) c))
        {
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
        return "#!"+val;
    }
}
