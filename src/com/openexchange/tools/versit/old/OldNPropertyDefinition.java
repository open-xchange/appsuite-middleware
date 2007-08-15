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
import com.openexchange.tools.versit.StringScanner;

/**
 * @author Viktor Pracht (design)
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (bugfix 8844)
 *
 */
public class OldNPropertyDefinition extends OldCompoundPropertyDefinition {

	public OldNPropertyDefinition(String[] paramNames,
			OldParamDefinition[] params) {
		super(paramNames, params);
	}

	protected Object parseValue(final Property property, final OldScanner s, final byte[] value,
			final String charset) throws IOException {
		final ArrayList<Object> al = new ArrayList<Object>();
		final StringScanner ss = new StringScanner(s, new String(value, charset));
		String element = getElement(ss);
		while (ss.peek == ';') {
			final ArrayList<String> al2 = new ArrayList<String>();
			al2.add(element);
			al.add(al2);
			ss.read();
			element = getElement(ss);
		}
		final ArrayList<String> al2 = new ArrayList<String>();
		al2.add(element);
		al.add(al2);
		return al;
	}

	protected String writeValue(final Property property, final Object value) {
		final StringBuilder sb = new StringBuilder();
		final ArrayList al = (ArrayList) value;
		final int size = al.size();
		final Iterator i = al.iterator();
		if (size > 0) {
			Object val = i.next();
			if (val != null) {
				append(sb, val);
			}
			for (int k = 1; k < size; k++) {
				sb.append(';');
				val = i.next();
				if (val != null) {
					append(sb, val);
				}
			}
		}
		return sb.toString();
	}
	
	private void append(final StringBuilder sb, final Object list) {
		final ArrayList al = ((ArrayList) list);
		final int size = al.size();
		final Iterator i = al.iterator();
		if (size == 0) {
			return;
		}
		Object val = i.next(); //remember: size decreases by one, for loop must start with 1 - found during bugfix 8844 
		if (val != null) {
			sb.append(val.toString().replaceAll(";", "\\\\;"));
		}
		for (int k = 1; k < size; k++) {
			sb.append(',');
			val = i.next();
			if (val != null) {
				sb.append(val.toString().replaceAll(";", "\\\\;"));
			}
		}
	}

}
