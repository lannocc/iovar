/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;
import net.iovar.web.dev.trans.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:
import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 *
 * @author  shawn@lannocc.com
 */
public class Cookie extends HttpServlet
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
        
        out.println ("usage: cookie [name] ...");
        out.println ();
        out.println ("Delete one or more named cookies, or list all cookies if no names given.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
    }
    
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        try
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
            
            resp.setContentType("text/plain");
            final PrintWriter out = resp.getWriter ();
            
            final List<String> args = params.get (null);
            if (args==null || args.isEmpty ())
            {
                final String key = Http.class.getName ();
                final CookieStore cookies = (CookieStore) context.getAttribute (key);

                if (cookies==null)
                {
                    out.println("[none]");
                }
                else if (cookies.getCookies ().isEmpty ())
                {
                    out.println("[empty]");
                }
                else
                {
                    for (final org.apache.http.cookie.Cookie cookie : cookies.getCookies ())
                    {
                        out.println(cookie);
                    }
                }
            }
            else
            {
                final String key = Http.class.getName ();
                final CookieStore cookies = (CookieStore) context.getAttribute (key);
                
                if (cookies!=null)
                {
                    for (final org.apache.http.cookie.Cookie cookie : cookies.getCookies ())
                    {
                        for (final String name : args)
                        {
                            if (name.equals (cookie.getName ()))
                            {
                                out.println("removing cookie: " + cookie);

                                final BasicClientCookie newcookie = new BasicClientCookie (cookie.getName(), "");
                                newcookie.setDomain (cookie.getDomain ());
                                newcookie.setPath (cookie.getPath ());
                                cookies.addCookie (newcookie);
                            }
                        }
                    }
                }
            }
            
            out.flush ();
        }
        catch (Exception e)
        {
            Log.fatal("error", e);
            final PrintWriter out = resp.getWriter();
            out.println(e);
            StackTraceElement[] elems = e.getStackTrace ();
            for (StackTraceElement elem : elems)
            {
                out.println(elem);
                Log.fatal (elem.toString ());
            }
        }
    }
}
