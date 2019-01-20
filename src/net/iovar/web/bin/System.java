/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;
import net.iovar.web.dev.trans.*;
import net.iovar.web.dev.trans.File;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.util.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Execute a system command (runs as the servlet container user).
 *
 * @author  shawn@lannocc.com
 */
public class System extends HttpServlet
{
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
    
    public void usage (final HttpServletResponse resp) throws IOException
    {
        Log.debug ("displaying usage");
        
        resp.setContentType ("text/plain");
        final PrintWriter out = resp.getWriter ();
        
        out.println ("usage: system <command> [arguments]");
        out.println ();
        out.println ("Executes system command.");
        out.println ("  - optional arguments are passed to the command");
        out.println ("  - double dash (--) can be used to stop interpreting further arguments as possible transports");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help          - display this help screen");
        out.println ("   ?<name>=<val>  - any other named parameters are passed as environment variables");
    }
    
    /**
     * Execute. Anonymous parameters expected:
     *  arg[0]: resource to execute
     * 
     * Exit code is set to the number of problems encountered while processing.
     */
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        final String query = req.getQueryString ();
        final Map<String,List<String>> params = Utils.getParams (query);
        
        if (params.containsKey ("help"))
        {
            usage (resp);
            Shell.exit (req, context, 0);
            return;
        }
        
        final List<String> resources = params.remove (null);
        
        if (resources==null || resources.size ()<1)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final String command = translateResource (resources.remove (0), context, resp, req.getSession ());
        if (command==null)
        {
            Shell.exit (req, context, 2);
            return;
        }

        String[] env = null;
        if (! params.isEmpty ())
        {
            env = new String[params.size ()];
            int i=0;

            for (final Map.Entry<String,List<String>> entry : params.entrySet ())
            {
                env[i] = entry.getKey () + "=" + entry.getValue ().get (0);
                i++;
            }
        }
        
        final List<String> cmds = new ArrayList<String> (resources.size ()+1);
        cmds.add (command);
        boolean transports = true;
        
        for (int i=0; i<resources.size (); i++)
        {
            final String arg = resources.get (i);
            
            if ("--".equals (arg))
            {
                transports = false;
                continue;
            }
            
            // FIXME: ugly hack; bound to cause trouble
            final int colon = transports ? arg.indexOf (':') : -1;
            
            if (colon>0)
            {
                if (arg.charAt (colon-1)!='\\')
                {
                    final String resource = translateResource (arg, context, resp, req.getSession ());
                    if (resource==null)
                    {
                        Shell.exit (req, context, 3);
                        return;
                    }
                    cmds.add (resource);
                }
                else
                {
                    cmds.add (arg.substring (0, colon-1) + arg.substring (colon));
                }
            }
            else
            {
                cmds.add (arg);
            }
        }
        
        // FIXME: we get a session here to avoid Shell.exit () from trying to
        // create a user session after the response has already been committed.
        final Session shell = Sessions.get (req);
        
        try
        {
            Log.info ("executing: "+cmds);
            final Process p = Runtime.getRuntime ().exec (cmds.toArray (new String[cmds.size ()]), env);
            
            // FIXME #11: can't directly pipe all input to the process here, if
            //  it's a lot then the process may stall due to full output buffer.
            //  Therefore we need to start reading off the process output right
            //  away.
            /*
            Utils.pipe (req.getInputStream (), p.getOutputStream ());
            p.getOutputStream ().close ();
            p.waitFor ();

            try
            {
                Utils.pipe (p.getInputStream (), resp.getOutputStream ());
                Utils.pipe (p.getErrorStream (), resp.getOutputStream ());
                Shell.exit (req, context, p.exitValue ());
            }
            catch (final IOException e)
            {
                Log.error (e);
                resp.getWriter ().println ("system: "+e);
                Shell.exit (req, context, 3);
            }
            */
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream ();
            
            final Thread pin = new Thread ()
            {
                public void run ()
                {
                    try
                    {
                        final OutputStream out = p.getOutputStream ();
                        Utils.pipe (req.getInputStream (), out);
                        out.close ();
                    }
                    catch (final IOException e)
                    {
                        Log.error (e);
                        try
                        {
                            resp.getWriter ().println ("system: "+e);
                            Shell.exit (req, context, 3);
                        }
                        catch (final IOException ee)
                        {
                            Log.fatal ("while handling previous exception", ee);
                        }
                    }
                }
            };
            
            final Thread pout = new Thread ()
            {
                public void run ()
                {
                    try
                    {
                        Utils.pipe (p.getInputStream (), out);
                    }
                    catch (final IOException e)
                    {
                        Log.error (e);
                        try
                        {
                            resp.getWriter ().println ("system: "+e);
                            Shell.exit (req, context, 3);
                        }
                        catch (final IOException ee)
                        {
                            Log.fatal ("while handling previous exception", ee);
                        }
                    }
                }
            };
            
            final Thread perr = new Thread ()
            {
                public void run ()
                {
                    try
                    {
                        Utils.pipe (p.getErrorStream (), out);
                    }
                    catch (final IOException e)
                    {
                        Log.error (e);
                        try
                        {
                            resp.getWriter ().println ("system: "+e);
                            Shell.exit (req, context, 3);
                        }
                        catch (final IOException ee)
                        {
                            Log.fatal ("while handling previous exception", ee);
                        }
                    }
                }
            };
            
            pin.start ();
            pout.start ();
            perr.start ();
            
            p.waitFor ();
            pin.join ();
            pout.join ();
            perr.join ();
            
            ByteArrayInputStream in = new ByteArrayInputStream (out.toByteArray ());
            Utils.pipe (in, resp.getOutputStream ());

            Shell.exit (req, context, p.exitValue ());
        }
        catch (final Exception e)
        {
            Log.error (e);
            resp.getWriter ().println ("system: "+e);
            Shell.exit (req, context, 4);
        }
    }
    
    /**
     * FIXME: SECURITY! ugly hack, many assumptions here...
     */
    String translateResource (final String path, final ServletContext context, final HttpServletResponse resp, final HttpSession htsession) throws IOException
    {
        final Transport resource = Transport.handler (path, context, htsession);
        Log.info ("resource: "+resource);
        
        if (resource instanceof File)
        {
            return resource.getPath ();
        }
        else if (resource instanceof Local || resource instanceof Resource)
        {
            final java.io.File root = new java.io.File (context.getRealPath ("/"));
            final java.io.File file = new java.io.File (root, java.io.File.separator+resource.getPath ()).getCanonicalFile ();
        
            return file.getPath ();
        }
        else
        {
            Log.error ("unsupported transport: "+resource);
            resp.getWriter ().println ("system: unsupported transport type: "+resource.getClass ());
            return null;
        }
    }
}
