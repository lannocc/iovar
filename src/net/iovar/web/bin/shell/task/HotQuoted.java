/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.parse.*;
import net.iovar.web.dev.*;
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
class HotQuoted implements Graph, Text, Task
{
    List<Graph> items;
    
    HotQuoted ()
    {
        this.items = new ArrayList<Graph> (30);
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('`'!=in.peek ()) return false;
        in.discard ();
        
        // re-usable objects for testing
        Escaped escaped = new Escaped ();
        Variable variable = new Variable ();
        Literal literal = new Literal ();
        
        for (int c; (c = in.peek ()) >= 0; )
        {
            if ('`'==c)
            {
                in.discard ();
                return true;
            }
            
            if (escaped.assemble (in))
            {
                items.add (escaped);
                escaped = new Escaped ();
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
        
        throw new GraphException ("Missing closing back-quote: "+string ());
    }
    
    public String toString ()
    {
        return '`'+string ()+'`';
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
        try
        {
            /* FIXME (user) */
            final Return r = exec (new TaskData (shell, context, null, htsession));
            if (r==null)
            {
                Log.error ("null return for "+this);
                return null;
            }
            
            if (r.type==null || !r.type.startsWith ("text/"))
            {
                Log.warn ("non-text mime type returned for "+this+": "+r.type);
            }
            
            if (r.data==null)
            {
                Log.warn ("null data stream returned for "+this);
                return null;
            }
            
            final StringBuffer s = new StringBuffer ();
            final InputStreamReader in = new InputStreamReader (r.data);
            for (int c; (c = in.read ())>=0; s.append ((char) c));
            return s.toString ();
        }
        catch (final IOException e)
        {
            Log.error ("shell exec failed", e);
            return "shell exec failed: "+e;
        }
        catch (final ServletException e)
        {
            Log.error ("shell exec failed", e);
            return "shell exec failed: "+e;
        }
    }
    
    public Return exec (final TaskData task) throws IOException, ServletException
    {
        Log.debug ("exec: "+this);
        
        final StringBuffer value = new StringBuffer ();
        for (final Graph item : items)
        {
            if (item instanceof Text)
            {
                value.append (((Text) item).value (task.shell, task.context, task.htsession));
            }
            else
            {
                value.append (item.toString ());
            }
        }
        
        try
        {
            final Group group = new Group ();
            
            if (! group.assemble (new GraphReader (new StringReader (value.toString ()))))
            {
                Log.warn ("empty assembly for "+this);
                return null; //FIXME?
            }
            
            return group.exec (task);
        }
        catch (final GraphException e)
        {
            Log.error ("graph assembly failed for "+this, e);
            throw new ServletException ("graph assembly failed", e);
        }
    }
}
