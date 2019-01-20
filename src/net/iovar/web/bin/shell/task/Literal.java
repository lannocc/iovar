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
class Literal implements Graph, Text
{
    char c;
    
    Literal ()
    {
    }
    
    public Literal (final char c)
    {
        this.c = c;
    }
    
    public boolean assemble (final GraphReader in) throws IOException
    {
        int c = in.pop ();
        if (c<0) return false;
        
        this.c = (char) c;
        return true;
    }
    
    public String toString ()
    {
        return String.valueOf (c);
    }
    
    public char value ()
    {
        return c;
    }
    
    public String value (final Session shell, final ServletContext context, final HttpSession htsession)
    {
        return String.valueOf (c);
    }
}
