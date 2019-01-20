/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;

// java imports:
import java.io.*;
import java.math.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * @author  shawn@lannocc.com
 */
public class Test extends HttpServlet
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
        
        out.println ("usage: test [options] [<expression>]");
        out.println ();
        out.println ("Test <expression> and return status based on true/false result of the expression.");
        out.println ("Lack of an expression defaults to false.");
        out.println ();
        out.println ("Expression tests:");
        out.println ("   <string1> = <string2>");
        out.println ("       - the two strings are equal");
        out.println ("   <string1> != <string2>");
        out.println ("       - the two strings are not equal");
        out.println ("   <int1> -gt <int2>");
        out.println ("       - int1 is greater than int2");
        out.println ("   <int1> -ge <int2>");
        out.println ("       - int1 is greater than or equal to int2");
        out.println ("   <int1> -lt <int2>");
        out.println ("       - int1 is less than int2");
        out.println ("   <int1> -le <int2>");
        out.println ("       - int1 is less than or equal to int2");
        out.println ("   <object> is <type>");
        out.println ("       - strictly determine if object (input string) matches given type, one of:");
        out.println ("              int[eger]");
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
        
        final List<String> vals = params.get (null);
        if (vals==null || vals.size ()<1)
        {
            Log.debug ("empty expression; returning false status");
            Shell.exit (req, context, 1);
            return;
        }
        
        if (vals.size ()!=3)
        {
            resp.getWriter ().print ("test: unsupported expression type (wrong number of arguments)");
            Shell.exit (req, context, 2);
            return;
        }
        
        final String op = vals.get (1);
        
        if ("=".equals (op) || "!=".equals (op))
        {
            final String s1 = vals.get (0);
            //resp.getWriter ().println ("s1:"+(s1==null?"null":"'"+s1+"'"));
            final String s2 = vals.get (2);
            //resp.getWriter ().println ("s2:"+(s2==null?"null":"'"+s2+"'"));
            
            final boolean equal = s1==null ? s1==s2 : s1.equals (s2);
            final boolean equals = ("=".equals (op));
            
            if (equals) Shell.exit (req, context, equal ? 0 : 1);
            else Shell.exit (req, context, equal ? 1 : 0);
        }
        else if ("-gt".equals (op) || "-ge".equals (op) || "-lt".equals (op) || "-le".equals (op))
        {
            final BigInteger int1, int2; try
            {
                int1 = new BigInteger (vals.get (0));
                int2 = new BigInteger (vals.get (2));
            }
            catch (final NumberFormatException e)
            {
                resp.getWriter ().print ("test: not an integer: "+e);
                Shell.exit (req, context, 3);
                return;
            }
            
            if ("-gt".equals (op)) Shell.exit (req, context, int1.compareTo (int2) > 0 ? 0 : 1);
            else if ("-ge".equals (op)) Shell.exit (req, context, int1.compareTo (int2) >= 0 ? 0 : 1);
            else if ("-lt".equals (op)) Shell.exit (req, context, int1.compareTo (int2) < 0 ? 0 : 1);
            else Shell.exit (req, context, int1.compareTo (int2) <= 0 ? 0 : 1);
        }
        else if ("is".equals (op))
        {
            final String object = vals.get (0);
            final String type = vals.get (2);
            
            if ("int".equals (type) || "integer".equals (type))
            {
                try
                {
                    new BigInteger (object);
                    Shell.exit (req, context, 0);
                }
                catch (final NumberFormatException e)
                {
                    Shell.exit (req, context, 1);
                }
            }
            else
            {
                resp.getWriter ().print ("test: unsupported type for 'is' operator: " + type);
                Shell.exit (req, context, 4);
                return;
            }
            
        }
        else
        {
            resp.getWriter ().print ("test: unsupported expression type (unsupported operator)");
            Shell.exit (req, context, 5);
            return;
        }
    }
}
