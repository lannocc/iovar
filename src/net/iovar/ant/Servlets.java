/*
 * Copyright (C) 2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.ant;

// local imports:

// java imports:
import java.io.*;
import java.util.*;

// 3rd-party imports:
import org.apache.tools.ant.*;
//import org.apache.tools.ant.types.Reference;

/**
 * Produce servlet definitions and servlet-mapping entries that would make up part of a web.xml file.
 * The servlet mappings are found by scanning the files representing symbolic links in the provided
 * directory for links to WEB-INF/classes/**.class files.
 *
 * @author  shawn@lannocc.com
 */
public class Servlets extends Task
{
    File tofile;
    File syslinkdir;
    File sysdevdir;
    
    public void setTofile (final File tofile)
    {
        this.tofile = tofile;
    }
    
    public void setSyslinkdir (final File syslinkdir)
    {
        this.syslinkdir = syslinkdir;
    }
    
    public void setSysdevdir (final File sysdevdir)
    {
        this.sysdevdir = sysdevdir;
    }
    
    void validateAttributes () throws BuildException, IOException
    {
        if (tofile==null) throw new BuildException ("Must specify destination web.xml stub file with 'toFile' attribute");
        
        if (syslinkdir==null) throw new BuildException ("Must specify root directory to search with 'syslinkdir' attribute");
        if (! (syslinkdir.exists () && syslinkdir.isDirectory ())) throw new BuildException ("Search root does not exist or is not a directory: "+syslinkdir.getCanonicalPath ());
        
        if (sysdevdir!=null)
        {
            if (! sysdevdir.exists ()) sysdevdir = null;
            else if (! sysdevdir.isDirectory ()) throw new BuildException ("System device location must be a directory: "+sysdevdir.getCanonicalPath ());
        }
    }
    
