/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;

// java imports:
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

// 3rd-party imports:

/**
 * FIXME: SECURITY WARNING: NOT SAFE! Need to sanitize all inputs when building SQL statements.
 *
 * @author  shawn@lannocc.com
 */
public class MySql extends HttpServlet
{
    public static final String NAMESPACE = "http://iovar.net/ns/mysql";
    
    static final String CONFIG_PARAM = "config";
    
    /** @deprecated **/
    static final String CONFIG_URI = "local:/etc/mysql.conf";
    
    public static final String PARAM_XSL = "xsl";
    
    static Map<String,Map<Config,String>> configs;
    
    String label; // the config name for this servlet instance
    
    enum Config
    {
        name ("database"),
        host ("host"),
        user ("user"),
        pass ("password");
        
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
    
    public void init () throws ServletException
    {
        final ServletContext context = getServletContext ();
        
        if (configs==null) try
        {
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            configs = new HashMap<String,Map<Config,String>> ();
        }
        catch (final ClassNotFoundException e)
        {
            throw new ServletException (e);
        }
        catch (final InstantiationException e)
        {
            throw new ServletException (e);
        }
        catch (final IllegalAccessException e)
        {
            throw new ServletException (e);
        }
        
        final String path = getInitParameter (CONFIG_PARAM);
        
        if (path!=null)
        {
            final int idx = path.indexOf ("dev/mysql/"); // FIXME: super hacky
            
            if (idx >= 0)
            {
                this.label = path.substring (idx + 10);
                
                try
                {
                    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
                    factory.setNamespaceAware (true);
                    factory.setCoalescing (true);
                    factory.setIgnoringElementContentWhitespace (true);
                    
                    final DocumentBuilder parser = factory.newDocumentBuilder ();
                    // FIXME (?):
                    //      For security and portability, we require all configs to exist as real
                    //      files beneath the project root. This prevents any embedded application's
                    //      stored configs from getting used.
                    final Document xml = parser.parse (new File (context.getRealPath("/"), path.substring (idx)));
                    Node node = xml.getDocumentElement ();
                    
                    final String namespace = node.getNamespaceURI ();
                    if (! NAMESPACE.equals (namespace))
                    {
                        throw new ServletException ("config namespace not valid: " + namespace + " (should be: "+NAMESPACE+")");
                    }
                    
                    final String name = node.getNodeName ();
                    if (! "database".equals (name))
                    {
                        throw new ServletException ("expecting config root tag <database>, got: <"+name+">");
                    }
                    
                    final Map<Config,String> config = new HashMap<Config,String> ();
                    for (node = node.getFirstChild (); node != null; node = node.getNextSibling ()) try
                    {
                        if (NAMESPACE.equals (node.getNamespaceURI ()))
                        {
                            config.put (Config.valueOf (node.getLocalName ()), node.getTextContent ());
                        }
                    }
                    catch (final IllegalArgumentException e)
                    {
                        throw new ServletException (e);
                    }

                    if (config.get (Config.host) == null) throw new ServletException ("missing config entry required: "+Config.host);
                    if (config.get (Config.name) == null) throw new ServletException ("missing config entry required: "+Config.name);

                    configs.put (label, config);

                }
                catch (final ParserConfigurationException e)
                {
                    throw new ServletException (e);
                }
                catch (final SAXException e)
                {
                    throw new ServletException (e);
                }
                catch (final IOException e)
                {
                    throw new ServletException (e);
                }
            }
            else
            {
                throw new ServletException ("unsupported config path location: " + path);
            }
        }
        else // legacy behavior
        {
            if (configs.containsKey (null))
            {
                throw new ServletException ("only one servlet instantiation allowed at a time in legacy mode");
            }
            
            this.label = null;
        
            try
            {
                final Map<String,String> loaded = Utils.configMap (Transport.handler (CONFIG_URI, context, null).get ());
                final Map<Config,String> config = new HashMap<Config,String> ();
                
                config.put (Config.host, loaded.get (Config.host.key));
                config.put (Config.name, loaded.get (Config.name.key));
                config.put (Config.user, loaded.get (Config.user.key));
                config.put (Config.pass, loaded.get (Config.pass.key));

                if (config.get (Config.host) == null) throw new ServletException ("missing config entry required: "+Config.host);
                if (config.get (Config.name) == null) throw new ServletException ("missing config entry required: "+Config.name);
                
                configs.put (null, config);
            }
            catch (final IOException e)
            {
                throw new ServletException (e);
            }
        }
    }
    
