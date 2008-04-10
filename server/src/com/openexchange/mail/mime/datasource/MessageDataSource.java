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

package com.openexchange.mail.mime.datasource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.activation.DataSource;

import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MessageDataSource} - Allows creation of a data source by either an
 * input stream, a string or a byte array.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MessageDataSource implements DataSource {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MessageDataSource.class);

	private static final int DEFAULT_BUF_SIZE = 0x2000;

	private static final String DEFAULT_ENCODING = MailConfig.getDefaultMimeCharset();

	private final byte[] data;

	private final String contentType;

	private String name;

	/**
	 * Create a data source from an input stream
	 */
	public MessageDataSource(final InputStream inputStream, final String contentType) throws IOException {
		this(inputStream, contentType, null);
	}

	/**
	 * Create a data source from an input stream
	 */
	public MessageDataSource(final InputStream inputStream, final ContentType contentType) throws IOException {
		this(inputStream, contentType, null);
	}

	/**
	 * Create a data source from an input stream
	 */
	public MessageDataSource(final InputStream inputStream, final String contentType, final String name)
			throws IOException {
		this.contentType = contentType;
		final UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
		copyStream(inputStream, baos);
		data = baos.toByteArray();
		this.name = name;
	}

	/**
	 * Create a data source from an input stream
	 */
	public MessageDataSource(final InputStream inputStream, final ContentType contentType, final String name)
			throws IOException {
		this.contentType = contentType.toString();
		final UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
		copyStream(inputStream, baos);
		data = baos.toByteArray();
		this.name = name;
	}

	/**
	 * Create a data source from a byte array
	 */
	public MessageDataSource(final byte[] data, final String contentType) {
		this.contentType = contentType;
		this.data = new byte[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
	}

	/**
	 * Create a data source from a String
	 */
	public MessageDataSource(final String data, final String contentType) throws UnsupportedEncodingException,
			MailException {
		this.contentType = contentType;
		final ContentType ct = new ContentType(contentType);
		this.data = data.getBytes(ct.getCharsetParameter() == null ? DEFAULT_ENCODING : ct.getCharsetParameter());
	}

	/**
	 * Create a data source from a String
	 */
	public MessageDataSource(final String data, final ContentType contentType) throws UnsupportedEncodingException {
		this.contentType = contentType.toString();
		this.data = data.getBytes(contentType.getCharsetParameter() == null ? DEFAULT_ENCODING : contentType
				.getCharsetParameter());
	}

	/**
	 * returns the inputStream
	 */
	public InputStream getInputStream() throws IOException {
		if (data == null) {
			throw new IOException("no data");
		}
		return new UnsynchronizedByteArrayInputStream(data);
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

	protected static int copyStream(final InputStream inputStreamArg,
			final UnsynchronizedByteArrayOutputStream outputStream) throws IOException {
		final InputStream inputStream = inputStreamArg instanceof BufferedInputStream ? (BufferedInputStream) inputStreamArg
				: new BufferedInputStream(inputStreamArg);
		try {
			final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
			int len;
			int totalBytes = 0;
			while ((len = inputStream.read(bbuf)) != -1) {
				outputStream.write(bbuf, 0, len);
				totalBytes += len;
			}
			outputStream.flush();
			return totalBytes;
		} finally {
			try {
				inputStream.close();
			} catch (final IOException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
			try {
				outputStream.close();
			} catch (final IOException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}
}
