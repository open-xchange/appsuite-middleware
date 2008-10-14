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

package com.openexchange.ajp13;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;

import com.openexchange.ajp13.exception.AJPv13Exception;

/**
 * {@link AJPv13Request} - Abstract super class for AJP requests
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class AJPv13Request {

	/**
	 * Max size of an incoming request body: 8192 (8K) - 4 bytes (0x12 + 0x34 +
	 * data length integer)
	 * 
	 * @value 8188
	 */
	protected final static int MAX_REQUEST_BODY_CHUNK_SIZE = 8188;

	protected final byte[] payloadData;

	private int payloadDataIndex;

	/**
	 * Initializes a new {@link AJPv13Request}
	 * 
	 * @param payloadData
	 *            The payload data
	 */
	protected AJPv13Request(final byte[] payloadData) {
		this.payloadData = new byte[payloadData.length];
		System.arraycopy(payloadData, 0, this.payloadData, 0, payloadData.length);
		payloadDataIndex = 0;
	}

	/**
	 * Process the AJP request
	 * 
	 * @param ajpRequestHandler
	 *            The AJP request handler providing session data
	 * @throws AJPv13Exception
	 *             If an AJP error occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public abstract void processRequest(AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, IOException;

	/**
	 * Writes AJP response package
	 * 
	 * @param ajpRequestHandler
	 *            The AJP request handler providing session data
	 * @throws AJPv13Exception
	 *             If an AJP error occurs
	 * @throws ServletException
	 *             If a servlet occurs
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public void response(final AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception, ServletException,
			IOException {
		if (!ajpRequestHandler.isServiceMethodCalled()
				&& !(ajpRequestHandler.isFormData() && !ajpRequestHandler.isAllDataRead())) {
			/*
			 * Call servlet's service() method which will then request all
			 * receivable data chunks from client through OXServletInputStream
			 */
			ajpRequestHandler.doServletService();
		}
		final OutputStream out = ajpRequestHandler.getAJPConnection().getOutputStream();
		/*
		 * Send response headers first.
		 */
		ajpRequestHandler.doWriteHeaders(out);
		/*
		 * Get remaining data
		 */
		byte[] remainingData = null;
		try {
			remainingData = ajpRequestHandler.getAndClearResponseData();
		} catch (final IOException e) {
			remainingData = new byte[0];
		}
		if (remainingData.length > 0) {
			/*
			 * Send rest of data cut into MAX_BODY_CHUNK_SIZE pieces
			 */
			if (remainingData.length > AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE) {
				final byte[] currentData = new byte[AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE];
				do {
					final byte[] tmp = new byte[remainingData.length - currentData.length];
					System.arraycopy(remainingData, 0, currentData, 0, currentData.length);
					System.arraycopy(remainingData, currentData.length, tmp, 0, tmp.length);
					writeResponse(AJPv13Response.getSendBodyChunkBytes(currentData), out, true);
					remainingData = tmp;
				} while (remainingData.length > AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE);
			}
			if (remainingData.length > 0) {
				/*
				 * Send final SEND_BODY_CHUNK package
				 */
				writeResponse(AJPv13Response.getSendBodyChunkBytes(remainingData), out, false);
			}
		}
		/*
		 * Write END_RESPONSE package
		 */
		writeResponse(AJPv13Response.getEndResponseBytes(), out, true);
		ajpRequestHandler.setEndResponseSent();
	}

	/**
	 * Writes array of <code>byte</code> into <code>out</code>
	 */
	protected static final void writeResponse(final byte[] responseBytes, final OutputStream out,
			final boolean flushStream) throws IOException {
		out.write(responseBytes);
		if (flushStream) {
			out.flush();
		}
	}

	/**
	 * Writes <code>response</code> instance into <code>out</code>
	 */
	protected static final void writeResponse(final AJPv13Response response, final OutputStream out,
			final boolean flushStream) throws IOException, AJPv13Exception {
		writeResponse(response.getResponseBytes(), out, flushStream);
	}

	protected int parseInt() {
		return ((nextByte() & 0xff) << 8) + (nextByte() & 0xff);
	}

	protected final byte[] getByteSequence(final int numOfBytes) {
		final byte[] retval = new byte[numOfBytes];
		System.arraycopy(payloadData, payloadDataIndex, retval, 0, numOfBytes);
		payloadDataIndex += numOfBytes;
		return retval;
	}

	protected final byte nextByte() {
		return payloadData[payloadDataIndex++];
	}

	protected final boolean compareNextByte(final int compareTo) {
		if (hasNext()) {
			return (payloadData[payloadDataIndex] == compareTo ? true : false);
		}
		return false;
	}

	protected final boolean hasNext() {
		return (payloadDataIndex < payloadData.length);
	}

	protected static final int unsignedByte2Int(final byte b) {
		return (b & 0xff);
	}

}
