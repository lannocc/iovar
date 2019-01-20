/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.web.lib.*;

// java imports:
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Encapsulation of all the data required for task execution.
 *
 * @author  shawn@lannocc.com
 */
public class TaskData
{
    public final Session shell;
    public final ServletContext context;
    public final String user;
    public final HttpSession htsession;
    public final InputStream in;
    public final String contentType;
    public final String disposition;
    public final String allowOrigin;
    
    /**
     * Minimal constructor.
     */
    public TaskData (final Session shell, final ServletContext context, final String user, final HttpSession htsession)
    {
        this (shell, context, user, htsession, null, null, null, null);
    }
    
    /**
     * Maximal constructor.
     */
    public TaskData (final Session shell, final ServletContext context, final String user, final HttpSession htsession, final InputStream in, final String contentType, final String disposition, final String allowOrigin)
    {
        this.shell = shell;
        this.context = context;
        this.user = user;
        this.htsession = htsession;
        this.in = in;
        this.contentType = contentType;
        this.disposition = disposition;
        this.allowOrigin = allowOrigin;
    }
    
    public String toString ()
    {
        return super.toString () + " { shell: "+shell+" }";
    }
}
