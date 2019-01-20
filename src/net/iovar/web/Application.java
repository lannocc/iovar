/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.web;

// local imports:

// java imports:
import javax.servlet.*;

// 3rd-party imports:

/**
 * Looks for execute bit on file system and if set will simulate POST on GET.
 * When request is for a folder, redirect to folder/index.
 * 
 * Everything handled by Application may have security/authorization checks
 * applied (currently handled via me.idfree.Authentication servlet-filter).
 *
 * @author  shawn@lannocc.com
 */
public class Application extends Default
{
    public void init () throws ServletException
    {
        super.init ();
        
        autoExec = true;
    }
}
