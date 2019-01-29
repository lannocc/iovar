/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2012-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.ant;

// local imports:

// java imports:
import java.io.*;
import java.nio.file.*;
import java.util.*;

// 3rd-party imports:
import org.apache.tools.ant.*;
//import org.apache.tools.ant.types.Reference;

/**
 * Take a web.xml "stub" and insert servlet definitions by looking at the filesystem for symbolic
 * links to .class files. The web.xml stub file should contain an XML comment with "[iovar]" to
 * indicate the insertion point.
 *
 * @author  shawn@lannocc.com
 * 
 * @deprecated  Replaced by the Servlets task and improvements to the build.xml script.
 */
public class WebBuilder extends Task
{
    File src;
    File dst;
    File dir;
    File include;
    File links;
    Integer startupBegin;
    Integer sqlStart;
    String excludeDirs;
    //Reference classpathRef;
    String projectKey;
    
    public void setSrc (final File src)
    {
        this.src = src;
    }
    
    public void setDst (final File dst)
    {
        this.dst = dst;
    }
    
    public void setDir (final File dir)
    {
        this.dir = dir;
    }

    public void setInclude (final File include)
    {
        this.include = include;
    }
    
    public void setLinks (final File links)
    {
        this.links = links;
    }
    
    public void setStartupBegin (final Integer startupBegin)
    {
        this.startupBegin = startupBegin;
    }
    
    public void setSqlStart (final Integer sqlStart)
    {
        this.sqlStart = sqlStart;
    }
    
    /**
     * list of directories, relative to dir (root), separated by colons, e.g.:
     * "pub:tmp:var/cache"
     */
    public void setExcludeDirs (final String excludeDirs)
    {
        this.excludeDirs = excludeDirs;
    }
    
    /**
     * Classpath reference to use when checking to see if links extend Servlet class.
     */
    /*
    public void setClasspathRef (final Reference classpathRef)
    {
        this.classpathRef = classpathRef;
    }
    */
    
    /**
     * A unique identifier string prepended to the mysql servlet names so we can avoid conflict when
     * an inherited IOVAR application references the same /dev/mysql/ config file.
     */
    public void setProjectKey (final String projectKey)
    {
        this.projectKey = projectKey;
    }
    
    
    void validateAttributes () throws BuildException, IOException
    {
        if (src==null) throw new BuildException ("Must specify source web.xml file with 'src' attribute");
        if (! (src.exists () && src.isFile ())) throw new BuildException ("Source web.xml does not exist or is not a file: "+src.getCanonicalPath ());
        if (dst==null) throw new BuildException ("Must specify destination web.xml file with 'dst' attribute");
        if (dir==null) throw new BuildException ("Must specify root directory to search with 'dir' attribute");
        if (! (dir.exists () && dir.isDirectory ())) throw new BuildException ("Search root does not exist or is not a directory: "+dir.getCanonicalPath ());
        if (include!=null && ! (include.exists () && (include.isFile () || include.isDirectory()))) throw new BuildException ("Include file does not exist or is not a file/directory: "+include.getCanonicalPath ());
        if (links!=null && links.exists () && !links.isFile ()) throw new BuildException ("Links target exists but is not a file: "+links.getCanonicalPath ());
        if (startupBegin!=null && startupBegin<0) throw new BuildException ("Startup begin parameter must be positive");
        if (sqlStart!=null && sqlStart<0) throw new BuildException ("sqlStart parameter must be positive");
        if (projectKey==null) projectKey = "";
    }
    
