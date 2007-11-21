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
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import javax.servlet.ServletOutputStream;

import com.openexchange.tools.ajp13.AJPv13Connection;
import com.openexchange.tools.ajp13.AJPv13Response;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * OXServletOutputStream
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */

public final class OXServletOutputStream extends ServletOutputStream {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXServletOutputStream.class);

	private static final String ERR_OUTPUT_CLOSED = "OutputStream is closed";

	private final AJPv13Connection ajpCon;

	private final UnsynchronizedByteArrayOutputStream byteBuffer;

	private boolean isClosed;

	public OXServletOutputStream(final AJPv13Connection ajpCon) {
		this.ajpCon = ajpCon;
		byteBuffer = new UnsynchronizedByteArrayOutputStream(AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE);
	}

	public void resetBuffer() {
		byteBuffer.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		flushByteBuffer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(final int i) throws IOException {
		if (isClosed) {
			throw new IOException(ERR_OUTPUT_CLOSED);
		}
		if (byteBuffer.size() >= (AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE)) {
			responseToWebServer();
		}
		byteBuffer.write(i);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public byte[] getData() throws IOException {
		if (isClosed) {
			throw new IOException(ERR_OUTPUT_CLOSED);
		}
		/*
		 * try { byteBuffer.flush(); } catch (IOException e) {
		 * LOG.error(e.getMessage(), e); }
		 */
		return byteBuffer.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (isClosed) {
			throw new IOException(ERR_OUTPUT_CLOSED);
		} else if (b == null) {
			throw new NullPointerException("OXServletOutputStream.write(byte[], int, int): Byte array is null");
		} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException("OXServletOutputStream.write(byte[], int, int): Invalid arguments");
		} else if (len == 0) {
			return;
		}
		final int restCapacity = AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE - byteBuffer.size();
		if (len <= restCapacity) {
			/*
			 * Everything fits into buffer!
			 */
			byteBuffer.write(b, off, len);
			return;
		}
		/*
		 * Write fitting bytes into buffer
		 */
		byteBuffer.write(b, off, restCapacity);
		/*
		 * Write full byte buffer
		 */
		responseToWebServer();
		/*
		 * Write rest of byte array
		 */
		int numOfWrittenBytes = restCapacity;
		int numOfWithheldBytes = len - restCapacity;
		while (numOfWithheldBytes > AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE) {
			/*
			 * As long as withheld bytes exceed max body chunk size, write them
			 * cut into MAX_BODY_CHUNK_SIZE pieces
			 */
			final byte[] responseBodyChunk = new byte[AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE];
			System.arraycopy(b, off + numOfWrittenBytes, responseBodyChunk, 0, responseBodyChunk.length);
			byteBuffer.write(responseBodyChunk, 0, responseBodyChunk.length);
			responseToWebServer();
			numOfWrittenBytes += AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE;
			numOfWithheldBytes -= AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE;
		}
		/*
		 * Extract remaining bytes
		 */
		final byte[] withheldBytes = new byte[numOfWithheldBytes];
		System.arraycopy(b, off + numOfWrittenBytes, withheldBytes, 0, withheldBytes.length);
		/*
		 * Fill byte buffer with withheld bytes
		 */
		byteBuffer.write(withheldBytes, 0, withheldBytes.length);
	}

	private static final String ERR_BROKEN_PIPE = "Broken pipe";

	/**
	 * Sends response headers to web server if not done before and writes all
	 * buffered bytes cut into AJP SEND_BODY_CHUNK packages to web server
	 * 
	 * @throws IOException
	 */
	private void responseToWebServer() throws IOException {
		try {
			if (!ajpCon.getAjpRequestHandler().isHeadersSent()) {
				ajpCon.getOutputStream().write(
						AJPv13Response.getSendHeadersBytes(ajpCon.getAjpRequestHandler().getServletResponse()));
				ajpCon.getOutputStream().flush();
				ajpCon.getAjpRequestHandler().setHeadersSent(true);
				ajpCon.getAjpRequestHandler().getServletResponse().setCommitted(true);
			}
			/*
			 * Send data cut into MAX_BODY_CHUNK_SIZE pieces
			 */
			while (byteBuffer.size() > AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE) {
				final byte[] currentData = new byte[AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE];
				final byte[] tmp = new byte[byteBuffer.size() - AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE];
				final byte[] bufferedBytes = byteBuffer.toByteArray();
				System.arraycopy(bufferedBytes, 0, currentData, 0, currentData.length);
				System.arraycopy(bufferedBytes, currentData.length, tmp, 0, tmp.length);
				ajpCon.getOutputStream().write(AJPv13Response.getSendBodyChunkBytes(currentData));
				ajpCon.getOutputStream().flush();
				byteBuffer.reset();
				byteBuffer.write(tmp, 0, tmp.length);
			}
			if (byteBuffer.size() > 0) {
				ajpCon.getOutputStream().write(AJPv13Response.getSendBodyChunkBytes(byteBuffer.toByteArray()));
				ajpCon.getOutputStream().flush();
			}
			/*
			 * Since we do not expect any answer here, request handler's
			 * processPackage() method need not to be called.
			 */
			byteBuffer.reset();
		} catch (final SocketException e) {
			if (e.getMessage().indexOf(ERR_BROKEN_PIPE) != -1) {
				LOG.warn(new StringBuilder("Underlying (TCP) protocol communication aborted:").append(e.getMessage())
						.toString(), e);
			} else {
				LOG.error(e.getMessage(), e);
			}
			throw new IOException(e.getMessage());
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
			throw e;
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * @param chars
	 * @throws IOException
	 */
	public void write(final char[] chars) throws IOException {
		try {
			final String s = new String(chars);
			write(s.getBytes(ajpCon.getAjpRequestHandler().getServletResponse().getCharacterEncoding()));
		} catch (final UnsupportedEncodingException e) {
			LOG.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * @throws IOException
	 */
	public void flushByteBuffer() throws IOException {
		if (isClosed) {
			throw new IOException(ERR_OUTPUT_CLOSED);
		}
		responseToWebServer();
	}

	/**
	 * @throws IOException
	 */
	public void clearByteBuffer() throws IOException {
		if (isClosed) {
			throw new IOException(ERR_OUTPUT_CLOSED);
		}
		byteBuffer.reset();
	}
}
