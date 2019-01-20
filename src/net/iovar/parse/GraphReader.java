/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.parse;

// local imports:

// java imports:
import java.io.*;

// 3rd-party imports:

/**
 * @author  shawn@lannocc.com
 */
public class GraphReader
{
    final static int READY = -2;
    
    final Reader src;
    final StringBuffer buffer;
    
//    int line = 0;
//    int col = 0;
    
    public GraphReader (final Reader src)
    {
        this.src = src;
        this.buffer = new StringBuffer ();
    }
    
    public int peek () throws IOException
    {
        return peek (1);
    }
    
    public int peek (final int ahead) throws IOException
    {
        while (buffer.length () < ahead)
        {
            final int c = src.read ();
            if (c<0) return c;
            buffer.append ((char) c);
        }
        
        return buffer.charAt (ahead-1);
    }
    
    public int pop () throws IOException
    {
        int c = peek ();
        discard ();
        return c;
    }
    
    public boolean discard () throws IOException
    {
        return discard (1);
    }
    
    public boolean discard (final int count) throws IOException
    {
        for (int i=0; i < count; i++)
        {
            int c = peek ();
            if (c<0) return false;

            buffer.deleteCharAt (0);
        }
        
        return true;
    }
    
    /*
    private void increment (final char c)
    {
        if ('\n'==c)
        {
            line++;
            col = 0;
        }
        else
        {
            if (col == 0)
            {
                line++;
            }
            
            col++;
        }
    }
    
    public int getLine ()
    {
        return line;
    }
    
    public int getColumn ()
    {
        return col;
    }
    
    public static void main (final String[] args) throws Exception
    {
        final GraphReader in = new GraphReader (new StringReader (
                "Hello.\nThis is a test"
                ));
        
        System.out.println ("peek: "+((char) in.peek ()));
        System.out.println ("peek: "+((char) in.peek ()));
        System.out.println ("next: "+((char) in.next ()));
        System.out.println ("peek: "+((char) in.peek ()));
        System.out.println ("peek: "+((char) in.peek ()));
        System.out.println ("next: "+((char) in.next ()));
        System.out.println ("next: "+((char) in.next ()));
        System.out.println ("line,col: "+in.getLine ()+","+in.getColumn());
        System.out.println ("next: "+((char) in.next ()));
        System.out.println ("next: "+((char) in.next ()));
        System.out.println ("line,col: "+in.getLine ()+","+in.getColumn());
    }
    */
}
