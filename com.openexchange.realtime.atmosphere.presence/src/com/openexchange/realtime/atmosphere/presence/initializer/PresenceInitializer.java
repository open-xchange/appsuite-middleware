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

package com.openexchange.realtime.atmosphere.presence.initializer;

import java.util.Collection;
import com.openexchange.realtime.atmosphere.stanza.StanzaInitializer;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link PresenceInitializer}
 * 
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class PresenceInitializer implements StanzaInitializer<Presence> {

    @Override
    public Presence initialize(Presence presence) {
        initShow(presence);
        initStatus(presence);
        initPriority(presence);

        return presence;
    }

    /*
     * The Status Message
     */
    private void initStatus(Presence presence) {
        PayloadTree status = getSinglePayload(presence, Presence.STATUS_PATH);
        if (status != null) {
            Object data = status.getRoot().getPayloadElement().getData();
            if (!(data instanceof String)) {
                throw new IllegalStateException("Payload not transformed yet");
            }
            presence.setMessage((String)data);
        }
    }

    /*
     * The Status shown
     */
    private void initShow(Presence presence) {
        PayloadTree show = getSinglePayload(presence, Presence.SHOW_PATH);
        if (show != null) {
        Object data = show.getRoot().getPayloadElement().getData();
            if (!(data instanceof PresenceState)) {
                throw new IllegalStateException("Payload not transformed yet");
            }
            presence.setState((PresenceState)data);
        }
    }

    /*
     * The Priority of the Stanza
     */
    private void initPriority(Presence presence) {
        PayloadTree priority = getSinglePayload(presence, Presence.PRIORITY_PATH);
        if (priority != null) {
        Object data = priority.getRoot().getPayloadElement().getData();
            if (!(data instanceof Byte)) {
                throw new IllegalStateException("Payload not transformed yet");
            }
            presence.setPriority((Byte)data);
        }
    }

    /**
     * @param presence      The Presence Stanza to search in
     * @param elementPath   The ElementPath of the PayloadTree we want
     * @return Null or the PayloadTree matching the ElementPath
     */
    private PayloadTree getSinglePayload(Presence presence, ElementPath elementPath) {
        Collection<PayloadTree> trees = presence.getDefaultPayloads();
        PayloadTree candidate = null;
        for (PayloadTree tree : trees) {
            if(elementPath.equals(tree.getElementPath())) {
                candidate = tree;
                break;
            }
        }
        return candidate;
    }

}
