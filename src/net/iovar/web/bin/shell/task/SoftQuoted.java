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
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
class SoftQuoted implements Graph, Text
{
    List<Graph> items;
    
    SoftQuoted ()
    {
        this.items = new ArrayList<Graph> (30);
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('"' != in.peek ()) return false;
        in.discard ();
        
        // re-usable objects for testing
        Escaped escaped = new Escaped ();
        HotQuoted hot = new HotQuoted ();
        Variable variable = new Variable ();
        Literal literal = new Literal ();
        
        for (int c; (c = in.peek ()) >= 0; )
        {
            if ('"'==c)
            {
                in.discard ();
                return true;
            }
            
            if (escaped.assemble (in))
            {
                items.add (escaped);
                escaped = new Escaped ();
            }
            else if (hot.assemble (in))
            {
                items.add (hot);
                hot = new HotQuoted ();
            }
            else if (variable.assemble (in))
            {
                items.add (variable);
                variable = new Variable ();
            }
            else if (literal.assemble (in))
            {
                items.add (literal);
                literal = new Literal ();
            }
            else
            {
                throw new GraphException ();
            }
        }
        
        throw new GraphException ("Missing closing double-quote: "+string ());
    }
    
    public String toString ()
    {
        return '"'+string ()+'"';
    }
    
    String string ()
    {
        final StringBuffer s = new StringBuffer ();
        
        for (final Graph item : items)
        {
            s.append (item.toString ());
        }
        
        return s.toString ();
    }
    
    public String value (final Session shell, final ServletContext context, final HttpSession htsession)
    {
        final StringBuffer s = new StringBuffer ();
        
        for (final Graph item : items)
        {
            if (item instanceof Text)
            {
                final String val = ((Text) item).value (shell, context, htsession);
                if (val!=null) s.append (val);
            }
            else
            {
                s.append (item.toString ());
            }
        }
        
        /*
                return call.exec (shell, context, from!=null ? from.read (shell, context) : (r!=null ? r.data : in),
                        from!=null ? null : (r!=null ? r.type : contentType));
                        */
        
        
        return s.toString ();
    }
}
