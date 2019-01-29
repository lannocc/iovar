/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev.trans;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.bin.shell.task.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.io.File;
import java.util.*;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:
//import me.idfree.lib.*;

/**
 * Web application resources (files, JAR-packaged objects, etc.), relative to project root.
 * Local files have precedence over JAR-packaged files or other resources.
 * Additionally, JAR-packaged files cannot be written to or deleted.
 *
 * @author  shawn@lannocc.com
 */
public class Resource extends Transport
{
    final String user;
    
    /**
     * Default constructor is necessary and used for servlet installation only.
     */
    public Resource ()
    {
        this.user = null;
    }
    
    public Resource (final String path, final ServletContext context, final HttpSession htsession)
    {
        this (path, context, null, new HashMap<String,List<String>> (), htsession);
    }
    
    public Resource (final String path, final ServletContext context, final String user, final Map<String,List<String>> params, final HttpSession htsession)
    {
//        super (path, params);
        setContext (context);
        setPath (path);
        setParams (params);
        setSession (htsession);
        
        this.user = user;
    }
    
    public InputStream get () throws IOException
    {
        Log.debug ("resource GET: "+path);
        InputStream in = context.getResourceAsStream (path);
        if (in==null) in = getClass ().getResourceAsStream (path);
        
        if (in==null)
        {
            final String syslink = "/sys/link/" + path;
            in = context.getResourceAsStream (syslink);
            if (in==null)
            {
                in = Resource.class.getResourceAsStream (syslink);
            }
            
            if (in!=null)
            {
                final BufferedReader r = new BufferedReader (new InputStreamReader (in));
                try
                {
                    final String target = r.readLine();
                    Log.debug ("resource is LINK to: " + target);
                    
                    final File root = new File (context.getRealPath ("/")).getCanonicalFile ();
                    final File parent = new File (context.getRealPath (path)).getCanonicalFile ().getParentFile ();
                    final File file = new File (parent, target).getCanonicalFile ();
                    final String local = Utils.getLocalPath (root, file);
                    Log.debug ("   root: " + root + ", parent: " + parent + ", file: " + file + ", local: " + local);
                    
                    if (local != null)
                    {
                        return new Resource (local, context, htsession).get ();
                    }
                    else
                    {
                        return new net.iovar.web.dev.trans.File (file.getPath (), context, htsession).get ();
                    }
                }
                finally
                {
                    r.close ();
                }
            }
        }
        
        return in;
    }
    
    public InputStream put (final InputStream data) throws IOException
    {
        Log.debug ("resource PUT: "+path);
        return write (data, false);
    }
        
    public InputStream patch (final InputStream data, final String contentType) throws IOException
    {
        Log.debug ("resource PATCH: "+path);
        // FIXME
        return write (data, true);
    }
    
