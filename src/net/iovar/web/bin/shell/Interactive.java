/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2011-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.task.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;
import net.iovar.web.usr.bin.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Front-end interface for the web shell.
 *
 * @author  shawn@lannocc.com
 */
public class Interactive extends HttpServlet
{
    public static final String CONFIG_URI = "local:/etc/interactive.conf";
    public static final String PARAM_SHRC = "shrc";
    
    enum Config
    {
        STYLE ("style");
        
        final String key;
        
        Config (final String key)
        {
            this.key = key;
        }
        
        public String toString ()
        {
            return key;
        }
    }
    
    protected void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doHead (this, req, resp);
    }
    
    /**
     * This is one of the few that doesn't follow the standard Utils.goGet () because
     * this is the servlet likely to be called directly from an HTML browser (which have
     * limited support for anything but GET).
     */
    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        //Utils.doGet (this, req, resp);
        
        final String path = req.getPathInfo ();
        
        if (path==null) // run self
        {
            doPost (req, resp);
        }
        else try // run something else
        {
            exec (req, resp, getServletContext (), path);
        }
        catch (final Exception e)
        {
            Log.fatal (Utils.getTrace (e));
            throw new ServletException (e);
        }
    }
    
    protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doPut (this, req, resp);
    }
    
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        final String path = req.getPathInfo ();
        
        if (path==null) // run interpreter
        {
            final Map<String,List<String>> params = Utils.getParams (req.getQueryString ());
            
            if (params.containsKey (Shell.PARAM_CMD)) // parse command ; requires session
            {
                Shell.exec (getServletContext (), req.getRemoteUser (), params, req, resp);
            }
            else if (params.containsKey (null)) // parse command ; auto-session
            {
                params.put (Shell.PARAM_CMD, params.remove (null));
                final Session session = Sessions.get (req);
                params.put (Shell.EXT_PARAM_SESSION, Arrays.asList (new String[] { session.getPath () }));
                Shell.exec (getServletContext (), req.getRemoteUser (), params, req, resp);
            }
            else // present the interactive interface
            {
                interactive (req, resp);
            }
        }
        else try // run something else
        {
            exec (req, resp, getServletContext (), path);
        }
        catch (final Exception e)
        {
            Log.fatal (Utils.getTrace (e));
            throw new ServletException (e);
        }
    }
    
    /**
     * Interactive mode will utilize shell master session if it exists
     * (usually set up via /sbin/init). If none exists it will create a session.
     */
    void interactive (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
            
        /*
        final Enumeration attrs = req.getSession ().getAttributeNames ();
        while (attrs.hasMoreElements ())
        {
            Log.debug ("http session attribute: " + attrs.nextElement ());
        }
        */

        final String user = req.getRemoteUser ();
        Log.debug ("remote user: "+user);
        
        final Session shell = Sessions.get (req);
        Log.info ("shell session: "+shell);
        
        
        /*
        Session session = Sessions.get (context);
        if (session!=null) Log.debug ("found master session: "+session);
        else
        {
            Log.debug ("creating session");
            session = Sessions.newInstance (context);
        }
        Log.info ("session: "+session);
        session.save (context);
        */
        
        // shrc support (login/interactive shell)
        String shrc = req.getParameter (PARAM_SHRC);
        if (shrc==null) shrc = "res:/etc/shrc";
        if (shrc.length ()>0)
        {
            final Map<String,List<String>> params = new HashMap<String,List<String>> ();
            params.put (Shell.EXT_PARAM_SESSION, Arrays.asList (new String[] { shell.getPath () }));
            
            final Transport rc = Transport.handler (shrc, params, context, req.getSession ());
            if (rc.exists ())
            {
                rc.post (null, null);
            }
        }
        
        if ("root".equals (user) || (user==null && "127.0.0.1".equals (req.getRemoteAddr ())))
        {
            final Map<String,List<String>> params = new HashMap<String,List<String>> ();
            params.put (Shell.EXT_PARAM_SESSION, Arrays.asList (new String[] { shell.getPath () }));
            final Transport rc = Transport.handler ("/root/.ioshrc", params, context, req.getSession ());
            if (rc.exists ())
            {
                rc.post (null, null);
            }
        }
        else if (user!=null)
        {
            final Map<String,List<String>> params = new HashMap<String,List<String>> ();
            params.put (Shell.EXT_PARAM_SESSION, Arrays.asList (new String[] { shell.getPath () }));
            final Transport rc = Transport.handler ("/home/"+user+"/.ioshrc", params, context, req.getSession ());
            if (rc.exists ())
            {
                rc.post (null, null);
            }
        }
        
        boolean styled = false;
        try
        {
            final Map<String,String> config = Utils.configMap (Transport.handler (CONFIG_URI, context, req.getSession ()).get ());
            final String style = config.get (Config.STYLE.toString ());
            
            if (style!=null)
            {
                final String xml =
                        "<shell>\n"+
                        "   <version>"+Shell.VERSION+"</version>\n"+
                        "   <session>"+shell.getPath ()+"</session>\n"+
                        "   <shell-class>"+Shell.class.getName ()+"</shell-class>\n"+
                        "   <interactive-class>"+this.getClass ().getName ()+"</interactive-class>\n"+
                        "</shell>";
                
                XSLT.transform (context, Transport.handler (style, context, req.getSession ()).get (),
                        new ByteArrayInputStream (xml.getBytes ()), resp, req.getSession ());
                
                styled = true;
            }
        }
        catch (final Exception e)
        {
            // no-op
            Log.warn ("can't use XML-styled interface", e);
        }
        
        if (!styled)
        {
            resp.setContentType ("text/html");
            final PrintWriter out = resp.getWriter ();

            out.println ("<!DOCTYPE html>");
            out.println ("<html>");
            out.println ("  <head>");
            out.println ("      <title>$ # iovar shell</title>");
            out.println ("      <link rel=\"shortcut icon\" sizes=\"16x16\" href=\"usr/include/iovar/favicon/favicon-16.png\">");
            out.println ("      <link rel=\"icon\" sizes=\"16x16\" href=\"usr/include/iovar/favicon/favicon-16.png\">");
            out.println ("      <link rel=\"stylesheet\" type=\"text/css\" href=\"usr/include/iovar/shell.css\">");
            out.println ("      <script language=\"javascript\" type=\"application/javascript\" src=\"/usr/include/iovar/shell.js\"> </script>");
            out.println ("  </head>");
            out.println ("  <body onload=\"document.forms[0].elements['"+Shell.PARAM_CMD+"'].focus()\">");
            
            out.println ("      <h1><span title=\""+Interactive.class.getName ()+"\">iovar "+Shell.VERSION+"</span> | <a href=\"/var/log/shell\" target=\"log\">log</a></h1>");

            out.println ("      <form target=\"output\" method=\"GET\" onsubmit=\"exec_submit(this)\">");
            out.println ("          <input type=\"hidden\" name=\""+Shell.PARAM_SESSION+"\" value=\""+shell.getPath ()+"\">");
            out.println ("          <input id=\"cmd_exec\" type=\"submit\" value=\"exec\">");
            out.println ("          <label id=\"cmd\"><img id=\"prompt\" class=\"begin\" alt=\"iovar shell\" src=\"/usr/include/iovar/favicon/favicon-16.png\">");
            out.println ("              <span class=\"legacy\">$</span> <input type=\"text\" id=\"cmd_in\" name=\""+Shell.PARAM_CMD+"\" size=\"60\"></label>");
            out.println ("      </form>");
            out.println ("      <iframe name=\"output\" width=\"100%\" height=\"600\" onload=\"exec_load(this)\"> </iframe>");
            out.println ("      <script language=\"javascript\"> document.getElementById ('cmd_in').focus (); </script>");
            out.println ("  </body>");
            out.println ("</html>");
        }
    }
    
    public static void exec (final HttpServletRequest req, final HttpServletResponse resp, final ServletContext context, final String path) throws IOException, ServletException
    {
        final InputStream in = req.getInputStream (); // for some reason we have to grab this before getSession ()
        
        final Session shell = Sessions.get (req);
        Log.debug ("shell session for exec: "+shell);
        Log.debug ("request data contentType: "+req.getContentType ());
        
        final Map<String,List<String>> params = Utils.getParams (req.getQueryString ());
        // FIXME: need to completely re-evaluate how the ?iosession param should be used (if at all).
        // Currently, iosession is passed automatically by IOVAR calls. However, when calling
        // on a remote machine this will not be a valid session. For now, we disable accepting
        // any sessions here (relying on master session / cookies instead).
        if (params.containsKey (Shell.EXT_PARAM_SESSION))
        {
            Log.debug ("removing iosession parameter");
            params.remove (Shell.EXT_PARAM_SESSION);
        }
        
        // FIXME: content-disposition, allow-origin not yet utilized here
        final Return r = Shell.exec (path, params, new TaskData (shell, context, req.getRemoteUser (), req.getSession (), in, req.getContentType (), null, null));
        Log.debug ("output content type: "+r.type);
        resp.setContentType (r.type);
        if (r.disposition!=null)
        {
            resp.setHeader ("Content-Disposition", r.disposition);
        }
        if (r.allowOrigin!=null)
        {
            resp.setHeader ("Access-Control-Allow-Origin", r.allowOrigin);
        }
        Utils.pipe (r.data, resp.getOutputStream ());
    }
}
