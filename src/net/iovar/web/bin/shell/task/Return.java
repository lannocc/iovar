/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.web.dev.Log;

// java imports:
import java.io.*;

// 3rd-party imports:

/**
 * Container for things that a task returns (presently mime-type, disposition, InputStream, Status).
 *
 * @author  shawn@lannocc.com
 */
public class Return
{
    public final String type;
    public final String disposition;
    public final String allowOrigin;
    public final InputStream data;
    public final Status status;
    
    public Return (final InputStream data, final Status status)
    {
        this (null, null, null, data, status);
    }
    
    public Return (final String type, final String disposition, final String allowOrigin, final InputStream data, final Status status)
    {
        this.type = type;
        this.disposition = disposition;
        this.allowOrigin = allowOrigin;
        this.data = data;
        this.status = status;
        
        Log.debug ("new Return ( "+type+", "+disposition+", "+data+", "+status+" )");
    }
    
    public String toString ()
    {
        return super.toString () + " [type="+type+", disposition="+disposition+", allowOrigin="+allowOrigin+", data="+data+", status="+status+"]";
    }
}
