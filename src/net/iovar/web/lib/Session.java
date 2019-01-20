/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.lib;

// local imports:
import net.iovar.web.*;
import net.iovar.web.dev.*;
import net.iovar.web.dev.trans.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.util.*;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class Session implements Serializable
{
    public static final String ENV_INSTANCE = "INSTANCE"; // instance ID for the session
    public static final String ENV_VERSION = "IOVAR_VERSION"; // version of the IOVAR shell
    public static final String ENV_PATH = "PATH"; // user search path for executables
    public static final String ENV_USER = "USER"; // current username

    final String id;
    final String path;

    final String parentPath;

    final HashMap<String,String> env;
    final HashMap<String,String> locals; // locals do not propagate to children
    Object user;
    Integer exit;

    final Set<String> children;

    public Session (final ServletContext context)
    {
        this.id = String.valueOf (super.toString ().hashCode ());
        String sessions = context.getInitParameter (Sessions.SESSIONS_PARAM);
        if (sessions==null) sessions = Sessions.SESSIONS_DEFAULT;
        this.path = sessions+"/"+id;

        this.parentPath = null;

        this.env = new HashMap<String,String> ();

        env.put (ENV_INSTANCE, id);
        env.put (ENV_VERSION, net.iovar.web.bin.shell.Shell.VERSION);
        //env.put (ENV_ROOT, root);
        //env.put (ENV_WORKING_URI_BASE, pwu);
        //env.put (ENV_WORKING_DIRECTORY, pwd);
        //env.put (ENV_ROOT, "http://localhost:8080/iovar/");
        //env.put (ENV_HOME, "http://localhost:8080/iovar/");
        //env.put (ENV_PATH, "/usr/local/bin:/usr/bin:/bin:/opt/bin");

        this.locals = new HashMap<String,String> ();
        this.exit = null;

        this.children = new HashSet<String> ();
    }

    public Session (final ServletContext context, final Session parent)
    {
        if (parent==null)
        {
            Log.error ("null parent");
            throw new NullPointerException ("null parent");
        }

        // FIXME: use a better id value
        this.id = String.valueOf (super.toString ().hashCode ());
        String root = context.getInitParameter (Sessions.SESSIONS_PARAM);
        if (root==null) root = Sessions.SESSIONS_DEFAULT;
        this.path = root+"/"+id;

        this.parentPath = parent.path;

        this.env = (HashMap<String,String>) parent.env.clone ();
        this.env.put (ENV_INSTANCE, id);

        this.locals = new HashMap<String,String> ();
        this.exit = parent.exit;

        this.children = new HashSet<String> ();

        // add to parent's children if parent is not master session
        parent.children.add (this.path);

        Log.debug ("new session ["+id+"] with parent: "+parent.id);
    }

    public String getId ()
    {
        return id;
    }

    public String getPath ()
    {
        return path;
    }

    public boolean isMaster ()
    {
        return parentPath==null;
    }

    public Session getParent (final ServletContext context, final HttpSession htsession) throws IOException
    {
        if (this.parentPath==null) return null;

        return Sessions.load (context, htsession, parentPath);
    }
    
    public void removeChild (final String childPath)
    {
        children.remove (childPath);
    }

    public String get (final String key)
    {
        Log.debug ("get ["+id+"]: "+key);
        if ("?".equals (key)) {
            Log.debug ("returning exit value: "+exit);
            return String.valueOf (exit);
        }
        else if (locals.containsKey (key)) return locals.get (key);
        else return env.get (key);
    }

    public String set (final String key, final String val)
    {
        Log.debug ("set ["+id+"]: "+key+"="+val);
        return env.put (key, val);
    }

    public String setLocal (final String key, final String val)
    {
        Log.debug ("set local ["+id+"]: "+key+"="+val);
        return locals.put (key, val);
    }

    public void setExit (final int exit)
    {
        Log.debug ("set exit ["+id+"]: "+exit);
        this.exit = new Integer (exit);
    }

    public Integer getExit ()
    {
        return exit;
    }

    public void clearExit ()
    {
        Log.debug ("clear exit ["+id+"]");
        this.exit = null;
    }

    /**
     * Non-local variables only.
     */
    public Map<String,String> getVariables ()
    {
        final Map<String,String> env =  (Map<String,String>) this.env.clone ();
        if (exit!=null) env.put ("?", exit.toString ());
        else env.remove ("?");
        return env;
    }

    /**
     * Local variables only.
     */
    public Map<String,String> getLocals ()
    {
        return (Map<String,String>) locals.clone ();
    }

    /**
     * All variables.
     */
    public Map<String,String> getEnvironment ()
    {
        final Map<String,String> env = getVariables ();

        for (final Map.Entry<String,String> local : locals.entrySet ())
        {
            env.put (local.getKey (), local.getValue ());
        }

        return env;
    }

    public String export (final ServletContext context, final HttpSession htsession, final String key, final String val) throws IOException
    {
        return export (context, htsession, key, val, false);
    }

    public String export (final ServletContext context, final HttpSession htsession, final String key, final String val, final boolean master) throws IOException
    {
        if (master)
        {
            return export (-2, context, htsession, key, val);
        }
        else
        {
            // FIXME: this probably shouldn't go all the way up
            return export (-1, context, htsession, key, val);
        }
    }

    /**
     * @param   count   How far up to go. Use -1 to go all the way to the user shell.
     *                  Use -2 to go up to the master session.
     */
    public String export (final int count, final ServletContext context, final HttpSession htsession, final String key, final String val) throws IOException
    {
        final Session parent = getParent (context, htsession);

        if (count>0)
        {
            if (parent!=null) parent.export (count-1, context, htsession, key, val);
        }
        else if (count<0)
        {
            if (parent!=null) parent.export (count, context, htsession, key, val);
        }

        if (count==-2 || ! isMaster ())
        {
            return set (key, val);
        }
        else
        {
            return null;
        }
    }

    /*
     * Sets the given user object in the top user shell (one down from master session).
     */
    public void setUser (final ServletContext context, final HttpSession htsession, final Object user) throws IOException
    {
        final Session parent = getParent (context, htsession);

        if (parent != null)
        {
            if (parent.isMaster () && !isMaster ())
            {
                this.user = user;
            }
            else
            {
                parent.setUser (context, htsession, user);
            }
        }
    }

    public Object getUser (final ServletContext context, final HttpSession htsession) throws IOException
    {
        final Session parent = getParent (context, htsession);

        if (parent != null)
        {
            if (parent.isMaster () && !isMaster ())
            {
                return this.user;
            }
            else
            {
                return parent.getUser (context, htsession);
            }
        }

        return null;
    }

    public List<String> getPathList ()
    {
        final String path = get (ENV_PATH);
        if (path==null) return null;

        final List<String> list = new ArrayList<String> ();

        // using comma as separator because colon is so prevalent in URIs
        for (final StringTokenizer st = new StringTokenizer (path, ","); st.hasMoreTokens (); )
        {
            list.add (st.nextToken ());
        }

        return list;
    }

    /*
    public void saveTo (final String path, final ServletContext context) throws IOException
    {
        Log.info ("put "+this+" at "+path+":");

        final ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
        Utils.xstream.toXML (this, bytes);
        / *
        final ObjectOutputStream out = new ObjectOutputStream (bytes); try
        {
            out.writeObject (this);
        }
        finally
        {
            out.close ();
        }
        * /

        Transport.handler (path, context).put (new ByteArrayInputStream (bytes.toByteArray ()));
    }
    */

    public InputStream save (final ServletContext context, final HttpSession htsession) throws IOException
    {
        Transport handler = Transport.handler (path, context, htsession);

        if (handler instanceof Local || handler instanceof Resource)
        {
            // FIXME... evaluate completeness
            Sessions.put (context, this);
            /*
            Map<String,Instance> sessions = (Map<String,Instance>) context.getAttribute (Instance.class.getName ());
            if (sessions==null)
            {
                sessions = new HashMap<String,Instance> ();
                context.setAttribute (Instance.class.getName (), sessions);
            }

            sessions.put (handler.getPath (), this);
            */
            return null;
        }
        else
        {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
            Utils.xstream.toXML (this, bytes);
            return handler.put (new ByteArrayInputStream (bytes.toByteArray ()));
        }
    }

    /**
     * FIXME: a quick hack; not sure this should even exist (SECURITY)
     */
    public void saveUp (final ServletContext context, final HttpSession htsession) throws IOException
    {
        saveUp (-1, context, htsession);
    }


    /**
     * FIXME: a quick hack; not sure this should even exist (SECURITY)
     * @param   count   How far up to go. Use -1 to go all the way.
     */
    public void saveUp (final int count, final ServletContext context, final HttpSession htsession) throws IOException
    {
        save (context, htsession);

        if (count!=0)
        {
            final Session parent = getParent (context, htsession);
            if (parent!=null) parent.saveUp (count-1, context, htsession);
        }
    }

    /**
     * Create and store a child session containing the environment of this session.
     */
    public Session fork (final ServletContext context) throws IOException
    {
        final Session shell = new Session (context, this);
        Sessions.put (context, shell);
        return shell;
    }

    /**
     * FIXME (not implemented)
     */
    public Session merge (Session other)
    {
        // FIXME
        throw new RuntimeException ("merging not yet implemented");
    }

    /**
     * Delete this shell and its children.
     */
    public void delete (final ServletContext context, final HttpSession htsession) throws IOException
    {
        for (final String child : children)
        {
            Sessions.delete (context, htsession, child);
        }

        Sessions.delete (context, htsession, path);
    }

    public String toString ()
    {
        return super.toString () + "{ "+id+" }";
    }
}
