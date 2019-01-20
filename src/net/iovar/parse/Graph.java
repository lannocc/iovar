/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.parse;

// local imports:

// java imports:
import java.io.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public interface Graph
{
    boolean assemble (final GraphReader in) throws GraphException, IOException;
}