    InputStream write (final InputStream data, final boolean append) throws IOException
    {
        final File root = new File (context.getRealPath ("/"));
        final File file = new File (root, File.separator+path).getCanonicalFile ();
        final File parent = file.getParentFile ();
        
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
                    // FIXME: consider when local file doesn't exist but JAR-packaged
                    // file does: need to read that in first when append==true
                    
                    final FileOutputStream out = new FileOutputStream (file, append); try
                    {
                        if (data!=null) Utils.pipe (data, out);

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
                    if (data!=null) Utils.pipe (data, out);

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
        Log.debug ("resource DELETE: "+path);
        
        final File root = new File (context.getRealPath ("/"));
        final File file = new File (root, File.separator+path).getCanonicalFile ();
        
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
        Log.debug ("resource POST: "+path);
        
        InputStream in = get ();

        if (in!=null) try
        {
            int c0 = in.read ();
            
            if (c0 < 0) // no data
            {
                // FIXME: assuming emptiness to be a directory to enable /index auto-execing... check on this later
                Log.debug ("no data for this Resource request... looking for a ./index instead...");
                
                if (exists (context, path + "/index"))
                {
                    // FIXME: do client side redirect here instead?
                    Log.debug ("found a matching ./index Resource to use");
                    in.close ();
                    in = context.getResourceAsStream (path + "/index");
                    if (in==null) in = getClass ().getResourceAsStream (path + "/index");
                    if (in!=null) c0 = in.read ();
                }
            }
            
            final String session;
            {
                final List<String> sessions = params.get (Shell.EXT_PARAM_SESSION);
                if (sessions!=null && sessions.size ()>0) session = sessions.get (0);
                else session = null;
            }
            Log.debug ("session path: "+session);
            
            final String interpreter;
            {
                if (c0==0x23 && in.read ()==0x21) // shebang magic number: #!
                {
                    final BufferedReader inr = new BufferedReader (new InputStreamReader (in));
                    interpreter = inr.readLine ();
                }
                else // use default exec
                {
                    interpreter = "local:/bin/exec";
                }
            }

            // FIXME: this logic is duplicated (small differences) in File.java:
            // We insert the requested script as the first anonymous argument.
            // All other anonymous arguments get shifted down and all other
            // parameters get passed through to the interpreter.
            List<String> anon = params.get (null);
            if (anon==null)
            {
                anon = new ArrayList<String> ();
                params.put (null, anon);
            }
            // FIXME: hardcoded reference to res: method
            anon.add (0, "res:"+path);

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
        else
        {
            //Status.set (resp, resp.SC_NOT_FOUND, "cannot exec webapp resource `"+resource+"': not found");
            Log.error ("cannot exec webapp resource `"+path+"': not found");
            throw new IOException ("cannot exec webapp resource `"+path+"': not found");
        }
    }
    
    public boolean exists () throws IOException
    {
        return exists (context, path);
    }
    
    private static boolean exists (final ServletContext context, final String resource) throws IOException
    {
        InputStream in = null; try
        {
            //Log.debug ("checking for existence through servlet context: "+resource);
            in = context.getResourceAsStream (resource);
            if (in==null)
            {
                //Log.debug ("checking for existence through class loader: "+resource);
                in = Resource.class.getResourceAsStream (resource);
            }
        }
        finally
        {
            if (in != null) in.close ();
        }
        
        if (in != null)
        {
            return true;
        }
        
        final String syslink = "/sys/link/" + resource;
        in = context.getResourceAsStream (syslink);
        if (in==null)
        {
            in = Resource.class.getResourceAsStream (syslink);
        }
        
        return (in != null);
    }
    
    public Boolean directory () throws IOException
    {
        final InputStream in = get ();
        if (in==null) return Boolean.FALSE;
        
        try
        {
            if (in.read () < 0)
            {
                // resource exists but has no data... could be a directory
                return null;
            }
            else
            {
                return Boolean.FALSE;
            }
        }
        finally
        {
            in.close ();
        }
    }
    
    public Boolean executable () throws IOException
    {
        InputStream in = get ();
        if (in==null) return false;
        
        final String sysexec = "/sys/exec/" + path;
        in = context.getResourceAsStream (sysexec);
        if (in==null)
        {
            in = Resource.class.getResourceAsStream (sysexec);
        }
        
        // FIXME: look for hash-bang here?
        return in!=null;
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
        String sysindex = "/sys/index/.index";
        InputStream in = context.getResourceAsStream (sysindex);
        if (in==null)
        {
            in = Resource.class.getResourceAsStream (sysindex);
        }
        if (in==null)
        {
            return null;
        }
        
        Set<String> entries = new TreeSet<String> ();
        
        BufferedReader rindex = new BufferedReader (new InputStreamReader (in)); try
        {
            for (String project; (project = rindex.readLine ()) != null; )
            {
                sysindex = "/sys/index/" + project;
                Log.debug ("looking for " + path + " in " + sysindex);
                
                in = context.getResourceAsStream (sysindex);
                if (in==null)
                {
                    in = Resource.class.getResourceAsStream (sysindex);
                }
                if (in==null)
                {
                    continue;
                }
                
                BufferedReader rproject = new BufferedReader (new InputStreamReader (in)); try
                {
                    for (String entry; (entry = rproject.readLine ()) != null; )
                    {
                        if (("/"+entry).startsWith (path))
                        {
                            entry = entry.substring (path.length () - 1);
                            
                            if (entry.isEmpty ()) // file
                            {
                                entries.add (new File (path).getName ());
                            }
                            else // directory
                            {
                                if (entry.startsWith ("/")) entry = entry.substring (1);
                                
                                if ( ! all && entry.startsWith ("."))
                                {
                                    continue;
                                }
                                
                                int slash = entry.indexOf ("/");
                                if (slash > 0)
                                {
                                    if (recurse)
                                    {
                                        // FIXME: this should add entries for just the directories
                                        //  as well; also this doesn't yet recursively handle hidden
                                        //  files
                                        entries.add (entry);
                                    }
                                    
                                    entry = entry.substring (0, slash) + "/";
                                }
                                
                                entries.add (entry);
                            }
                        }
                    }
                }
                finally
                {
                    rproject.close ();
                }
            }
        }
        finally
        {
            rindex.close ();
        }
        
        if (entries.isEmpty ())
        {
            return null;
        }
        else
        {
            return entries;
        }
    }
}
