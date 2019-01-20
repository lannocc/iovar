/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell;

// local imports:
import net.iovar.parse.*;
import net.iovar.web.Utils;
import net.iovar.web.bin.shell.task.*;
//import net.iovar.web.bin.shell.task.legacy.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Web shell.
 *
 * @author  shawn@lannocc.com
 */
public class Shell extends HttpServlet
{
    public static final String VERSION = "1.1";
    
    //static final String INIT_PARAM_PARSER = "parser";
    
    public static final String EXT_PARAM_SESSION = "iosession";
    /** FIXME: SECURITY */
    public static final String INT_PARAM_USER = "_iouser_";

    static final String PARAM_VERSION = "version";
    //static final String PARAM_SESSION = "session";
    static final String PARAM_SESSION = EXT_PARAM_SESSION; // FIXME?
    //static final String PARAM_SAVE = "save";
    static final String PARAM_CMD = "cmd";
    static final String PARAM_GRAPH = "graph";
    
    protected void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doHead (this, req, resp);
    }
    
    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doGet (this, req, resp);
    }
    
    protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doPut (this, req, resp);
    }
   
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        // FIXME: add a usage () method, support ?help, etc...
        
/*
Log.fatal ("shell posting "+req.getPathInfo ());
BufferedReader something = new BufferedReader (new InputStreamReader (req.getInputStream ()));
for (String line; (line = something.readLine ())!=null; )
{
    Log.fatal ("shell FOO: "+line);
}
*/
        final String query = req.getQueryString ();
        
        if (query==null)
        {
            StringBuilder info = new StringBuilder ();
            info.append ("supported parameters:");
            info.append (" ").append (PARAM_VERSION);
            info.append (" ").append (PARAM_SESSION);
//            info.append (" ").append (PARAM_SAVE);
            info.append (" ").append (PARAM_CMD);
            information (info.toString (), resp);
        }
        else
        {
            final Map<String,List<String>> params = Utils.getParams (query);
            exec (/*getServletConfig (), */getServletContext (), req.getRemoteUser (), params, req, resp);
        }
    }
    
    static void exec (/*final ServletConfig config, */final ServletContext context, String user, final Map<String,List<String>> params, final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        // We remove our known parameters from the parameter map as we go so
        // they aren't passed in to a script (if supplied).
        
        // FIXME: SECURITY
        if (user==null && params.containsKey (INT_PARAM_USER))
        {
            user = params.remove (INT_PARAM_USER).get (0);
        }
        
        /*
        for (final Object entry : params.entrySet ())
        {
            Log.debug ("key: "+((Map.Entry) entry).getKey ());
            Log.debug ("val: "+((Map.Entry) entry).getValue ());
        }
         */

        if (params.containsKey (PARAM_VERSION))
        {
            params.remove (PARAM_VERSION);
            information (null, resp);
        }

        //final PrintWriter out = resp.getWriter ();
        final Session session;

        if (params.containsKey (PARAM_SESSION))
        {
            final List<String> vals = params.remove (PARAM_SESSION);

            if (vals==null) // create new session
            {
                // FIXME (not implemented)
                session = null;
                /*
                Log.info ("create session");
                session = Sessions.newInstance (context);
                */
            }
            else // load session(s) (merge multiple)
            {
                Session s = null;

                for (final String path : vals)
                {
                    Log.info ("load session: "+path);

                    final InputStream in = Transport.handler (path, context, req.getSession ()).get (); try
                    {
                        if (s==null) s = (Session) Utils.xstream.fromXML (in);
                        else s = s.merge ((Session) Utils.xstream.fromXML (in));
                        
                        /*
                        ObjectInputStream sin = new ObjectInputStream (in); try
                        {
                            if (s==null) s = (Session) sin.readObject ();
                            else s = s.merge ((Session) sin.readObject ());
                        }
                        catch (final ClassNotFoundException e)
                        {
                            Status.set (resp, resp.SC_INTERNAL_SERVER_ERROR, "cannot deserialize `"+path+"': "+e);
                            return;
                        }
                        finally
                        {
                            sin.close ();
                        }
                    }
                    catch (final EOFException e)
                    {
                        Status.set (resp, resp.SC_NOT_ACCEPTABLE, "cannot deserialize `"+path+"': "+e);
                        return;
                        */
                    }
                    finally
                    {
                        in.close ();
                    }
                }

                session = s;
            }

            Log.info ("session: "+session);
        }
        /*
        else if (params.containsKey (EXT_PARAM_SESSION))
        {
            session = Sessions.load (context);
        }
        */
        else // no session
        {
            session = null;
        }

        /*
        if (params.containsKey (PARAM_SAVE))
        {
            if (session!=null)
            {
                List<String> locations = params.get (PARAM_SAVE);

                if (locations==null) // use default location
                {
                    locations = new ArrayList<String> (1);
                    final String sessions = context.getInitParameter (SESSIONS_PARAM);
                    locations.add (sessions+session.getId ());
                }

                for (final String path : locations)
                {
                    session.saveTo (path, context);
                }
            }
            else
            {
                Status.set (resp, resp.SC_BAD_REQUEST, "nothing to save");
                return;
            }
        }
        */

        if (params.containsKey (PARAM_CMD))
        {
            final List<String> vals = params.remove (PARAM_CMD);

            if (vals!=null)
            {
                for (final String line : vals)
                {
                    Log.debug ("parse: "+line);
                    
                    // save history
                    String history = session.get ("HISTORY");
                    if (history==null) history = line;
                    else history = line + '\n' + history;
                    session.export (context, req.getSession (), "HISTORY", history);
                    session.saveUp (context, req.getSession ());
                    
                    //Script task = new Parser ().parse (line);
                    Group task = new Group (); try
                    {
                        if (! task.assemble (new GraphReader (new StringReader (line))))
                        {
                            Log.warn ("group did not assemble");
                            resp.getWriter ().println ("sh: group did not assemble");
                            task = null;
                        }
                    }
                    catch (final GraphException e)
                    {
                        Log.error (e);
                        resp.getWriter ().println ("sh: "+e);
                        task = null;
                    }
                    Log.info ("task: "+task);

                    if (task!=null) try
                    {
                        if (params.containsKey (PARAM_GRAPH))
                        {
                            resp.setContentType ("text/xml");
                            Utils.xstream.toXML (task, resp.getWriter ());
                        }
                        else
                        {
                            // first fork and save child session
                            // FIXME: figure out a way these don't need to be saved, or could be easily cleaned up
                            final Session forked = session.fork (context);
                            try
                            {
                                forked.save (context, req.getSession ());
                                /*
                                String sessions = context.getInitParameter (SESSIONS_PARAM);
                                // FIXME: hardcoded session path
                                if (sessions==null) sessions = "local:/proc/shell/";
                                forked.saveTo (sessions+forked.getId (), context);
                                */

                                //task.exec (forked, context, req, resp, out);

                                final Return r = task.exec (new TaskData (forked, context, user, req.getSession ()));
                                Log.debug ("cmd returned: "+r);

                                if (r!=null)
                                {
                                    Log.debug ("setting content-type: "+r.type);
                                    resp.setContentType (r.type);
                                    Utils.pipe (r.data, resp.getWriter ());
                                }
                            }
                            finally
                            {
                                forked.delete (context, req.getSession ());
                            }
                        }
                    }
                    catch (final IOException e)
                    {
                        Log.error (e);
                        resp.getWriter ().println ("sh: "+e);
                    }
                    
                    //resp.getWriter ().println ();
                }
            }
            else // error
            {
                Status.set (resp, resp.SC_BAD_REQUEST, "missing command line");
            }
        }

        if (params.containsKey (null))
        {
            final List<String> vals = params.get (null);

            if (vals!=null) // first value is script to run, anything else gets passed to the script
            {
                final String script = vals.remove (0);
                Log.debug ("script: "+script);

                final Transport resource = Transport.handler (script, context, req.getSession ());
                Log.debug("shell script resource: "+resource);
                final InputStream sin = resource.get ();

                Script task = new Script (); try
                {
                    if (! task.assemble (new GraphReader (new InputStreamReader (sin))))
                    {
                        Log.warn ("script did not assemble");
                        resp.getWriter ().println ("sh: script did not assemble");
                        task = null;
                    }
                }
                catch (final GraphException e)
                {
                    Log.error (e);
                    resp.getWriter ().println ("sh: "+e);
                    task = null;
                }
                Log.info ("task: "+task);

                if (task!=null) try
                {
                    if (params.containsKey (PARAM_GRAPH))
                    {
                        resp.setContentType ("text/xml");
                        Utils.xstream.toXML (task, resp.getWriter ());
                    }
                    else
                    {
                        // we don't fork the session here because it will already be forked at every
                        // call within the script
                        /*
                        // first fork and save child session
                        // FIXME: figure out a way these don't need to be saved, or could be easily cleaned up
                        final Session forked = session.fork ();
                        String sessions = config.getInitParameter (INIT_PARAM_SESSIONS);
                        // FIXME: hardcoded session path
                        if (sessions==null) sessions = "/proc/shell/";
                        forked.saveTo (sessions+forked.getId (), context);

                        task.exec (forked, context, req, resp, out);
                         */

                        // prepare a localized session for the script
                        final Session local = Call.valueOf (script, params).prepare (params, new TaskData (session, context, user, req.getSession ()));
                        try
                        {
                            local.save (context, req.getSession ());

                            //task.exec (session, context, req, resp, out);
                            // FIXME: content-disposition, allow-origin not yet utilized here
                            final Return r = task.exec (new TaskData (local, context, user, req.getSession (), req.getInputStream (), req.getContentType (), null, null));
                            Log.debug ("script returned: "+r);

                            if (r!=null)
                            {
                                Log.debug ("setting content-type: "+r.type);
                                resp.setContentType (r.type);
                                if (r.disposition!=null)
                                {
                                    resp.setHeader ("Content-Disposition", r.disposition);
                                }
                                if (r.allowOrigin!=null)
                                {
                                    resp.setHeader ("Access-Control-Allow-Origin", r.allowOrigin);
                                }
                                Utils.pipe (r.data, resp.getWriter ());
                            }

                            // FIXME: so hacky (propagating exit value up)
                            final Session parent = session.getParent (context, req.getSession ());
                            parent.clearExit ();
                            if (local.getExit () != null)
                            {
                                parent.setExit (local.getExit ());
                                parent.save (context, req.getSession ());
                            }
                            /*
                            parent.clearExit ();
                            if (session.getExit () != null)
                            {
                                parent.setExit (session.getExit ());
                                parent.save (context);
                            }
                            */
                        }
                        finally
                        {
                            local.delete (context, req.getSession ());
                        }
                    }
                }
                catch (final IOException e)
                {
                    Log.error (e);
                    resp.getWriter ().println ("sh: "+e);
                }
            }
            else // error
            {
                Log.error ("missing script resource");
                resp.getWriter ().println ("sh: missing script resource");
                Status.set (resp, resp.SC_BAD_REQUEST, "missing script resource");
            }
        }
        else // try interpreting stdin as commands to execute
        {
            final InputStream in = req.getInputStream ();
            if (in!=null)
            {
                Log.debug ("possibly have stdin cmds to execute");

                // try interpreting stdin as commands to execute
                Group task = new Group (); try
                {
                    final GraphReader gin = new GraphReader (new InputStreamReader (in));
                    if (! task.assemble (gin))
                    {
                        if (gin.peek ()>=0)
                        {
                            Log.warn ("stdin group did not assemble");
                            resp.getWriter ().println ("sh: stdin group did not assemble");
                        }

                        task = null;
                    }

                    if (task!=null)
                    {
                        // we don't fork the session here because it will already be forked at every
                        // call within the script
                        /*
                        // first fork and save child session
                        // FIXME: figure out a way these don't need to be saved, or could be easily cleaned up
                        final Session forked = session.fork ();
                        String sessions = config.getInitParameter (INIT_PARAM_SESSIONS);
                        // FIXME: hardcoded session path
                        if (sessions==null) sessions = "/proc/shell/";
                        forked.saveTo (sessions+forked.getId (), context);

                        task.exec (forked, context, req, resp, out);
                         */

                        //task.exec (session, context, req, resp, out);
                        final Return r = task.exec (new TaskData (session, context, user, req.getSession ()));
                        Log.debug ("cmd (stdin) returned: "+r);
                        
                        if (r!=null)
                        {
                            Log.debug ("setting content-type: "+r.type);
                            resp.setContentType (r.type);
                            Utils.pipe (r.data, resp.getWriter ());
                        }
                    }
                }
                catch (final GraphException e)
                {
                    Log.error (e);
                    resp.getWriter ().println ("sh: "+e);
                }
                catch (final IOException e)
                {
                    Log.error (e);
                    resp.getWriter ().println ("sh: "+e);
                }
            }
        }
    }
    
    /**
     * Execute a given literal command with parameters (no shell processing occurs).
     */
    public static Return exec (final String resource, final Map<String,List<String>> params, final TaskData task) throws IOException, ServletException
    {
        Log.debug ("shell exec: "+resource);
        Return r = Call.valueOf (resource, params).exec (task);
        Log.debug ("shell exec returned: "+r);
        return r;
    }
    
    /**
     * Convenience method to set exit value in the current shell session.
     */
    public static void exit (final HttpServletRequest req, final ServletContext context, final int code) throws IOException
    {
        Session shell = Sessions.get (req);
        if (shell==null) 
        {
            Log.error ("unable to retrieve session");
            throw new IllegalArgumentException ("need a session to set exit value");
        }
        
        // FIXME: re-evaluate this... we actually store the exit value in the parent session
        shell = shell.getParent (context, req.getSession ());
        if (shell==null) 
        {
            Log.error ("unable to retrieve parent session");
            throw new IllegalArgumentException ("need a parent session to set exit value");
        }
        
        if (shell.getExit ()!=null)
        {
            Log.error ("session already has an exit value");
            throw new IllegalArgumentException ("session already has an exit value");
        }
        
        shell.setExit (code);
        
        /*
        // FIXME: hardcoded path to shell session storage
        shell.saveTo ("local:/proc/shell/"+shell.getId (), context);
        */
        shell.save (context, req.getSession ());
    }
    
    static void information (final String message, final HttpServletResponse resp) throws IOException
    {
        resp.setContentType ("text/plain");
        final PrintWriter out = resp.getWriter ();
        
        out.println (Shell.class.getName ());
        out.println ("version: "+VERSION);
        out.println ();
        if (message!=null) out.println (message);
    }
}
