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
 * Divert output to a named resource.
 *
 * @author  shawn@lannocc.com
 */
class Output extends Command implements Graph
{
    boolean append = false;
    CommandText resource;
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('>'!=in.peek ()) return false;
        in.discard ();
        
        if ('>'==in.peek ())
        {
            in.discard ();
            append = true;
        }
        
        new Whitespace ().assemble (in);
        
        CommandText resource = new CommandText ();
        if (! resource.assemble (in)) throw new GraphException ("expecting resource name to follow '>' command");
        this.resource = resource;
        
        return true;
    }
    
    public InputStream write (final Session shell, final ServletContext context, final InputStream data, final String contentType, final HttpSession htsession) throws IOException
    {
        final Transport trans = Transport.handler (resource.value (shell, context, htsession), context, htsession);
        if (append) return trans.patch (data, contentType);
        else return trans.put (data);
    }
    
    public String toString ()
    {
        return "-"+(append? ">>": ">")+"- "+resource;
    }
}
