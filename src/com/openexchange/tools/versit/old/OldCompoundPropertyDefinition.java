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
import java.util.Iterator;

import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.Scanner;
import com.openexchange.tools.versit.StringScanner;
import com.openexchange.tools.versit.VersitException;

public class OldCompoundPropertyDefinition extends OldShortPropertyDefinition {

	public OldCompoundPropertyDefinition(String[] paramNames,
			OldParamDefinition[] params) {
		super(paramNames, params);
	}

	protected String getElement(final Scanner s) throws IOException {
		final StringBuilder sb = new StringBuilder();
		while (s.peek != -1 && s.peek != -2 && s.peek != ';') {
			int c = s.read();
			if (c == '\\') {
				if (s.peek == -1 || s.peek == -2) {
					throw new VersitException(s,
							"Escape sequence \"\\\" CRLF is not supported.");
				}
				c = s.read();
			}
			sb.append((char) c);
		}
		return sb.length() == 0 ? null : sb.toString().trim();
	}

	protected Object parseValue(final Property property, final StringScanner s)
			throws IOException {
		final ArrayList<String> al = new ArrayList<String>();
		String element = getElement(s);
		while (s.peek == ';') {
			al.add(element);
			s.read();
			element = getElement(s);
		}
		al.add(element);
		return al;
	}

	protected String writeValue(final Property property, final Object value) {
		final StringBuffer sb = new StringBuffer();
		final ArrayList al = (ArrayList) value;
		final int size = al.size();
		final Iterator i = al.iterator();
		if (size > 0) {
			Object val = i.next();
			if (val != null) {
				sb.append(writeElement(property, val).replaceAll(";", "\\\\;"));
			}
			for (int k = 1; k < size; k++) {
				sb.append(';');
				val = i.next();
				if (val != null) {
					sb.append(writeElement(property, val).replaceAll(";",
							"\\\\;"));
				}
			}
		}
		return sb.toString();
	}

	protected String writeElement(final Property property, final Object value) {
		return value.toString();
	}
}
