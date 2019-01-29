/*
 * Copyright (C) 2019 Virgo Venture, Inc.
 * @%@~LICENSE~@%@
 */
package net.iovar.ant;

// local imports:

// java imports:
import java.io.*;
import java.nio.file.*;

// 3rd-party imports:
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.selectors.*;

/**
 * Determine if file is a symbolic link.
 *
 * @author  shawn@lannocc.com
 */
public class SymLink implements FileSelector
{
    public boolean isSelected (final File dir, final String name, final File file)
            throws BuildException
    {
        return Files.isSymbolicLink (file.toPath ());
    }
}
