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

package com.openexchange.importexport.helpers;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.importexport.Format;

/**
 * Defines a wrapper for an <code>InputStream</code> that also knows the size of the provided data.
 * <p>
 * This is necessary to be able to set the correct size when returning a HTTP-response.<br>
 * Else the whole connection might be cancelled either too early (resulting in corrupt data) or too late (resulting in a lot of waiting).
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class SizedInputStream extends InputStream {

	private final InputStream in;
	private final long size;
	private final Format format;

	/**
	 * Initializes a new {@link SizedInputStream}.
	 *
	 * @param in The input stream to delegate to
	 * @param size The size (if known) or <code>-1</code>
	 * @param format The format identifier
	 */
	public SizedInputStream(final InputStream in, final long size, final Format format) {
	    super();
		this.size = size;
		this.in = in;
		this.format = format;
	}

	/**
	 * Gets the size (if known)
	 *
	 * @return The size or <code>-1</code>
	 */
	public long getSize() {
		return this.size;
	}

	/**
	 * Gets the associated format
	 *
	 * @return The format
	 */
	public Format getFormat(){
		return this.format;
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public void mark(int readlimit) {
		in.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	@Override
	public void reset() throws IOException {
		in.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}

}
