/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */



package com.openexchange.ssl;

import java.io.IOException;
import java.io.InputStream;

/*
 * author: Leonardo Di Lella, leonardo.dilella@netline-is.de
 * date: Fri Jul 23 14:36:51 GMT 2004
 */

public class SSLInputStream extends InputStream
{
	private long ssl;
	private SSLSocket socket;
	private InputStream is;

	private native int nativeAvailable(long ssl);
   	private native int nativeReadArray(long ssl, 
                                       byte[] in, 
                                       int inoff, 
                                       int inlen, 
                                       byte out[], 
                                       int outoff, 
                                       int outlen) throws SSLException;

	public SSLInputStream(SSLSocket sslsock, InputStream is) throws IOException
	{
		this.socket = sslsock;
		this.is = is;
		ssl = this.socket.getSSL();
    }

	public int read() throws IOException
	{
       	int n;
		byte[] b = new byte[1];

		n = read(b);
		if(n < 0) 
            return n;
		else 
            return b[0];
	}

   	public int read(byte b[]) throws IOException
	{
       	return(read(b, 0, b.length));
   	}

	public int read(byte b[], int off, int len) throws IOException
	{
		int ret;
		int n;

		n = nativeAvailable(ssl);
		if(n > 0) 
		{
			if(n > len) 
                n = len;

			try 
			{
				ret = nativeReadArray(ssl, null, 0, 0, b, off, n);
			} catch(SSLException e) 
			{
				e.printStackTrace();
				throw new IOException();
			}
		} 
		else 
		{
			byte in[] = new byte[4096];
			n = is.read(in, 0, in.length);
			if(n < 0) 
				return n;

			try 
			{
				ret = nativeReadArray(ssl, in, 0, n, b, off, len);
			} catch(SSLException e) 
			{
				e.printStackTrace();
				throw new IOException();
			}
		}
		return ret;
    }

    public long skip(long n) throws IOException
	{
		byte[] in = new byte[(int)n];
		return read(in, 0, in.length);
    }

  	public int available() throws IOException
  	{
		byte in[] = new byte[4096];
	  	int len;

	  	len = is.available();
	  	if(len > 0) 
		{
			if(len > in.length) 
			{
				len = in.length;
			 	is.read(in, 0, len);
			 	try 
				{
					nativeReadArray(ssl, in, 0, len, null, 0, 0);
			 	} catch(SSLException e) 
				{
				 	e.printStackTrace();
				 	throw new IOException();
			 	}
		  	}
	  	}
	  	return nativeAvailable(ssl);
  	}

  	public void close() throws IOException 
	{
    		is.close();
  	}

  	public synchronized void mark(int readlimit)
  	{
		is.mark(readlimit);
  	}

  	public synchronized void reset() throws IOException
  	{
    	is.reset();
  	}

  	public boolean markSupported()
  	{
    	return(is.markSupported());
  	}
}
