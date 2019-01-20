/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Convert Java object to XML tree.
 * 
 * @author  shawn@lannocc.com
 */
public class Java2XML extends HttpServlet
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
        
        out.println ("usage: java2xml");
        out.println ();
        out.println ("Convert Java object passed as standard input into XML tree.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
    }
    
    /**
     * Execute.
     */
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
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
        
        /*
        if (params.size ()!=0)
        {
            resp.getWriter ().println ("java2xml: no arguments expected");
            Shell.exit (req, context, 1);
            return;
        }
        */
        
        /*
        if (!"application/x-java-serialized-object".equals (req.getContentType ()))
        {
            resp.getWriter ().println ("java2xml: expected input of 'application/x-java-serialized-object' content-type");
            Shell.exit (req, context, 2);
            return;
        }
        */
        
        try
        {
            ObjectInputStream in = new ObjectInputStream (req.getInputStream ()); try
            {
                Utils.xstream.toXML (in.readObject (), resp.getOutputStream ());
            }
            catch (final ClassNotFoundException e)
            {
                Log.error ("failed to read input as Java object", e);
                resp.getWriter ().println ("java2xml: failed reading input: "+e);
                Shell.exit (req, context, 3);
                return;
            }
            finally
            {
                in.close ();
            }

            Shell.exit (req, context, 0);
        }
        catch (final Exception e)
        {
            Log.fatal ("uncaught exception", e);
            resp.getWriter ().println ("java2xml: uncaught exception: "+e);
            Shell.exit (req, context, 99);
        }
            
    }
}
