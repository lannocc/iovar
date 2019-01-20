/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.json.*;
import net.iovar.parse.*;
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
 *
 * @author  shawn@lannocc.com
 */
public class JSON extends HttpServlet
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
        
        out.println ("usage: json [node] ...");
        out.println ();
        out.println ("Return the value of stdin interpreted as json and traversed down the given nodes (optional).");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?quiet     - quiet operation: a matching value is still printed");
        out.println ("                but errors and mismatches are not displayed.");
        out.println ("   ?clean     - clean output: remove the quotes around a json string,");
        out.println ("                usable for final json value only.");
    }
    
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
        
        final PrintWriter out = resp.getWriter ();
        final boolean quiet = params.containsKey ("quiet");

        JSONValue json = new JSONValue (); try
        {
            final GraphReader in = new GraphReader (new InputStreamReader (req.getInputStream ()));
            if (json.assemble (in))
            {
                final List<String> nodes = params.get (null);
                if (nodes != null)
                {
                    for (final String node : nodes)
                    {
                        final Value val = json.value ();
                        
                        if (val instanceof JSONMap)
                        {
                            json = ((JSONMap) val).get (node);
                            
                            if (json == null)
                            {
                                if (! quiet) out.println ("json: requested node does not exist as key in json map: "+node);
                                Shell.exit (req, context, 3);
                                return;
                            }
                        }
                        else if (val instanceof JSONArray)
                        {
                            try
                            {
                                json = ((JSONArray) val).get (Integer.parseInt (node));
                            }
                            catch (final NumberFormatException e)
                            {
                                if (! quiet) out.println ("json: requested node is not an integer index for json array: "+node+": "+e);
                                Shell.exit (req, context, 4);
                                return;
                            }
                            catch (final IndexOutOfBoundsException e)
                            {
                                if (! quiet) out.println ("json: requested node json array index does not exist: "+e);
                                Shell.exit (req, context, 5);
                                return;
                            }
                        }
                        else
                        {
                            if (! quiet) out.println ("json: requesting a node beyond applicable input: " + node);
                            Shell.exit (req, context, 6);
                            return;
                        }
                    }
                }
                
                if (params.containsKey ("clean"))
                {
                    final Value val = json.value ();
                    
                    if (val instanceof JSONString)
                    {
                        out.print (((JSONString) val).value ());
                    }
                    else if (val instanceof JSONNumber || val instanceof JSONNull ||
                            val instanceof JSONTrue || val instanceof JSONFalse)
                    {
                        out.print (json);
                    }
                    else
                    {
                        if (! quiet) out.println ("json: requested json data cannot be converted to clean output");
                        Shell.exit (req, context, 7);
                        return;
                    }
                    
                    out.flush ();
                }
                else
                {
                    out.println (json);
                }
                
                Shell.exit (req, context, 0);
            }
            else
            {
                Shell.exit (req, context, 1);
            }
        }
        catch (final GraphException e)
        {
            Log.warn (e);
            if (! quiet) out.println ("json: bad json data: " + e);
            Shell.exit (req, context, 2);
        }
    }
}
