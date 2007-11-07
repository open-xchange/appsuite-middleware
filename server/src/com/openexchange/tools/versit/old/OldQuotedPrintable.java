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
import java.util.ArrayList;

import com.openexchange.tools.versit.VersitException;

public class OldQuotedPrintable implements OldEncoding {

	public static final OldEncoding Default = new OldQuotedPrintable();

	private int getHexDigit(final OldScanner s) throws IOException {
		int retval;
		if (s.peek >= '0' && s.peek <= '9') {
			retval = s.read() - '0';
		}
		else if (s.peek >= 'A' && s.peek <= 'F') {
			retval = s.read() - 'A' + 10;
		}
		else if (s.peek >= 'a' && s.peek <= 'f') {
			retval = s.read() - 'a' + 10;
		}
		else {
			throw new VersitException(s,
					"Invalid character in Quoted-Printable encoding");
		}
		return retval;
	}

	public byte[] decode(final OldScanner s) throws IOException {
		final ArrayList<Byte> al = new ArrayList<Byte>();
		int len = 0;
		s.unfold = false;
		while (s.peek != -1 && s.peek != -2) {
			if (s.peek == '=') {
				len = al.size();
				s.read();
				if (s.peek >= '0' && s.peek <= '9' || s.peek >= 'A'
						&& s.peek <= 'F' || s.peek >= 'a' && s.peek <= 'f') {
					al.add(Byte.valueOf(
							(byte) ((getHexDigit(s) << 4) + getHexDigit(s))));
					len = al.size();
				} else if (s.peek == -2) {
					s.read();
				} else {
					al.add(Byte.valueOf((byte) '='));
				}
			} else if (s.peek >= 33 && s.peek <= 126) {
				al.add(Byte.valueOf((byte) s.read()));
				len = al.size();
			} else if (s.peek == 9 || s.peek == 32) {
				al.add(Byte.valueOf((byte) s.read()));
			} else {
				throw new VersitException(s,
						"Invalid character in Quoted-Printable encoding");
			}
		}
		byte[] retval = new byte[len];
		for (int i = 0; i < len; i++) {
			retval[i] =  al.get(i).byteValue();
		}
		s.unfold = true;
		return retval;
	}

	private byte hexDigit(final int value) {
		final int val = value & 15;
		return (byte) (val + (val > 9 ? 'A' - 10 : '0'));
	}

	private static final byte[] SoftBreak = { '=', '\r', '\n' };

	public void encode(final OldFoldingWriter fw, final byte[] b) throws IOException {
		int len = fw.lineLength();
		fw.rawStart();
		byte[] Escape = { '=', 0, 0 };
		byte[] Simple = { 0 };
		for (int i = 0; i < b.length; i++) {
			if (b[i] != '\t' && b[i] < 32 || b[i] == '=' || b[i] > 126
					|| i == b.length - 1 && (b[i] == '\t' || b[i] == ' ')) {
				if (len >= 73) {
					fw.writeRaw(SoftBreak);
					len = 0;
				}
				Escape[1] = hexDigit(b[i] >> 4);
				Escape[2] = hexDigit(b[i]);
				fw.writeRaw(Escape);
				len += 3;
			} else {
				if (len >= 75) {
					fw.writeRaw(SoftBreak);
					len = 0;
				}
				Simple[0] = b[i];
				fw.writeRaw(Simple);
				len++;
			}
		}
		fw.rawEnd();
	}
}
