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
 * A named parameter assignment (in contrast/compliment to anonymous arguments).
 *
 * @author  shawn@lannocc.com
 */
public class Parameter implements Graph
{
    CommandText name;
    CommandText value;
    
    Parameter ()
    {
        
    }
    
    Parameter (final String name, final String value)
    {
        this.name = new CommandText (name);
        this.value = new CommandText (value);
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('?' != in.peek ()) return false;
        in.discard ();
        
        name = new CommandText ();
        if (! name.assemble (in)) return false;
        
        value = name.splitAt ('=');
        return true;
    }
    
    public CommandText getName ()
    {
        return name;
    }
    
    public CommandText getValue ()
    {
        return value;
    }
    
    public String toString ()
    {
        return "?{"+name+"}={"+value+"}";
    }
}
