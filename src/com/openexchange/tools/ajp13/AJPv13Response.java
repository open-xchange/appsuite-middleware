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



package com.openexchange.tools.ajp13;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.openexchange.tools.ajp13.AJPv13Exception.AJPCode;
import com.openexchange.tools.servlet.http.HttpServletResponseWrapper;

/**
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class AJPv13Response {

	public static final int MAX_INT_VALUE = 65535;

	public static final int MAX_PACKAGE_SIZE = 8192;
	
	public static final int MAX_SEND_BODY_CHUNK_SIZE = 8185; // 8192 - 7

	/**
	 * Byte sequence indicating a packet from Servlet Container to Web Server.
	 */
	private static final int[] PACKAGE_FROM_CONTAINER_TO_SERVER = { 'A', 'B' };

	public static final int SEND_BODY_CHUNK_PREFIX_CODE = 3;

	public static final int SEND_HEADERS_PREFIX_CODE = 4;

	public static final int END_RESPONSE_PREFIX_CODE = 5;

	public static final int GET_BODY_CHUNK_PREFIX_CODE = 6;

	public static final int CPONG_REPLY_PREFIX_CODE = 9;

	public static final Map<String, Integer> headerMap = new HashMap<String, Integer>();

	static {
		headerMap.put("Content-Type", Integer.valueOf(0x01));
		headerMap.put("Content-Language", Integer.valueOf(0x02));
		headerMap.put("Content-Length", Integer.valueOf(0x03));
		headerMap.put("Date", Integer.valueOf(0x04));
		headerMap.put("Last-Modified", Integer.valueOf(0x05));
		headerMap.put("Location", Integer.valueOf(0x06));
		headerMap.put("Set-Cookie", Integer.valueOf(0x07));
		headerMap.put("Set-Cookie2", Integer.valueOf(0x08));
		headerMap.put("Servlet-Engine", Integer.valueOf(0x09));
		headerMap.put("Status", Integer.valueOf(0x0A));
		headerMap.put("WWW-Authenticate", Integer.valueOf(0x0B));
	}

	private final int prefixCode;

	private int dataLength = -1;

	// private byte[] responseBytes = null;

	private ByteArrayOutputStream byteArray;

	private int contentLength = -1;

	private byte[] responseDataChunk;

	private HttpServletResponseWrapper servletResponse;

	private boolean closeConnection;

	public AJPv13Response(int prefixCode) {
		super();
		this.prefixCode = prefixCode;
	}

	public AJPv13Response(int prefixCode, boolean closeConnection) {
		super();
		this.prefixCode = prefixCode;
		this.closeConnection = closeConnection;
	}

	public AJPv13Response(int prefixCode, byte[] responseDataChunk) {
		super();
		this.prefixCode = prefixCode;
		this.responseDataChunk = new byte[responseDataChunk.length];
		System.arraycopy(responseDataChunk, 0, this.responseDataChunk, 0, responseDataChunk.length);
	}

	public AJPv13Response(int prefixCode, HttpServletResponseWrapper resp) {
		super();
		this.prefixCode = prefixCode;
		this.servletResponse = resp;
	}

	public AJPv13Response(int prefixCode, int requestedLength) {
		super();
		this.prefixCode = prefixCode;
		this.contentLength = requestedLength;
	}

	public byte[] getResponseBytes() throws AJPv13Exception {
		switch (prefixCode) {
		case SEND_BODY_CHUNK_PREFIX_CODE:
			final int length = responseDataChunk.length;
			if (length == 0) {
				throw new AJPv13Exception(AJPCode.NO_EMPTY_SENT_BODY_CHUNK);
			}
			/*
			 * prefix + chunk_length (2 bytes) + chunk bytes
			 */
			dataLength = 3 + length;
			if (dataLength + 4 > MAX_PACKAGE_SIZE) {
				throw new AJPv13MaxPackgeSizeException((dataLength + 4));
			}
			byteArray = new ByteArrayOutputStream(dataLength + 4);
			fillStartBytes();
			writeInt(length);
			writeByteArray(responseDataChunk);
			break;
		case SEND_HEADERS_PREFIX_CODE:
			/*
			 * prefix + http_status_code + http_status_msg (empty string) +
			 * num_headers (integer)
			 */
			final Map<String, List<String>> formattedCookies = servletResponse.getFormatedCookies();
			dataLength = getHeaderSizeInBytes(servletResponse) + getCookiesSizeInBytes(formattedCookies) + 5
					+ servletResponse.getStatusMsg().length() + 2 + 1;
			if (dataLength + 4 > MAX_PACKAGE_SIZE) {
				throw new AJPv13MaxPackgeSizeException((dataLength + 4));
			}
			byteArray = new ByteArrayOutputStream(dataLength + 4);
			fillStartBytes();
			writeInt(servletResponse.getStatus());
			writeString(servletResponse.getStatusMsg());
			writeInt(servletResponse.getHeadersSize() + getNumOfCookieHeader(formattedCookies));
			final int headersSize = servletResponse.getHeadersSize();
			final Iterator<String> iter = servletResponse.getHeaderNames();
			for (int i = 0; i < headersSize; i++) {
				final String headerName = iter.next();
				final String headerValue = servletResponse.getHeader(headerName);
				writeHeader(headerName, headerValue);
			}
			final int size = formattedCookies.size();
			final Iterator<Map.Entry<String, List<String>>> iter2 = formattedCookies.entrySet().iterator();
			for (int i = 0; i < size; i++) {
				final Map.Entry<String, List<String>> entry = iter2.next();
				final int listSize = entry.getValue().size();
				for (int j = 0; j < listSize; j++) {
					writeHeader(entry.getKey(), entry.getValue().get(j));
				}
			}
			break;
		case END_RESPONSE_PREFIX_CODE:
			dataLength = 2; // prefix + boolean (1 byte)
			/*
			 * No need to check against max package size cause it's a static
			 * package size of 6
			 */
			byteArray = new ByteArrayOutputStream(dataLength + 4);
			fillStartBytes();
			if (closeConnection) {
				writeBoolean(false);
			} else if (AJPv13Config.isAJPModJK()) {
                writeBoolean(true);
            } else {
				final boolean reuseConnection = AJPv13Server.getNumberOfOpenAJPSockets() <= AJPv13Config.getAJPMaxNumOfSockets();
				writeBoolean(reuseConnection);
			}
			break;
		case GET_BODY_CHUNK_PREFIX_CODE:
			dataLength = 3; // prefix + integer (2 bytes)
			/*
			 * No need to check against max package size cause it's a static
			 * package size of 7
			 */
			byteArray = new ByteArrayOutputStream(dataLength + 4);
			fillStartBytes();
			writeInt(contentLength);
			break;
		case CPONG_REPLY_PREFIX_CODE:
			break;
		default:
			throw new AJPv13Exception(AJPCode.UNKNOWN_PREFIX_CODE, Integer.valueOf(prefixCode));
		}
		return byteArray.toByteArray();
	}

	private static final int getHeaderSizeInBytes(final HttpServletResponseWrapper servletResponse) {
		int retval = 0;
		final int headersSize = servletResponse.getHeadersSize();
		final Iterator<String> iter = servletResponse.getHeaderNames();
		for (int i = 0; i < headersSize; i++) {
			final String headerName = iter.next();
			final String headerValue = servletResponse.getHeader(headerName);
			if (headerMap.containsKey(headerName)) {
				/*
				 * Header can be encoded as an integer
				 */
				retval += 2;
			} else {
				/*
				 * Header can NOT be encoded as an integer
				 */
				retval += headerName.length() + 3;
			}
			retval += headerValue.length() + 3;
		}
		return retval;
	}

	private static final int getCookiesSizeInBytes(final Map<String, List<String>> formattedCookies) {
		int retval = 0;
		final int size = formattedCookies.size();
		final Iterator<Map.Entry<String, List<String>>> iter = formattedCookies.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, List<String>> entry = iter.next();
			/*
			 * Set-Cookie and Set-Cookie2 is enoced in ajp protocol as integer
			 * value
			 */
			final boolean encodedHeader = headerMap.containsKey(entry.getKey());
			final int listSize = entry.getValue().size();
			for (int j = 0; j < listSize; j++) {
				retval += (encodedHeader ? 2 : (entry.getKey().length() + 3));
				retval += entry.getValue().get(j).length() + 3;
			}
		}
		return retval;
	}

	private static final int getNumOfCookieHeader(final Map<String, List<String>> formattedCookies) {
		int retval = 0;
		final int size = formattedCookies.size();
		final Iterator<Map.Entry<String, List<String>>> iter = formattedCookies.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			final Map.Entry<String, List<String>> entry = iter.next();
			retval += entry.getValue().size();
		}
		return retval;
	}

	private void writeHeader(final String name, final String value) throws AJPv13Exception {
		if (headerMap.containsKey(name)) {
			final int code = (0xA0 << 8) + (headerMap.get(name)).intValue();
			writeInt(code);
		} else {
			writeString(name);
		}
		writeString(value);
	}

	private void fillStartBytes() throws AJPv13Exception {
		writeByte(PACKAGE_FROM_CONTAINER_TO_SERVER[0]);
		writeByte(PACKAGE_FROM_CONTAINER_TO_SERVER[1]);
		writeInt(dataLength);
		writeByte(prefixCode);
	}

	private void writeByte(final int byteValue) {
		byteArray.write(byteValue);
	}

	private void writeByteArray(final byte[] bytes) {
		this.byteArray.write(bytes, 0, bytes.length);
	}

	private void writeInt(final int intValue) throws AJPv13Exception {
		if (intValue > MAX_INT_VALUE) {
			throw new AJPv13Exception(AJPCode.INTEGER_VALUE_TOO_BIG, Integer.valueOf(intValue));
		}
		final int high = (intValue >> 8);
		final int low = (intValue & (255));
		byteArray.write(high);
		byteArray.write(low);
	}

	private void writeBoolean(final boolean boolValue) {
		byteArray.write(boolValue ? 1 : 0);
	}

	private void writeString(final String strValue) throws AJPv13Exception {
		final int strLength = strValue.length();
		writeInt(strLength);
		/*
		 * Write string content and terminating '0'
		 */
		final char[] chars = strValue.toCharArray();
		final byte[] bytes = new byte[strLength];
		for (int i = 0; i < strLength; i++) {
			bytes[i] = (byte) chars[i];
		}
		byteArray.write(bytes, 0, strLength);
		byteArray.write(0);
	}

}
