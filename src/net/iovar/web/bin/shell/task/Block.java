/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.parse.*;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import javax.servlet.*;

// 3rd-party imports:

/**
 * A formalized group (i.e. list of operations surrounded by curly braces, invoked sequentially in a sub-shell).
 *
 * @author  shawn@lannocc.com
 */
class Block implements Graph, Task
{
    Group ops;
    
    public boolean assemble (final GraphReader in) throws GraphException, IOException
    {
        if ('{' != in.peek ()) return false;
        // FIXME: we really need a double-peek so we can require whitespace or newline after opening brace
        in.discard ();
        
        Log.debug ("will assemble");
        
        ops = new Group ();
        if (!ops.assemble (in)) ops = null;
        
        if ('}' != in.peek ()) throw new GraphException ("Missing closing curly brace");
        in.discard ();
        
        return true;
    }
    
    public Return exec (final TaskData task) throws IOException, ServletException
    {
        return ops.exec (task);
    }
    
    public String toString ()
    {
        final StringBuffer s = new StringBuffer ();
        s.append ("{ ");
        if (ops!=null) s.append (ops.toString ());
        s.append (" }");
        
        return s.toString ();
    }
}
