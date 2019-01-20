/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014-2015 Lannocc Technologies
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

// 3rd-party imports:
//import org.apache.commons.fileupload.*;
//import org.apache.commons.fileupload.servlet.*;

/**
 * Extract a file from multipart/form-data input.
 *
 * @author shawn@lannocc.com
 */
public class FormFile extends HttpServlet
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
        
        out.println ("usage: formfile [options] <name> [<path>]");
        out.println ();
        out.println ("Extract a file keyed by <name> from multipart/form-data input.");
        out.println ();
        out.println ("FormFile is easily chained to FormFile or Form2XML:");
        out.println ("If <path> is supplied then the file contents are saved there and the");
        out.println ("remaining parts are echoed to output. Otherwise the file contents");
        out.println ("are echoed to output.");
        out.println ();
        out.println ("Options:");
        out.println ("   ?help      - display this help screen");
        out.println ("   ?quiet     - run in silent mode (no error reporting)");
        out.println ("   ?unnamed=<RANDOM>");
        out.println ("              - randomly generate a filename when none is submitted");
        out.println ("   ?set=<name>");
        out.println ("              - set a variable <name> with the computed filename");
        out.println ();
        out.println (" (the following options are available only when <path> is used)");
        out.println ("   ?empty=<DELETE|IGNORE>");
        out.println ("              - whether to delete or ignore when empty file is submitted");
        out.println ("              - default behavior will create an empty file");
        out.println ("   ?path=<APPEND|RANDOM>");
        out.println ("              - APPEND the submitted (or generated) filename to <path>");
        out.println ("              - append a unique RANDOM string to <path>");
        out.println ("                (use in combination with ?set)");
        out.println ("   ?subname   - substitue filename for the file content part data");
    }
    
    /**
     * Execute.
     */
    protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException
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
        
        final List<String> vals = params.get (null);
        if (vals==null || vals.size ()<1 || vals.size ()>2)
        {
            usage (resp);
            Shell.exit (req, context, 1);
            return;
        }
        
        final boolean quiet = params.containsKey ("quiet");
        
        final InputStream in = req.getInputStream ();
        Log.debug ("content-type: "+req.getContentType ());
        
//        if (!"multipart/form-data".equalsIgnoreCase (req.getContentType ()))
        if (req.getContentType ()==null || !req.getContentType ().startsWith ("multipart/form-data; boundary="))
        {
            if (!quiet)
            {
                Log.error ("not multipart/form-data content-type");
                resp.getWriter ().println ("formfile: not multipart/form-data content-type");
            }
            
            Shell.exit (req, context, 2);
            return;
        }
        
        final String name = vals.get (0);
        final String path = vals.size ()>1 ? vals.get (1) : null;
