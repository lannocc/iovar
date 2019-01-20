/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2013-2015 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.web.usr.bin;

// local imports:
import net.iovar.web.*;
import net.iovar.web.bin.shell.*;
import net.iovar.web.dev.*;
import net.iovar.web.lib.*;
import net.iovar.web.proc.*;

// java imports:
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

// 3rd-party imports:

/**
 * Extensible Stylesheet Language Transformation processor.
 *
 * @author  shawn@lannocc.com
 */
public class XSLT extends HttpServlet
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
        
        out.println ("usage: xslt <stylesheet> [<xml-document>] [--] [<arguments>]");
        out.println ("   - when <xml-document> is omitted stdin is used");
        out.println ("   - any named parameters are passed to the stylesheet as xsl param variables");
        out.println ("   - unless ?variables is used, any additional arguments");
        out.println ("      (use '--' when xml-document is omitted)");
        out.println ("      are passed as parameters named arg1, arg2, etc.");
        out.println ();
        // FIXME: don't really want options here... this is temporary until useful buffer support
        //      (needed for cattamboo /bin/view)
        out.println ("Options:");
        out.println ("   ?help                     - display this help screen");
        out.println ("   ?variables                - the <arguments> are taken to be variable names");
        out.println ("                               and the value of these variables will be passed");
        out.println ("                               by name to the stylesheet as xsl param variables");
        out.println ("   ?preproc=<pre-stylesheet> - preprocess <stylesheet> using <pre-stylesheet> ");
        out.println ("                               and then use the result as stylesheet");
    }
    
    /**
     * Execute.
     */
    public void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
    {
        Log.debug ("begin xslt POST");
        
        final ServletContext context = getServletContext ();
        final String query = req.getQueryString ();
        
        if (query==null)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final Map<String,List<String>> params = Utils.getParams (query);
        final List<String> resources = params.get (null);

        if (resources==null || resources.size ()<1)
        {
            usage (resp);
            Shell.exit (req, context, 2);
            return;
        }
        
        final String stylesheet = resources.remove (0);
        final String resource;
        
        if (resources.isEmpty ())
        {
            resource = null;
        }
        else if ("--".equals (resources.get (0)))
        {
            resource = null;
            resources.remove (0);
        }
        else
        {
            resource = resources.remove (0);
            
            if (! resources.isEmpty () && "--".equals (resources.get (0)))
            {
                resources.remove (0);
            }
        }
        
        final String prestyle;
        {
            final List<String> prestyles = params.remove ("preprocess");
            if (prestyles!=null) prestyle = prestyles.get (0);
            else prestyle = null;
        }
        
        final List<String> args = new ArrayList<String> ();
        
        if (params.containsKey ("variables"))
        {
            params.remove ("variables");
        
            final Session session = Sessions.get (req);
            if (session==null) 
            {
                Log.error ("unable to retrieve session");
                throw new IllegalArgumentException ("need a session for variable substitution mode");
            }
            
            for (final String arg : resources)
            {
                params.put (arg, Arrays.asList (new String[] { session.get (arg) }));
            }
        }
        else for (final String arg : resources)
        {
            args.add (arg);
        }
        
        try
        {
            final TransformerFactory tf = TransformerFactory.newInstance();
            tf.setURIResolver (new TransportResolver (context, req.getSession ()));

            final ByteArrayOutputStream bytes; // FIXME: #11
            if (prestyle!=null)
            {
                final StreamSource xsl = new StreamSource (Transport.handler (prestyle, context, req.getSession ()).get ());
                final Transformer trx = tf.newTransformer (xsl);
                trx.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                final StreamSource xml = new StreamSource (Transport.handler (stylesheet, context, req.getSession ()).get ());
                bytes = new ByteArrayOutputStream ();
                trx.transform (xml, new StreamResult (bytes));
            }
            else
            {
                bytes = null;
            }
            
            final StreamSource xsl;
            {
                if (bytes!=null) xsl = new StreamSource (new ByteArrayInputStream (bytes.toByteArray ()));
                else xsl = new StreamSource (Transport.handler (stylesheet, context, req.getSession ()).get ());
            }

            final StreamSource xml;
            {
                if (resource!=null) xml = new StreamSource (Transport.handler (resource, context, req.getSession ()).get ());
                else xml = new StreamSource (req.getInputStream ());
            }
            
            transform (tf, context, xsl, xml, params, args, resp);
            
            Shell.exit (req, context, 0);
        }
        catch (final IOException e)
        {
            Log.error (e);
            resp.getWriter ().print ("xslt: i/o error: "+e+"\n");
            Shell.exit (req, context, 3);
        }
        catch (final TransformerConfigurationException e)
        {
            Log.error (e);
            //throw new ServletException (e);
            resp.getWriter ().print ("xslt: xml transform config failure: "+e+"\n");
            Shell.exit (req, context, 4);
        }
        catch (final TransformerException e)
        {
            Log.error (e);
            //throw new ServletException ("xml transform failed: ", e);
            resp.getWriter ().print ("xslt: xml transform failed: "+e+"\n");
            Shell.exit (req, context, 5);
        }
    }
    
    /**
     * Public access for easy transform
     */
    public static void transform (final ServletContext context, final InputStream xsl,
            final InputStream xml, final HttpServletResponse resp, final HttpSession htsession)
            
            throws IOException, TransformerConfigurationException, TransformerException
    {
        final TransformerFactory tf = TransformerFactory.newInstance();
        tf.setURIResolver (new TransportResolver (context, htsession));
        
        transform (tf, context, new StreamSource (xsl), new StreamSource (xml), null, null, resp);
    }
    
    static void transform (final TransformerFactory tf, final ServletContext context,
            final StreamSource xsl, final StreamSource xml,
            final Map<String,List<String>> params, final List<String> args,
            final HttpServletResponse resp)
            
            throws IOException, TransformerConfigurationException, TransformerException
    {
        final Transformer trx = tf.newTransformer (xsl);
        trx.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // set any named parameters that were passed in
        if (params!=null)
        {
            for (final Map.Entry<String,List<String>> entry : params.entrySet ())
            {
                final String name = entry.getKey ();
                if (name!=null)
                {
                    final List<String> value = entry.getValue ();
                    if (value!=null && !value.isEmpty ())
                    {
                        // xslt transformer requires a non-null value
                        final String realval = value.get (0);
                        if (realval!=null) trx.setParameter (name, realval);
                    }
                }
            }
        }

        // set any anonymous arguments passed in
        if (args!=null)
        {
            int idx=0;
            for (final String arg : args)
            {
                trx.setParameter ("arg"+(++idx), arg);
            }
        }

        final String type = trx.getOutputProperty (OutputKeys.MEDIA_TYPE);
        Log.debug ("media type from XSL stylesheet: "+type);
        if (type!=null)
        {
            Log.info ("setting content type: "+type);
            resp.setContentType (type);
        }

        Log.info ("applying xsl transformation");
        trx.transform (xml, new StreamResult (resp.getWriter ()));
    }
    
    
    static class TransportResolver implements URIResolver
    {
        final ServletContext context;
        final HttpSession htsession;
        
        TransportResolver (final ServletContext context, final HttpSession htsession)
        {
            this.context = context;
            this.htsession = htsession;
        }
        
        public Source resolve (final String href, final String base) throws TransformerException
        {
            Log.debug ("resolving href="+href+", base="+base);
            try
            {
                if (href.startsWith ("data:text/xml,"))
                {
                    return new StreamSource (new StringReader (href.substring (14)));
                }
                else
                {
                    return new StreamSource (Transport.handlerHref (href, context, htsession).get ());
                }
            }
            catch (final IOException e)
            {
                Log.error ("failed resolving "+href, e);
                throw new TransformerException ("failed resolving "+href, e);
            }
        }
    }
}
