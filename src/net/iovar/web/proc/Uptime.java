/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.proc;

// local imports:

// java imports:
import java.io.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Track system uptime (in milliseconds).
 *
 * @author  shawn@lannocc.com
 */
public class Uptime extends HttpServlet
{
    final static long start;
    
    static
    {
        start = System.currentTimeMillis ();
    }
    
    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final PrintWriter out = resp.getWriter ();
        
        out.println (get ());
    }
    
    public static long get ()
    {
        return System.currentTimeMillis () - start;
    }
}
