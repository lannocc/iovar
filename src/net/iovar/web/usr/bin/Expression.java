/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2015 Lannocc Technologies
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
 * Evaluate arithmetic expressions.
 * 
 * @author  shawn@lannocc.com
 */
public class Expression extends HttpServlet
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
        
        out.println ("usage: expr [options] <expression>");
        out.println ();
        out.println ("Evaluate arithmetic expression <expression> and output the result.");
        out.println ();
        out.println ("Supported expressions:");
        out.println ("   <num1> + <num2>");
        out.println ("       - Add the two numbers");
        out.println ("   <num1> - <num2>");
        out.println ("       - Subtract <num2> from <num1>");
        out.println ("   <num1> * <num2>");
        out.println ("       - Multiply the two numbers");
        out.println ("   <num1> / <num2>");
        out.println ("       - Divide <num2> into <num1>");
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
        /*
        if (vals==null || vals.size ()<1)
        {
            Log.debug ("empty expression; returning false status");
            Shell.exit (req, context, 1);
            return;
        }
        */
        
        if (vals==null || vals.size ()!=3)
        {
            resp.getWriter ().print ("expr: unsupported expression type (wrong number of arguments)");
            Shell.exit (req, context, 2);
            return;
        }
        
        final long num1; try
        {
            num1 = Long.parseLong (vals.get (0));
        }
        catch (final NumberFormatException e)
        {
            resp.getWriter ().print ("expr: first value is not an integer: "+vals.get (0));
            Shell.exit (req, context, 3);
            return;
        }
        
        final long num2; try
        {
            num2 = Long.parseLong (vals.get (2));
        }
        catch (final NumberFormatException e)
        {
            resp.getWriter ().print ("expr: second value is not an integer: "+vals.get (2));
            Shell.exit (req, context, 4);
            return;
        }
        
        final String op = vals.get (1);
        final PrintWriter out = resp.getWriter ();
        
        if ("+".equals (op))
        {
            out.print (String.valueOf (num1+num2));
        }
        else if ("-".equals (op))
        {
            out.print (String.valueOf (num1-num2));
        }
        else if ("*".equals (op))
        {
            out.print (String.valueOf (num1*num2));
        }
        else if ("/".equals (op))
        {
            out.print (String.valueOf (num1/num2));
        }
        else
        {
            resp.getWriter ().print ("expr: unsupported expression type (unsupported operator)");
            Shell.exit (req, context, 5);
            return;
        }
        
        Shell.exit (req, context, 0);
    }
}
