/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev.trans;

// local imports:
import net.iovar.web.*;

// java imports:
import java.net.*;
import java.util.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class Https extends Http
{
    /**
     * Default constructor is necessary and used for servlet installation only.
     */
    public Https ()
    {
        // no-op
    }
    
    /*
    public Https (final String path)
    {
        super (path);
    }
    
    public Https (final String path, final Map<String,List<String>> params)
    {
        super (path, params);
    }
    */
    
    public Https (final Local local)
    {
        super (local);
    }
    
    protected URL getURL () throws MalformedURLException
    {
        final String query = params!=null ? ("?"+Utils.buildQuery (params)) : "";
        return new URL (new URL ("https", "", ""), path+query);
    }
}
