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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * author: Leonardo Di Lella, leonardo.dilella@open-xchange.com
 * date: Fri Jul 23 14:36:51 GMT 2004
 */

public class SSLSocket
{
	private Socket socket = null;
	private long ssl = 0;
	private SSLCtx ctx;

	private native long nativeNew(long ctx) throws SSLException;
	private native void nativeFree(long ssl);
	private native boolean nativeConnectFinished(long ssl);
	private native void nativeShutdown(long ssl);
	private native void nativeAsignSession(long ssl, byte sess[], int len) throws SSLException;
	private	native byte[] nativeConnect(long ssl, byte[] in, int off, int len) throws SSLException;

	private void initialize(SSLCtx ctx) throws SSLException
	{
		if(ctx == null)
			throw new SSLException("SSLCtx is null!");

		this.ctx = ctx;
		ssl = nativeNew(ctx.getCTX());
	}

	public long getSSL()
	{
		return ssl;
	}

	public SSLSocket(long ssl, Socket socket) throws UnknownHostException, IOException, SSLException
	{
		this.socket = socket;
		this.ssl = ssl;
	}

	public SSLSocket(SSLCtx ctx, String host, int port) throws UnknownHostException, IOException, SSLException
   	{
		socket = new Socket(host, port);
		initialize(ctx);
		SSLConnect();
   	}

   	public SSLSocket(SSLCtx ctx, InetAddress address, int port) throws IOException, SSLException
	{
    	socket = new Socket(address, port);
		initialize(ctx);
		SSLConnect();
   	}

   	public SSLSocket(SSLCtx ctx, String host, int port, InetAddress localAddr, int localPort) throws IOException, SSLException
	{
    	socket = new Socket(host, port, localAddr, localPort);
		initialize(ctx);
		SSLConnect();
   	}

   	public SSLSocket(SSLCtx ctx, InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException, SSLException
	{
		socket = new Socket(address, port, localAddr, localPort);
		initialize(ctx);
		SSLConnect();
   	}

   	public SSLSocket(SSLCtx ctx, String host, int port, boolean stream) throws IOException, SocketException
	{
	    	throw new SocketException("not supported constructor");
   	}

   	public SSLSocket(SSLCtx ctx, InetAddress host, int port, boolean stream) throws IOException, SocketException
	{
		throw new SocketException("not supported constructor");
   	}

	public void finalize()
	{
		if(ssl != 0) 
        {
			nativeFree(ssl);
			ssl = 0;
		}
	}

	private void SSLConnect() throws IOException, SSLException
	{
		DataInputStream ips;
	    	DataOutputStream ops;
		int timeout;

		timeout = socket.getSoTimeout();
		socket.setSoTimeout(60 * 1000);
		ips = new DataInputStream(socket.getInputStream());
		ops = new DataOutputStream(socket.getOutputStream());

		byte ret[], in[] = new byte[4096];
		int len;

        /** shake my hand, baby **/
		ret = nativeConnect(ssl, null, 0, 0);
		if(ret != null) 
		{
			ops.write(ret);
			ops.flush();
		}

		do 
		{
			len = ips.read(in, 0, in.length);
			if(len == -1) 
			{
				throw new IOException("the end of the stream has been reached");
			}
			ret = nativeConnect(ssl, in, 0, len);
			if(ret != null) 
			{
				ops.write(ret);
				ops.flush();
			}
		} while (!nativeConnectFinished(ssl));
		socket.setSoTimeout(timeout);
   	}

	public InetAddress getInetAddress()
	{
		return socket.getInetAddress();
   	}

   	public InetAddress getLocalAddress()
	{
		return socket.getLocalAddress();
   	}

   	public int getPort()
	{
    	return socket.getPort();
   	}

   	public int getLocalPort()
	{
		return socket.getLocalPort();
   	}

   	public InputStream getInputStream() throws IOException
	{
		return new SSLInputStream(this, socket.getInputStream());
   	}

   	public OutputStream getOutputStream() throws IOException
	{
		return new SSLOutputStream(this, socket.getOutputStream());
   	}

   	public void setTcpNoDelay(boolean on) throws SocketException
	{
		socket.setTcpNoDelay(on);
   	}

   	public boolean getTcpNoDelay() throws SocketException
	{
		return socket.getTcpNoDelay();
   	}

   	public void setSoLinger(boolean on, int linger) throws SocketException
	{
		socket.setSoLinger(on, linger);
   	}

   	public int getSoLinger() throws SocketException
	{
		return socket.getSoLinger();
   	}

   	public synchronized void setSoTimeout(int timeout) throws SocketException
	{
		socket.setSoTimeout(timeout);
   	}

   	public synchronized int getSoTimeout() throws SocketException
	{
		return socket.getSoTimeout();
   	}

   	public synchronized void setSendBufferSize(int size) throws SocketException
	{
		socket.setSendBufferSize(size);
   	}

   	public synchronized int getSendBufferSize() throws SocketException
	{
		return socket.getSendBufferSize();
   	}

    public synchronized void setReceiveBufferSize(int size) throws SocketException
	{
	    socket.setReceiveBufferSize(size);
    }

   	public synchronized int getReceiveBufferSize() throws SocketException
	{
		return socket.getReceiveBufferSize();
   	}

   	public void setKeepAlive(boolean on) throws SocketException
	{
		socket.setKeepAlive(on);
   	}

   	public boolean getKeepAlive() throws SocketException
	{
		return socket.getKeepAlive();
   	}

   	public synchronized void close() throws IOException
	{
		socket.close();
		nativeShutdown(ssl);
   	}

    public void shutdownInput() throws IOException
    {
	    socket.shutdownInput();
   	}

   	public void shutdownOutput() throws IOException
   	{
		socket.shutdownOutput();
   	}

   	public String toString()
	{
		return ("SSL" + socket.toString());
	}
}


