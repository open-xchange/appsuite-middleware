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



package com.openexchange.tools.servlet;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletInputStream;

import com.openexchange.ajp13.AJPv13Connection;
import com.openexchange.ajp13.AJPv13Exception;
import com.openexchange.ajp13.AJPv13Response;

/**
 * OXServletInputStream
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class OXServletInputStream extends ServletInputStream {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXServletInputStream.class);
	
	private final Lock MUTEX = new ReentrantLock();

	private final AJPv13Connection ajpCon;

	private byte[] data;

	private int pos;

	private boolean dataSet;

	private boolean isClosed;

	private static final String EXC_MSG = "No data found in servlet's input stream!";

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		isClosed = true;
	}

	/**
	 * @param ajpCon -
	 *            associated AJP connection
	 */
	public OXServletInputStream(final AJPv13Connection ajpCon) {
		this.ajpCon = ajpCon;
	}

	/**
	 * This method is called to set or append new data. If new data is
	 * <code>null</code> then value <code>-1</code> will be returned on
	 * invocations of any read method
	 * 
	 * @param newData -
	 *            the new data
	 * @throws IOException
	 */
	public void setData(final byte[] newData) throws IOException {
		MUTEX.lock();
		try {
			if (isClosed) {
				throw new IOException("InputStream is closed");
			}
			if (data != null && pos < data.length) {
				/*
				 * Copy rest of previous data
				 */
				final byte[] temp = new byte[data.length - pos];
				System.arraycopy(data, pos, temp, 0, temp.length);
				/*
				 * Append new data
				 */
				if (newData == null) {
					this.data = temp;
				} else {
					this.data = new byte[temp.length + newData.length];
					System.arraycopy(temp, 0, data, 0, temp.length);
					System.arraycopy(newData, 0, data, temp.length, newData.length);
				}
				pos = 0;
			} else {
				if (newData == null) {
					/*
					 * Data is set to null and dataSet is left to false
					 */
					data = null;
					pos = 0;
					return;
				}
				data = new byte[newData.length];
				System.arraycopy(newData, 0, data, 0, newData.length);
				pos = 0;
			}
			dataSet = true;
		} finally {
			MUTEX.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		MUTEX.lock();
		try {
			if (isClosed) {
				throw new IOException("OXServletInputStream.read(): InputStream is closed");
			} else if (!dataSet) {
				if (data == null || pos >= data.length) {
					return -1;
				}
				throw new IOException(new StringBuilder("OXServletInputStream.read(): ").append(EXC_MSG).toString());
			}
			if (pos >= data.length) {
				dataSet = false;
				if (!requestMoreDataFromWebServer()) {
					/*
					 * Web server sent an empty data package to indicate no more
					 * available data
					 */
					return -1;
				}
			}
			return (data[pos++] & 0xff);
		} finally {
			MUTEX.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		MUTEX.lock();
		try {
			if (isClosed) {
				throw new IOException("OXServletInputStream.read(byte[], int, int): InputStream is closed");
			} else if (!dataSet) {
				if (data == null || pos >= data.length) {
					return -1;
				}
				throw new IOException(new StringBuilder("OXServletInputStream.read(byte[], int, int): ")
						.append(EXC_MSG).toString());
			} else if (b == null) {
				throw new NullPointerException("OXServletInputStream.read(byte[], int, int): Byte array is null");
			} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException("OXServletInputStream.read(byte[], int, int): Invalid arguments");
			} else if (len == 0) {
				return 0;
			}
			final int numOfAvailableBytes = data.length - pos;
			/*
			 * Number of available bytes is greater than or equal to requested
			 * length (len)
			 */
			if (numOfAvailableBytes >= len) {
				System.arraycopy(data, pos, b, off, len);
				pos += len;
				return len;
			}
			/*
			 * Caller requests more than currently available bytes. First copy
			 * all available bytes into byte array.
			 */
			if (numOfAvailableBytes > 0) {
				System.arraycopy(data, pos, b, off, numOfAvailableBytes);
				pos = data.length;
			}
			dataSet = false;
			int remainingLen = len - numOfAvailableBytes;
			int numOfFilledBytes = numOfAvailableBytes;
			while (remainingLen > 0 && requestMoreDataFromWebServer()) {
				if (data.length >= remainingLen) {
					/*
					 * New data size is equal to or greater than remaining len
					 */
					System.arraycopy(data, pos, b, off + numOfFilledBytes, remainingLen);
					pos += remainingLen;
					return len;
				}
				/*
				 * Copy data from web server into byte array
				 */
				System.arraycopy(data, pos, b, off + numOfFilledBytes, data.length);
				pos = data.length;
				dataSet = false;
				numOfFilledBytes += data.length;
				remainingLen -= data.length;
			}
			return numOfFilledBytes == 0 ? -1 : numOfFilledBytes;
		} finally {
			MUTEX.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(final long n) throws IOException {
		MUTEX.lock();
		try {
			if (!dataSet) {
				if (data == null || pos >= data.length) {
					return 0;
				}
				throw new IOException("OXServletInputStream.skip(long): No data found");
			} else if (isClosed) {
				throw new IOException("OXServletInputStream.skip(long): InputStream is closed");
			} else if (n > Integer.MAX_VALUE) {
				throw new IOException("OXServletInputStream.skip(long): Too many bytes to skip: " + n);
			}
			final byte[] tmp = new byte[(int) n];
			return (read(tmp, 0, tmp.length));
		} finally {
			MUTEX.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		if (!dataSet) {
			if (data == null || pos >= data.length) {
				return 0;
			}
			throw new IOException("OXServletInputStream.available(): No data found");
		} else if (isClosed) {
			throw new IOException("OXServletInputStream.available(): InputStream is closed");
		}
		return (data.length - pos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return false;
	}

	/**
	 * Requests more data from web server. Note: If web server transmits an
	 * empty data package the setData() method sets data to <code>null</code>.
	 * This should be checked after calling this method.
	 * 
	 * @return <code>true</code> if new data could be read successfully,
	 *         <code>false</code> if no more data is expected or an empty data
	 *         package has been sent from web server
	 */
	private boolean requestMoreDataFromWebServer() throws IOException {
		try {
			if (ajpCon.getAjpRequestHandler().isAllDataRead()) {
				/*
				 * No more data expected
				 */
				return false;
			}
			ajpCon.getOutputStream().write(
					AJPv13Response.getGetBodyChunkBytes(ajpCon.getAjpRequestHandler().getNumOfBytesToRequestFor()));
			ajpCon.getOutputStream().flush();
			/*
			 * Trigger request handler to process expected incoming data package
			 * which in turn calls the setData() method.
			 */
			ajpCon.getAjpRequestHandler().processPackage();
			return (data != null);
		} catch (final AJPv13Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}
	}
}