    public void execute () throws BuildException
    {
        try
        {
            syslinkdir = syslinkdir.getCanonicalFile ();
            final File classes = new File (new File (syslinkdir, "WEB-INF"), "classes");
            //log (classes.toString ());
            final File dev = new File (syslinkdir, "dev");

            final Map<String,List<String>> devmap1 = new HashMap<String,List<String>> ();
            if (sysdevdir != null)
            {
                final Map<File,String> links = readLinks (sysdevdir);
                if (links != null)
                {
                    for (final Map.Entry<File,String> link : links.entrySet ())
                    {
                        final String device = getLocalPath (sysdevdir, link.getKey ());
                        //log (device);
                        final String className = link.getValue ();

                        List<String> mappings = devmap1.get (className);
                        if (mappings == null)
                        {
                            mappings = new ArrayList<String> ();
                            devmap1.put (className, mappings);
                        }

                        mappings.add (device);
                    }
                }
            }
            
            final Map<String,List<String>> servlets = new HashMap<String,List<String>> ();
            final Map<String,List<String>> devmap2 = new HashMap<String,List<String>> ();
            final Map<File,String> links = readLinks ();
            if (links != null)
            {
                for (final Map.Entry<File,String> link : links.entrySet ())
                {
                    final String mapping = getLocalPath (syslinkdir, link.getKey ());
                    //log (mapping);
                    if (mapping == null)
                    {
                        continue;
                    }

                    final File file = new File (link.getKey ().getParent (), link.getValue ()).getCanonicalFile ();
                    //log (file.toString ());

                    final String servlet = getClassName (classes, file, false);
                    //log (servlet);
                    if (servlet == null)
                    {
                        continue;
                    }

                    List<String> mappings = servlets.get (servlet);
                    if (mappings == null)
                    {
                        mappings = new ArrayList<String> ();
                        servlets.put (servlet, mappings);
                    }

                    mappings.add (mapping);
                }
                
                if (! devmap1.isEmpty ())
                {
                    for (final Map.Entry<File,String> link : links.entrySet ())
                    {
                        //log (link.toString ());
                        final File file = new File (link.getKey ().getParent (), link.getValue ()).getCanonicalFile ();
                        
                        final String subdev = getLocalPath (dev, file);
                        if (subdev == null)
                        {
                            continue;
                        }
                        //log(subdev);
                        
                        final String mapping = getLocalPath (syslinkdir, link.getKey ());
                        //log (mapping);
                        if (mapping == null)
                        {
                            continue;
                        }
                        
                        List<String> mappings = devmap2.get (subdev);
                        if (mappings == null)
                        {
                            mappings = new ArrayList<String> ();
                            devmap2.put (subdev, mappings);
                        }
                        
                        mappings.add (mapping);
                    }
                }
            }
            
            final BufferedWriter out = new BufferedWriter (new FileWriter (tofile));
            try
            {
                out.write ("<!-- begin iovar auto-generation section -->"); out.newLine ();
                
                for (final Map.Entry<String,List<String>> device : devmap1.entrySet ())
                {
                    log ("device: " + device.getKey ());
                    
                    out.write ("<servlet>"); out.newLine ();
                    out.write ("    <servlet-name>iodev:" + device.getKey () + "</servlet-name>"); out.newLine ();
                    out.write ("    <servlet-class>" + device.getKey () + "</servlet-class>"); out.newLine ();
                    out.write ("</servlet>"); out.newLine ();
                    
                    for (final String mapping : device.getValue ())
                    {
                        log ("   mapping: " + "/dev" + mapping);
                        
                        out.write ("<servlet-mapping>"); out.newLine ();
                        out.write ("    <servlet-name>iodev:" + device.getKey () + "</servlet-name>"); out.newLine ();
                        out.write ("    <url-pattern>/dev" + mapping + "</url-pattern>"); out.newLine ();
                        out.write ("</servlet-mapping>"); out.newLine ();
                        out.write ("<servlet-mapping>"); out.newLine ();
                        out.write ("    <servlet-name>iodev:" + device.getKey () + "</servlet-name>"); out.newLine ();
                        out.write ("    <url-pattern>/dev" + mapping + "/*</url-pattern>"); out.newLine ();
                        out.write ("</servlet-mapping>"); out.newLine ();

                        for (final Map.Entry<String,List<String>> subdev : devmap2.entrySet ())
                        {
                            final String config = getLocalPath (new File (mapping), new File (subdev.getKey ()));
                            if (config == null)
                            {
                                continue;
                            }
                            //log (config);

                            log ("   configured: " + "/dev" + mapping + config);
                    
                            out.write ("<servlet>"); out.newLine ();
                            out.write ("    <servlet-name>iocdev:" + mapping + config + "</servlet-name>"); out.newLine ();
                            out.write ("    <servlet-class>" + device.getKey () + "</servlet-class>"); out.newLine ();
                            out.write ("    <init-param>"); out.newLine ();
                            out.write ("        <param-name>config</param-name>"); out.newLine ();
                            out.write ("        <param-value>/dev" + mapping + config + "</param-value>"); out.newLine ();
                            out.write ("    </init-param>"); out.newLine ();
                            out.write ("</servlet>"); out.newLine ();
                        
                            /*
                            out.write ("<servlet-mapping>"); out.newLine ();
                            out.write ("    <servlet-name>iocdev:" + mapping + config + "</servlet-name>"); out.newLine ();
                            out.write ("    <url-pattern>/dev" + mapping + config + "</url-pattern>"); out.newLine ();
                            out.write ("</servlet-mapping>"); out.newLine ();
                            out.write ("<servlet-mapping>"); out.newLine ();
                            out.write ("    <servlet-name>iocdev:" + mapping + config + "</servlet-name>"); out.newLine ();
                            out.write ("    <url-pattern>/dev" + mapping + config + "/*</url-pattern>"); out.newLine ();
                            out.write ("</servlet-mapping>"); out.newLine ();
                            */

                            for (final String mapping2 : subdev.getValue ())
                            {
                                log ("      mapping: " + mapping2);
                        
                                out.write ("<servlet-mapping>"); out.newLine ();
                                out.write ("    <servlet-name>iocdev:" + mapping + config + "</servlet-name>"); out.newLine ();
                                out.write ("    <url-pattern>" + mapping2 + "</url-pattern>"); out.newLine ();
                                out.write ("</servlet-mapping>"); out.newLine ();
                                out.write ("<servlet-mapping>"); out.newLine ();
                                out.write ("    <servlet-name>iocdev:" + mapping + config + "</servlet-name>"); out.newLine ();
                                out.write ("    <url-pattern>" + mapping2 + "/*</url-pattern>"); out.newLine ();
                                out.write ("</servlet-mapping>"); out.newLine ();
                            }
                        }
                    }
                }

                for (final Map.Entry<String,List<String>> servlet : servlets.entrySet ())
                {
                    log ("servlet: " + servlet.getKey ());
                    
                    out.write ("<servlet>"); out.newLine ();
                    out.write ("    <servlet-name>iovar:" + servlet.getKey () + "</servlet-name>"); out.newLine ();
                    out.write ("    <servlet-class>" + servlet.getKey () + "</servlet-class>"); out.newLine ();
                    out.write ("</servlet>"); out.newLine ();
                    
                    for (final String mapping : servlet.getValue ())
                    {
                        log ("   mapping: " + mapping);
                        
                        out.write ("<servlet-mapping>"); out.newLine ();
                        out.write ("    <servlet-name>iovar:" + servlet.getKey () + "</servlet-name>"); out.newLine ();
                        out.write ("    <url-pattern>" + mapping + "</url-pattern>"); out.newLine ();
                        out.write ("</servlet-mapping>"); out.newLine ();
                        out.write ("<servlet-mapping>"); out.newLine ();
                        out.write ("    <servlet-name>iovar:" + servlet.getKey () + "</servlet-name>"); out.newLine ();
                        out.write ("    <url-pattern>" + mapping + "/*</url-pattern>"); out.newLine ();
                        out.write ("</servlet-mapping>"); out.newLine ();
                    }
                }
                
                out.write ("<!-- end iovar auto-generation section -->"); out.newLine ();
            }
            finally
            {
                out.close ();
            }
        }
        catch (final IOException e)
        {
            throw new BuildException ("I/O error building servlet definitions: " + e, e);
        }
        
        log ("servlet definitions generated at: "+tofile);
    }
    