    Connection getConnection () throws IOException, SQLException, ServletException
    {
        return getConnection (getServletContext (), label);
    }
    
    public static Connection getConnection (final ServletContext context, final String label) throws IOException, SQLException, ServletException
    {
        if (configs == null)
        {
            throw new ServletException ("servlet must be loaded first");
        }
        
        final Map<Config,String> config = configs.get (label);
        if (config == null)
        {
            throw new ServletException ("no config matching this label: "+ label);
        }
        
        final String uri = "jdbc:mysql://" + config.get (Config.host) + "/" + config.get (Config.name);
        
        return DriverManager.getConnection (uri, config.get (Config.user), config.get (Config.pass));
    }
    
    public void service (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        final String method = req.getMethod ();
        
        if ("PATCH".equalsIgnoreCase (method))
        {
            doPatch (req, resp);
        }
        else
        {
            super.service (req, resp);
        }
    }
    
    public void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        // FIXME: let this be a smarter check so we can use `exists` test with SQL records
        Utils.doHead (this, req, resp);
    }
    
    /**
     * Any anonymous arguments are taken to be column names to fetch.
     */
    public void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        final String path = req.getPathInfo ();
        final PrintWriter out = resp.getWriter ();
        final Map<String,List<String>> params = Utils.getParams (req.getQueryString ());
        final List<String> cols = params.get (null);
        final List<String> xsl = params.get (PARAM_XSL);
        
        final String table = new File (req.getServletPath ()).getName ();
        Log.debug ("table: "+table);
        
        try
        {
            final Connection conn = getConnection (); try
            {
                resp.setContentType ("text/xml");
                out.println ("<?xml version=\"1.0\"?>");
                if (xsl!=null) out.println ("<?xml-stylesheet href=\""+xsl.get (0)+"\" type=\"text/xsl\"?>");
                
                if (path==null)
                {
                    out.println ("<"+table+">");
                    
                    for (final ResultSet desc = conn.prepareStatement ("DESC `"+table+"`").executeQuery (); desc.next (); )
                    {
                        final String name = desc.getString (1);
                        boolean show = true;
                        
                        if (cols!=null && !cols.isEmpty ())
                        {
                            show = false;
                            
                            for (final String col : cols)
                            {
                                if (name.equals (col))
                                {
                                    show = true;
                                    break;
                                }
                            }
                        }
                        
                        if (show)
                        {
                            final String tag = Utils.tagify (name);
                            out.println ("    <"+tag+(tag!=null ? " tag-name-orig=\""+name+"\"" : "")+"/>");
                        }
                    }
                    
                    out.println ("</"+table+">");
                }
                else if ("/".equals (path))
                {
                    //out.println ("<"+table+"-list xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
                    out.println ("<"+table+"-list>");
                    
                    for (final ResultSet list = getList (conn, table, cols, params.get ("join"), params.get ("where"), params.get ("group"), params.get ("order"), params.get ("limit")); list.next (); )
                    {
                        final ResultSetMetaData meta = list.getMetaData ();
                        //out.println ("    <"+table+" xlink:href=\"./"+list.getObject (1)+"\">");
                        out.println ("    <"+table+">");
                        
                        for (int i=1; i <= meta.getColumnCount (); i++)
                        {
                            final String name = meta.getColumnLabel (i);
                            final String tag = Utils.tagify (name);
                            final Object val = list.getObject (i);
                            
                            out.print ("        <"+tag+" tag-name-orig=\""+name+"\"");

                            if (val==null)
                            {
                                out.println ("/>");
                            }
                            else
                            {
                                final List<String> follows = params.get ("follow");
                                boolean follow = false;

                                if (follows!=null)
                                {
                                    for (String s : follows)
                                    {
                                        if (name.equals (s))
                                        {
                                            follow = true;
                                            break;
                                        }
                                    }
                                }

                                if (follow)
                                {
                                    final ResultSet frow = getRow (conn, name, "/"+val.toString (), null);
                                    final ResultSetMetaData fmeta = frow.getMetaData ();
                                    
                                    if (! frow.next ())
                                    {
                                        Log.warn ("not found: "+val);
                                        out.println ("/>");
                                    }
                                    else if (! frow.isLast ())
                                    {
                                        Log.warn ("multiple results: "+val);
                                        out.println ("/>");
                                    }
                                    else
                                    {
                                        out.println (">");

                                        for (int fi=1; fi <= fmeta.getColumnCount (); fi++)
                                        {
                                            final String fname = fmeta.getColumnLabel (fi);
                                            final String ftag = Utils.tagify (fname);
                                            final Object fval = frow.getObject (fi);
                                            
                                            out.print ("        <"+ftag+" tag-name-orig=\""+fname+"\"");

                                            if (fval==null)
                                            {
                                                out.println ("/>");
                                            }
                                            else
                                            {
                                                out.println (">"+Utils.toXML (fval.toString ())+"</"+ftag+">");
                                            }
                                        }

                                        out.println ("    </"+tag+">");
                                    }
                                }
                                else
                                {
                                    out.println (">"+Utils.toXML (val.toString ())+"</"+tag+">");
                                }
                            }

                        }
                        
                        out.println ("    </"+table+">");
                    }

                    out.println ("</"+table+"-list>");
                }
                else
                {
                    displayRow (conn, table, path, params, resp);
                }
            }
            finally
            {
                conn.close ();
            }
        }
        catch (final SQLException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
        /*
        catch (final Exception e)
        {
            Log.error (e);
            for (StackTraceElement s : e.getStackTrace ())
            {
                Log.error (s.toString ());
            }
            throw new IOException (e);
        }
        */
    }
    
