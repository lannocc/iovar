/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.proc;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;
import net.iovar.web.dev.trans.*;
import net.iovar.web.lib.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Encapsulation of a shell session (instance variables, search path, history, etc.).
 * 
 * FIXME: this class (especially Session) needs some serious re-work!
 *
 * @author  shawn@lannocc.com
 */
public class Sessions extends HttpServlet
{
    static final String MASTER_SESSION = "_iodefault_";
    
    public static final String SESSIONS_PARAM = "iovar.sessions";
    public static final String SESSIONS_DEFAULT = "/proc/shell";
    
    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        
        if ("/".equals (req.getPathInfo ()))
        {
            stats (context, resp);
        }
        else
        {
            final String id = getId (req);
            final Session shell = get (context, id);

            if (shell!=null)
            {
                Log.debug ("instance found: "+shell);
                resp.setContentType ("text/xml");

                Utils.xstream.toXML (shell, resp.getOutputStream ());
            }
            else
            {
                Log.warn ("instance NOT found");
                Status.set (resp, resp.SC_NOT_FOUND, "instance `"+id+"' not found");
            }
        }
    }
    
    protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        final String id = getId (req);
        final Session shell = (Session) Utils.xstream.fromXML (req.getInputStream ());
        
        // FIXME: what about path/id mismatch?
        /*
        String root = context.getInitParameter (SESSIONS_PARAM);
        if (root==null) root = SESSIONS_DEFAULT;
        shell.path = root+"/"+id;
        */
        
        put (context, shell);
        
        Log.debug ("instance "+id+" saved");
    }
    
    protected void doDelete (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        final String id = getId (req);
        
        final Session shell = get (context, id);
        
        if (shell!=null)
        {
            shell.delete (context, req.getSession ());
            Log.debug ("instance "+id+" deleted");
        }
        else
        {
            Log.debug ("cannot delete; shell instance does not exist: "+id);
        }
    }
    
    String getId (final HttpServletRequest req)
    {
        String id = req.getPathInfo (); if (id==null)
        {
            throw new IllegalArgumentException ("expecting an id path");
        }
        if (id.startsWith ("/")) id = id.substring (1);
        
        id = id.trim (); if (id.length ()<1)
        {
            throw new IllegalArgumentException ("expecting an id path");
        }
        
        return id;
    }
    
    void stats (final ServletContext context, final HttpServletResponse resp) throws IOException
    {
        final PrintWriter out = resp.getWriter ();
        
        final Map<String,Session> sessions = (Map<String,Session>) context.getAttribute (Session.class.getName ());
        
        if (sessions==null)
        {
            out.println ("null");
        }
        else
        {
            out.println (sessions.size ());
        }
    }
    
    
    /**
     * Store a local instance
     */
    public static void put (final ServletContext context, final Session shell) throws IOException
    {
        Map<String,Session> shells = (Map<String,Session>) context.getAttribute (Session.class.getName ());
        
        if (shells==null)
        {
            if (shell.isMaster ())
            {
                shells = new HashMap<String,Session> ();
                context.setAttribute (Session.class.getName (), shells);
            }
            else
            {
                Log.error ("trying to store non-master local session yet no shell sessions exist locally");
                return;
            }
        }
        
        shells.put (shell.getId (), shell);
        Log.info ("stored local session: "+shell);
    }
    
    /**
     * Retrieve a local instance
     */
    static Session get (final ServletContext context, final String id) throws IOException
    {
        Log.debug ("request for shell: "+id);
        final Map<String,Session> shells = (Map<String,Session>) context.getAttribute (Session.class.getName ());
        if (shells==null)
        {
            Log.warn ("requested a local session yet no shell sessions exist locally");
            return null;
        }
        
        return shells.get (id);
    }
    
    
    
    /**
     * Return the "master" session.
     * The master session is created if it does not already exist.
     */
    public static Session master (final ServletContext context) throws IOException
    {
        Log.debug ("getting shell master session");
        
        Session shell = (Session) context.getAttribute (MASTER_SESSION);
        
        if (shell==null)
        {
            Log.debug ("create master session");
            shell = new Session (context);
            Log.info ("master session created: "+shell);
            put (context, shell);
            
            context.setAttribute (MASTER_SESSION, shell);
        }
        else
        {
            Log.info ("re-using master session: "+shell);
        }
        
        return shell;
    }
    
    /**
     * Retrieve the local user instance id from existing http session.
     * Does not create a new shell session.
     */
    public static String getIdFromSession (final HttpSession htsession)
    {
        // NOTE: this should match get(HttpServletRequest) below
        final String user = /* FIXME SECURITY: req.getRemoteUser () */ "XXX";
        final String key = Session.class.getName ()+":"+user;
        final String id = (String) htsession.getAttribute (key);
        return id;
    }
    
    /**
     * Retrieve the local user instance.
     * If the request contains the "iosession" parameter, that shell is retrieved.
     * If no session parameter was specified, the user session is retrieved via cookies.
     * An instance is created if it does not already exist.
     */
    public static Session get (final HttpServletRequest req) throws IOException
    {
        final HttpSession session = req.getSession ();
        final ServletContext context = session.getServletContext ();
        final String user = /* FIXME SECURITY: req.getRemoteUser () */ "XXX";
        
        // FIXME #17: Cannot use getParameter() here because this function may have been invoked
        //      by the idfree Authentication servlet filter, and would then interfere with any
        //      form2xml usage behind authenticated URLs.
        
        //final String path = req.getParameter (Shell.EXT_PARAM_SESSION);
        final Map<String,List<String>> params = Utils.getParams (req.getQueryString ());
        final String path;
        {
            final List<String> paths = params.get (Shell.EXT_PARAM_SESSION);
            if (paths != null) path = paths.get (0);
            else path = null;
        }
        
        if (path!=null)
        {
            Log.debug ("request for shell from request, path supplied: "+path);
            return load (context, req.getSession (), path);
        }
        else
        {
            Log.debug ("request for shell from request, no path supplied");
            final String key = Session.class.getName ()+":"+user;
            Session shell = null; try
            {
                final String id = (String) session.getAttribute (key);
                if (id!=null) shell = get (context, id);
            }
            catch (Exception e)
            {
                // Due to legacy/upgrade (Session used to be stored in HttpSession... BAD IDEA)
                // we could get a ClassCastException because user sessions may
                // be persisted across restarts. Just remove the bogus entry.
                Log.warn ("LEGACY: bad data getting user session", e);
                session.removeAttribute (key);
            }
            Log.debug ("shell from user session: "+shell);
            
            // Note that user sessions can survive a servlet container restart.
            // Since any such shell would no longer be valid we check for its
            // existence here.
            final Map<String,Session> shells = (Map<String,Session>) context.getAttribute (Session.class.getName ());
            if (shell!=null && (shells==null || ! shells.containsKey (shell.getId ())))
            {
                Log.debug ("removing stale user session shell: "+shell);
                shell = null;
            }

            if (shell==null)
            {
                shell = new Session (context, master (context));
                put (context, shell);
                session.setAttribute (key, shell.getId ());
            }

            return shell;
        }
    }
    
    
    /**
     * Load a local or remote shell instance.
     */
    public static Session load (final ServletContext context, final HttpSession htsession, final String path) throws IOException
    {
        Log.debug ("request to load shell at: "+path);
        final Transport handler = Transport.handler (path, context, htsession);
        
        if (handler instanceof Local || handler instanceof Resource)
        {
            String root = context.getInitParameter (SESSIONS_PARAM);
            if (root==null) root = SESSIONS_DEFAULT;
            
            if (handler.getPath ().startsWith (root+"/"))
            {
                String id = handler.getPath ().substring (root.length () + 1);
                while (id.startsWith ("/"))
                {
                    id = id.substring (1);
                }
                
                return get (context, id);
            }
            else
            {
                Log.error ("not a recognized local path for shell sessions: "+handler.getPath ());
                throw new IllegalArgumentException ("not a recognized local path for shell sessions: "+handler.getPath ());
            }
        }
        else
        {
            return (Session) Utils.xstream.fromXML (handler.get ());
        }
    }
    
    /**
     * Delete a local or remote shell instance.
     */
    public static void delete (final ServletContext context, final HttpSession htsession, final String path) throws IOException
    {
        Log.debug ("request to delete shell at: "+path);
        final Transport handler = Transport.handler (path, context, htsession);
        
        if (handler instanceof Local || handler instanceof Resource)
        {
            String root = context.getInitParameter (SESSIONS_PARAM);
            if (root==null) root = SESSIONS_DEFAULT;
            
            if (handler.getPath ().startsWith (root+"/"))
            {
                String id = handler.getPath ().substring (root.length () + 1);
                while (id.startsWith ("/"))
                {
                    id = id.substring (1);
                }
                
                final Map<String,Session> shells = (Map<String,Session>) context.getAttribute (Session.class.getName ());
                final Session shell = shells.remove (id);
                
                if (shell!=null)
                {
                    Log.debug ("shell instance removed: "+shell);
                    
                    final Session parent = shell.getParent (context, htsession);
                    if (parent!=null)
                    {
                        parent.removeChild (shell.getPath ());
                    }
                }
                else
                {
                    Log.warn ("cannot delete; shell instance does not exist: "+id);
                }
            }
            else
            {
                Log.error ("not a recognized local path for shell sessions: "+handler.getPath ());
                throw new IllegalArgumentException ("not a recognized local path for shell sessions: "+handler.getPath ());
            }
        }
        else
        {
            handler.delete ();
        }
    }







    
    public static class Listener implements HttpSessionListener
    {
        public void sessionCreated (final HttpSessionEvent event)
        {
            
        }
        
        public void sessionDestroyed (final HttpSessionEvent event)
        {
            final HttpSession session = event.getSession ();
            final ServletContext context = session.getServletContext ();
            //final Map<String,Session> shells = (Map<String,Session>) context.getAttribute (Session.class.getName ());
            
            for (final Enumeration<String> keys = session.getAttributeNames (); keys.hasMoreElements (); )
            {
                final String key = keys.nextElement ();
                if (key.startsWith (Session.class.getName ()+":"))
                {
                    final String id = (String) session.getAttribute (key);
                    try
                    {
                        final Session shell = get (context, id);
                        shell.delete (context, null /* FIXME check this */);
                    }
                    catch (final IOException e)
                    {
                        Log.error ("exception while deleting: "+id, e);
                    }
                    
                    session.removeAttribute (key);
                    
                    // this already happens in shell.delete ()
                    //shells.remove (shell.getId ());
                }
            }
        }
    }
}
