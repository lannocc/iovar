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
 * Reference to a variable.
 *
 * @author  shawn@lannocc.com
 */
class Variable implements Graph, Text
{
    String name;
    
    Variable ()
    {
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('$' != in.peek ()) return false;
        in.discard ();
        this.name = null;
        
        boolean braces = ('{'==in.peek ());
        if (braces) in.discard ();
        
        // special reserved variable names
        if ('?'==in.peek () || '@'==in.peek () || '#'==in.peek ())
        {
            final char c = (char) in.pop ();
            this.name = String.valueOf (c);
            if (!braces) return true;
        }
        
        final StringBuffer s = new StringBuffer ();
        
        for (int c; (c = in.peek ())>=0; s.append ((char) c))
        {
            if (braces)
            {
                in.discard ();
                
                if ('}'==c)
                {
                    braces = false;
                    break;
                }
            }
            else if ( !allowed ((char) c) )
            {
                break;
            }
            else
            {
                in.discard ();
            }
        }
        
        if (braces) throw new GraphException ("Missing closing curly-brace: "+s);
        
        if (this.name==null)
        {
            if (s.length ()<1) throw new GraphException ("Zero-length variable name");
            this.name = s.toString ();
        }
        else // special variable
        {
            if ("#".equals (this.name))
            {
                if (s.length () > 0) try
                {
                    final int ival = Integer.parseInt (s.toString ());
                    if (ival<0) throw new GraphException ("Invalid parameter number for $#: "+ival);
                    this.name += ival;
                }
                catch (NumberFormatException e)
                {
                    throw new GraphException ("Invalid parameter number for $#: "+e);
                }
            }
            else if (s.length () > 0)
            {
                throw new GraphException ("Invalid variable name: "+this.name + s);
            }
        }
        
        return true;
    }

    public String value (Session shell, final ServletContext context, final HttpSession htsession)
    {
        if (shell!=null) return shell.get (name);
        else return "";
    }
    
    public String toString ()
    {
        return "${"+name+"}";
    }
    
    /**
     * Test if the specified character is allowed in a variable name.
     */
    public static boolean allowed (final char c)
    {
        return ('a'<=c && c<='z') || ('A'<=c && c<='Z') || ('1'<=c && c<='9') || '0'==c || '_'==c;
    }
}
