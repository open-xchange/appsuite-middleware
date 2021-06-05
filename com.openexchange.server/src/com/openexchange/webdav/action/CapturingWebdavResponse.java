/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
		if (stream != null) {
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
		if (stream == null) {
			return "No Body";
		}
		try {
			return new String(stream.getCapture().toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
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
