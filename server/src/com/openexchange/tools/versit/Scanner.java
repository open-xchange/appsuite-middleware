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

package com.openexchange.tools.versit;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Viktor Pracht
 */
public abstract class Scanner implements VersitDefinition.Reader {

	/**
	 * Look-ahead character.
	 */
	public int peek;

	protected int Column = 0;

	protected int Line = 0;

	public int getColumn() {
		return Column;
	}

	public int getLine() {
		return Line;
	}

	public void skipWS() throws IOException {
		while (peek == '\t' || peek == ' ') {
			read();
		}
	}

	public String parseName() throws IOException {
		final StringBuilder sb = new StringBuilder();
		while (peek >= 'A' && peek <= 'Z' || peek >= 'a' && peek <= 'z' || peek == '-' || peek >= '0' && peek <= '9') {
			sb.append((char) read());
		}
		return sb.toString();
	}

	public int parseNumber() throws IOException {
		if (peek < '0' || peek > '9') {
			throw new VersitException(this, "Number expected");
		}
		int retval = 0;
		while (peek >= '0' && peek <= '9') {
			retval = retval * 10 + read() - '0';
		}
		return retval;
	}

	public void parseNumber(final StringBuffer sb) throws IOException {
		if (peek < '0' || peek > '9') {
			throw new IOException("Number expected");
		}
		do {
			sb.append((char) read());
		} while (peek >= '0' && peek <= '9');
	}

	public int parseNumber(final int digits) throws IOException {
		int retval = 0;
		for (int i = 0; i < digits; i++) {
			if (peek < '0' || peek > '9') {
				throw new VersitException(this, digits + "-digit number expected");
			}
			retval = retval * 10 + read() - '0';
		}
		return retval;
	}

	public void parseNumber(final StringBuffer sb, final int digits) throws IOException {
		for (int i = 0; i < digits; i++) {
			if (peek < '0' || peek > '9') {
				throw new IOException(digits + "-digit number expected");
			}
			sb.append((char) read());
		}
	}

	public boolean optionalNumber(final StringBuffer sb) throws IOException {
		if (peek < '0' || peek > '9') {
			return false;
		}
		sb.setLength(0);
		while (peek >= '0' && peek <= '9') {
			sb.append((char) read());
		}
		return true;
	}

	public int[] parseNumList() throws IOException {
		final ArrayList list = new ArrayList();
		while (true) {
			int sign = 1;
			if (peek == '+') {
				read();
			} else if (peek == '-') {
				sign = -1;
				read();
			}
			int i = parseNumber();
			list.add(Integer.valueOf(i * sign));
			if (peek != ',') {
				break;
			}
			read();
		}
		int[] retval = new int[list.size()];
		for (int i = 0; i < retval.length; i++) {
			retval[i] = ((Integer) list.get(i)).intValue();
		}
		return retval;
	}

	/**
	 * Returns a single unfolded character and updates peek.
	 * 
	 * @return The character, or -1 at the end of the stream, or -2 at the end
	 *         of a logical line.
	 * @throws IOException
	 */
	public int read() throws IOException {
		final int retval = peek;
		peek = readImpl();
		return retval;
	}

	/**
	 * Reads a single character and performs unfolding.
	 * 
	 * @return The character, or -1 at the end of the stream, or -2 at the end
	 *         of a logical line.
	 * @throws IOException
	 */
	protected abstract int readImpl() throws IOException;

}
