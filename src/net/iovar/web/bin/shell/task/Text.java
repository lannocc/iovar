/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.web.lib.*;

// java imports:
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
interface Text
{
    public String value (final Session shell, final ServletContext context, final HttpSession htsession);
}
