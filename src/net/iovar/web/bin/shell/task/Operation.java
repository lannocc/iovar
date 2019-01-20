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
 * A set of commands invoked together as a single unit.
 *
 * @author  shawn@lannocc.com
 */
class Operation implements Graph, Task
{
    List<Command> cmds;
    
    public Operation ()
    {
        cmds = new ArrayList<Command> (10);
    }
    
    /**
     * Note that this may gobble up whitespace yet fail assembly.
     */
    public boolean assemble (final GraphReader in) throws GraphException, IOException
    {
        Log.debug ("attempting assembly");
        
        // re-usable objects for testing
        VariableAssign var = new VariableAssign ();
        Call call = new Call ();
        Input input = new Input ();
        Output output = new Output ();
        Chain chain = new Chain ();
        Background back = new Background ();
        Whitespace white = new Whitespace ();
        
        boolean setVar = false; // have any variable assignments?
        Object lastCallOrChain = null;
        
        if (in.peek ()<0) return false;
        
        // if there are any variable assignments they have to come first
        while (var.assemble (in))
        {
            setVar = true;
            cmds.add (var);
            var = new VariableAssign ();
            
            if (white.assemble (in))
            {
                //white = new Whitespace ();
            }
        }
        
        while (true)
        {
            if (call.assemble (in))
            {
                if (lastCallOrChain!=null && lastCallOrChain instanceof Call)
                {
                    throw new GraphException ("not expecting another call here: "+call);
                }
                
                cmds.add (call);
                lastCallOrChain = call;
                call = new Call ();
            }
            else if (input.assemble (in))
            {
                cmds.add (input);
                input = new Input ();
            }
            else if (output.assemble (in))
            {
                cmds.add (output);
                output = new Output ();
            }
            else if (chain.assemble (in))
            {
                Log.debug ("lastCallOrChain: "+lastCallOrChain);
                
                if (lastCallOrChain==null || ! (lastCallOrChain instanceof Call))
                {
                    throw new GraphException ("expecting a call to come somewhere before chain command: "+chain);
                }
                
                cmds.add (chain);
                lastCallOrChain = chain;
                chain = new Chain ();
            }
            else if (back.assemble (in))
            {
                if (lastCallOrChain==null)
                {
                    throw new GraphException ("expecting a call or chain to come before background command: "+back);
                }
                
                cmds.add (back);
                lastCallOrChain = back;
                back = new Background ();
            }
            else if (white.assemble (in))
            {
                //white = new Whitespace ();
            }
            else if (cmds.size ()<1)
            {
                return false;
            }
            else if (lastCallOrChain==null)
            {
                if (setVar) return true;
                
                throw new GraphException ("operation expects a call (or variable assignment, at least) to come somewhere: "+(char) in.peek ());
            }
            else if (! (lastCallOrChain instanceof Call || lastCallOrChain instanceof Background))
            {
                throw new GraphException ("operation needs to end with a call");
            }
            else
            {
                return true;
            }
        }
    }
    
    /**
     * Note this returns <tt>null</tt> if the operation is merely a variable assignment.
     */
    public Return exec (final TaskData task) throws IOException, ServletException
    {
        Log.debug ("passed-in contentType: "+task.contentType);
        // FIXME: use PipedInputStream + PipedOutputStream in a new thread
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream ();
        Return r = null;
        
        Session shell = task.shell;
        
        Call call = null;
        Input from = null;
        Output to = null;
        
        for (final Command cmd : cmds)
        {
            if (cmd instanceof VariableAssign)
            {
                final VariableAssign var = (VariableAssign) cmd;
                final String name = var.getName ();
                final CommandText val = var.getValue ();
                
                // FIXME: re-evaluate this...
                
                shell.set (name, val!=null ? val.value (shell, task.context, task.htsession) : null);
                //shell.export (1, context, name, val!=null ? val.value (shell, context) : null);
                

                // FIXME: not sure about saving here
                
                shell.save (task.context, task.htsession);
                //shell.saveUp (1, context);
            }
            else if (cmd instanceof Call)
            {
                call = (Call) cmd;
            }
            else if (cmd instanceof Chain)
            {
                r = call.exec (new TaskData (shell, task.context, task.user, task.htsession, from!=null ? from.read (shell, task.context, task.htsession) : (r!=null ? r.data : task.in),
                        from!=null ? null : (r!=null ? r.type : task.contentType),
                        from!=null ? null : (r!=null ? r.disposition : task.disposition),
                        from!=null ? null : (r!=null ? r.allowOrigin : task.allowOrigin)
                ));
                Utils.pipe (r.data, out);
                r = new Return (r.type, r.disposition, r.allowOrigin, new ByteArrayInputStream (out.toByteArray ()), r.status);
                out.reset();
                
                // session may have changed after exec
                // FIXME: re-evaluate
                Log.debug ("reloading session: "+shell);
                shell = Sessions.load (task.context, task.htsession, shell.getPath ());
                // FIXME: hardcoded path to shell session storage
                //shell = (Session) Utils.xstream.fromXML (Transport.handler ("local:/proc/shell/"+shell.getId (), task.context).get ());
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
                
                call = null;
            }
            else if (cmd instanceof Input)
            {
                from = (Input) cmd;
            }
            else if (cmd instanceof Output)
            {
                to = (Output) cmd;
            }
            else if (cmd instanceof Background)
            {
                Log.debug ("background: "+cmd);
                
                // FIXME: content-type, disposition, allow-origin not yet utilized here
                final Job job = Job.background (call, new TaskData (shell, task.context, task.user, task.htsession, from!=null ? from.read (shell, task.context, task.htsession) : (r!=null ? r.data : task.in), null, null, null));
                out.write (job.toString ().getBytes ());
                r = new Return (new ByteArrayInputStream (out.toByteArray ()), new Status () {
                    public boolean affirmative () { return true; }
                });
                out.reset ();
                
                call = null;
            }
            else
            {
                throw new ServletException ("unsupported Command type: "+cmd.getClass ());
            }
        }
        
        if (call!=null)
        {
            if (to!=null)
            {
                // FIXME: contentType not fully utilized here
                r = call.exec (new TaskData (shell, task.context, task.user, task.htsession, from!=null ? from.read (shell, task.context, task.htsession) : (r!=null ? r.data : task.in),
                        from!=null ? null : (r!=null ? r.type : task.contentType),
                        from!=null ? null : (r!=null ? r.disposition : task.disposition),
                        from!=null ? null : (r!=null ? r.allowOrigin : task.allowOrigin)
                ));
                Utils.pipe (r.data, out);
                return new Return (to.write (shell, task.context, new ByteArrayInputStream (out.toByteArray ()), r.type, task.htsession), r.status);
            }
            else
            {
                // FIXME: contentType not fully utilized here???
                return call.exec (new TaskData (shell, task.context, task.user, task.htsession, from!=null ? from.read (shell, task.context, task.htsession) : (r!=null ? r.data : task.in),
                        from!=null ? null : (r!=null ? r.type : task.contentType),
                        from!=null ? null : (r!=null ? r.disposition : task.disposition),
                        from!=null ? null : (r!=null ? r.allowOrigin : task.allowOrigin)
                ));
            }
        }
        else
        {
            return r;
        }
    }
    
    public String toString ()
    {
        final StringBuffer s = new StringBuffer ();
        String sep = "";
        
        for (final Command cmd : cmds)
        {
            s.append (sep).append (cmd);
            sep = " ";
        }
        
        return s.toString ();
    }
}
