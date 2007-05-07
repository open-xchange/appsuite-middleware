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
import java.io.InputStream;
import java.io.PushbackInputStream;

import com.openexchange.tools.versit.Scanner;

public class OldScanner extends Scanner {

	private final PushbackInputStream r;

	public boolean unfold;

	private boolean spaces;

	public OldEncoding DefaultEncoding = OldXBitEncoding.Default;

	public String DefaultCharset = "US-ASCII";

	public OldScanner(InputStream is) throws IOException {
		r = new PushbackInputStream(is);
		peek = readImpl();
	}

	@Override
	protected int readImpl() throws IOException {
		int c = r.read();
		Column++;
		switch (c) {
		case '\t':
		case ' ':
			spaces = true;
			break;
		case '\r':
		case '\n':
			if (unfold) {
				if (c == '\r') {
					c = r.read();
					if (c == '\n') {
						c = r.read();
					}
				} else {
					c = r.read();
				}
				Line++;
				if (c != '\t' && c != ' ') {
					r.unread(c);
					Column = 0;
					return -2;
				}
				if (Column >= 76 && !spaces) {
					c = r.read();
					Column = 2;
				} else {
					Column = 1;
				}
				spaces = false;
			} else {
				if (c == '\r') {
					c = r.read();
					if (c != '\n') {
						r.unread(c);
					}
				}
				Line++;
				Column = 0;
				spaces = false;
				return -2;
			}
		default:
		}
		return c;
	}

	public String parseWord() throws IOException {
		final StringBuilder sb = new StringBuilder();
		while (peek > ' ' && peek < 127 && peek != '[' && peek != ']'
				&& peek != '=' && peek != ':' && peek != '.' && peek != ','
				&& peek != ';') {
			sb.append((char) read());
		}
		return sb.toString();
	}

}
