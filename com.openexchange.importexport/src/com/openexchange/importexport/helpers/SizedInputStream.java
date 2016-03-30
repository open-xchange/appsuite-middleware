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

package com.openexchange.importexport.helpers;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.importexport.formats.Format;

/**
 * Defines a wrapper for an InputStream that also contains the size of this
 * InputStream. This is necessary to be able to set the correct size when
 * returning a HTTP-response - else the whole connection might be cancelled
 * either too early (resulting in corrupt data) or to late (resulting in
 * a lot of waiting).
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class SizedInputStream extends InputStream{

	private final InputStream in;
	private final long size;
	private final Format format;

	public SizedInputStream(final InputStream in, final long size, final Format format){
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
	public void mark(final int readlimit) {
		synchronized (this) {
			in.mark(readlimit);
		}
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return in.read(b);
	}

	@Override
	public void reset() throws IOException {
		synchronized (this) {
			in.reset();
		}
	}

	@Override
	public long skip(final long n) throws IOException {
		return in.skip(n);
	}

}
