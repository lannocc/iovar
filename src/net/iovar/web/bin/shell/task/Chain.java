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
 * Chain command passes output from one call as input to another call.
 *
 * @author  shawn@lannocc.com
 */
class Chain extends Command implements Graph
{
    boolean buffered;
    
    public boolean assemble (final GraphReader in) throws IOException
    {
        if ('|'!=in.peek ()) return false;
        int c = in.peek (2);
        
        // FIXME: this test isn't exhaustive
        if ('|'==c) return false;
        
        in.discard ();
        
        buffered = '=' == c;
        if (buffered) in.discard ();
        
        return true;
    }
    
    public String toString ()
    {
        return "-|"+(buffered ? "=" : "")+"-";
    }
}
