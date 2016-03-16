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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.webdav.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

public class CapturingWebdavResponse implements WebdavResponse {
	private final WebdavResponse delegate;
	private CapturingOutputStream stream;

	public CapturingWebdavResponse(final WebdavResponse delegate) {
		this.delegate = delegate;
	}

	@Override
    public OutputStream getOutputStream() throws IOException {
		if(stream != null) {
			return stream;
		}
		return stream = new CapturingOutputStream(delegate.getOutputStream());
	}

	@Override
    public int getStatus() {
		return delegate.getStatus();
	}

	@Override
    public void setHeader(final String header, final String value) {
		delegate.setHeader(header, value);
	}

	@Override
    public void setStatus(final int status) {
		delegate.setStatus(status);
	}

	private static final class CapturingOutputStream extends OutputStream {
		private final OutputStream delegate;
		private final ByteArrayOutputStream capture;

		public CapturingOutputStream(final OutputStream delegate) {
			this.delegate = delegate;
			this.capture = new UnsynchronizedByteArrayOutputStream();
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}

		@Override
		public boolean equals(final Object arg0) {
			return delegate.equals(arg0);
		}

		@Override
		public void flush() throws IOException {
			delegate.flush();
		}

		@Override
		public int hashCode() {
			return delegate.hashCode();
		}

		@Override
		public String toString() {
			return delegate.toString();
		}

		@Override
		public void write(final byte[] arg0, final int arg1, final int arg2) throws IOException {
			delegate.write(arg0, arg1, arg2);
			capture.write(arg0,arg1,arg2);
		}

		@Override
		public void write(final byte[] arg0) throws IOException {
			delegate.write(arg0);
			capture.write(arg0);
		}

		@Override
		public void write(final int arg0) throws IOException {
			delegate.write(arg0);
			capture.write(arg0);
		}

		public ByteArrayOutputStream getCapture() {
			return capture;
		}
	}

	public String getBodyAsString() {
		if(stream == null) {
			return "No Body";
		}
		try {
			return new String(stream.getCapture().toByteArray(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			return e.toString();
		}
	}

	@Override
    public void setContentType(final String s) {
		delegate.setContentType(s);
	}

    @Override
    public void sendString(final String notFound) throws IOException {
        final byte[] bytes = notFound.getBytes("UTF-8");
        setHeader("Content-Length", String.valueOf(bytes.length));
        getOutputStream().write(bytes);
    }
}
