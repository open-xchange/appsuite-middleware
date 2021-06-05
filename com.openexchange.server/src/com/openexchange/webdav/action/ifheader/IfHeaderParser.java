/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
		while (i < length) {
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
		while (i < length) {
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
		while (i < length) {
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
		if (c != 'o' && c != 'O') {
			throw new IfHeaderParseException("Illegal character "+c+" in list",i+1);
		}
		c = cs.charAt(i++);
		if (c != 't' && c != 'T') {
			throw new IfHeaderParseException("Illegal character "+c+" in list",i+1);
		}

	}

	private IfHeaderEntity lockToken(final boolean matches, final String cs) throws IfHeaderParseException {
		try {
			final IfHeaderEntity entity =  new IfHeaderEntity.LockToken(tag(cs));
			entity.setMatches(matches);
			return entity;
		} catch (IfHeaderParseException x) {
			throw new IfHeaderParseException("Unfinished LockToken", x.getColumn());
		}

	}

	private IfHeaderEntity etag(final boolean matches, final String cs) throws IfHeaderParseException {
		final StringBuilder etag = new StringBuilder();
		final int start = i;
		final int length = cs.length();
		while (i < length) {
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
