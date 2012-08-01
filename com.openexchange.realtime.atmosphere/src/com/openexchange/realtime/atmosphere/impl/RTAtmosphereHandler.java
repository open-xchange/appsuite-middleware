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

package com.openexchange.realtime.atmosphere.impl;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.OXRTHandler;
import com.openexchange.realtime.atmosphere.StanzaSender;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link RTAtmosphereHandler} - Handler that gets associated with the
 * {@link RTAtmosphereChannel} and does the main work of handling incoming and
 * outgoing Stanzas. Transformation of Stanzas is handed over to the proper
 * OXRTHandler 
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RTAtmosphereHandler implements AtmosphereHandler, StanzaSender {
	// TODO: Figure Out JSONP and Long-Polling State management. Hot-Swap the
	// AtmosphereResource.
	// TODO: Close connections and get rid of em

	private final ServiceLookup services;

	// Keep track of session <-> RTAtmosphereState associations
	private final ConcurrentMap<String, RTAtmosphereState> uuid2State;
	
	private final IDMap<RTAtmosphereState> id2State;
	private final HandlerLibrary library;

	/**
	 * Initializes a new {@link RTAtmosphereHandler}.
	 * 
	 * @param library  The library to use for OXRTHandler lookups needed for
	 *                  transformations of incoming and outgoing stanzas
	 * @param services The service-lookup providing needed services
	 */
	public RTAtmosphereHandler(HandlerLibrary library, ServiceLookup services) {
	    super();
	    uuid2State = new ConcurrentHashMap<String, RTAtmosphereState>();
	    id2State = new IDMap<RTAtmosphereState>();
		this.library = library;
		this.services = services;
	}

	@Override
    public void destroy() {
	    // Ignore for now
	}

	@Override
    public void onRequest(AtmosphereResource r) throws IOException {
		AtmosphereRequest req = r.getRequest();
		RTAtmosphereState state = getState(r);
		try {
			state.lock();
			if (req.getMethod().equalsIgnoreCase("GET")) {
				if (state.handshake) {
					r.getResponse().write("OK");
					state.handshake = false;
				} else {
					r.suspend();
					state.r = r;
				}
			} else {
				handleIncoming(StanzaParser.parse(req.getReader().readLine()),
						state);
			}

		} catch (OXException e) {
			// TODO
		} finally {
			state.unlock();
		}

	}

	/**
	 * Check the AtmosphereResource for the session header/parameter, add it to
	 * the uuid2state map that tracks 
	 * @param r the AtmosphereResource
	 * @return
	 */
	private RTAtmosphereState getState(AtmosphereResource r) {
		RTAtmosphereState state = new RTAtmosphereState();
		String session = r.getRequest()
				.getHeader("session");
		if (session == null) {
			session = r.getRequest().getParameter("session");
		}
		//TODO: missing session valid?
		RTAtmosphereState previous = uuid2State.putIfAbsent(session, state);
		return previous != null ? previous : state;
	}

	@Override
    public void onStateChange(AtmosphereResourceEvent evt) throws IOException {

	}

	protected void handleIncoming(Stanza stanza, RTAtmosphereState state)
			throws OXException {
		if (isInternal(stanza)) {
			handleInternally(stanza, state);
		} else {
			stampStanza(stanza, state);
			dispatchStanza(stanza, state);
		}
	}

	private boolean isInternal(Stanza stanza) {
		return stanza.getNamespace().startsWith("ox:");
	}

	private void handleInternally(Stanza stanza, RTAtmosphereState state)
			throws OXException {
		if ("ox:handshake".equals(stanza.getNamespace())) {
			getSession(stanza, state);
		}
	}

	private void getSession(Stanza stanza, RTAtmosphereState state)
			throws OXException {
		try {
			JSONObject json = (JSONObject) stanza.getPayload().getData();

			String sessionId = json.getString("session");

			Session session = services.getService(SessiondService.class)
					.getSession(sessionId);
			if (session == null) {
				throw OXException.general("Invalid sessionId " + sessionId);
			}

			state.session = ServerSessionAdapter.valueOf(session);
			state.id = new ID("ox", session.getLoginName(),
					contextName(session.getLogin()), json.optString("resource"));
			id2State.put(state.id, state);
		} catch (JSONException x) {
			throw OXException.general(x.toString());
		}

	}

	private String contextName(String login) {
		int index = login.indexOf('@');
		if (index < 0) {
			return "";
		}
		return login.substring(index + 1);
	}

	private void stampStanza(Stanza stanza, RTAtmosphereState state) {
		stanza.setFrom(state.id);
	}

	private void dispatchStanza(Stanza stanza, RTAtmosphereState state)
			throws OXException {
		OXRTHandler transformer = library.getHandlerFor(stanza.getNamespace());
		if (transformer == null) {
			throw OXException.general("No transformer for namespace "
					+ stanza.getNamespace());
		}
		transformer.incoming(stanza, state.session);
	}

	public void handleOutgoing(Stanza stanza, ServerSession session)
			throws OXException {
		OXRTHandler transformer = library.getHandlerFor(stanza.getNamespace());
		if (transformer == null) {
			throw OXException.general("No transformer for namesapce "
					+ stanza.getNamespace());
		}
		transformer.outgoing(stanza, session, this);
	}

	public boolean isConnected(ID id) {
		return id2State.get(id) != null
				|| !id2State.getEquivalents(id).isEmpty();
	}

	@Override
    public void send(Stanza stanza) throws OXException {
		String stanzaStr = StanzaWriter.write(stanza).toString();

		RTAtmosphereState state = id2State.get(stanza.getTo());
		if (state != null) {
			send(stanzaStr, state.r);
		} else {
			for (Entry<ID, RTAtmosphereState> equiState : id2State
					.getEquivalents(stanza.getTo())) {
				send(stanzaStr, equiState.getValue().r);
			}
		}

	}

	private void send(String stanzaStr, AtmosphereResource r) {
		r.getResponse().write(stanzaStr);
		switch (r.transport()) {
		case JSONP:
		case LONG_POLLING:
			r.resume();
			break;
		case WEBSOCKET:
		case STREAMING:
			try {
				r.getResponse().getWriter().flush();
			} catch (IOException e) {
				// IGNORE
			}
			break;
		}
	}

}
