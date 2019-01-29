/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev.trans;

// local imports:
import net.iovar.web.bin.shell.*;
import net.iovar.web.bin.shell.task.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.util.*;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:
//import me.idfree.lib.*;

/**
 * Access files on the host (root) file system.
 *
 * @author  shawn@lannocc.com
 */
public class File extends Transport
{
    final String user;
    
    /**
     * Default constructor is necessary and used for servlet installation only.
     */
    public File ()
    {
        // no-op
        this.user = null;
    }
    
    public File (final String path, final ServletContext context, final HttpSession htsession)
    {
        this (path, context, null, new HashMap<String,List<String>> (), htsession);
    }
    
    public File (final String path, final ServletContext context, final String user, final Map<String,List<String>> params, final HttpSession htsession)
    {
        setContext (context);
        setPath (path);
        setParams (params);
        setSession (htsession);
        
        this.user = user;
    }
    
    public InputStream get () throws IOException
    {
        Log.debug ("file GET: "+path);
        
        //return context.getResourceAsStream (path);

        final java.io.File file = new java.io.File (path).getCanonicalFile ();
        
        if (!file.exists ())
        {
            Log.error ("cannot get file `"+path+"': no such file");
            throw new IOException ("cannot get file `"+path+"': no such file");
        }
        
        return new FileInputStream (file);
    }
    
    public InputStream put (final InputStream data) throws IOException
    {
        Log.debug ("file PUT: "+path);
        return write (data, false);
    }
        
    public InputStream patch (final InputStream data, final String contentType) throws IOException
    {
        Log.debug ("file PATCH: "+path);
        // FIXME
        return write (data, true);
    }
    
    InputStream write (final InputStream data, final boolean append) throws IOException
    {
        final java.io.File file = new java.io.File (path).getCanonicalFile ();
        final java.io.File parent = file.getParentFile ();
        
        if (path.endsWith ("/")) // directory
        {
            // TODO: what to do with InputStream data argument?
            
            if (file.exists ())
            {
                if (file.isDirectory ())
                {
//                    Status.set (resp, resp.SC_OK);
                }
                else
                {
//                    Status.set (resp, resp.SC_CONFLICT, "cannot write directory `"+path+"': file exists");
                    Log.error ("cannot write directory `"+path+"': file exists");
                    throw new IOException ("cannot write directory `"+path+"': file exists");
                }
            }
            else if (parent.isDirectory ())
            {
                if (file.mkdir ())
                {
//                    Status.set (resp, resp.SC_CREATED);
                }
                else
                {
//                    Status.set (resp, resp.SC_INTERNAL_SERVER_ERROR, "cannot write directory `"+path+"': unknown failure");
                    Log.error ("cannot write directory `"+path+"': unknown failure");
                    throw new IOException ("cannot write directory `"+path+"': unknown failure");
                }
            }
            else
            {
//                Status.set (resp, resp.SC_NOT_FOUND, "cannot write directory `"+path+"': no such file or directory");
                Log.error ("cannot write directory `"+path+"': no such file or directory");
                throw new FileNotFoundException ("cannot write directory `"+path+"': no such file or directory");
            }
        }
        else // regular file
        {
            if (file.exists ())
            {
                if (!file.isDirectory ()) try
                {
                    final FileOutputStream out = new FileOutputStream (file, append); try
                    {
                        if (data!=null) for (int b; (b=data.read ())>=0; out.write (b));

//                        Status.set (resp, resp.SC_OK);
                    }
                    finally
                    {
                        out.close ();
                    }
                }
                catch (final FileNotFoundException e)
                {
//                    Status.set (resp, resp.SC_FORBIDDEN, "cannot write file `"+path+"': server failure: "+e.getMessage ());
                    Log.error ("cannot write file `"+path+"': server failure: "+e.getMessage ());
                    throw new IOException ("cannot write file `"+path+"': server failure: "+e.getMessage ());
                }
                else
                {
//                    Status.set (resp, resp.SC_CONFLICT, "cannot write file `"+path+"': is a directory");
                    Log.error ("cannot write file `"+path+"': is a directory");
                    throw new IOException ("cannot write file `"+path+"': is a directory");
                }
            }
            else if (parent.isDirectory ()) try
            {
                final FileOutputStream out = new FileOutputStream (file); try
                {
                    if (data!=null) for (int b; (b=data.read ())>=0; out.write (b));

//                    Status.set (resp, resp.SC_CREATED);
                }
                finally
                {
                    out.close ();
                }
            }
            catch (final FileNotFoundException e)
            {
//                Status.set (resp, resp.SC_FORBIDDEN, "cannot write file `"+path+"': server failure: "+e.getMessage ());
                Log.error ("cannot write file `"+path+"': server failure: "+e.getMessage ());
                throw new IOException ("cannot write file `"+path+"': server failure: "+e.getMessage ());
            }
            else
            {
//                Status.set (resp, resp.SC_NOT_FOUND, "cannot write file `"+path+"': no such file or directory");
                Log.error ("cannot write file `"+path+"': no such file or directory");
                throw new IOException ("cannot write file `"+path+"': no such file or directory");
            }
        }
        
        return null;
    }
    
