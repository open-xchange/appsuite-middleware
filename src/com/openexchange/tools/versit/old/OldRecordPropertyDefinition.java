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

import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.StringScanner;

public class OldRecordPropertyDefinition extends OldCompoundPropertyDefinition {

	protected final OldShortPropertyDefinition[] Elements;

	public OldRecordPropertyDefinition(String[] paramNames,
			OldParamDefinition[] params, OldShortPropertyDefinition[] elements) {
		super(paramNames, params);
		Elements = elements;
	}

	protected Object parseValue(final Property property, final StringScanner s)
			throws IOException {
		final ArrayList<Object> al = new ArrayList<Object>();
		for (int i = 0; i < Elements.length; i++) {
			final String element = getElement(s);
			if (element == null || element.length() == 0) {
				al.add(null);
			} else {
				al.add(Elements[i].parseValue(property, new StringScanner(s,
						element.trim())));
			}
		}
		return al;
	}

	protected String writeValue(final Property property) {
		final StringBuilder sb = new StringBuilder();
		final ArrayList al = (ArrayList) property.getValue();
		if (Elements.length > 0 && al.size() > 0) {
			Object val = al.get(0);
			if (val != null) {
				sb.append(Elements[0].writeValue(property, val).replaceAll(";",
						"\\\\;"));
			}
			for (int i = 1; i < Elements.length && i < al.size(); i++) {
				sb.append(';');
				val = al.get(i);
				if (val != null) {
					sb.append(Elements[i].writeValue(property, val).replaceAll(
							";", "\\\\;"));
				}
			}
		}
		return sb.toString();
	}

}
