/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2015 Lannocc Technologies
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
import javax.servlet.*;

// 3rd-party imports:

/**
 * Do something one or more times, conditionally.
 *
 * @author  shawn@lannocc.com
 */
public class DoWhile implements Graph, Task
{
    Task todo;
    Task condition;
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        if ('d' != in.peek (1)) return false;
        if ('o' != in.peek (2)) return false;
        
        // FIXME: would like to use Whitespace class here (add support to GraphReader?)
        if (' ' != in.peek (3) && '\t' != in.peek (3)) return false;
        in.discard (3);
        
        Whitespace white = new Whitespace ();
        Block block = new Block ();
        Operation op = new Operation ();
        
        white.assemble (in);
        
        if (block.assemble (in))
        {
            todo = block;
            block = new Block ();
        }
        else if (op.assemble (in))
        {
            todo = op;
            op = new Operation ();
        }
        else
        {
            throw new GraphException ("expecting block or operation to follow 'do'");
        }
        
        white.assemble (in);
        
        if ('w' != in.pop ()) throw new GraphException ("expecting 'while' to follow block or operation after 'do'");
        if ('h' != in.pop ()) throw new GraphException ("expecting 'while' to follow block or operation after 'do'");
        if ('i' != in.pop ()) throw new GraphException ("expecting 'while' to follow block or operation after 'do'");
        if ('l' != in.pop ()) throw new GraphException ("expecting 'while' to follow block or operation after 'do'");
        if ('e' != in.pop ()) throw new GraphException ("expecting 'while' to follow block or operation after 'do'");
        if (! white.assemble (in)) throw new GraphException ("expecting 'while' to follow block or operation after 'do'");
        
        if (block.assemble (in))
        {
            condition = block;
        }
        else if (op.assemble (in))
        {
            condition = op;
        }
        else
        {
            throw new GraphException ("expecting block or operation to follow 'while'");
        }
        
        return true;
    }
    
    public Return exec (final TaskData task) throws IOException, ServletException
    {
        // NOTE: this logic mostly copied from Group.
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream ();
        Return r = null;
        
        Session shell = task.shell;
        
        // FIXME: does it really make sense to allow input piping here?
        InputStream in = task.in;
        
        do
        {
            //final Writer out2 = new OutputStreamWriter (out);
            //out2.write (shell.toString ()+'\n');
            //out2.write (shell.getEnvironment ().toString ()+'\n');
            //out2.flush ();
            
            Return r_old = r;
            
            r = todo.exec (new TaskData (shell, task.context, task.user, task.htsession, in, task.contentType, task.disposition, task.allowOrigin));
            Log.debug ("todo exec returned: "+r);
            if (r==null) r = r_old; // may be null if operation was simply variable assignment
            if (r!=null) Utils.pipe (r.data, out);

            // session may have changed after exec
            // FIXME: re-evaluate
            Log.debug ("reloading session: "+shell);
            shell = Sessions.load (task.context, task.htsession, shell.getPath ());
            
            in = null;
            r_old = r;
            
            //out2.write ("pre-condition: "+shell.toString ()+'\n');
            //out2.write (shell.getEnvironment ().toString ()+'\n');
            //out2.flush ();

            r = condition.exec (new TaskData (shell, task.context, task.user, task.htsession, in, task.contentType, task.disposition, task.allowOrigin));
            Log.debug ("condition exec returned: "+r);
            if (r==null) r = r_old; // may be null if operation was simply variable assignment
            if (r!=null) Utils.pipe (r.data, out);

            // session may have changed after exec
            // FIXME: re-evaluate
            Log.debug ("reloading session: "+shell);
            shell = Sessions.load (task.context, task.htsession, shell.getPath ());
            
            //out2.write ("post-condition: "+shell.toString ()+'\n');
            //out2.write (shell.getEnvironment ().toString ()+'\n');
            //out2.flush ();
        }
        while (r.status.affirmative () && shell.getExit () == 0);
        
        // FIXME: return the last condition's type and status (as we are), or something else instead?
        return new Return (r.type, r.disposition, r.allowOrigin, new ByteArrayInputStream (out.toByteArray()), r.status);
    }
}