    void displayRow (final Connection conn, final String table, final String path, final Map<String,List<String>> params, final HttpServletResponse resp) throws IOException, SQLException
    {
        Log.info ("displaying row "+path+" for table "+table);
        final PrintWriter out = resp.getWriter ();
        final ResultSet row = getRow (conn, table, path, params.get ("id"));
        final ResultSetMetaData meta = row.getMetaData ();

        if (! row.next ())
        {
            Log.error ("not found: "+path);
            Status.set (resp, resp.SC_NOT_FOUND, "not found: "+path);
            return;
        }

        if (! row.isLast ())
        {
            Log.error ("multiple results: "+path);
            Status.set (resp, resp.SC_NOT_FOUND, "multiple results: "+path);
            return;
        }

        out.println ("<"+table+">");

        for (int i=1; i <= meta.getColumnCount (); i++)
        {
            final String name = meta.getColumnLabel (i);
            final String tag = Utils.tagify (name);
            final Object val = row.getObject (i);

            out.print ("    <"+tag+" tag-name-orig=\""+name+"\"");

            if (val==null)
            {
                out.println ("/>");
            }
            else
            {
                final List<String> follows = params.get ("follow");
                boolean follow = false;

                if (follows!=null)
                {
                    for (String s : follows)
                    {
                        if (name.equals (s))
                        {
                            follow = true;
                            break;
                        }
                    }
                }

                if (follow)
                {
                    final ResultSet frow = getRow (conn, name, "/"+val.toString (), null);
                    final ResultSetMetaData fmeta = frow.getMetaData ();

                    if (! frow.next ())
                    {
                        Log.warn ("not found: "+val);
                        out.println ("/>");
                    }
                    else if (! frow.isLast ())
                    {
                        Log.warn ("multiple results: "+val);
                        out.println ("/>");
                    }
                    else
                    {
                        out.println (">");

                        for (int fi=1; fi <= fmeta.getColumnCount (); fi++)
                        {
                            final String fname = fmeta.getColumnLabel (fi);
                            final String ftag = Utils.tagify (fname);
                            final Object fval = frow.getObject (fi);

                            out.print ("        <"+ftag+" tag-name-orig=\""+fname+"\"");

                            if (fval==null)
                            {
                                out.println ("/>");
                            }
                            else
                            {
                                out.println (">"+Utils.toXML (fval.toString ())+"</"+ftag+">");
                            }
                        }

                        out.println ("    </"+tag+">");
                    }
                }
                else
                {
                    out.println (">"+Utils.toXML (val.toString ())+"</"+tag+">");
                }
            }
        }

        out.println ("</"+table+">");
    }
    
