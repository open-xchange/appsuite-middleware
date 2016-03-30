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
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.json.stanza.StanzaBuilder;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PresenceBuilder} - Parse a request and build a Presence Stanza from it by adding the recipients ID.
 * Building includes transformation from JSON to POJO and Initialization from the PayloadTree.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PresenceBuilder extends StanzaBuilder<Presence> {

    /**
     * Create a new PresenceBuilder Initializes a new {@link PresenceBuilder}.
     *
     * @param from The sender's ID, must not be null
     * @param json The sender's message, must not be null
     * @param session 
     * @param serverSession The ServerSession associated with the incoming Stanza/Sender
     * @throws IllegalArgumentException if from or json are null
     */
    public PresenceBuilder(ID from, JSONObject json, ServerSession session) {
        super(session);
        if (from == null || json == null) {
            throw new IllegalArgumentException();
        }
        this.from = from;
        this.json = json;
        this.stanza = new Presence();
    }

    @Override
    public Presence build() throws RealtimeException {
        basics();
        type();
        return stanza;

    }

    private void type() {
        if (json.has("type")) {
            String type = json.optString("type");
            for (Presence.Type t : Presence.Type.values()) {
                if (t.name().equalsIgnoreCase(type)) {
                    stanza.setType(t);
                    break;
                }
            }
        } else {
            stanza.setType(Type.NONE);
        }
    }

}
