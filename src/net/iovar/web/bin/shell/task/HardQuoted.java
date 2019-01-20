/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.parse.*;
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
class HardQuoted implements Graph, Text
{
    String str;
    
    HardQuoted ()
    {
    }
    
    HardQuoted (final String str)
    {
        this.str = str;
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('\'' != in.peek ()) return false;
        in.discard ();
        
        final StringBuffer s = new StringBuffer ();
        
        for (int c; (c = in.pop ()) >= 0; s.append ((char) c))
        {
            if ('\''==c)
            {
                str = s.toString ();
                return true;
            }
        }
        
        throw new GraphException ("Missing closing single-quote: "+s);
    }
    
    public String toString ()
    {
        return "'"+str+"'";
    }
    
    public String value (final Session shell, final ServletContext context, final HttpSession htsession)
    {
        return str;
    }
}