    /*
     * FIXME: currently PUT is identical to PATCH
     */
    public void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        final String path = req.getPathInfo ();
        if (path==null)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "PUT to root location (no slash) not supported");
            return;
        }
        final Map<String,List<String>> params = Utils.getParams (req.getQueryString ());
        
        final InputStream in = req.getInputStream ();
        if (in==null)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "expecting XML data to create/update an entry");
            return;
        }
        
        final String table = new File (req.getServletPath ()).getName ();
        Log.debug ("table: "+table);
        
        try
        {
            final Document tree = DocumentBuilderFactory.newInstance ().newDocumentBuilder ().parse (in);
            
            final Element root = tree.getDocumentElement ();
            if (! table.equals (root.getNodeName ()))
            {
                throw new IllegalArgumentException ("expecting <"+table+">, got: "+root.getNodeName ());
            }

            final Map<String,String> data = new HashMap<String,String> ();
            for (Node node = root.getFirstChild (); node!=null; node = node.getNextSibling ())
            {
                if (Node.ELEMENT_NODE != node.getNodeType ()) continue;

                final Node child = node.getFirstChild ();
                Node col = node.getAttributes ().getNamedItem ("tag-name-orig");

                if (col!=null)
                {
                    data.put (col.getNodeValue (), child!=null ? child.getNodeValue () : null);
                }
                else
                {
                    data.put (node.getNodeName (), child!=null ? child.getNodeValue () : null);
                }
            }

            final Connection conn = getConnection (); try
            {
                if ("/".equals (path))
                {
                    if (params.get ("display")!=null)
                    {
                        Integer id = add (conn, table, data, true);

                        //resp.sendRedirect (String.valueOf (id));
                        
                        resp.getWriter ().println ("<?xml version=\"1.0\"?>");
                        displayRow (conn, table, "/"+id, params, resp);
                    }
                    else
                    {
                        add (conn, table, data);
                    }
                }
                else
                {
                    update (conn, table, path, params.get ("id"), data);
                    
                    if (params.get ("display")!=null)
                    {
                        resp.getWriter ().println ("<?xml version=\"1.0\"?>");
                        displayRow (conn, table, path, params, resp);
                    }
                }
            }
            finally
            {
                conn.close ();
            }
        }
        catch (final ParserConfigurationException e)
        {
            Log.error (e);
            throw new ServletException (e);
        }
        catch (final SAXException e)
        {
            Log.error (e);
            throw new ServletException (e);
        }
        catch (final IllegalArgumentException e)
        {
            Log.error (e);
            Status.set (resp, resp.SC_BAD_REQUEST, "bad data: "+e);
            return;
        }
        catch (final SQLException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
    }
    
    /*
     * FIXME: currently PATCH is identical to PUT
     */
    public void doPatch (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        Log.info ("PATCH");
        final Map<String,List<String>> params = Utils.getParams (req.getQueryString ());
        
        final String path = req.getPathInfo ();
        if (path==null)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "PATCH to root location (no slash) not supported");
            return;
        }
        
        final InputStream in = req.getInputStream ();
        if (in==null)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "expecting XML data to create/update an entry");
            return;
        }
        
        final String table = new File (req.getServletPath ()).getName ();
        Log.debug ("table: "+table);
        
        try
        {
            //resp.getWriter ().println ("XXX: "+new BufferedReader (new InputStreamReader (in)).readLine ());
            //if (true) return;
            
            final Document tree = DocumentBuilderFactory.newInstance ().newDocumentBuilder ().parse (in);
            
            final Element root = tree.getDocumentElement ();
            if (! table.equals (root.getNodeName ()))
            {
                throw new IllegalArgumentException ("expecting <"+table+">, got: "+root.getNodeName ());
            }
            
            final Map<String,String> data = new HashMap<String,String> ();
            for (Node node = root.getFirstChild (); node!=null; node = node.getNextSibling ())
            {
                if (Node.ELEMENT_NODE != node.getNodeType ()) continue;
                
                final Node child = node.getFirstChild ();
                Node col = node.getAttributes ().getNamedItem ("tag-name-orig");
                
                if (col!=null)
                {
                    data.put (col.getNodeValue (), child!=null ? child.getNodeValue () : null);
                }
                else
                {
                    data.put (node.getNodeName (), child!=null ? child.getNodeValue () : null);
                }
            }
            
            final Connection conn = getConnection (); try
            {
                if ("/".equals (path))
                {
                    add (conn, table, data);
                }
                else
                {
                    update (conn, table, path, params.get ("id"), data);
                }
            }
            finally
            {
                conn.close ();
            }
        }
        catch (final ParserConfigurationException e)
        {
            Log.error (e);
            throw new ServletException (e);
        }
        catch (final SAXException e)
        {
            Log.error (e);
            throw new ServletException (e);
        }
        catch (final IllegalArgumentException e)
        {
            Log.error (e);
            Status.set (resp, resp.SC_BAD_REQUEST, "bad data: "+e);
            return;
        }
        catch (final SQLException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
    }
    
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        Log.info ("POST");
        final Map<String,List<String>> params = Utils.getParams (req.getQueryString ());
        
        final String path = req.getPathInfo ();
        if (path==null)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "POST to root location (no slash) not supported");
            return;
        }
        
        final InputStream in = req.getInputStream ();
        if (in==null)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "expecting XML data for bulk update");
            return;
        }
        
        if (! "/".equals (path))
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "POST to slash required for bulk update");
            return;
        }
                
        final String table = new File (req.getServletPath ()).getName ();
        Log.debug ("table: "+table);
        
        try
        {
            final Document tree = DocumentBuilderFactory.newInstance ().newDocumentBuilder ().parse (in);

            final Element root = tree.getDocumentElement ();
            final NodeList updates = root.getChildNodes ();

            final Connection conn = getConnection (); try
            {
                for (int u=0; u<updates.getLength (); u++)
                {
                    final Node update = updates.item (u);

                    if (! table.equals (update.getNodeName ()))
                    {
                        throw new IllegalArgumentException ("bulk update: expecting <"+table+">, got: "+update.getNodeName ());
                    }

                    final Node upath = update.getAttributes ().getNamedItem ("path");
                    if (upath==null)
                    {
                        throw new IllegalArgumentException ("expecting path attribute for bulk update item");
                    }
                    else if (upath.getNodeValue ()==null || upath.getNodeValue ().length ()<2 || ! upath.getNodeValue ().startsWith ("/"))
                    {
                        throw new IllegalArgumentException ("path attribute must begin with a slash");
                    }

                    final Map<String,String> data = new HashMap<String,String> ();
                    for (Node node = update.getFirstChild (); node!=null; node = node.getNextSibling ())
                    {
                        if (Node.ELEMENT_NODE != node.getNodeType ()) continue;

                        final Node child = node.getFirstChild ();
                        Node col = node.getAttributes ().getNamedItem ("tag-name-orig");

                        if (col!=null)
                        {
                            data.put (col.getNodeValue (), child!=null ? child.getNodeValue () : null);
                        }
                        else
                        {
                            data.put (node.getNodeName (), child!=null ? child.getNodeValue () : null);
                        }
                    }

                    update (conn, table, upath.getNodeValue (), params.get ("id"), data);
                }
            }
            finally
            {
                conn.close ();
            }
        }
        catch (final ParserConfigurationException e)
        {
            Log.error (e);
            throw new ServletException (e);
        }
        catch (final SAXException e)
        {
            Log.error (e);
            throw new ServletException (e);
        }
        catch (final IllegalArgumentException e)
        {
            Log.error (e);
            Status.set (resp, resp.SC_BAD_REQUEST, "bad data: "+e);
            return;
        }
        catch (final SQLException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
    }
    
    public void doDelete (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        final String path = req.getPathInfo ();
        final Map<String,List<String>> params = Utils.getParams (req.getQueryString ());
        final List<String> xsl = params.get (PARAM_XSL);
        
        final String table = new File (req.getServletPath ()).getName ();
        Log.debug ("table: "+table);
        
        if (path==null)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "DELETE to root location not supported");
            return;
        }
        
        try
        {
            final Connection conn = getConnection (); try
            {
                if ("/".equals (path))
                {
                    final StringBuilder stmt = new StringBuilder ("DELETE");
                    
                    final List<String> tables = params.get (null);
                    if (tables != null && ! tables.isEmpty ())
                    {
                        String sep = " ";
                        for (final String name : tables)
                        {
                            stmt.append (sep).append ('`').append (name).append ('`');
                            sep = ", ";
                        }
                    }
                    
                    stmt.append (" FROM `").append (table).append ("`");
        
                    final List<String> join = params.get ("join");
                    if (join!=null && ! join.isEmpty ())
                    {
                        for (final String clause : join)
                        {
                            stmt.append (" LEFT JOIN ").append (clause);
                        }
                    }

                    final List<String> where = params.get ("where");
                    if (where!=null && ! where.isEmpty ())
                    {
                        stmt.append (" WHERE ");
                        String sep = "";

                        for (final String clause : where)
                        {
                            stmt.append (sep).append (clause);
                            sep = " AND ";
                        }
                    }
                    
                    Log.debug ("delete statement: "+stmt);
                    final int count = conn.prepareStatement (stmt.toString ()).executeUpdate ();
                    // FIXME: do something with delete count
                }
                else
                {
                    final int count = delete (conn, table, path, params.get ("id"));

                    if (count!=1)
                    {
                        Log.error ("bad update count: "+count);
                        Status.set (resp, resp.SC_BAD_REQUEST, "bad upate count: "+count);
                        return;
                    }
                }
            }
            finally
            {
                conn.close ();
            }
        }
        catch (final SQLException e)
        {
            Log.error (e);
            throw new IOException (e);
        }
    }
    
    protected ResultSet getList (final Connection conn, final String table, final List<String> cols, final List<String> join, final List<String> where, final List<String> group, final List<String> order, final List<String> limit) throws SQLException
    {
        final StringBuffer stmt = new StringBuffer ("SELECT ");
        
        if (cols!=null && !cols.isEmpty ())
        {
            if (cols.size ()==1 && "*".equals (cols.get (0)))
            {
                stmt.append ("*");
            }
            else
            {
                String sep = "";
                
                for (final String col : cols)
                {
                    // FIXME: this is all so hacky and brittle...
                    
                    if (col.indexOf ("(")>0 || col.indexOf (" AS ")>0)
                    {
                        stmt.append (sep).append (col);
                    }
                    else
                    {
                        final int dot = col.indexOf ('.');
                        
                        if (dot>0)
                        {
                            /*
                            stmt.append (sep).append (col);
                            */
                            
                            stmt.append (sep).append (col.substring(0, dot+1));
                            stmt.append ('`').append (col.substring (dot+1)).append ('`');
                        }
                        else
                        {
                            stmt.append (sep).append ('`').append (col).append ('`');
                        }
                    }
                    
                    sep = ", ";
                }
            }
        }
        else
        {
            stmt.append ("id");
        }
        
        stmt.append (" FROM `").append (table).append ("`");
        
        if (join!=null && ! join.isEmpty ())
        {
            for (final String clause : join)
            {
                stmt.append (" LEFT JOIN ").append (clause);
            }
        }
        
        if (where!=null && ! where.isEmpty ())
        {
            stmt.append (" WHERE ");
            String sep = "";
            
            for (final String clause : where)
            {
                stmt.append (sep).append (clause);
                sep = " AND ";
            }
        }
        
        if (group!=null && ! group.isEmpty ())
        {
            stmt.append (" GROUP BY ");
            String sep = "";
            
            for (final String clause : group)
            {
                stmt.append (sep).append (clause);
                sep = ", ";
            }
        }
        
        if (order!=null && ! order.isEmpty ())
        {
            stmt.append (" ORDER BY ");
            String sep = "";
            
            for (final String clause : order)
            {
                stmt.append (sep).append (clause);
                sep = ", ";
            }
        }
        
        if (limit!=null && ! limit.isEmpty ())
        {
            stmt.append (" LIMIT ").append (limit.get (0));
        }
        
        //Log.fatal (stmt.toString ());
        return conn.prepareStatement (stmt.toString ()).executeQuery ();
    }
    
    protected ResultSet getRow (final Connection conn, final String table, final String idPath, final List<String> idCols) throws SQLException
    {
        final StringBuffer query = new StringBuffer ("SELECT * FROM `"+table+"` WHERE");
        String sep = " ";
        
        if (idCols==null || idCols.isEmpty ())
        {
            query.append (sep).append ("id = ?");
        }
        else for (final String idCol : idCols)
        {
            // FIXME: this isn't fully supported
            query.append (sep).append (idCol).append (" = ?");
            sep = " AND ";
        }
        
        Log.info ("getRow ["+idPath.substring (1)+"]: "+query);

        final PreparedStatement stmt = conn.prepareStatement (query.toString ());
        // FIXME: multiple id columns not supported yet (use additional /id path components?)
        stmt.setObject (1, idPath.substring (1));
        return stmt.executeQuery ();
    }
    
    protected void add (final Connection conn, final String table, final Map<String,String> data) throws SQLException
    {
        add (conn, table, data, false);
    }
    
    protected Integer add (final Connection conn, final String table, final Map<String,String> data, final boolean getId) throws SQLException
    {
        final StringBuffer sb = new StringBuffer ("INSERT INTO `"+table+"` (");
        String sep = "";
        
        for (final String key : data.keySet ())
        {
            sb.append (sep).append ('`').append (key).append ("`");
            sep = ",";
        }
        
        sb.append (") VALUES (");
        sep = "";
        
        for (int i=0; i < data.size (); i++)
        {
            sb.append (sep).append ("?");
            sep = ",";
        }
        
        sb.append (")");
        
        PreparedStatement stmt = conn.prepareStatement (sb.toString ());
        int col = 0;
        
        for (final String key : data.keySet ())
        {
            stmt.setObject (++col, data.get (key));
        }
        
        final int count = stmt.executeUpdate ();
        
        if (count!=1)
        {
            throw new SQLException ("unexpected update count for row add: "+count);
        }
        
        if (getId)
        {
            // FIXME: this is dangerous and not safe for concurrency
            // (should be in a single transaction with the insert)
            stmt = conn.prepareStatement ("SELECT MAX(id) FROM `"+table+"`");
            
            final ResultSet id = stmt.executeQuery ();
            
            if (id.first () && id.isLast ())
            {
                return id.getInt (1);
            }
            else
            {
                throw new SQLException ("zero or multiple rows returned for MAX(id)");
            }
        }
        else
        {
            return null;
        }
    }
    
    protected void update (final Connection conn, final String table, final String idPath, final List<String> idCols, final Map<String,String> data) throws SQLException
    {
        final StringBuffer sb = new StringBuffer ("UPDATE `"+table+"` SET ");
        String sep = "";
        
        if (data.isEmpty ())
        {
            Log.warn ("no data for update: " + idPath.substring(1));
            return;
        }
        
        for (final String key : data.keySet ())
        {
            sb.append (sep).append ('`').append (key).append ("`=?");
            sep = ",";
        }
        
        sep = " WHERE ";
        if (idCols==null || idCols.isEmpty ())
        {
            sb.append (sep).append ("id = ?");
        }
        else for (final String idCol : idCols)
        {
            sb.append (sep).append (idCol).append (" = ?");
            sep = " AND ";
        }
        
        Log.info ("update ["+idPath.substring (1)+"]: "+sb);
        final PreparedStatement stmt = conn.prepareStatement (sb.toString ());
        int col = 0;
        
        for (final String key : data.keySet ())
        {
            stmt.setObject (++col, data.get (key));
        }
        
        // FIXME: this doesn't handle multiple id columns
        stmt.setObject (++col, idPath.substring (1));
        
        stmt.executeUpdate ();
    }
    
    protected int delete (final Connection conn, final String table, final String idPath, final List<String> idCols) throws SQLException
    {
        final StringBuffer sb = new StringBuffer ("DELETE FROM `"+table+"`");
        String sep = " WHERE ";

        if (idCols==null || idCols.isEmpty ())
        {
            sb.append (sep).append ("id = ?");
        }
        else for (final String idCol : idCols)
        {
            sb.append (sep).append (idCol).append (" = ?");
            sep = " AND ";
        }
        
        final PreparedStatement stmt = conn.prepareStatement (sb.toString ());
        stmt.setObject (1, idPath.substring (1));
        return stmt.executeUpdate();
    }
}
