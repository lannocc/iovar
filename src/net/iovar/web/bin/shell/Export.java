/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2012-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell;

// local imports:
import net.iovar.web.*;
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
 * Assign variable in parent shell.
 *
 * @author  shawn@lannocc.com
 */
public class Export extends HttpServlet
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
        
        out.println ("usage: export <name>[=[<val>]]...");
        out.println ();
        out.println ("Defines a variable <name> and exports it to the parent environment.");
        out.println ("When called with a name and without equals sign then standard input is used for value");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help          - display this help screen");
        out.println ("   ?master        - export to the master session");
    }
    
    /**
     * Execute.
     */
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
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

        final List<String> exports = params.get (null);
        if (exports==null || exports.isEmpty ())
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final Session session = Sessions.get (req);
        if (session==null) 
        {
            Log.error ("unable to retrieve session");
            throw new IllegalArgumentException ("need a session to export to");
        }
        
        final boolean master = params.containsKey ("master");
        
        final StringBuffer s = new StringBuffer ();
        final InputStream in = req.getInputStream ();
        if (in!=null)
        {
            final InputStreamReader reader = new InputStreamReader (in);
            for (int c; (c = reader.read ())>=0; s.append ((char) c));
        }
        
        for (final String export : exports)
        {
            int idx = export.indexOf ("=");
            String key = idx>=0? export.substring (0, idx): export;
            String val = idx>=0? export.substring (idx+1): null;
            if (val==null) val = s.toString ();

            session.export (context, req.getSession (), key, val, master);
        }

        // FIXME: probably shouldn't automatically save here
        session.saveUp (context, req.getSession ());
        
        // FIXME: is this necessary?
        //Sessions.put (req, context, session);
    }
}