    Map<File,String> readLinks () throws IOException
    {
        log ("reading links at " + syslinkdir + " ...");
        return readLinks (syslinkdir);
    }
    
    Map<File,String> readLinks (final File start) throws IOException
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
                final Map<File,String> sublinks = readLinks (file);
                
                if (sublinks!=null)
                {
                    if (links==null) links = sublinks;
                    else links.putAll (sublinks);
                }
            }
            else
            {
                final String target;
                final BufferedReader in = new BufferedReader (new FileReader (file)); try
                {
                    target = in.readLine ();
                }
                finally
                {
                    in.close ();
                }
                
                if (links == null) links = new HashMap<File,String> ();
                links.put (file, target);
            }
        }
        
        return links;
    }
    
    public static boolean isChild (final File parentDir, final File childFile)
    {
        final File parent = childFile.getParentFile ();
        
        if (parent == null)
        {
            return false;
        }
        else if (parent.equals (parentDir))
        {
            return true;
        }
        else
        {
            return isChild (parentDir, parent);
        }
    }
    
    public static String getLocalPath (final File parentDir, final File childFile)
    {
        final File parent = childFile.getParentFile ();
        
        if (parent == null)
        {
            return null;
        }
        else if (parent.equals (parentDir))
        {
            return "/" + childFile.getName ();
        }
        else
        {
            final String local = getLocalPath (parentDir, parent);
            if (local == null) return null;
            else return local + "/" + childFile.getName ();
        }
    }
    
    public static String getClassName (final File classesDir, final File childFile, boolean isPkg)
    {
        final File parent = childFile.getParentFile ();
        
        if (parent == null)
        {
            return null;
        }
        else if (parent.equals (classesDir))
        {
            return isPkg ? childFile.getName () : toClassName (childFile.getName ());
        }
        else
        {
            final String pkg = getClassName (classesDir, parent, true);
            //System.out.println (pkg);
            if (pkg == null) return null;
            else return pkg + "." + (isPkg ? childFile.getName () : toClassName (childFile.getName ()));
        }
    }
    
    public static String toClassName (String name)
    {
        if (! name.endsWith (".class"))
        {
            return null;
        }
        
        name = name.substring (0, name.length () - 6);
        name = name.replace ("$", ".");
        return name;
    }
}
