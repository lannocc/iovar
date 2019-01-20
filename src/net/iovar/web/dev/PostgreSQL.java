/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013 Lannocc Technologies
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
public class PostgreSQL extends HttpServlet
{
    final static String CONFIG_URI = "local:/etc/postgresql.conf";
    
    public static final String PARAM_XSL = "xsl";
    
    enum Config
    {
        HOST ("host"),
        USER ("user"),
        PASSWORD ("password"),
        DATABASE ("database");
        
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
        try
        {
            Class.forName ("org.postgresql.Driver").newInstance ();
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
    }
    
    public void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        
    }
    
    /**
     * Any anonymous arguments are taken to be column names to fetch.
     */
    public void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
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
                if (path==null || "/".equals (path))
                {
                    resp.setContentType ("text/xml");
                    out.println ("<?xml version=\"1.0\"?>");
                    if (xsl!=null) out.println ("<?xml-stylesheet href=\""+xsl.get (0)+"\" type=\"text/xsl\"?>");
                    out.println ("<"+table+"-list xmlns:xlink=\"http://www.w3.org/1999/xlink\">");

                    for (final ResultSet list = getList (conn, table, cols); list.next (); )
                    {
                        final ResultSetMetaData meta = list.getMetaData ();
                        out.println ("    <"+table+" xlink:href=\"./"+list.getObject (1)+"\">");
                        
                        for (int i=1; i <= meta.getColumnCount (); i++)
                        {
                            final String name = list.getMetaData ().getColumnName (i);
                            out.println ("        <"+name+">"+list.getObject (i)+"</"+name+">");
                        }
                        
                        out.println ("    </"+table+">");
                    }

                    out.println ("</"+table+"-list>");
                }
                else
                {
                    //final ResultSet row = getRow (conn, mapId (path));
                    final ResultSet row = getRow (conn, table, path);
                    final ResultSetMetaData meta = row.getMetaData ();
                    
                    if (! row.next ())
                    {
                        Status.set (resp, resp.SC_NOT_FOUND, "not found: "+path);
                        return;
                    }
                    
                    if (! row.isLast ())
                    {
                        Status.set (resp, resp.SC_NOT_FOUND, "multiple results: "+path);
                        return;
                    }
                    
                    resp.setContentType ("text/xml");
                    out.println ("<?xml version=\"1.0\"?>");
                    if (xsl!=null) out.println ("<?xml-stylesheet href=\""+xsl.get (0)+"\" type=\"text/xsl\"?>");
                    out.println ("<"+table+">");
                    
                    for (int i=1; i <= meta.getColumnCount (); i++)
                    {
                        final String name = meta.getColumnName (i);
                        out.println ("    <"+name+">"+row.getObject (i)+"</"+name+">");
                    }
                    
                    out.println ("</"+table+">");
                }
            }
            finally
            {
                conn.close ();
            }
        }
        catch (final SQLException e)
        {
            throw new IOException (e);
        }
    }
    
    public void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        final String path = req.getPathInfo ();
        if (path==null || "/".equals (path))
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "PUT to root location not supported");
            return;
        }
        
        final InputStream in = req.getInputStream ();
        if (in==null)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "expecting XML POST data to update an entry");
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
                data.put (node.getNodeName (), child!=null ? child.getNodeValue () : null);
            }
            
            final Connection conn = getConnection (); try
            {
                update (conn, table, path, data);
            }
            finally
            {
                conn.close ();
            }
        }
        catch (final ParserConfigurationException e)
        {
            throw new ServletException (e);
        }
        catch (final SAXException e)
        {
            throw new ServletException (e);
        }
        catch (final IllegalArgumentException e)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "bad data: "+e);
            return;
        }
        catch (final SQLException e)
        {
            throw new IOException (e);
        }
    }
    
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        final InputStream in = req.getInputStream ();
        if (in==null)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "expecting XML POST data to create an entry");
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
                data.put (node.getNodeName (), child!=null ? child.getNodeValue () : null);
            }
            
            final Connection conn = getConnection (); try
            {
                add (conn, table, data);
            }
            finally
            {
                conn.close ();
            }
        }
        catch (final ParserConfigurationException e)
        {
            throw new ServletException (e);
        }
        catch (final SAXException e)
        {
            throw new ServletException (e);
        }
        catch (final IllegalArgumentException e)
        {
            Status.set (resp, resp.SC_BAD_REQUEST, "bad data: "+e);
            return;
        }
        catch (final SQLException e)
        {
            throw new IOException (e);
        }
    }
    
    Connection getConnection () throws IOException, SQLException
    {
        final Map<String,String> config = Utils.configMap (Transport.handler (CONFIG_URI, getServletContext (), null).get ());
        
        final String host = config.get (Config.HOST.key);
        if (host==null) throw new IllegalArgumentException ("missing config entry required: "+Config.HOST);
        final String database = config.get (Config.DATABASE.key);
        if (database==null) throw new IllegalArgumentException ("missing config entry required: "+Config.DATABASE);
        final String user = config.get (Config.USER.key);
        final String pass = config.get (Config.PASSWORD.key);
        
        return DriverManager.getConnection ("jdbc:postgresql://"+host+"/"+database, user, pass);
    }
    
    /*
    String mapId (final String path)
    {
        // FIXME: make more robust!
        return path.substring (root.length ());
    }
     */
    
    protected ResultSet getList (final Connection conn, final String table, final List<String> cols) throws SQLException
    {
        if (cols!=null && !cols.isEmpty ())
        {
            final StringBuffer stmt = new StringBuffer ("SELECT ");
            String sep = "";
            
            for (final String col : cols) {
                stmt.append (sep).append (col);
                sep = ", ";
            }
            stmt.append (" FROM ").append (table);
            
            return conn.prepareStatement (stmt.toString ()).executeQuery ();
        }
        else
        {
            return conn.prepareStatement ("SELECT id FROM "+table).executeQuery ();
        }
    }
    
    protected ResultSet getRow (final Connection conn, final String table, final String idPath) throws SQLException
    {
        final PreparedStatement stmt = conn.prepareStatement ("SELECT * FROM "+table+" WHERE id = ?");
        //stmt.setObject (1, idPath.substring (1));
        stmt.setInt (1, Integer.parseInt (idPath.substring (1)));
        return stmt.executeQuery ();
    }
    
    protected void add (final Connection conn, final String table, final Map<String,String> data) throws SQLException
    {
        final StringBuffer sb = new StringBuffer ("INSERT INTO "+table+" (");
        String sep = "";
        
        for (final String key : data.keySet ())
        {
            sb.append (sep).append (key);
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
        
        final PreparedStatement stmt = conn.prepareStatement (sb.toString ());
        int col = 0;
        
        for (final String key : data.keySet ())
        {
            stmt.setObject (++col, data.get (key));
        }
        
        stmt.executeUpdate ();
    }
    
    protected void update (final Connection conn, final String table, final String idPath, final Map<String,String> data) throws SQLException
    {
        final StringBuffer sb = new StringBuffer ("UPDATE "+table+" SET ");
        String sep = "";
        
        for (final String key : data.keySet ())
        {
            sb.append (sep).append (key).append ("=?");
            sep = ",";
        }
        
        sb.append (" WHERE id=?");
        
        final PreparedStatement stmt = conn.prepareStatement (sb.toString ());
        int col = 0;
        
        for (final String key : data.keySet ())
        {
            stmt.setObject (++col, data.get (key));
        }
        
        stmt.setObject (++col, idPath.substring (1));
        
        stmt.executeUpdate ();
    }
}
