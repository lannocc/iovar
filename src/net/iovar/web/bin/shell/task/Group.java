/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.parse.*;
import net.iovar.web.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;

// 3rd-party imports:

/**
 * One or more comments (ignored), blocks and/or operations, invoked sequentially (an informal list), sometimes conditionally.
 * Will not assemble an empty group (however a group may simply be whitespace or a comment). [IS THIS STILL TRUE???--SAW]
 * 
 * Presently all output is combined.
 *
 * @author  shawn@lannocc.com
 */
public class Group implements Graph, Task
{
    List tasks; // list of Task items as well as possible conditionials (Or, And)
    
    public Group ()
    {
        tasks = new ArrayList (20);
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        // re-usable objects for testing
        Whitespace white = new Whitespace ();
        Comment blah = new Comment ();
        Block block = new Block ();
        DoWhile doWhile = new DoWhile ();
        Operation op = new Operation ();
        Or or = new Or ();
        And and = new And ();
        
        if (in.peek ()<0) return false;
        
        Log.debug ("will assemble");
        
        while (true)
        {
            if (white.assemble (in))
            {
                //white = new Whitespace ();
            }
            else if (blah.assemble (in))
            {
                //blah = new Comment ();
            }
            else if (block.assemble (in))
            {
                tasks.add (block);
                block = new Block ();
            }
            else if (';'==in.peek () || '\n'==in.peek ())
            {
                in.discard ();
            }
            else if (doWhile.assemble (in))
            {
                tasks.add (doWhile);
                doWhile = new DoWhile ();
            }
            else if (op.assemble (in))
            {
                tasks.add (op);
                op = new Operation ();
            }
            else if (or.assemble (in))
            {
                if (tasks.isEmpty () || ! (tasks.get (tasks.size ()-1) instanceof Task))
                {
                    throw new GraphException ("expecting a Task to precede 'Or' condition: "+or);
                }
                
                tasks.add (or);
                or = new Or ();
            }
            else if (and.assemble (in))
            {
                if (tasks.isEmpty () || ! (tasks.get (tasks.size ()-1) instanceof Task))
                {
                    throw new GraphException ("expecting a Task to precede 'And' condition: "+and);
                }
                
                tasks.add (and);
                and = new And ();
            }
            else if (in.peek ()<0)
            {
                if (! tasks.isEmpty ())
                {
                    final Object last = tasks.get (tasks.size ()-1);
                    
                    if (! (last instanceof Task))
                    {
                        throw new GraphException ("expecting a Task to follow the last condition: "+last);
                    }
                }
                
                // FIXME: don't we actually want: return tasks.size()>1; ???
                return true;
            }
            else if ('}'==in.peek ())
            {
                Log.debug ("found closing curly brace and returning");
                // FIXME: don't we actually want: return tasks.size()>1; ???
                return true;
            }
            else
            {
                throw new GraphException ("unexpected (Group): "+((char) in.peek ()));
            }
        }
    }

    public Return exec (final TaskData tdata) throws IOException, ServletException
    {
        Log.debug ("exec: "+this);
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream ();
        Return r = null;
        Task task = null;
        
        Session shell = tdata.shell;
        InputStream in = tdata.in;
        
        boolean skip = false;
        
        for (final Object taskOrCondition : tasks)
        {
            if (skip) // ignore current item
            {
                skip = false;
            }
            else if (taskOrCondition instanceof Task)
            {
                task = (Task) taskOrCondition;
                
                final Return r_old = r;
                Log.debug ("task exec: "+task);
                r = task.exec (new TaskData (shell, tdata.context, tdata.user, tdata.htsession, in, tdata.contentType, tdata.disposition, tdata.allowOrigin));
                Log.debug ("task exec returned: "+r);
                if (r==null) r = r_old; // may be null if operation was simply variable assignment
                if (r!=null) Utils.pipe (r.data, out);

                // session may have changed after exec
                // FIXME: re-evaluate
                Log.debug ("reloading session: "+shell);
                shell = Sessions.load (tdata.context, tdata.htsession, shell.getPath ());
                // FIXME: hardcoded path to shell session storage
                //shell = (Session) Utils.xstream.fromXML (Transport.handler ("local:/proc/shell/"+shell.getId (), tdata.context).get ());
                /*
                final ObjectInputStream sin = new ObjectInputStream (Transport.handler ("local:/proc/shell/"+shell.getId (), context).get ()); try
                {
                    shell = (Session) sin.readObject ();
                }
                catch (final ClassNotFoundException e)
                {
                    Log.error ("while reloading session: "+e);
                    throw new IOException (e);
                }
                finally
                {
                    sin.close ();
                }
                */

                in = null;
            }
            else if (taskOrCondition instanceof Or || taskOrCondition instanceof And)
            {
                Log.debug ("   r.status.affirmative(): "+r.status.affirmative ());
                Log.debug ("   shell.getExit(): "+shell.getExit ());
                
                skip = r.status.affirmative () && (shell.getExit ()==null || shell.getExit ()==0)
                        ? taskOrCondition instanceof Or
                        : taskOrCondition instanceof And;
            }
            else
            {
                throw new RuntimeException ("unsupported task or condition type: "+taskOrCondition);
            }
        }
        
        if (r!=null)
        {
            return new Return (r.type, r.disposition, r.allowOrigin, new ByteArrayInputStream (out.toByteArray()), r.status);
        }
        else // r may be null for a single operation that is simply a variable assignment
        {
            return null;
        }
    }
    
    public String toString ()
    {
        final StringBuffer s = new StringBuffer ();
        String sep = "";
        
        for (final Object taskOrCondition : tasks)
        {
            s.append (sep).append (taskOrCondition);
            sep = "\n";
        }
        
        return s.toString ();
    }
    
    public static void main (final String [] args) throws Exception
    {
//        final GraphReader in = new GraphReader (new StringReader ("\n\t\t   \n    ab;{'c' foo; } bar$blah \"ok then\"$something and${more}stuff and\"not\"'$more'   ; #ok\n  x y z"));
        final GraphReader in = new GraphReader (new StringReader (
                "echo hello>yo | echo world"
                /*
                "local:/bin/export PATH=local:/usr/local/bin,local:/usr/bin,local:/bin,local:/opt/bin\n"+
                "\n"+
                "# USER is set by the initial shell\n"+
                "#export HOME=local:/home/${USER}\n"+
                ""
                */
        ));

        Group g = new Group ();
        
        try
        {
            g.assemble (in);
            System.out.println (g);
        }
        catch (final GraphException e)
        {
            System.err.println ("bad syntax: "+e.getMessage ());
            //System.err.println ("   @ "+in.getLine ()+", "+in.getColumn ());
            e.printStackTrace();
        }
    }
}
