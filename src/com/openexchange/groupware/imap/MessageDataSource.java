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

package com.openexchange.groupware.imap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;

import com.openexchange.api2.OXException;
import com.openexchange.tools.mail.ContentType;

/**
 * MessageDataSource
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MessageDataSource implements DataSource {

	protected static final int DEFAULT_BUF_SIZE = 0x2000;

	protected static final String DEFAULT_ENCODING = "UTF-8";

	protected byte[] data;

	protected String contentType;

	protected String name;

	/**
	 * Create a datasource from an input stream
	 */
	public MessageDataSource(final InputStream inputStream, final String contentType) throws IOException {
		this(inputStream, contentType, null);
	}

	/**
	 * Create a datasource from an input stream
	 */
	public MessageDataSource(final InputStream inputStream, final String contentType, final String name)
			throws IOException {
		this.contentType = contentType;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copyStream(inputStream, baos);
		data = baos.toByteArray();
		this.name = name;
	}

	/**
	 * Create a datasource from a byte array
	 */
	public MessageDataSource(final byte[] data, final String contentType) {
		this.contentType = contentType;
		this.data = new byte[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
	}

	/**
	 * Create a datasource from a String
	 */
	public MessageDataSource(final String data, final String contentType) throws UnsupportedEncodingException,
			OXException {
		this.contentType = contentType;
		final ContentType ct = new ContentType(contentType);
		this.data = data.getBytes(ct.getParameter("charset") == null ? DEFAULT_ENCODING : ct.getParameter("charset"));
	}

	/**
	 * returns the inputStream
	 */
	public InputStream getInputStream() throws IOException {
		if (data == null) {
			throw new IOException("no data");
		}
		return new ByteArrayInputStream(data);
	}

	/**
	 * Not implemented
	 */
	public OutputStream getOutputStream() throws IOException {
		throw new IOException(this.getClass().getName() + ".getOutputStream() isn't implemented");
	}

	/**
	 * returns the contentType for this data source
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * returns the name of this data source
	 */
	public String getName() {
		return name;
	}

	protected static int copyStream(final InputStream inputStreamArg, final OutputStream outputStreamArg)
			throws IOException {
		final InputStream inputStream = new BufferedInputStream(inputStreamArg);
		final OutputStream outputStream = new BufferedOutputStream(outputStreamArg);

		final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
		int len;
		int totalBytes = 0;
		while ((len = inputStream.read(bbuf)) != -1) {
			outputStream.write(bbuf, 0, len);
			totalBytes += len;
		}
		outputStream.flush();
		return totalBytes;
	}
}
