/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.sbin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.bin.shell.task.Call;
import net.iovar.web.bin.shell.task.TaskData;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Execute commands on a schedule.
 *
 * @author  shawn@lannocc.com
 */
public class Cron extends HttpServlet
{
    protected void doHead (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doHead (this, req, resp);
    }
    
    protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doGet (this, req, resp);
    }
    
    protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        Utils.doPut (this, req, resp);
    }
    
    public void usage (final HttpServletResponse resp) throws IOException
    {
        Log.debug ("displaying usage");
        
        resp.setContentType ("text/plain");
        final PrintWriter out = resp.getWriter ();
        
        out.println ("usage: cron [<frequency> <command> [<arg>...]");
        out.println ();
        out.println ("Execute a given command on the given frequency.");
        out.println ("The <frequency> may be 'daily', a given time, or number of seconds.");
        out.println ();
        out.println ("Will list currently scheduled jobs if called without arguments.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help  - display this help screen");
        out.println ("   ?name= - give the job a unique label to prevent multiple instances");
    }
    
    /**
     * Execute.
     */
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        final ServletContext context = getServletContext ();
        final String query = req.getQueryString ();
        final Map<String,List<String>> params = Utils.getParams (query);
        
        if (params.containsKey ("help"))
        {
            usage (resp);
            Shell.exit (req, context, 0);
            return;
        }
        
        final Session shell = Sessions.get (req);
        if (shell==null) 
        {
            Log.error ("unable to retrieve session");
            throw new IllegalArgumentException ("need a session for path information");
        }
        
        Map<String,Job> labeled = (Map<String,Job>) context.getAttribute (Job.class.getName ()+".labeled");
        if (labeled==null)
        {
            labeled = new HashMap<String,Job> ();
            context.setAttribute (Job.class.getName ()+".labeled", labeled);
        }
        
        List<Job> unlabeled = (List<Job>) context.getAttribute (Job.class.getName ()+".unlabeled");
        if (unlabeled==null)
        {
            unlabeled = new ArrayList<Job> ();
            context.setAttribute (Job.class.getName ()+".unlabeled", unlabeled);
        }
        
        final List<String> resources = params.get (null);
        if (resources==null || resources.isEmpty ())
        {
            resp.setContentType ("text/plain");
            resp.getWriter ().println ("cron: currently scheduled jobs:");
            listJobs (labeled, unlabeled, resp.getWriter ());
            Shell.exit (req, context, 0);
            return;
        }
        else if (resources.size ()<2)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final String user = req.getRemoteUser ();
        
        final String freq = resources.get (0);
        final String cmd = resources.get (1);
        final List<String> args = new ArrayList<String> ();
        final List<String> name = params.get ("name");
        
        if (resources.size ()>2)
        {
            for (int i=2; i < resources.size (); i++)
            {
                args.add (resources.get (i));
            }
        }
        
        final DateFormat tf = new SimpleDateFormat ("HH:mm:ss");
        tf.setLenient (true);

        try
        {
            if (name==null || name.isEmpty () || !labeled.containsKey (name.get (0)))
            {
                Job job;
                
                if ("daily".equals (freq))
                {
                    job = new DailyJob (cmd, args, new TaskData (shell, context, user, req.getSession ()), null);
                }
                else try
                {
                    job = new HertzJob (Integer.parseInt (freq), cmd, args, new TaskData (shell, context, user, req.getSession ()));
                }
                catch (final NumberFormatException e)
                {
                    try
                    {
                        final Date time = tf.parse (freq);
                        final Calendar cal = Calendar.getInstance ();
                        cal.setTime (time);
                        
                        job = new DailyJob (cmd, args, new TaskData (shell, context, user, req.getSession ()), cal);
                    }
                    catch (final ParseException ee)
                    {
                        throw new IllegalArgumentException ();
                    }
                }
                
                job.thread = new Thread (job);
                job.thread.start ();
                Log.info ("job thread started: "+job);

                if (name!=null && ! name.isEmpty ())
                {
                    labeled.put (name.get (0), job);
                }
                else
                {
                    unlabeled.add (job);
                }
            }
        }
        catch (final IllegalArgumentException e)
        {
            Log.warn (e);
            resp.getWriter ().println ("cron: not 'daily', a time (hh:mm:ss) or a number: "+freq);
            Shell.exit (req, context, 2);
            return;
        }
        
        Shell.exit (req, context, 0);
    }
    
    static void listJobs (final Map<String,Job> labeled, final List<Job> unlabeled, final PrintWriter out)
    {
        for (final Map.Entry<String,Job> entry : labeled.entrySet ())
        {
            final String label = entry.getKey ();
            out.print ("<"+label+"> ");
            out.println (entry.getValue ());
        }
        
        for (final Job job : unlabeled)
        {
            out.println (job);
        }
    }
    
    static abstract class Job implements Runnable
    {
        Thread thread;
        
        final Call call;
        
        final TaskData task;
        
        Job (final String cmd, final List<String> args, final TaskData task)
        {
            Map argMap = new HashMap<String,List<String>> ();
            if (args!=null && !args.isEmpty ()) argMap.put (null, args);
            this.call = Call.valueOf (cmd, argMap);
            
            this.task = task;
        }
        
        public void run ()
        {
            Log.debug ("cron job running: "+this);
            
            while (true) try
            {
                idle ();
                call.exec (task);
            }
            catch (final IOException e)
            {
                Log.error (e);
            }
            catch (final ServletException e)
            {
                Log.error (e);
            }
            catch (final InterruptedException e)
            {
                Log.warn ("interrupted: "+e);
                break;
            }
            catch (final Exception e)
            {
                Log.fatal ("uncaught exception", e);
                break;
            }
        }
        
        abstract void idle () throws InterruptedException;
    }
    
    static class HertzJob extends Job
    {
        final int secs;
        
        HertzJob (final int secs, final String cmd, final List<String> args, final TaskData task)
        {
            super (cmd, args, task);
            this.secs = secs;
        }
        
        void idle () throws InterruptedException
        {
            Thread.sleep (secs*1000);
        }
        
        public String toString ()
        {
            return "[every "+secs+" secs] "+call;
        }
    }
    
    static class DailyJob extends Job
    {
        final Calendar next;
        
        DailyJob (final String cmd, final List<String> args, final TaskData task, final Calendar time)
        {
            super (cmd, args, task);
            
            next = Calendar.getInstance ();
            
            if (time!=null)
            {
                next.set (Calendar.SECOND, time.get (Calendar.SECOND));
                next.set (Calendar.MINUTE, time.get (Calendar.MINUTE));
                next.set (Calendar.HOUR_OF_DAY, time.get (Calendar.HOUR_OF_DAY));
            }
            else
            {
                next.clear (Calendar.SECOND);
                next.clear (Calendar.MINUTE);
                next.clear (Calendar.HOUR);
            }
            
            if (next.compareTo (Calendar.getInstance ())<0)
            {
                next.roll (Calendar.DATE, true);
            }
        }
        
        void idle () throws InterruptedException
        {
            while (next.getTime ().compareTo (new Date ()) >= 0)
            {
                Thread.sleep (10000); // 10 seconds
            }
            
            next.roll (Calendar.DATE, true);
        }
        
        public String toString ()
        {
            return "[every day (next: "+next.getTime ()+")] "+call;
        }
    }
}