    public void execute () throws BuildException
    {
        final int rootln = dir.getAbsolutePath ().length ();
        try
        {
            final Map<File,String> iovar = findLinks ();
            final BufferedReader in = new BufferedReader (new FileReader (src));
            final BufferedWriter out = new BufferedWriter (new FileWriter (dst));
            
            for (String line; (line = in.readLine ())!=null; )
            {
                if (line.contains ("[iovar]"))
                {
                    if (include!=null)
                    {
                        if (include.isFile ())
                        {
                            includeFile (include, out);
                        }
                        else if (include.isDirectory ())
                        {
                            for (final File file : include.listFiles ())
                            {
                                includeFile (file, out);
                            }
                        }
                        else
                        {
                            throw new IOException ("not a file or directory: " + include);
                        }
                    }

                    out.write ("    <!-- iovar auto-generation begin -->");
                    out.newLine ();
                    
                    final Set<String> servlets = new HashSet<String> ();

                    final BufferedWriter lout;
                    if (links!=null) lout = new BufferedWriter (new FileWriter (links));
                    else lout = null;
                    
                    if (iovar!=null) for (final Iterator<Map.Entry<File,String>> links = iovar.entrySet ().iterator (); links.hasNext (); )
                    {
                        final Map.Entry<File,String> entry = links.next ();
                        // FIXME: make this more robust
                        final File file = entry.getKey ().getAbsoluteFile ();
                        final String url = file.getPath ().substring (rootln);
                        boolean check = true;
                        String clname = entry.getValue ();
                        if (clname.startsWith (":"))
                        {
                            clname = clname.substring (1);
                            check = false;
                        }
                        
                        //try
                        {
                            final int mysql = clname.indexOf ("dev/mysql/");
                            
                            if (mysql >= 0)
                            {
                                if (! servlets.contains (clname))
                                {
                                    String config = new File (file, "../" + clname).getCanonicalPath ();
                                    
                                    // FIXME: store config path relative to project root
                                    //      (facilitates app embedded and running on different systems)
                                    
                                    out.write ("    <servlet>");
                                    out.write ("<servlet-name>" + projectKey + ":" + clname + "</servlet-name>");
                                    out.write ("<servlet-class>net.iovar.web.dev.MySql</servlet-class>");
                                    if (sqlStart!=null)
                                    {
                                        out.write ("<load-on-startup>" + sqlStart + "</load-on-startup>");
                                        sqlStart++;
                                    }
                                    out.write ("<init-param><param-name>config</param-name><param-value>" + config + "</param-value></init-param>");
                                    out.write ("</servlet>");
                                    out.newLine ();

                                    if (lout!=null)
                                    {
                                        lout.write ("    <servlet>");
                                        lout.write ("<servlet-name>" + projectKey + ":" + clname + "</servlet-name>");
                                        lout.write ("<servlet-class>net.iovar.web.dev.MySql</servlet-class>");
                                        if (sqlStart!=null)
                                        {
                                            lout.write ("<load-on-startup>" + sqlStart + "</load-on-startup>");
                                            sqlStart++;
                                        }
                                        lout.write ("<init-param><param-name>config</param-name><param-value>" + config + "</param-value></init-param>");
                                        lout.write ("</servlet>");
                                        lout.newLine ();
                                    }
                                    
                                    servlets.add (clname);
                                }

                                out.write ("    <servlet-mapping>");
                                out.write ("<servlet-name>"+projectKey + ":" + clname+"</servlet-name>");
                                out.write ("<url-pattern>"+url+"</url-pattern>");
                                out.write ("</servlet-mapping>");
                                out.newLine ();

                                out.write ("    <servlet-mapping>");
                                out.write ("<servlet-name>"+projectKey + ":" + clname+"</servlet-name>");
                                out.write ("<url-pattern>"+url+"/*</url-pattern>");
                                out.write ("</servlet-mapping>");
                                out.newLine ();
                                
                                if (lout != null)
                                {
                                    lout.write ("    <servlet-mapping>");
                                    lout.write ("<servlet-name>"+projectKey + ":" + clname+"</servlet-name>");
                                    lout.write ("<url-pattern>"+url+"</url-pattern>");
                                    lout.write ("</servlet-mapping>");
                                    lout.newLine ();

                                    lout.write ("    <servlet-mapping>");
                                    lout.write ("<servlet-name>"+projectKey + ":" + clname+"</servlet-name>");
                                    lout.write ("<url-pattern>"+url+"/*</url-pattern>");
                                    lout.write ("</servlet-mapping>");
                                    lout.newLine ();
                                }
                            }
                            else if (!check || true /* Servlet.class.isAssignableFrom (Class.forName (clname))*/)
                                // It's too cumbersome to try checking for servlet inheritence here...
                                //  because the class we're checking may not be in our current classpath.
                            {
                                if (! servlets.contains (clname))
                                {
                                    out.write ("    <servlet>");
                                    out.write ("<servlet-name>"+clname+"</servlet-name>");
                                    out.write ("<servlet-class>"+clname+"</servlet-class>");
                                    if (startupBegin!=null)
                                    {
                                        out.write ("<load-on-startup>"+startupBegin+"</load-on-startup>");
                                        startupBegin++;
                                    }
                                    out.write ("</servlet>");
                                    out.newLine ();

                                    if (lout!=null)
                                    {
                                        lout.write ("    <servlet>");
                                        lout.write ("<servlet-name>"+clname+"</servlet-name>");
                                        lout.write ("<servlet-class>"+clname+"</servlet-class>");
                                        if (startupBegin!=null) out.write ("<load-on-startup>"+startupBegin+"</load-on-startup>");
                                        lout.write ("</servlet>");
                                        lout.newLine ();
                                    }
                                    
                                    servlets.add (clname);
                                }

                                out.write ("    <servlet-mapping>");
                                out.write ("<servlet-name>"+clname+"</servlet-name>");
                                out.write ("<url-pattern>"+url+"</url-pattern>");
                                out.write ("</servlet-mapping>");
                                out.newLine ();

                                out.write ("    <servlet-mapping>");
                                out.write ("<servlet-name>"+clname+"</servlet-name>");
                                out.write ("<url-pattern>"+url+"/*</url-pattern>");
                                out.write ("</servlet-mapping>");
                                out.newLine ();

                                if (lout!=null)
                                {
                                    lout.write ("    <servlet-mapping>");
                                    lout.write ("<servlet-name>"+clname+"</servlet-name>");
                                    lout.write ("<url-pattern>"+url+"</url-pattern>");
                                    lout.write ("</servlet-mapping>");
                                    lout.newLine ();

                                    lout.write ("    <servlet-mapping>");
                                    lout.write ("<servlet-name>"+clname+"</servlet-name>");
                                    lout.write ("<url-pattern>"+url+"/*</url-pattern>");
                                    lout.write ("</servlet-mapping>");
                                    lout.newLine ();
                                }
                            }
                            else
                            {
                                log ("(skipping) not a servlet class: "+clname);
                            }
                        }
                        /*
                        catch (final ClassNotFoundException e)
                        {
                            log ("class not found: "+clname, Project.MSG_ERR);
                        }
                        */
                    }
                    else
                    {
                        out.write ("    <!-- no iovar links found -->");
                        out.newLine ();
                    }
                    
                    out.write ("    <!-- iovar auto-generation end -->");
                    out.newLine ();

                    if (lout!=null)
                    {
                        lout.close ();
                    }
                }
                else
                {
                    out.write (line);
                    out.newLine ();
                }
            }
            
            in.close ();
            out.close ();
        }
        catch (final IOException e)
        {
            throw new BuildException ("I/O building web-app definition file", e);
        }
        
        log ("web-app definition generated at: "+dst);
    }
    
