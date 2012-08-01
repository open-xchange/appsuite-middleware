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

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IQ;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Payload;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Stanza;

/**
 * {@link StanzaParser} -  
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class StanzaParser {

	/**
	 * @param request the incoming request to parse into a Stanza  
	 * @return the parsed Stanza
	 * @throws OXException if the request String could not be parsed
	 */
	public static Stanza parse(String request) throws OXException {
		try {
			JSONObject object = new JSONObject(request);

			if (!object.has("kind")
					|| object.getString("kind").equalsIgnoreCase("message")) {
				return parseMessage(object);
			} else if (object.getString("kind").equalsIgnoreCase("presence")) {
				return parsePresence(object);
			} else if (object.getString("kind").equalsIgnoreCase("iq")) {
				return parseIQ(object);
			}

		} catch (JSONException e) {
			throw OXException.general("Could not parse " + request);
		}
		return null;
	}

	private static Stanza parseIQ(JSONObject object) {
		IQ query = new IQ();
		basics(query, object);

		String type = object.optString("type");

		for (IQ.Type t : IQ.Type.values()) {
			if (t.name().equalsIgnoreCase(type)) {
				query.setType(t);
				break;
			}
		}

		return query;
	}

	private static Stanza parsePresence(JSONObject object) {
		Presence presence = new Presence();
		basics(presence, object);

		return presence;
	}

	private static Stanza parseMessage(JSONObject object) {
		Message message = new Message();
		basics(message, object);

		String type = object.optString("type");

		if (type == null || type.equals("")) {
			message.setType(Message.Type.normal);
		} else {
			for (Message.Type t : Message.Type.values()) {
				if (t.name().equalsIgnoreCase(type)) {
					message.setType(t);
					break;
				}
			}
		}

		return message;
	}

	private static void basics(Stanza stanza, JSONObject object) {
		namespace(stanza, object);
		payload(stanza, object);
		to(stanza, object);
	}

	private static void to(Stanza message, JSONObject object) {
		if (object.has("to")) {
			message.setTo(new ID(object.optString("to")));
		}
	}

	private static void payload(Stanza message, JSONObject object) {
		if (object.has("data")) {
			message.setPayload(new Payload(object.optJSONObject("data"), "json"));
		}
	}

	private static void namespace(Stanza message, JSONObject object) {
		if (object.has("ns")) {
			message.setNamespace(object.optString("ns"));
		}

		if (object.has("namespace")) {
			message.setNamespace(object.optString("namespace"));
		}

	}

}
