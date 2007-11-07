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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Viktor Pracht
 */
public class StringScanner extends Scanner {

	private final String Text;
	
	private String UpcaseText;

	private int pos;

	/**
	 * Creates a scanner for a string.
	 * 
	 * @param text
	 *            is the string to scan.
	 */
	public StringScanner(final Scanner s, final String text) {
		Line = s.getLine();
		Column = s.getColumn();
		Text = text;
		peek = readImpl();
	}

	protected int readImpl() {
		if (pos < Text.length()) {
			Column++;
			return Text.charAt(pos++);
		}
		return -1;
	}

	public boolean match(final String text) {
		if (peek == -1) {
			return text.length() == 0;
		}
		final boolean retval = Text.startsWith(text, pos - 1);
		if (retval) {
			pos += text.length() - 1;
			peek = readImpl();
		}
		return retval;
	}

	public boolean imatch(final String text) {
		if (peek == -1) {
			return text.length() == 0;
		}
		if (UpcaseText == null) {
			UpcaseText = Text.toUpperCase();
		}
		final boolean retval = UpcaseText.startsWith(text.toUpperCase(Locale.ENGLISH), pos - 1);
		if (retval) {
			pos += text.length() - 1;
			peek = readImpl();
		}
		return retval;
	}
	
	public String regex(final Pattern pattern) {
		if (peek == -1) {
			return null;
		}
		final String rest = Text.substring(pos - 1, Text.length());
		final Matcher m = pattern.matcher(rest);
		if (!m.lookingAt()) {
			return null;
		}
		final String retval = Text.substring(pos - 1, pos - 1 + m.end());
		pos += m.end() - 1;
		peek = readImpl();
		return retval;
	}
	
	public String getRest() {
		if (peek < 0) {
			return "";
		}
		final int start = pos - 1;
		pos = Text.length();
		peek = -1;
		return Text.substring(start);
	}

}
