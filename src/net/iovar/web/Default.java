/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web;

// local imports:
import net.iovar.web.bin.shell.*;
import net.iovar.web.bin.shell.task.*;
import net.iovar.web.dev.*;
import net.iovar.web.dev.trans.*;

// java imports:
import java.io.*;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Default handler for all requests not handled by other servlets.
 * Should be installed last in web.xml and mapped with url-pattern: /*
 *
 * @author  shawn@lannocc.com
 */

public class Default extends HttpServlet
{
    public static final String CANON_RES = "/etc/canon";
    public static final String CANON_PREFIX = "/-";
    
    protected boolean autoExec = false;
    
    boolean canon = false;
    
    public void init () throws ServletException
    {
        try
        {
            Transport resource = new Resource (CANON_RES, getServletContext (), null);
            if (resource.exists ())
            {
                canon = true;
                final Map<String,String> canon = Utils.configMap (resource.get ());
                putCanon (canon);
            }
        }
        catch (final IOException e)
        {
            throw new ServletException (e);
        }
    }
    
    void putCanon (final Map<String,String> canon)
    {
        final ServletContext context = getServletContext ();
        context.setAttribute (Default.class.getName ()+"-canon", canon);
    }
    
    Map<String,String> getCanon ()
    {
        return getCanon (getServletContext ());
    }
    
    public static Map<String,String> getCanon (final ServletContext context)
    {
        return (Map<String,String>) context.getAttribute (Default.class.getName ()+"-canon");
    }
    
    public static void addCanon (final ServletContext context, final String alias, final String redirect)
    {
        final Map<String,String> canon = getCanon (context);
        canon.put (alias, redirect);
    }
    
    boolean isCanon (String path)
    {
        while (path.startsWith ("//"))
        {
            path = path.substring (1);
        }
        
        return path.startsWith (CANON_PREFIX);
    }
    
    void redirect (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        String path = getPath (req);
        Log.debug ("canon check: "+path);
        
        while (path.startsWith ("//"))
        {
            path = path.substring (1);
        }
        path = path.substring (CANON_PREFIX.length ());
        
        final String redirect = getCanon ().get (path);
        
        if (redirect!=null)
        {
            Log.info ("canon redirect: " + redirect);
            resp.sendRedirect (redirect);
        }
        else
        {
            Log.warn ("canon not found: " + path);
            resp.sendError (HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    protected void service (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        if ("PATCH".equalsIgnoreCase(req.getMethod ()))
        {
            doPatch (req, resp);
        }
        else
        {
            super.service (req, resp);
        }
    }

    protected void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        final String path = getPath (req);
        Log.debug ("local HEAD: "+path);
        
        if (canon && isCanon (path))
        {
            redirect (req, resp);
        }
        else
        {
            final Transport resource = Default.getResource (context, req.getSession (), req.getRemoteUser (), Utils.getParams (req.getQueryString ()), path);
            Log.debug ("resource: "+resource);

            if (! resource.exists ())
            {
                Log.debug ("does not exist: "+resource);
                resp.sendError (resp.SC_NOT_FOUND, "does not exist: "+path);
                return;
            }
        }
    }

    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        final ServletContext context = getServletContext ();
        String path = getPath (req);
        Log.debug ("local GET: "+path);

        if (canon && isCanon (path))
        {
            redirect (req, resp);
        }
        else
        {
            final Transport resource = Default.getResource (context, req.getSession (), req.getRemoteUser (), Utils.getParams (req.getQueryString ()), path);
            Log.debug ("resource: "+resource);
            
            boolean handled = false;
            
            if (autoExec)
            {
                if (! Boolean.FALSE.equals (resource.directory ()))
                {
                    handled = true;
                    Log.debug ("auto-exec enabled and resource is (potentially) a directory...");
                    //doPost (req, resp);
                    Interactive.exec (req, resp, context, path + "/index");
                }
                else if (! Boolean.FALSE.equals (resource.executable ()))
                {
                    handled = true;
                    Log.debug ("auto-exec enabled and resource is (potentially) executable...");
                    //doPost (req, resp);
                    Interactive.exec (req, resp, context, path);
                }
            }
            
            if (!handled)
            {
                final InputStream data = resource.get ();

                if (data==null)
                {
                    Log.error ("not found: "+path);
                    resp.sendError (resp.SC_NOT_FOUND, "not found: "+path);
                    return;
                }

                Utils.pipe (data, resp.getOutputStream ());
            }
        }
    }

    protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        String path = getPath (req);
        Log.debug ("local PUT: "+path);

        if (canon && isCanon (path))
        {
            redirect (req, resp);
        }
        else
        {
            final Transport resource = Default.getResource (context, req.getSession (), req.getRemoteUser (), Utils.getParams (req.getQueryString ()), path);
            Log.debug ("resource: "+resource);

            Utils.pipe (resource.put (req.getInputStream ()), resp.getOutputStream ());
        }
    }

    protected void doPatch (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        String path = getPath (req);
        Log.debug ("local PATCH: "+path);

        if (canon && isCanon (path))
        {
            redirect (req, resp);
        }
        else
        {
            final Transport resource = Default.getResource (context, req.getSession (), req.getRemoteUser (), Utils.getParams (req.getQueryString ()), path);
            Log.debug ("resource: "+resource);

            Utils.pipe (resource.patch (req.getInputStream (), req.getContentType ()), resp.getOutputStream ());
        }
    }

