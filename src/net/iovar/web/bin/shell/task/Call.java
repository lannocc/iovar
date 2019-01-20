/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.bin.shell.task;

// local imports:
import net.iovar.parse.*;
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;
import net.iovar.web.dev.trans.*;
import net.iovar.web.lib.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

// 3rd-party imports:

/**
 * Invoke a named executable with optional parameters/arguments.
 *
 * @author  shawn@lannocc.com
 */
public class Call extends Command implements Graph, Task
{
    List<CommandText> data; // un-normalized command and anonymous arguments
    List<Parameter> params; // named parameters
    
    // normalized
    String cmd;         // command
    List<String> args;  // anonymous arguments
    
    Call ()
    {
        this.data = new ArrayList<CommandText> ();
        this.params = new ArrayList<Parameter> ();
        
        this.cmd = null;
        this.args = null;
    }
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        // re-usable objects for testing
        CommandText text = new CommandText ();
        Whitespace white = new Whitespace ();
        Parameter param = new Parameter ();
        
        if (! text.assemble (in)) return false;
        
        Log.debug ("will assemble");
        
        this.data.add (text);
        text = new CommandText ();
        
        for (int c; (c = in.peek ())>=0; )
        {
            if (white.assemble (in))
            {
                //white = new Whitespace ();
            }
            
            if (param.assemble (in))
            {
                this.params.add (param);
                param = new Parameter ();
            }
            else if (text.assemble (in))
            {
                this.data.add (text);
                text = new CommandText ();
            }
            else
            {
                return true;
            }
        }
        
