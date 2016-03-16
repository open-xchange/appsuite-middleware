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

package com.openexchange.webdav.action.ifheader;

import java.util.ArrayList;
import java.util.List;

// Not Thread Safe!
public class IfHeaderParser {

	private int i;

	public IfHeader parse(final String string) throws IfHeaderParseException {
		i = 0;
		return ifHeader(string);
	}

	private IfHeader ifHeader(final String cs) throws IfHeaderParseException {
		final IfHeader ifHeader = new IfHeader();
		String tag = null;
		final int length = cs.length();
		while(i < length) {
			final char c = cs.charAt(i++);
			switch(c) {
			case '(' : ifHeader.addList(list(tag, cs)); tag = null; break;
			case '<' : tag = tag(cs); break;
			case ' ' : break;
			default : throw new IfHeaderParseException("Illegal Character "+c+" for ifHeader", i+1);
			}
		}
		return ifHeader;
	}

	private String tag(final String cs) throws IfHeaderParseException {
		final StringBuffer tag = new StringBuffer();
		final int start = i;
		final int length = cs.length();
		while(i < length) {
			final char c = cs.charAt(i++);
			switch(c) {
			case '>' : return tag.toString();
			case ' ' : break;
			default : tag.append(c);
			}
		}
		throw new IfHeaderParseException("Unfinished Tag", start+1);
	}

	private IfHeaderList list(final String tag, final String cs) throws IfHeaderParseException {
		final List<IfHeaderEntity> list = new ArrayList<IfHeaderEntity>();
		boolean matches = true;
		final int start = i;
		final int length = cs.length();
		while(i < length) {
			final char c = cs.charAt(i++);
			switch(c) {
			case '<' : list.add(lockToken(matches, cs)); matches = true; break;
			case '[' : list.add(etag(matches, cs)); matches = true; break;
			case ')' : return new IfHeaderList(tag, list);
			case 'n' :
			case 'N' : not(cs); matches = false; break;
			case ' ' : break;
			default	 : throw new IfHeaderParseException("Illegal character "+c+" in list",i+1);
			}
		}
		throw new IfHeaderParseException("Unfinished List", start+1);
	}

	private void not(final String cs) throws IfHeaderParseException {
		char c = cs.charAt(i++);
		if(c != 'o' && c != 'O') {
			throw new IfHeaderParseException("Illegal character "+c+" in list",i+1);
		}
		c = cs.charAt(i++);
		if(c != 't' && c != 'T') {
			throw new IfHeaderParseException("Illegal character "+c+" in list",i+1);
		}

	}

	private IfHeaderEntity lockToken(final boolean matches, final String cs) throws IfHeaderParseException {
		try {
			final IfHeaderEntity entity =  new IfHeaderEntity.LockToken(tag(cs));
			entity.setMatches(matches);
			return entity;
		} catch (final IfHeaderParseException x) {
			throw new IfHeaderParseException("Unfinished LockToken", x.getColumn());
		}

	}

	private IfHeaderEntity etag(final boolean matches, final String cs) throws IfHeaderParseException {
		final StringBuilder etag = new StringBuilder();
		final int start = i;
		final int length = cs.length();
		while(i < length) {
			final char c = cs.charAt(i++);
			switch(c) {
			case ']' : final IfHeaderEntity entity = new IfHeaderEntity.ETag(etag.toString()); entity.setMatches(matches); return entity;
			case ' ' : break;
			default : etag.append(c);
			}
		}

		throw new IfHeaderParseException("Unfinished ETag", start+1);
	}

}