    static void includeFile (final File file, final BufferedWriter out) throws IOException
    {
        out.write ("    <!-- iovar include begin ("+file+") -->");
        out.newLine ();

        final BufferedReader iin = new BufferedReader (new FileReader (file));
        for (String iline; (iline = iin.readLine ())!=null; )
        {
            out.write (iline);
            out.newLine ();
        }

        out.write ("    <!-- iovar include end -->");
        out.newLine ();
        out.newLine ();    }
    
    Map<File,String> findLinks () throws IOException
    {
        log ("scanning for symbolic links to .class files...");
        return findLinks (dir);
    }
    
    Map<File,String> findLinks (final File start) throws IOException
    {
        final File files[] = start.listFiles ();
        if (files==null) return null;

        final List<File> sorted = Arrays.asList (files);
        Collections.sort (sorted);
        
        Map<File,String> links = null;
        
        file: for (final File file : sorted)
        {
            if (file.isDirectory ())
            {
                if (excludeDirs!=null && ! excludeDirs.isEmpty ())
                {
                    for (final StringTokenizer excludes = new StringTokenizer (excludeDirs, ":"); excludes.hasMoreTokens (); )
                    {
                        final String exclude = excludes.nextToken ();
                        if (file.getCanonicalFile ().equals (new File (dir, exclude).getCanonicalFile ()))
                        {
                            // current file (directory) matches one of our exclude directories!
                            continue file;
                        }
                    }
                }
                
                final Map<File,String> sublinks = findLinks (file);
                
                if (sublinks!=null)
                {
                    if (links==null) links = sublinks;
                    else links.putAll (sublinks);
                }
            }
            else
            {
                final Path path = file.toPath ();
                
                if (Files.isSymbolicLink (path))
                {
                    if (file.exists ())
                    {
                        final Path target = Files.readSymbolicLink (path);
                        final String name = target.toString ();

                        /*
                        if (target.getRoot ()==null && name.startsWith ("iovar:"))
                        {
                            final String clname = name.substring (7);

                            if (links==null) links = new HashMap<File,String> ();
                            links.put (file, clname);
                        }
                         */

                        // FIXME: more robust path->class name translation
                        if (target.getRoot ()==null)
                        {
                            int idx;

                            if ((idx = name.indexOf ("WEB-INF/classes/")) >= 0)
                            {
                                final boolean real = name.endsWith (".class");
                                final String clname = (real ? "" : ":") + name.substring (idx+16, name.length () - (real ? 6 : 0)).replaceAll ("/", ".")/*.replaceAll ("\\$", ".")*/;

                                if (links==null) links = new HashMap<File,String> ();
                                links.put (file, clname);
                            }
                            else if ((idx = name.indexOf ("dev/mysql/")) >= 0)
                            {
                                if (links==null) links = new HashMap<File,String> ();
                                links.put (file, name);
                            }
                            else
                            {
                                log ("link to unsupported location: "+name, Project.MSG_ERR);
                            }
                        }
                    }
                    else
                    {
                        log ("broken link: "+file, Project.MSG_WARN);
                    }
                }
            }
        }
        
        return links;
    }
}
