/*
 * Copyright (C) 2019 Virgo Venture, Inc.
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
import org.apache.tools.ant.taskdefs.*;

/**
 * Extension of COPY that includes the link target instead of file contents.
 *
 * @author  shawn@lannocc.com
 */
public class CopySymLinks extends Copy
{
    protected void doFileOperations ()
    {
        if (! fileCopyMap.isEmpty ())
        {
            log("Copying " + fileCopyMap.size ()
                + " file" + (fileCopyMap.size () == 1 ? "" : "s")
                + " to " + destDir.getAbsolutePath ());

            for (final Map.Entry<String, String[]> entry : fileCopyMap.entrySet ())
            {
                final String from = entry.getKey ();

                for (final String to : entry.getValue ())
                {
                    if (from.equals (to))
                    {
                        log ("Skipping self-copy of " + from, verbosity);
                        continue;
                    }
                    
                    log ("Copying " + from + " to " + to, verbosity);
                    final File fromFile = new File (from);
                    final File toFile = new File (to);

                    try
                    {
                        final File parent = toFile.getParentFile ();
                        if ( ! parent.exists () )
                        {
                            parent.mkdirs ();
                        }
                        
                        final BufferedWriter out = new BufferedWriter (new FileWriter (toFile)); try
                        {
                            final Path target = Files.readSymbolicLink (fromFile.toPath ());
                            out.write (target.toString ());
                            out.newLine ();
                        }
                        finally
                        {
                            out.close ();
                        }
                    }
                    catch (final UnsupportedOperationException e)
                    {
                        // we are on Windows, perhaps?
                        log ("...  unsupported operation", verbosity);
                    }
                    catch (final NotLinkException e)
                    {
                        // no-op
                        log ("...  not a link", verbosity);
                    }
                    catch (final IOException e)
                    {
                        String msg = "Failed to copy " + from + " to " + to + ": " + e;
                        if ( /*! (e instanceof ResourceUtils.ReadOnlyTargetFileException)
                            &&*/ toFile.exists () && ! toFile.delete ())
                        {
                            msg += " and I couldn't delete the corrupt " + to;
                        }
                        
                        if (failonerror)
                        {
                            throw new BuildException (msg, e, getLocation ());
                        }
                        
                        log (msg, Project.MSG_ERR);
                    }
                }
            }
        }
    }
}