    public InputStream delete () throws IOException
    {
        Log.debug ("file DELETE: "+path);
        
        final java.io.File file = new java.io.File (path).getCanonicalFile ();
        
        if (path.endsWith ("/")) // directory
        {
            if (file.exists ())
            {
                if (file.isDirectory ())
                {
                    if (file.delete ())
                    {
                        //Status.set (resp, resp.SC_OK);
                    }
                    else
                    {
                        //Status.set (resp, resp.SC_INTERNAL_SERVER_ERROR, "cannot delete directory `"+path+"': unknown failure");
                        Log.error ("cannot delete directory `"+path+"': unknown failure");
                        throw new IOException ("cannot delete directory `"+path+"': unknown failure");
                    }
                }
                else
                {
                    //Status.set (resp, resp.SC_CONFLICT, "cannot delete directory `"+path+"': not a directory");
                    Log.error ("cannot delete directory `"+path+"': not a directory");
                    throw new IOException ("cannot delete directory `"+path+"': not a directory");
                }
            }
            else
            {
                //Status.set (resp, resp.SC_NOT_FOUND, "cannot delete directory `"+path+"': no such file or directory");
                Log.error ("cannot delete directory `"+path+"': no such file or directory");
                throw new IOException ("cannot delete directory `"+path+"': no such file or directory");
            }
        }
        else // regular file
        {
            if (file.exists ())
            {
                if (!file.isDirectory ())
                {
                    if (file.delete ())
                    {
                        //Status.set (resp, resp.SC_OK);
                    }
                    else
                    {
                        //Status.set (resp, resp.SC_INTERNAL_SERVER_ERROR, "cannot delete file `"+path+"': unknown failure");
                        Log.error ("cannot delete file `"+path+"': unknown failure");
                        throw new IOException ("cannot delete file `"+path+"': unknown failure");
                    }
                }
                else
                {
                    //Status.set (resp, resp.SC_CONFLICT, "cannot delete file `"+path+"': is a directory");
                    Log.error ("cannot delete file `"+path+"': is a directory");
                    throw new IOException ("cannot delete file `"+path+"': is a directory");
                }
            }
            else
            {
                //Status.set (resp, resp.SC_NOT_FOUND, "cannot delete file `"+path+"': no such file or directory");
                Log.error ("cannot delete file `"+path+"': no such file or directory");
                throw new IOException ("cannot delete file `"+path+"': no such file or directory");
            }
        }
        
        return null;
    }
    
