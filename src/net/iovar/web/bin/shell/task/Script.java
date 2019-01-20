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
 * An optional named interpreter followed by group of operations.
 * Empty input will not assemble.
 *
 * @author  shawn@lannocc.com
 */
public class Script implements Graph, Task
{
    HashBang interpreter;
    Group ops;
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        interpreter = new HashBang ();
        if (! interpreter.assemble (in))
        {
            interpreter = null;
        }
        Log.debug ("hashbang: "+interpreter);
        if (interpreter != null) Log.info ("interpreter: "+interpreter.getVal());
        
        ops = new Group ();
        if (! ops.assemble (in))
        {
            Log.debug ("no group assembly");
            return false;
        }
        
        return true;
    }
    
    /*
    public HashBang getInterpreter ()
    {
        return interpreter;
    }
    */
    
    /*
     * this is shell exec only: the calling party is responsible for invoking any other interpreter
     */
    public Return exec (final TaskData task) throws IOException, ServletException
    {
/*
Log.fatal ("script exec: "+ops);
BufferedReader something = new BufferedReader (new InputStreamReader (task.in));
for (String line; (line = something.readLine ())!=null; )
{
    Log.fatal ("BLAH: "+line);
}
*/
        return ops.exec (task);
    }
    
    public String toString ()
    {
        return super.toString ()+": interpreter="+interpreter+", { "+ops+" }";
    }
}
