/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev;

// local imports:
import net.iovar.web.*;

// java imports:
import java.io.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Simple do-nothing interface
 *
 * @author  shawn@lannocc.com
 */
public class Null extends HttpServlet
{
    protected void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doHead (this, req, resp);
    }
    
    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        //Utils.doGet (this, req, resp);
    }
    
    protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        //Utils.doPut (this, req, resp);
    }
}