    public Return post (final InputStream data, final String contentType) throws IOException
    {
        Log.debug ("file POST: "+path);
        
        java.io.File file = new java.io.File (path).getCanonicalFile ();
        if (file.isDirectory ())
        {
            final java.io.File index = new java.io.File (file, "index");
            
            if (index.exists ())
            {
                file = index;
            }
            else
            {
                Log.error ("cannot exec directory `"+file+"': no index file");
                throw new IOException ("cannot exec directory `"+file+"': no index file");
            }
        }
        Log.debug ("file: "+file);
        
        if (! file.exists ())
        {
            Log.error ("cannot exec file `"+file+"': not found");
            throw new IOException ("cannot exec file `"+file+"': not found");
        }
        
        if (! file.canExecute ())
        {
            Log.error ("cannot exec file `"+file+"': check execute bit");
            throw new IOException ("cannot exec file `"+file+"': check execute bit");
        }
        
        final InputStream in = new FileInputStream (file); if (in!=null) try
        {
            final String session;
            {
                final List<String> sessions = params.get (Shell.EXT_PARAM_SESSION);
                if (sessions!=null && sessions.size ()>0) session = sessions.get (0);
                else session = null;
            }
            Log.debug ("session: "+session);

            final String interpreter;
            {
                if (in.read ()==0x23 && in.read ()==0x21) // shebang magic number: #!
                {
                    final BufferedReader inr = new BufferedReader (new InputStreamReader (in));
                    interpreter = inr.readLine ();
                }
                else // use default exec
                {
                    interpreter = "local:/bin/exec";
                }
            }

            // FIXME: this logic is duplicated (small differences) in Resource.java:
            // We insert the requested script as the first anonymous argument.
            // All other anonymous arguments get shifted down and all other
            // parameters get passed through to the interpreter.
            List<String> anon = params.get (null);
            if (anon==null)
            {
                anon = new ArrayList<String> ();
                params.put (null, anon);
            }
            // FIXME: hardcoded reference to file: method
            anon.add (0, "file:"+path);

            final Session shell = Sessions.load (context, htsession, session);
            Log.debug ("shell session: "+shell);

            try
            {
                Log.debug ("about to exec "+interpreter+" with params: "+params);

                // FIXME: presently only local interpreters are supported
                return Shell.exec (interpreter, params, new TaskData (shell, context, user, htsession, data, contentType, null, null));
            }
            catch (final ServletException e)
            {
                throw new IOException (e);
            }
        }
        finally
        {
            in.close ();
        }
        else // 
        {
            /*
            //Status.set (resp, resp.SC_NOT_FOUND, "cannot exec file `"+file+"': not found");
            Log.error ("cannot exec file `"+path+"': not found");
            throw new IOException ("cannot exec file `"+path+"': not found");
            */
            return null;
        }
    }
    
    public Boolean executable () throws IOException
    {
        final java.io.File file = new java.io.File (path).getCanonicalFile ();
        return file.canExecute ();
    }
    
    public Boolean directory () throws IOException
    {
        final java.io.File file = new java.io.File (path).getCanonicalFile ();
        return file.isDirectory ();
    }
    
    public boolean exists () throws IOException
    {
        return exists (path);
    }
    
    private static boolean exists (final String resource) throws IOException
    {
        final java.io.File file = new java.io.File (resource).getCanonicalFile ();
        return file.exists ();
    }
    
    /*
    public List<Legend> list (final User user) throws IOException
    {
        final String[] files = new java.io.File (path).getCanonicalFile ().list ();
        if (files==null) return null;
        
        final List<Legend> entries = new ArrayList<Legend> ();
        for (final String path : files)
        {
            final Legend entry = new Legend ();
            entry.path = path;
            
            final java.io.File file = new java.io.File (this.path, path);
            entry.lastModified = file.lastModified ();
            
            entries.add (entry);
        }
        
        return entries;
    }
    */
    
    public Set<String> list (final boolean all, final boolean recurse) throws IOException
    {
        final java.io.File root = new java.io.File (path).getCanonicalFile ();
        Set<String> entries = new TreeSet<String> ();
        
        if (root.isDirectory ())
        {
            final String[] files = root.list ();
            if (files==null) return null;

            //entries.addAll (Arrays.asList (files));
            for (final String name : files)
            {
                if ( ! all && name.startsWith ("."))
                {
                    continue;
                }
                
                final java.io.File file = new java.io.File (root, name);
                
                if (file.isDirectory ())
                {
                    entries.add (name + "/");

                    if (recurse)
                    {
                        for (String rentry : new File (file.getPath (), context, htsession).list (all, true))
                        {
                            entries.add (name + "/" + rentry);
                        }
                    }
                }
                else
                {
                    entries.add (name);
                }
            }
        }
        else if (root.exists ())
        {
            entries.add (root.getName ());
        }
        else
        {
            return null;
        }

        return entries;
    }
}