        return true;
    }
    
    /*
     * FIXME: this should be improved (allow named parameters, too?)
     */
    void normalize (Session shell, final ServletContext context, final HttpSession htsession)
    {
        StringBuffer cmd = null;
        final List<String> args = new ArrayList<String> ();
        
        boolean inArgs = false;
        StringBuffer arg = null;

        for (final CommandText segment : data)
        {
            for (final Text text : segment.items)
            {
                final String str = text.value (shell, context, htsession);
                if (str==null)
                {
                    continue;
                }

                if (text instanceof Variable)
                {
                    char c;
                    for (int i=0; i<str.length (); i++)
                    {
                        c = str.charAt (i);
                        if (' '==c || '\t'==c)
                        {
                            if (cmd==null)
                            {
                                continue;
                            }

                            inArgs = true;

                            if (arg!=null)
                            {
                                args.add (arg.toString ());
                                arg = null;
                            }
                        }
                        else
                        {
                            if (! inArgs)
                            {
                                if (cmd==null)
                                {
                                    cmd = new StringBuffer ();
                                }
                                cmd.append (c);
                            }
                            else
                            {
                                if (arg==null)
                                {
                                    arg = new StringBuffer ();
                                }
                                arg.append (c);
                            }
                        }
                    }
                }
                else
                {
                    if (! inArgs)
                    {
                        if (cmd==null)
                        {
                            cmd = new StringBuffer ();
                        }

                        cmd.append (str);
                    }
                    else
                    {
                        if (arg==null)
                        {
                            arg = new StringBuffer ();
                        }
                        arg.append (str);
                    }
                }
            }

            if (! inArgs)
            {
                inArgs = true;
            }
            else if (arg!=null)
            {
                args.add (arg.toString ());
                arg = null;
            }
        }

        if (arg!=null)
        {
            args.add (arg.toString ());
            arg = null;
        }
        
        this.cmd = cmd.toString ();
        this.args = args;
    }
    
    public Return exec (final TaskData task) throws IOException, ServletException
    {
        Log.info ("executing: "+this);
        Log.debug ("task: "+task);
        
        // 0. normalize command name / arguments
        // Always re-normalize unless constructed with valueOf...this call may
        // be executed more than once, and variables may have changed.
        if (this.data != null && ! this.data.isEmpty ())
        {
            normalize (task.shell, task.context, task.htsession);
        }
        
        // 1. run shell processing on named parameters
        final Map<String,List<String>> params = new HashMap<String,List<String>> ();
        for (final Parameter param : this.params)
        {
            final String name = param.getName ().value (task.shell, task.context, task.htsession);
            final CommandText value = param.getValue ();

            List<String> vals = params.get (name);
            if (vals==null)
            {
                vals = new ArrayList<String> ();
                params.put (name, vals);
            }
            
            vals.add (value!=null ? value.value (task.shell, task.context, task.htsession) : null );
        }
        
        // 2. add anonymous arguments to parameter map
        if (this.args!=null && ! this.args.isEmpty ())
        {
            params.put (null, args);
        }
        
        Session local = prepare (params, task); try
        {
            // 8. invoke
            // FIXME: hardcoded path to shell session storage
            params.put (Shell.EXT_PARAM_SESSION, Arrays.asList (new String[] { "local:/proc/shell/"+local.getId () }));
            
            final Transport t = Which.reference (local, task.context, task.user, cmd, params, task.htsession);
            if (t!=null)
            {
                // #6 - this is very hacky :-(
                if (! (t instanceof Local || t instanceof Resource || t instanceof net.iovar.web.dev.trans.File))
                {
                    Log.debug ("removing "+Shell.EXT_PARAM_SESSION+" param");
                    List<String> removed = t.getParams ().remove (Shell.EXT_PARAM_SESSION);
                    if (removed!=null && !removed.isEmpty ()) Log.warn ("[HTSESSION] removed param ("+removed+"): "+t);
                    removed = params.remove (Shell.EXT_PARAM_SESSION);
                    if (removed!=null && !removed.isEmpty ()) Log.warn ("[HTSESSION] removed param ("+removed+"): "+this);
                }
                
                return invoke (local, t, task.context, task.in, task.contentType, params, task.htsession);
            }
            else
            {
                Log.error ("executable resource not found (even after searching through $PATH, if relative): "+cmd);
                return Utils.error ("executable resource not found (even after searching through $PATH, if relative): "+cmd);
            }
        }
        finally
        {
            local.delete (task.context, task.htsession);
        }
    }
    
    public Session prepare (final Map<String,List<String>> params, final TaskData task) throws IOException, ServletException
    {
        // 3. clear previous exit value
        task.shell.clearExit ();
        task.shell.save (task.context, task.htsession);
        
        // 4. fork the session
        final Session local = task.shell.fork (task.context);
        Log.info ("forked local session: "+local);

        // 5. any named parameters will be passed in as variables.
        for (final Map.Entry<String,List<String>> param : params.entrySet ())
        {
            final String name = param.getKey ();
            final List<String> vals = param.getValue ();
            final StringBuffer sval = new StringBuffer ();
            String sep = "";

            if (vals!=null) for (final String val : vals)
            {
                sval.append (sep).append (val);
                sep = ",";
            }

            local.set (name, sval.toString ());
        }

        // 6. set locals
        local.setLocal ("0", cmd.toString ());
        final StringBuffer all = new StringBuffer ();
        String sep = "";
        int index=0;
        if (args!=null)
        {
            for (final String arg : args)
            {
                all.append (sep).append (arg);
                sep = " ";
                local.setLocal (String.valueOf (++index), arg);
                local.setLocal ("#" + index, String.valueOf (arg==null ? 0 : arg.length ()));
            }
        }
        local.setLocal ("#", String.valueOf (index));
        local.setLocal ("@", all.toString ());

        // 7. set user
        local.set (Session.ENV_USER, task.user);

        return local;
    }
    
    static Return invoke (final Session shell, final Transport t, final ServletContext context, final InputStream in, final String contentType, final Map<String,List<String>> params, final HttpSession htsession) throws IOException
    {
        // re-set command name from more-specific transport path
        shell.setLocal ("0", t.toPathString ());
            
        // FIXME: hardcoded path to shell session storage
        //shell.saveTo ("local:/proc/shell/"+shell.getId (), context);
        shell.save (context, htsession);
        
/*
BufferedReader something = new BufferedReader (new InputStreamReader (in));
for (String line; (line = something.readLine ())!=null; )
{
    Log.fatal ("BLAH: "+line);
}
*/
        Log.debug ("invoking: "+t.toPathString ());
        final Return r = t.post (in, contentType);
        Log.debug ("invoking returned: "+r);
        
        if (r.status instanceof Http.Status && ((Http.Status) r.status).isRedirect ())
        {
            Log.debug ("return has redirect status: "+r);
            
            final String target = ((Http.Status) r.status).getHeaders ().get ("Location").get (0);
            Log.info ("redirecting to target: "+target);
            
            return invoke (shell, Transport.handler (target, params, context, htsession), context, in, contentType, params, htsession);
        }
        else
        {
            return r;
        }
    }
    
    public String toString ()
    {
        if (this.cmd==null)
        {
            final StringBuffer s = new StringBuffer ("<(non-normalized call)");
            
            for (final CommandText text : this.data)
            {
                s.append (" ").append (text);
            }
            
            s.append (">");
            return s.toString ();
        }
        else
        {
            final StringBuffer s = new StringBuffer ("<"+this.cmd+">");

            for (final Parameter param : params)
            {
                s.append (" ").append (param);
            }

            if (this.args!=null)
            {
                for (final String arg : this.args)
                {
                    s.append (" [").append (arg).append ("]");
                }
            }

            return s.toString ();
        }
    }
    
    public static Call valueOf (final String name, final Map<String,List<String>> params)
    {
        final Call call = new Call ();
        call.cmd = name;
        
        if (params!=null) for (final Map.Entry<String,List<String>> entry : params.entrySet ())
        {
            final String key = entry.getKey ();
            
            if (key==null)
            {
                call.args = new ArrayList<String> ();
                
                for (final String val : entry.getValue ())
                {
                    call.args.add (val);
                }
            }
            else if (entry.getValue ()==null)
            {
                call.params.add (new Parameter (key, null));
            }
            else
            {
                for (final String val : entry.getValue ())
                {
                    call.params.add (new Parameter (key, val));
                }
            }
        }
        
        return call;
    }
}
