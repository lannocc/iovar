/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2012-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.dev;

// local imports:
import net.iovar.web.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Basic system logging.
 * Set a minimum log level with 'iovar.log.level' context-param.
 * 
 * Logs to memory by default but will instead log to a file specified by
 * 'iovar.log.file', if set.
 *
 * @author  shawn@lannocc.com
 */
public class Log extends HttpServlet
{
    private static final Class CLASS = Log.class;
    
    public static final String LEVEL_PARAM = "iovar.log.level";
    public static final Level LEVEL_DEFAULT = Level.WARN;
    public static final String FILE_PARAM = "iovar.log.file";
    
    public static enum Level
    {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL;
        
        public String toString ()
        {
            switch (this)
            {
                case DEBUG: return "(debug)";
                case INFO:  return "-info--";
                case WARN:  return " WARN  ";
                case ERROR: return "*ERROR*";
                case FATAL: return "!FATAL!";
                default:    return "!-???-!";
            }
        }
    }
    
    /** minimum log level */
    static Level min = null;
    
    static PrintStream out = null;
    static List<Entry> entries = new LinkedList<Entry> ();
    
    public void init ()
    {
        if (min!=null) return;
        final ServletContext context = getServletContext ();
        final String level = context.getInitParameter (LEVEL_PARAM);

        if (level!=null) try
        {
            final Level min = Level.valueOf (level);
            Log.info ("setting minimum log level: "+min);
            this.min = min;
        }
        catch (final IllegalArgumentException e)
        {
            Log.fatal ("invalid minimum log level: "+level, e);
        }
        finally
        {
            if (min==null)
            {
                Log.info ("setting default minimum log level: "+LEVEL_DEFAULT);
                min = LEVEL_DEFAULT;
            }
        }
        
        final String path = context.getInitParameter (FILE_PARAM);
        final File file = new File (context.getRealPath (path));
        
        if (path!=null) try
        {
            Log.info ("log file: "+file.getAbsolutePath ());
            this.out = new PrintStream (new FileOutputStream (file));
            
            for (final Entry entry : this.entries)
            {
                out.println (entry);
            }
            
            this.entries.clear ();
        }
        catch (final IOException e)
        {
            Log.fatal ("failed creating output file: "+file, e);
        }
    }
    
    protected void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doHead (this, req, resp);
    }
    
    public void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doGet (this, req, resp);
    }
    
    protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doPut (this, req, resp);
    }

    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        final String path = context.getInitParameter (FILE_PARAM);
        
        if (path!=null)
        {
            resp.sendRedirect (context.getContextPath ()+"/"+path);
        }
        else
        {
            resp.setContentType ("text/plain");
            display (resp.getWriter ());
        }
    }
    
    protected static void log (final Level level, final String msg)
    {
        if (min==null || level.compareTo (min) >= 0)
        {
            final Entry entry = new Entry (level, msg);
            //System.err.println (entry);
            
            if (out!=null) out.println (entry);
            else entries.add (entry);
        }
    }
    
    public static void debug (final String msg)
    {
        log (Level.DEBUG, msg);
    }
    
    public static void info (final String msg)
    {
        log (Level.INFO, msg);
    }
    
    public static void warn (final String msg)
    {
        log (Level.WARN, msg);
    }
    
    public static void warn (final Exception e)
    {
        log (Level.WARN, e.toString ());
    }
    
    public static void warn (final String msg, final Exception e)
    {
        log (Level.WARN, msg+": "+e);
    }
    
    public static void error (final String msg)
    {
        log (Level.ERROR, msg);
    }
    
    public static void error (final Exception e)
    {
        //log (Level.ERROR, e.toString ());
        
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
        final PrintWriter out = new PrintWriter (new OutputStreamWriter (bytes));
        out.println(e.toString ());
        e.printStackTrace (out);
        out.close ();
        
        log (Level.ERROR, bytes.toString ());
    }
    
    public static void error (final String msg, final Exception e)
    {
        log (Level.ERROR, msg);
        
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
        final PrintWriter out = new PrintWriter (new OutputStreamWriter (bytes));
        e.printStackTrace (out);
        out.close ();
        
        log (Level.ERROR, bytes.toString ());
    }
    
    public static void fatal (final String msg)
    {
        log (Level.FATAL, msg);
    }
    
    public static void fatal (final Exception e)
    {
        //log (Level.FATAL, e.toString ());
        
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
        final PrintWriter out = new PrintWriter (new OutputStreamWriter (bytes));
        out.println(e.toString ());
        e.printStackTrace (out);
        out.close ();
        
        log (Level.FATAL, bytes.toString ());
    }
    
    public static void fatal (final String msg, final Exception e)
    {
        log (Level.FATAL, msg);
        
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream ();
        final PrintWriter out = new PrintWriter (new OutputStreamWriter (bytes));
        e.printStackTrace (out);
        out.close ();
        
        log (Level.FATAL, bytes.toString ());
    }
    
    public static void display (final PrintWriter out)
    {
        for (final Iterator<Entry> it = entries.iterator (); it.hasNext (); out.println (it.next ()));
        out.flush ();
    }
    
    static class Entry
    {
        final long time;
        final StackTraceElement trace;
        final Level level;
        final String msg;
        
        Entry (final Level level, final String msg)
        {
            this.time = System.currentTimeMillis ();
            
            final StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
            /*
            for (int i=0; i < stack.length; i++)
            {
                System.out.println (stack[i]);
            }
             */
            // FIXME: using the 5th element of the stack, probably not portable across VMs/servlet containers
            if (stack!=null && stack.length>=5) this.trace = stack[4];
            else this.trace = null;
            
            this.level = level;
            this.msg = msg;
        }
        
        public String toString ()
        {
            return level+/*" "+time+*/" @ "+trace+"\n > "+msg;
        }
    }
    
    public static void main (final String[] args)
    {
        log (Level.DEBUG, "just a test");
    }
}
