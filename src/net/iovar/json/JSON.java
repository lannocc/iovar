/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.json;

// local imports:
import net.iovar.parse.*;

// java imports:
import java.io.*;

// 3rd-party imports:

/**
 * Encapsulation and parser for JSON data.
 *
 * @author  shawn@lannocc.com
 */
public class JSON implements Graph
{
    JSONMap map;
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        new Whitespace ().assemble (in);
        
        map = new JSONMap ();
        if (! map.assemble (in))
        {
            throw new GraphException ("not a JSON map");
        }
        
        return true;
    }
    
    public String toString ()
    {
        return map.toString ();
    }
    
    public static void main (final String[] args) throws Exception
    {
        final GraphReader in = new GraphReader (new InputStreamReader (System.in, java.nio.charset.StandardCharsets.UTF_8));
        
        final Graph json = new JSON ();
        if (! json.assemble (in))
        {
            System.err.println ("did not assemble");
            return;
        }
        
        System.out.println (json);
    }
}
