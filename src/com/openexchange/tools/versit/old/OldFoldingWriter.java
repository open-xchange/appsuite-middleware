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



package com.openexchange.tools.versit.old;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import com.openexchange.tools.versit.VersitDefinition;

public class OldFoldingWriter implements VersitDefinition.Writer {

	public final String charset;

	public final CharsetEncoder encoder;

	private final OutputStream w;

	private final StringBuilder sb = new StringBuilder();

	int Break = 0;

	public OldFoldingWriter(OutputStream stream, String charset) {
		w = stream;
		this.charset = charset;
		encoder = Charset.forName(this.charset).newEncoder();
	}

	public int lineLength() {
		return sb.length();
	}

	private static final byte[] SoftBreak = { '\r', '\n', ' ' };

	private static final byte[] HardBreak = { '\r', '\n' };

	public void write(final String s) throws IOException {
		sb.append(s);
		if (sb.length() > 76 && Break > 0) {
			w.write(sb.toString().getBytes(charset), 0, Break);
			w.write(SoftBreak);
			sb.delete(0, Break);
		}
		Break = sb.length();
	}

	public void rawStart() throws IOException {
		w.write(sb.toString().getBytes(charset));
		sb.setLength(0);
		Break = 0;
	}

	public void writeRaw(final byte[] data) throws IOException {
		w.write(data);
	}

	public void rawEnd() throws IOException {
		w.write(HardBreak);
	}

	public void writeln(final byte[] value) throws IOException {
		rawStart();
		writeRaw(value);
		rawEnd();
	}

	public void flush() throws IOException {
		w.flush();
	}

	public void close() throws IOException {
		w.close();
	}

}