    protected void doDelete (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        String path = getPath (req);
        Log.debug ("local DELETE: "+path);

        if (canon && isCanon (path))
        {
            redirect (req, resp);
        }
        else
        {
            final Transport resource = Default.getResource (context, req.getSession (), req.getRemoteUser (), Utils.getParams (req.getQueryString ()), path);
            Log.debug ("resource: "+resource);

            Utils.pipe (resource.delete (), resp.getOutputStream ());
        }
    }

    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        // WARNING: do NOT call req.getParameter () anywhere in here because doing so will trigger
        //          automatic reading of any input stream that is application/x-www-form-urlencoded,
        //          preventing normal form POST data from propagating.
        
        final ServletContext context = getServletContext ();
        String path = getPath (req);
        Log.debug ("local POST: "+path);
        Log.debug (context.getServletContextName());
/*
BufferedReader something = new BufferedReader (new InputStreamReader (req.getInputStream ()));
for (String line; (line = something.readLine ())!=null; )
{
    Log.fatal ("BLAH: "+line);
}
*/

        if (canon && isCanon (path))
        {
            redirect (req, resp);
        }
        else
        {
            final Transport resource = Default.getResource (context, req.getSession (), req.getRemoteUser (), Utils.getParams (req.getQueryString ()), path);
            Log.debug ("resource: "+resource);
            final Map<String,List<String>> params = Utils.getParams (req.getQueryString ());
            boolean handled = false;
            
            // FIXME: we're checking for EXT_PARAM_SESSION here because otherwise an autoexec request
            //          hangs the servlet container
            if (! params.containsKey (Shell.EXT_PARAM_SESSION) && autoExec)
            {
                if (! Boolean.FALSE.equals (resource.directory ()))
                {
                    handled = true;
                    Log.debug ("auto-exec enabled and resource is (potentially) a directory...");
                    //doPost (req, resp);
                    Interactive.exec (req, resp, context, path + "/index");
                }
                else if (! Boolean.FALSE.equals (resource.executable ()))
                {
                    handled = true;
                    Log.debug ("auto-exec enabled and resource is (potentially) executable...");
                    //doPost (req, resp);
                    Interactive.exec (req, resp, context, path);
                }
            }
            
            if (!handled)
            {
                //if (session!=null) resource.params.put (Shell.EXT_PARAM_SESSION, Arrays.asList (new String[] { session }));

                final String contentType = req.getContentType ();
                Log.debug ("request data contentType: "+contentType);

                Return r = resource.post (req.getInputStream (), contentType);
                Log.debug ("local post returned: "+r);
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
    }
    
    static String getPath (final HttpServletRequest req)
    {
        String path = req.getPathInfo ();
        if (path==null) path = "";
        return req.getServletPath () + "/" + path;
    }
    
    /**
     * Traverse symbolic links and return reference to a given resource.
     */
    static Transport getResource (final ServletContext context, final HttpSession htsession, final String user, final Map<String,List<String>> params, final String path) throws IOException
    {
        final File root = new File (context.getRealPath ("/")).getCanonicalFile ();
        return getResource (context, htsession, user, params, root, root, path, path.endsWith ("/"));
    }
    
    private static Transport getResource (final ServletContext context, final HttpSession htsession, final String user, final Map<String,List<String>> params, final File root, File base, String path, final boolean isDir) throws IOException
    {
        Log.debug ("getResource () root: "+root);
        Log.debug ("getResource () base: "+base);
        Log.debug ("getResource () path: "+path);

        File target;
        {
            final File file;
            {
                final int sep = path.indexOf (File.separator);
                Log.debug ("sep: "+sep);
                
                if (sep<0)
                {
                    base = new File (base, path).getCanonicalFile ();
                    file = base;
                    path = "";
                }
                else if (sep==0)
                {
                    return getResource (context, htsession, user, params, root, base, path.substring (1), isDir);
                    //path = path.substring (1);
                    //file = new File (base, path).getCanonicalFile ();
                }
                else
                {
                    base = new File (base, path.substring (0, sep)).getCanonicalFile ();
                    path = path.substring (sep+1);
                    file = new File (base, path).getCanonicalFile ();
                }
            }
            Log.debug ("new base: "+base);
            Log.debug ("new path: "+path);
            Log.debug ("file: "+file);

            Path fpath = base.toPath ();
            
            if (Files.isSymbolicLink (fpath))
            {
                Log.debug ("getResource () -- found symbolic link");
                fpath = Files.readSymbolicLink (fpath);
                
                /* SECURITY: making link relative to project root (prevent sandbox escape) -- re-evaluate if needed */
                target = new File (base, fpath.toString ()).getCanonicalFile ();
                Log.debug ("getResource () link target: "+target);
                
                base = target;
                target = new File (base, path).getCanonicalFile ();
            }
            else
            {
                target = file;
            }
        }
        
        if (base.equals (target))
        {
            // FIXME: check back on this...
            //      basically, if a local File exists then we prefer that returned so
            //      we can check execute bit, etc.
            
            if (! target.exists () && (target.getPath ()+File.separator).startsWith (root.getPath ()))
            {
                return new Resource (/*"/"+*/ target.getPath ().substring (root.getPath ().length ()) + (isDir? "/": ""), context, user, params, htsession);
            }
            else
            {
                if (isDir) return new net.iovar.web.dev.trans.File (target.getPath ()+"/", context, user, params, htsession);
                else return new net.iovar.web.dev.trans.File (target.getPath (), context, user, params, htsession);
            }
        }
        else
        {
            return getResource (context, htsession, user, params, root, base, path, isDir);
        }
    }
}
