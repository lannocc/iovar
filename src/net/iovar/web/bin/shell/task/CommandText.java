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
 * Looks for text until reserved character, newline, or whitespace.
 *
 * @author  shawn@lannocc.com
 */
class CommandText implements Graph
{
    List<Text> items;
    
    CommandText ()
    {
        items = new ArrayList<Text> ();
    }
    
    CommandText (final List<Text> items)
    {
        this.items = items;
    }
    
    CommandText (final String text)
    {
        this.items = new ArrayList<Text> (1);
        items.add (new HardQuoted (text));
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        // re-usable objects for testing
        HardQuoted hard = new HardQuoted ();
        HotQuoted hot = new HotQuoted ();
        SoftQuoted soft = new SoftQuoted ();
        Escaped escaped = new Escaped ();
        Variable variable = new Variable ();
        Literal literal = new Literal ();
        
        for (int c; (c = in.peek ())>=0 && ';'!=c && ' '!=c && '\t'!=c && '\n'!=c && '|'!=c && '&'!=c && '#'!=c && '>'!=c && '<'!=c && '}'!=c; )
        {
            if (hard.assemble (in))
            {
                items.add (hard);
                hard = new HardQuoted ();
            }
            else if (hot.assemble (in))
            {
                items.add (hot);
                hot = new HotQuoted ();
            }
            else if (soft.assemble (in))
            {
                items.add (soft);
                soft = new SoftQuoted ();
            }
            else if (escaped.assemble (in))
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
                throw new GraphException ("unexpected: "+((char) c));
            }
        }
        
        if (items.size ()<1) return false;
        
        return true;
    }
    
    public String value (final Session shell, final ServletContext context, final HttpSession htsession)
    {
        final StringBuffer s = new StringBuffer ();
        
        for (final Text item : items)
        {
            final String val = ((Text) item).value (shell, context, htsession);
            if (val!=null) s.append (val);
        }
        
        return s.toString ();
    }
    
    public String toString ()
    {
        return string ();
    }
    
    String string ()
    {
        final StringBuffer s = new StringBuffer ();
        
        for (final Text item : items)
        {
            s.append (item.toString ());
        }
        
        return s.toString ();
    }
    
    /**
     * Split this text at the specified character.
     * This text is modified to contain everything just before the character.
     * 
     * @param   c   character to search for
     * 
     * @return  The text sequence after the character, possibly empty. Returns
     *          <tt>null</tt> if the character was not found.
     */
    public CommandText splitAt (final char c)
    {
        final ArrayList<Text> before = new ArrayList<Text> (items.size());
        final ArrayList<Text> after = new ArrayList<Text> (items.size());
        boolean found = false;
        
        for (final Text item : items)
        {
            if (!found)
            {
                if (item instanceof Literal && c==((Literal) item).value ())
                {
                    found = true;
                    continue;
                }
                
                before.add (item);
            }
            else // found
            {
                after.add (item);
            }
        }
        
        if (found)
        {
            this.items = before;
            return new CommandText (after);
        }
        else
        {
            return null;
        }
    }
}
