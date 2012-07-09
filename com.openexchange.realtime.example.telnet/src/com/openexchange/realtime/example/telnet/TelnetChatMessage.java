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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.example.telnet;

import java.util.HashMap;
import java.util.Map;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TelnetChatMessage}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 */
public class TelnetChatMessage {
	private Map<String, String> headers = new HashMap<String, String>();
	private String payload = "";
	private ServerSession session;
	private ID from;

	public TelnetChatMessage(String payload, ID from, ServerSession session) {
		super();
		this.payload = payload;
		this.session = session;
		this.from = from;
	}
	
	public TelnetChatMessage(String payload, Map<String, String> headers, ID from, ServerSession session) {
		super();
		this.payload = payload;
		this.session = session;
		this.headers = headers;
		this.from = from;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public void requireHeader(String...names) throws OXException {
		for (String string : names) {
			if (!headers.containsKey(string)) {
				throw OXException.general("Needed Header: "+string);
			}
		}
	}

	public ServerSession getSession() {
		return session;
	}
	
	public ID getFrom() {
		return from;
	}

	public void setHeader(String header, String value) {
		headers.put(header, value);
	}
	
	public ID getTo() {
		return new ID(getHeader("to"));
	}
	

}
