/*
 * Copyright (C) 2016-2019 Virgo Venture, Inc.
 * Copyright (C) 2014 Lannocc Technologies
 * @%@~LICENSE~@%@
 */
package net.iovar.json;

// local imports:
import net.iovar.parse.*;

// java imports:
import java.io.*;
import java.math.*;

// 3rd-party imports:

/**
 *
 * @author  shawn@lannocc.com
 */
public class JSONNumber implements Graph, Value
{
    //boolean negative;
    BigInteger integer;
    BigInteger fractional;
    
    //boolean exponeg;
    BigInteger tenpower;
    
    public boolean assemble (final GraphReader in) throws IOException, GraphException
    {
        integer = null;
        fractional = null;
        tenpower = null;
        
        boolean negate = false;
        if ('-' == in.peek ())
        {
            negate = true;
            in.discard ();
        }
        
        final StringBuilder digits = new StringBuilder ();
        
        // integer / fractional section
        while (true)
        {
            final int c = in.peek ();
            
            if ('0' <= c && c <= '9')
            {
                in.discard ();
                digits.append ((char) c);
            }
            else if ('.' == c)
            {
                in.discard ();
                if (! set (digits, negate))
                {
                    throw new GraphException ("at least one digit required before decimal point");
                }
            }
            else if ('e'==c || 'E'==c)
            {
                in.discard ();
                if (! set (digits, negate))
                {
                    throw new GraphException ("at least one digit required before exponent");
                }
                break;
            }
            else
            {
                return set (digits, negate);
            }
        }
        
        // exponent section
        final int sign = in.pop ();
        if ('+'==sign) negate = false;
        else if ('-'==sign) negate = true;
        else throw new GraphException ("sign required for exponent");
        
        while (true)
        {
            final int c = in.peek ();
            
            if ('0' <= c && c <= '9')
            {
                in.discard ();
                digits.append ((char) c);
            }
            else
            {
                setExponent (digits, negate);
                return true;
            }
        }
    }
    
    boolean set (final StringBuilder digits, final boolean negate) throws GraphException
    {
        if (integer==null)
        {
            if (digits.length () <= 0)
            {
                if (! negate) return false;
                else throw new GraphException ("at least one digit is required after minus sign");
            }
            
            integer = new BigInteger (digits.toString ());
            if (negate) integer = integer.negate ();
        }
        else if (fractional==null)
        {
            if (digits.length () <= 0)
            {
                throw new GraphException ("at least one digit is required after decimal point");
            }
            
            fractional = new BigInteger (digits.toString ());
            if (negate) fractional = fractional.negate ();
        }
        else
        {
            throw new GraphException ("encountered more than one decimal point");
        }
        
        digits.setLength (0);
        return true;
    }
    
    void setExponent (final StringBuilder digits, final boolean negate) throws GraphException
    {
        if (digits.length () <= 0)
        {
            throw new GraphException ("at least one digit is required for exponent");
        }
        
        tenpower = new BigInteger (digits.toString ());
        if (negate) tenpower = tenpower.negate ();
    }
    
    public String toString ()
    {
        final StringBuilder str = new StringBuilder (integer.toString ());
        if (fractional != null) str.append ('.').append (fractional.toString ());
        if (tenpower != null)
        {
            str.append ('e');
            if (tenpower.signum () >= 0) str.append ('+');
            str.append (tenpower.toString ());
        }
        return str.toString ();
    }
    
    public static void main (final String[] args) throws Exception
    {
        final GraphReader in = new GraphReader (new InputStreamReader (System.in, java.nio.charset.StandardCharsets.UTF_8));
        
        final Graph json = new JSONNumber ();
        if (! json.assemble (in))
        {
            System.err.println ("did not assemble");
            return;
        }
        
        System.out.println (json);
    }
}
