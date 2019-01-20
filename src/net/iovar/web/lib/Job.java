/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.lib;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.task.*;
import net.iovar.web.dev.*;
import net.iovar.web.bin.shell.task.Return;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;

// 3rd-party imports:

/**
 * Support for jobs (currently that means backgrounded tasks).
 *
 * @author  shawn@lannocc.com
 */
public class Job implements Runnable
{
    Thread thread;
    
    final Call call;
    final TaskData task;
    
    Return r;
    ByteArrayOutputStream out;
    
    Job (final Call call, final TaskData task)
    {
        this.call = call;
        this.task = task;
        this.out = new ByteArrayOutputStream ();
    }
    
    public void run ()
    {
        try
        {
            r = call.exec (task);
            Utils.pipe (r.data, out);
        }
        catch (final Exception e)
        {
            try
            {
                out.write (("job: while buffering background output: "+e).getBytes ());
            }
            catch (final IOException ee)
            {
                Log.error ("ack! while handling: "+e, ee);
                throw new RuntimeException (ee);
            }
        }
    }
    
    public Return join (final ServletContext context) throws InterruptedException
    {
        thread.join();
        thread = null;
        
        final Map<Integer,Job> jobs = getJobs (context);
        jobs.remove (getId ());
        
        // FIXME: session may have changed after exec
        
        final ByteArrayInputStream in = new ByteArrayInputStream (out.toByteArray ());
        return new Return (in, this.r.status);
    }
    
    public String toString ()
    {
        // FIXME: track a local (easier) job number for the brackets
        return "["+getId ()+"] "+getId ();
    }
    
    int getId ()
    {
        return System.identityHashCode (this);
    }
    
    public static Job background (final Call call, final TaskData task)
    {
        Map<Integer,Job> jobs = getJobs (task.context);
        if (jobs==null) 
        {
            jobs = new HashMap<Integer, Job> ();
            task.context.setAttribute (Job.class.getName (), jobs);
        }
        
        final Job job = new Job (call, task);
        Log.info ("job backgrounded: "+job);
        jobs.put (job.getId (), job);
        
        job.thread = new Thread (job);
        job.thread.start ();
        Log.debug ("   background thread started");
        
        return job;
    }
    
    public static Map<Integer,Job> getJobs (final ServletContext context)
    {
        return (Map<Integer,Job>) context.getAttribute (Job.class.getName ());
    }
    
    public static Job get (final Integer id, final ServletContext context)
    {
        final Map<Integer,Job> jobs = getJobs (context);
        if (jobs==null) return null;
        
        return jobs.get (id);
    }
}
