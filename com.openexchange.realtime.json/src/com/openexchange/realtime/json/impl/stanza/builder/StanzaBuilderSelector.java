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

package com.openexchange.realtime.json.impl.stanza.builder;

import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.json.stanza.StanzaBuilder;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link StanzaBuilderSelector} - Select and instantiate a new StanzaBuilder matching the client's message.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class StanzaBuilderSelector {

    /**
     * Get a parser that is adequate for he JSONObject that has to be parsed.
     * Incoming JSONObjects must contain an <code>element</code> key that let's us determine the needed StanzaBuilder.
     *
     * <pre>
     * {
     *  element: 'presence'
     *  ...
     * };
     * </pre>
     * @param session 
     *
     * @param json the JSONObject that has to be parsed.
     * @return a Builder adequate for the JSONObject that has to be transformed
     * @throws IllegalArgumentException if the JSONObject is null
     * @throws OXException if the JSONObject doesn't contain a <code>element</code> key specifying the Stanza or no adequate
     *             StanzaBuilder can be found
     */
    public static StanzaBuilder<? extends Stanza> getBuilder(ID from, ServerSession session, JSONObject json) throws RealtimeException {
        if (json == null) {
            throw new IllegalArgumentException();
        }
        String element = json.optString("element");
        if (element == null) {
            throw RealtimeExceptionCodes.STANZA_BAD_REQUEST.create("Obligatory key \"%1$s\" is missing from the Stanza");
        }
        if (element.equalsIgnoreCase("iq")) {
            return new IQBuilder(from, json, session);
        } else if (element.equalsIgnoreCase("message")) {
            return new MessageBuilder(from, json, session);
        } else if (element.equalsIgnoreCase("presence")) {
            return new PresenceBuilder(from, json, session);
        } else {
            throw RealtimeExceptionCodes.STANZA_BAD_REQUEST.create("Could not find a builder for the given element: . \"%1$s\"");
        }
    }
}
