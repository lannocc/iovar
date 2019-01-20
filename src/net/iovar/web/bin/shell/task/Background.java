/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.parse.*;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;

// 3rd-party imports:

/**
 * Start the operation in the background and return.
 *
 * @author  shawn@lannocc.com
 */
class Background extends Command implements Graph
{
    public boolean assemble (final GraphReader in) throws IOException
    {
        Log.debug ("testing for background");
        
        if ('&'!=in.peek ()) return false;
        // FIXME: this test not exhaustive
        if (! (in.peek (2)<0 || ';'==in.peek (2) || '\n'==in.peek (2) || ' '==in.peek (2))) return false;
        in.discard ();
        
        return true;
    }
    
    public String toString ()
    {
        return "-&-";
    }
}
