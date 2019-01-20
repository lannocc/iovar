/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
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
class And extends Command implements Graph
{
    public boolean assemble (final GraphReader in) throws IOException
    {
        if ('&'!=in.peek () || '&'!=in.peek (2)) return false;
        in.discard ();
        in.discard ();
        
        return true;
    }
    
    public String toString ()
    {
        return "-&&-";
    }
}
