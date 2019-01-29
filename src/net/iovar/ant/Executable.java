/*
 * Copyright (C) 2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.ant;

// local imports:

// java imports:
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

// 3rd-party imports:
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.selectors.*;

/**
 * Determine if file has the execute bit set.
 *
 * @author  shawn@lannocc.com
 */
public class Executable implements FileSelector
{
    public boolean isSelected (final File dir, final String name, final File file)
            throws BuildException
    {
        //return file.canExecute (); // this will always return true on Windows ?
        
        try
        {
            if (file.exists () && ! file.isDirectory ())
            {
                final Set<PosixFilePermission> perms = Files.getPosixFilePermissions (file.toPath ());
                return perms.contains (PosixFilePermission.OWNER_EXECUTE)
                        || perms.contains (PosixFilePermission.GROUP_EXECUTE)
                        || perms.contains (PosixFilePermission.OTHERS_EXECUTE);
            }
            else
            {
                return false;
            }
        }
        catch (final IOException e)
        {
            throw new BuildException (e);
        }
    }
}