//        Utils.pipe (in, resp.getOutputStream ());
        
        if (in==null)
        {
            if (!quiet) Log.warn ("no input data");
            Shell.exit (req, context, 0);
            return;
        }
        
        /*
        // discover the boundary (including carriage-return/newline)
        // FIXME: not to spec... RFC 2046 says boundary is passed in as parameter
        // in the Content-Type header. Also it doesn't have to be the first line.
        final String boundary;
        {
            int c = in.read ();
            if (c<0)
            {
                Log.warn ("no input data");
                Shell.exit (req, context, 0);
                return;
            }
            if (c!='-')
            {
                Log.error ("expecting '--' boundary on first line");
                resp.getWriter ().println ("formfile: expecting '--' boundary on first line");
                Shell.exit (req, context, 3);
                return;
            }
            c = in.read ();
            if (c!='-')
            {
                Log.error ("expecting '--' boundary on first line");
                resp.getWriter ().println ("formfile: expecting '--' boundary on first line");
                Shell.exit (req, context, 3);
                return;
            }
            
            final StringBuffer sb = new StringBuffer ();
            while ((c = in.read ())>=0)
            {
                sb.append ((char) c);
                if (c=='\n') break;
            }
            boundary = sb.toString ();
        }
        */
        
        final String boundary = "--"+req.getContentType ().substring (30);
        Log.debug ("boundary: "+boundary);
        
        // read (and ignore) until first boundary (preamble)
        final OutputStream dummy = new OutputStream ()
        {
            public void write (int b)
            {
                // no-op
            }
        };
        Utils.pipe (in, dummy, boundary);
        Utils.pipe (in, dummy, "\r\n");
        
        OutputStream out = null;
        
        while (true)
        {
            Log.debug ("in new multipart segment");
            
            // get headers: Content-Disposition is required
            final Map<String, String> headers = getHeaders (in);
            if (headers==null) break;
            else if (headers.size ()==1 && headers.containsKey ("--") && headers.get ("--")==null) break;
            else if (headers.size ()==0)
            {
                continue;
            }
            
            final String disposition = headers.get ("Content-Disposition");
            final String type = headers.get ("Content-Type");

            if (disposition==null)
            {
                if (!quiet)
                {
                    Log.error ("expecting 'Content-Disposition' header in the input data");
                    if (out==null) out = resp.getOutputStream ();
                    out.write ("formfile: expecting 'Content-Disposition' header in the input data".getBytes ());
                }
                
                Shell.exit (req, context, 4);
                return;
            }
            if (! disposition.startsWith ("form-data; name=\""))
            {
                if (!quiet)
                {
                    Log.error ("expecting form-data disposition including a named parameter");
                    if (out==null) out = resp.getOutputStream ();
                    out.write ("formfile: expecting form-data disposition including a named paramter".getBytes ());
                }
                
                Shell.exit (req, context, 5);
                return;
            }
            
            // get field name
            final String fname;
            {
                /*
                final StringBuffer sb = new StringBuffer ();
                for (int c; (c = in.read ())>=0; sb.append ((char) c)) if (c=='"') break;
                fname = sb.toString ();
                */
                int quote = disposition.indexOf ('"', 17);
                if (quote<0)
                {
                    if (!quiet)
                    {
                        Log.error ("unterminated form field name in Content-Disposition header");
                        if (out==null) out = resp.getOutputStream ();
                        out.write ("formfile: unterminated form field name in Content-Disposition header".getBytes ());
                    }
                    
                    Shell.exit (req, context, 6);
                    return;
                }
                fname = disposition.substring (17, quote);
            }
            Log.debug ("field name: "+fname);
            
            if (fname.equalsIgnoreCase (name))
            {
                Log.info ("found matching field: "+fname);
                
                // get file name (optional)
                String filename;
                {
                    final int begin = 17 + fname.length () + 1;
                    if (disposition.substring (begin).startsWith ("; filename=\""))
                    {
                        int quote = disposition.indexOf ('"', begin+12);
                        if (quote<0)
                        {
                            if (!quiet)
                            {
                                Log.error ("unterminated filename in Content-Disposition header");
                                if (out==null) out = resp.getOutputStream ();
                                out.write ("formfile: unterminated filename in Content-Disposition header".getBytes ());
                            }
                            
                            Shell.exit (req, context, 7);
                            return;
                        }
                        filename = disposition.substring (begin+12, quote);
                    }
                    else
                    {
                        filename = null;
                    }
                }
                Log.debug ("submitted filename: "+filename);
                
                if (filename==null || filename.trim ().isEmpty ())
                {
                    final List<String> unnames = params.get ("unnamed");
                    if (unnames!=null)
                    {
                        if (unnames.size ()!=1)
                        {
                            if (!quiet)
                            {
                                Log.error ("the ?unnamed option requires exactly one value");
                                if (out==null) out = resp.getOutputStream ();
                                out.write ("formfile: the ?unnamed option requires exactly one value".getBytes ());
                            }
                            
                            Shell.exit (req, context, 8);
                            return;
                        }

                        final String unnamed = unnames.get (0);
                        if ("random".equalsIgnoreCase (unnamed))
                        {
                            final Date now = new Date ();
                            filename = System.identityHashCode (now) + "_" + now.getTime ();
                        }
                        else
                        {
                            if (!quiet)
                            {
                                Log.error ("invalid value for ?unnamed option: "+unnamed);
                                if (out==null) out = resp.getOutputStream ();
                                out.write (("formfile: invalid value for ?unnamed option: "+unnamed).getBytes ());
                            }
                            
                            Shell.exit (req, context, 9);
                            return;
                        }
                    }
                }
                Log.info ("filename: "+filename);
                
                String setname = filename;
                
                if (path!=null) // save the file contents
                {
                    Log.debug ("path specified from command line: "+path);
                    final String finalpath;
                    
                    final List<String> options = params.get ("path");
                    if (options!=null)
                    {
                        if (options.size ()!=1)
                        {
                            if (!quiet)
                            {
                                Log.error ("the ?path option requires exactly one value");
                                if (out==null) out = resp.getOutputStream ();
                                out.write ("formfile: the ?path option requires exactly one value".getBytes ());
                            }
                            
                            Shell.exit (req, context, 10);
                            return;
                        }

                        final String option = options.get (0);

                        if ("append".equalsIgnoreCase (option))
                        {
                            Log.debug ("appending filename to path");
                            finalpath = path + filename;
                        }
                        else if ("random".equalsIgnoreCase (option))
                        {
                            Log.debug ("appending random string to path");
                            setname = "XXX_FIXME_XXX";
                            finalpath = path + setname;
                        }
                        else
                        {
                            if (!quiet)
                            {
                                Log.error ("invalid value for ?path option: "+option);
                                if (out==null) out = resp.getOutputStream ();
                                out.write (("formfile: invalid value for ?path option: "+option).getBytes ());
                            }
                            
                            Shell.exit (req, context, 11);
                            return;
                        }
                    }
                    else
                    {
                        finalpath = path;
                    }
                    Log.info ("image will be saved to: "+finalpath);
                    
                    final Transport resource = Transport.handler (finalpath, context, req.getSession ());
                    
                    // FIXME: not ideal
                    final ByteArrayOutputStream buffer = new ByteArrayOutputStream ();
                    Utils.pipe (in, buffer, "\r\n"+boundary);
                    
                    boolean put = true;
                    boolean isEmpty = false;
                    
                    if (buffer.size ()<1)
                    {
                        isEmpty = true;
                        Log.info ("matching file field was empty");
                        
                        final List<String> empties = params.get ("empty");
                        if (empties!=null)
                        {
                            if (empties.size ()!=1)
                            {
                                if (!quiet)
                                {
                                    Log.error ("the ?empty option requires exactly one value");
                                    if (out==null) out = resp.getOutputStream ();
                                    out.write ("formfile: the ?empty option requires exactly one value".getBytes ());
                                }
                                
                                Shell.exit (req, context, 12);
                                return;
                            }
                            
                            final String empty = empties.get (0);
                            if ("delete".equalsIgnoreCase (empty))
                            {
                                put = false;
                                Log.debug ("deleting");
                                resource.delete ();

                            }
                            else if ("ignore".equalsIgnoreCase (empty))
                            {
                                Log.debug ("ignoring");
                                put = false;
                            }
                            else
                            {
                                if (!quiet)
                                {
                                    Log.error ("invalid value for ?empty option: "+empty);
                                    if (out==null) out = resp.getOutputStream ();
                                    out.write (("formfile: invalid value for ?empty option: "+empty).getBytes ());
                                }
                                
                                Shell.exit (req, context, 13);
                                return;
                            }
                        }
                    }
                    
                    if (put)
                    {
                        Log.debug ("putting contents");
                        resource.put (new ByteArrayInputStream (buffer.toByteArray ()));
                    
                        if (params.containsKey ("subname") && !quiet)
                        {
                            if (out==null)
                            {
                                final String otype = "multipart/form-data; boundary="+boundary.substring (2);
                                Log.debug ("setting content type to: "+otype);
                                resp.setContentType (otype);
                                out = resp.getOutputStream ();

                                out.write (boundary.getBytes ());
                            }
                            out.write ("\r\n".getBytes ());

                            out.write (("Content-Disposition: form-data; name=\""+name+"\"").getBytes ());
                            out.write ("\r\n\r\n".getBytes ());

                            if (!isEmpty) out.write (filename.getBytes ());

                            out.write ("\r\n".getBytes ());
                            out.write (boundary.getBytes ());
                        }
                    }
                }
                else if (!quiet) // no path specified (output the file contents)
                {
                    if (out==null)
                    {
                        Log.debug ("setting content type: "+type);
                        if (type!=null) resp.setContentType (type);
                        out = resp.getOutputStream ();
                    }
                    Utils.pipe (in, out, boundary);
                    //Utils.pipe (in, out, "\r\n");
                }
                
                final List<String> sets = params.get ("set");
                if (sets!=null)
                {
                    final Session session = Sessions.get (req);
                    if (session==null) 
                    {
                        Log.error ("unable to retrieve session");
                        throw new IllegalArgumentException ("need a session to set variable to");
                    }

                    for (final String set : sets)
                    {
                        Log.debug ("setting filename to variable: "+set);
                        // FIXME: re-evaluate this...
                        session.export (1, context, req.getSession (), set, setname);
                    }

                    // FIXME: re-evaluate this...
                    session.saveUp (1, context, req.getSession ());
        
                    // FIXME: is this necessary?
                    //Sessions.put (req, context, session);
                }
            }
            else if (path!=null && !quiet) // no match (a)
            {
                if (out==null)
                {
                    final String otype = "multipart/form-data; boundary="+boundary.substring (2);
                    Log.debug ("setting content type to: "+otype);
                    resp.setContentType (otype);
                    out = resp.getOutputStream ();
                    
                    out.write (boundary.getBytes ());
                }
                out.write ("\r\n".getBytes ());
                
                for (final Map.Entry<String,String> header : headers.entrySet ())
                {
                    out.write (header.getKey ().getBytes ());
                    final String val = header.getValue ();
                    if (val!=null) out.write ((": "+val).getBytes ());
                    out.write ("\r\n".getBytes ());
                }
                
                out.write ("\r\n".getBytes ());
                Utils.pipe (in, out, boundary);
                out.write (boundary.getBytes ());
            }
            else if (!quiet) // no match (b)
            {
                // read (and ignore) until next boundary
                Utils.pipe (in, dummy, boundary);
                Utils.pipe (in, dummy, "\r\n");
            }
        }
        
        if (path!=null && !quiet)
        {
            out.write ("--".getBytes ());
        }
        
        /*
        // these are both possibly the same test... oh well better safe than sorry
        if (ServletFileUpload.isMultipartContent (req) && "multipart/form-data".equalsIgnoreCase (req.getContentType ())) try
        {
            final ServletFileUpload upload = new ServletFileUpload ();
            
            for (final FileItemIterator it = upload.getItemIterator (req); it.hasNext (); )
            {
                final FileItemStream item = it.next ();
                final String fname = item.getFieldName ();
                
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    System.out.println("Form field " + name + " with value "
                        + Streams.asString(stream) + " detected.");
                } else {
                    System.out.println("File field " + name + " with file name "
                        + item.getName() + " detected.");
                    // Process the input stream
                    ...
                }
            }
        }
        catch (final FileUploadException e)
        {
        }
        */
    }
    
    static Map<String,String> getHeaders (final InputStream in) throws IOException
    {
        final Map<String,String> headers = new HashMap<String,String> ();
        boolean data = false;
        
        for (String line; (line = readLine (in))!=null; )
        {
            if (line.endsWith ("\r\n")) line = line.substring (0, line.length ()-2);
            data = true;
            if (line.isEmpty ()) break;
            
            final int colon = line.indexOf (':');
            if (colon>=0)
            {
                final String key = line.substring (0, colon);
                Log.debug ("header key: "+key);
                final String val = line.substring (colon+2);
                Log.debug ("header val: "+val);
                
                headers.put (key, val);
            }
            else
            {
                Log.debug ("header key (no val): "+line);
                headers.put (line, null);
            }
        }
        
        if (data) return headers;
        else return null;
    }
    
    static String readLine (final InputStream in) throws IOException
    {
        boolean cr = false;
        final StringBuffer line = new StringBuffer ();
        
        for (int c; (c = in.read ())>=0; )
        {
            line.append ((char) c);
            if (cr && c=='\n') break;
            cr = c=='\r';
        }

        if (line.length ()>0) return line.toString ();
        else return null;
    }
}
