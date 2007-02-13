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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImplFactory;

/*
 * author: Leonardo Di Lella, leonardo.dilella@netline-is.de
 * date: Fri Jul 23 14:36:51 GMT 2004
 */

public class SSLServerSocket
{
	private ServerSocket socket;
	private SSLCtx ctx;

	private native long nativeNew(long ctx) throws SSLException;
	private native void nativeFree(long ssl);
	private native boolean nativeAcceptFinished(long ssl);
	private native byte[] nativeAccept(long ssl, byte[] in, int off, int len) throws SSLException;

	private void initialize(SSLCtx ctx) throws SSLException
	{
		if(ctx == null)
			throw new SSLException("SSLGlobal parameter null input");

		this.ctx = ctx;
	}

	public SSLServerSocket(SSLCtx ctx, int port) throws IOException, SSLException
	{
		initialize(ctx);
		socket = new ServerSocket(port);
	}

	public SSLServerSocket(SSLCtx ctx, int port, int backlog) throws IOException, SSLException
	{
		initialize(ctx);
		socket = new ServerSocket(port, backlog);
	}

	public SSLServerSocket(SSLCtx ctx, int port, int backlog, InetAddress bindAddr) throws IOException, SSLException
	{
		initialize(ctx);
		socket = new ServerSocket(port, backlog, bindAddr);
	}

	public void finalize()
	{
	}

	public SSLSocket accept() throws IOException, SSLException
	{
		SSLSocket ssock;
		Socket socket;
		long ssl;
		DataInputStream is;
    	DataOutputStream os;
		int timeout;

		socket = this.socket.accept();
		ssl = nativeNew(ctx.getCTX());
		ssock = new SSLSocket(ssl, socket);

		timeout = ssock.getSoTimeout();
		ssock.setSoTimeout(60 * 1000);
		os = new DataOutputStream(socket.getOutputStream());
		is = new DataInputStream(socket.getInputStream());

		byte ret[], in[] = new byte[4096];
		int len;

		do 
		{
			len = is.read(in, 0, in.length);
			if(len == -1) 
				throw new IOException("the end of the stream has been reached");

			ret = nativeAccept(ssl, in, 0, len);
			if(ret != null) 
			{
				os.write(ret);
				os.flush();
			}
		} while (!nativeAcceptFinished(ssl));

		ssock.setSoTimeout(timeout);
		return ssock;
	}

	public InetAddress getInetAddress()
	{
		return socket.getInetAddress();
   	}

   	public int getLocalPort()
	{
    	return socket.getLocalPort();
   	}

   	public void close() throws IOException
	{
    	socket.close();
   	}

   	public synchronized void setSoTimeout(int timeout) throws SocketException
	{
	    socket.setSoTimeout(timeout);
   	}

   	public synchronized int getSoTimeout() throws IOException
	{
		return socket.getSoTimeout();
   	}

   	public String toString()
	{
		return ("SSL" + socket.toString());
   	}

   	public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException
	{
		ServerSocket.setSocketFactory(fac);
	}
}
