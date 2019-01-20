/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.json;

// local imports:
import net.iovar.parse.*;

// java imports:
import java.io.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
class Escaped implements Graph, Text
{
    char c;
    
    Escaped ()
    {
    }
    
    public Escaped (final char c)
    {
        this.c = c;
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('\\' != in.peek ()) return false;
        in.discard ();
        
        int c = in.pop ();
        if (c<0) return false;

        switch ((char) c)
        {
            case '"':
            case '\\':
            case '/':
                this.c = (char) c;
                break;
                
            case 'b': this.c = '\b'; break;
            case 'f': this.c = '\f'; break;
            case 'n': this.c = '\n'; break;
            case 'r': this.c = '\r'; break;
            case 't': this.c = '\t'; break;
                
            case 'u':
                this.c = hex (c, in.pop (), in.pop (), in.pop ());
                break;
                
            default:
                throw new GraphException ("not a valid escape character: "+((char) c));
        }
        
        return true;
    }
    
    char hex (final int u1, final int u2, final int u3, final int u4) throws GraphException
    {
        return (char) (16*16*16 * hex (u1) + 16*16 * hex (u2) + 16 * hex (u3) + hex (u4));
    }
    
    char hex (final int c) throws GraphException
    {
        if (c<0) throw new GraphException ("end of stream while reading hex symbol (incomplete)");
        
        if ('0' <= c && c <= '9') return (char) (c - '0');
        if ('A' <= c && c <= 'F') return (char) (10 + c - 'A');
        if ('a' <= c && c <= 'f') return (char) (10 + c - 'a');
        
        throw new GraphException ("not a valid hex symbol: "+c);
    }
    
    public String toString ()
    {
        if ('"'==c || '\\'==c || '/'==c) return "\\"+c;
        else return value ();
    }
    
    public String value ()
    {
        return String.valueOf (c);
    }
}
