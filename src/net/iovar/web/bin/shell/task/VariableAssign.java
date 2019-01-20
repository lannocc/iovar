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
 * Variable assignment command.
 *
 * @author  shawn@lannocc.com
 */
class VariableAssign extends Command implements Graph
{
    String name;
    CommandText value;
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        int count = 0;
        for (int c; (c = in.peek (count+1))>=0 && Variable.allowed ((char) c); count++);
        if (count<1) return false;
        if ('='!=in.peek (count+1)) return false;
        
        final StringBuffer name = new StringBuffer ();
        for (int i=0; i<count; i++) name.append ((char) in.pop ());
        this.name = name.toString ();
        
        in.discard (); // equals sign
        
        value = new CommandText ();
        if (! value.assemble (in)) value = null;
        
        return true;
    }
    
    public String getName ()
    {
        return name;
    }
    
    public CommandText getValue ()
    {
        return value;
    }
    
    public String toString ()
    {
        return "{"+name+"}={"+value+"}";
    }
}
