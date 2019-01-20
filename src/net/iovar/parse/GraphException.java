/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.parse;

// local imports:

// java imports:

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class GraphException extends Exception
{
    //final GraphReader in;
    
    public GraphException (/*final GraphReader in*/)
    {
        super ("<unknown>");
        //this.in = in;
    }
    
    public GraphException (/*final GraphReader in, */final String msg)
    {
        super (msg);
        //this.in = in;
    }
}
