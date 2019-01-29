/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev.trans;

// local imports:
import net.iovar.web.bin.shell.task.*;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:
//import me.idfree.lib.*;

/**
 * Loopback to the local HTTP interface relative to the webapp root.
 *
 * @author  shawn@lannocc.com
 */
public class Local extends Transport
{
    /**
     * Default constructor is necessary and used for servlet installation only.
     */
    public Local ()
    {
        // no-op
    }
    
    public Local (final String path, final ServletContext context, final HttpSession htsession)
    {
        this (path, context, new HashMap<String,List<String>> (), htsession);
    }
    
    public Local (final String path, final ServletContext context, final Map<String,List<String>> params, final HttpSession htsession)
    {
        setContext (context);
        setPath (path);
        setParams (params);
        setSession (htsession);
    }
    
    public InputStream get () throws IOException
    {
        return new Http (this).get ();
    }
    
    public InputStream put (final InputStream data) throws IOException
    {
        return new Http (this).put (data);
    }
    
    public InputStream patch (final InputStream data, final String contentType) throws IOException
    {
        return new Http (this).patch (data, contentType);
    }
    
    public InputStream delete () throws IOException
    {
        return new Http (this).delete ();
    }
    
    public Return post (final InputStream data, final String contentType) throws IOException
    {
        return new Http (this).post (data, contentType);
    }
    
    public boolean exists () throws IOException
    {
        return new Http (this).exists ();
    }
    
    public Boolean directory () throws IOException
    {
        return new Http (this).directory ();
    }
    
    public Boolean executable () throws IOException
    {
        return new Http (this).executable ();
    }
 
    /*   
    public List<Legend> list (final User user) throws IOException
    {
        final List<Legend> entries = new ArrayList<Legend> ();
        // FIXME
        return entries;
    }
    */
    
    public Set<String> list (final boolean all, final boolean recurse) throws IOException
    {
        final File file = new File (context.getRealPath (path), context, htsession);
        Set<String> fentries = file.list (all, recurse);
        
        final Resource resource = new Resource (path, context, htsession);
        Set<String> rentries = resource.list (all, recurse);
        
        if (fentries == null && rentries == null)
        {
            return null;
        }
        
        final Set<String> entries = new TreeSet<String> ();
        if (fentries != null)
        {
            entries.addAll (fentries);
        }
        if (rentries != null)
        {
            entries.addAll (rentries);
        }
        
        return entries;
    }
}
