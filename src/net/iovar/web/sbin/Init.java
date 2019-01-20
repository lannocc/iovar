/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.sbin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.bin.shell.task.TaskData;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

// 3rd-party imports:

/**
 * System initialization.
 * Servlet init() looks for 'inittab' init-param that points to location of
 * init configuration file describing the actual steps.
 *
 * @author  shawn@lannocc.com
 */
public class Init extends HttpServlet
{
    public static final String INIT_PARAM = "inittab";
    public static final String WAIT_PARAM = "wait";
    public static final long DEFAULT_WAIT = 1500; // 1.5 seconds
    
    public static final String TAG_INIT = "init";
    public static final String TAG_BOOT_WAIT = "bootwait";

    /**
     * FIXME - this is a temporary hack
     * @since 1.1
     */
    public static ServletContext CONTEXT = null; // FIXME
    
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
    
    public void init ()
    {
        final ServletContext context = getServletContext ();
        CONTEXT = context;

        final String init = this.getInitParameter (INIT_PARAM);
        if (init==null) return;
        
        final long wait;
        final String swait = this.getInitParameter (WAIT_PARAM);
        if (swait!=null)
        {
            wait = Long.parseLong (swait);
        }
        else
        {
            wait = DEFAULT_WAIT;
        }
        
        Log.debug ("system init thread split");
        Log.debug ("init param: "+init);
        
        /*
         * Unfortunately, the Servlet API provides no good mechanism to insert
         * code that should be executed after all filter/servlet inits but
         * before servicing any user request.
         * 
         * Tomcat will not let any servlets service a request while still within
         * the init() process so we use this thread split and small sleep
         * timeout as an ugly hack.
         * 
         * FIXME: find a better place for "system initialization" ?
         */
        
        new Thread (new Runnable ()
        {
            public void run ()
            {
                Log.debug ("system init thread started");
                
                try
                {
                    Log.debug ("waiting for "+wait+" milliseconds before init");
                    Thread.sleep (wait);
                    
                    Log.info ("system init begin");
                    
                    Log.debug ("calling shell init for master environment");
                    final Session shell = Sessions.master (context);
        
                    final Transport config = Transport.handler (init, context, null);
                    Log.debug ("init config resource: "+config);
                    
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
                    Document xml = factory.newDocumentBuilder ().parse (config.get ());

                    if (!TAG_INIT.equals (xml.getDocumentElement ().getNodeName ()))
                    {
                        throw new IllegalArgumentException ("expecting <"+TAG_INIT+"> document, got: "+xml.getDocumentElement ());
                    }

                    NodeList tags = xml.getElementsByTagName (TAG_BOOT_WAIT);
                    for (int t=0; t<tags.getLength (); t++) try
                    {
                        //final String bootwait = tags.item (t).getNodeValue ();
                        final Node bootwait = tags.item (t).getFirstChild ();
                        Log.info ("bootwait: "+bootwait);
                        
                        if (bootwait!=null)
                        {
                            //final Transport resource = Transport.handler (bootwait.getNodeValue (), context);
                            //Log.debug ("bootwait resource: "+resource);
                            
                            // FIXME (user)
                            Shell.exec (bootwait.getNodeValue (), null, new TaskData (shell, context, null, null));
//shell.saveTo ("local:/proc/shell/"+shell.getId (), context);
                        }
                    }
                    catch (final ServletException e)
                    {
                        Log.error ("failure executing bootwait", e);
                    }
                    catch (final IOException e)
                    {
                        Log.error ("failure executing bootwait", e);
                    }
                }
                catch (final InterruptedException e)
                {
                    Log.warn ("interrupted", e);
                }
                catch (final IOException e)
                {
                    Log.fatal ("failure loading init file: "+init, e);
                }
                catch (final ParserConfigurationException e)
                {
                    Log.fatal ("failure loading init file: "+init, e);
                }
                catch (final SAXException e)
                {
                    Log.fatal ("failure loading init file: "+init, e);
                }
                catch (final IllegalArgumentException e)
                {
                    Log.fatal ("failure loading init file: "+init, e);
                }
                finally
                {
                    Log.info ("system init end");
                }
            }
        }).start();
    }
}
