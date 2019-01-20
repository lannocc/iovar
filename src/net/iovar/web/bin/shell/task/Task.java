/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:

// java imports:
import java.io.*;
import javax.servlet.*;

// 3rd-party imports:

/**
 * Any number of executable things.
 *
 * @author  shawn@lannocc.com
 */
interface Task
{
    //Status run (final Reader in, final Writer out, final Writer err) throws IOException;
    
    Return exec (final TaskData task) throws IOException, ServletException;
}
