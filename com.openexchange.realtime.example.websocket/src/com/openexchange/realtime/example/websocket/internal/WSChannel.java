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

package com.openexchange.realtime.example.websocket.internal;

import java.util.Map;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link WSChannel}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class WSChannel implements Channel {
	
	private final IDMap<WebSocketServerHandler> handlerMap = new IDMap<WebSocketServerHandler>();
	private final HandlerLibrary library;
	
	public WSChannel(HandlerLibrary library) {
		this.library = library;
	}
	
	
	@Override
	public String getProtocol() {
		return "ws";
	}

	@Override
	public boolean canHandle(String namespace, ID recipient,
			ServerSession session) throws OXException {
		
		if (!isConnected(recipient, session)) {
			return false;
		}
		
		if (!hasCapability(recipient, namespace, session)) {
			return false;
		}
		
		if (library.getHandlerFor(namespace) == null) {
			return false;
		}
		
		return true;
	}

	private boolean hasCapability(ID recipient, String namespace,
			ServerSession session) {
		return true;
	}

	@Override
	public int getPriority() {
		return 200;
	}

	@Override
	public boolean isConnected(ID id, ServerSession session) throws OXException {
		return handlerMap.get(id) != null || !handlerMap.getEquivalents(id).isEmpty();
	}

	@Override
	public void send(Stanza stanza, ServerSession session) throws OXException {
		ID recipient = stanza.getTo();
		WebSocketServerHandler handler = handlerMap.get(recipient);
		if (handler != null) {
			handler.sendStanza(stanza);
			return;
		}
		for(Map.Entry<ID, WebSocketServerHandler> entry:   handlerMap.getEquivalents(recipient)) {
			entry.getValue().sendStanza(stanza);
		}
	}
	
	public void addHandler(WebSocketServerHandler handler) {
		handlerMap.put(handler.getID(), handler);
	}
	
	public void removeHandler(WebSocketServerHandler handler) {
		handlerMap.remove(handler.getID());
	}


}
