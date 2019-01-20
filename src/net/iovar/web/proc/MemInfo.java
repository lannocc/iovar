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
 * Track memory usage statistics.
 *
 * @author  shawn@lannocc.com
 */
public class MemInfo extends HttpServlet
{
    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final PrintWriter out = resp.getWriter ();
        
        final Runtime run = Runtime.getRuntime ();
        
        out.println ("max: "+run.maxMemory ());
        out.println ("allocated: "+run.totalMemory ());
        out.println ("free: "+run.freeMemory());
    }
}
