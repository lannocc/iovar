/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.parse.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;

// java imports:
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
class Input extends Command implements Graph
{
    CommandText resource;
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('<'!=in.peek ()) return false;
        in.discard ();
        
        new Whitespace ().assemble (in);
        
        CommandText resource = new CommandText ();
        if (! resource.assemble (in)) throw new GraphException ("expecting resource name to follow '<' command");
        this.resource = resource;
        
        return true;
    }
    
    public InputStream read (final Session shell, final ServletContext context, final HttpSession htsession) throws IOException
    {
        return Transport.handler (resource.value (shell, context, htsession), context, htsession).get ();
    }
    
    public String toString ()
    {
        return "-<- "+resource;
    }
}
