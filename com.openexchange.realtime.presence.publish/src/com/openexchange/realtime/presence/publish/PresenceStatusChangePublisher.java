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

package com.openexchange.realtime.presence.publish;

import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.presence.PresenceChangeListener;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link PresenceStatusChangePublisher} - Listens for PresenceStatus changes at the PresenceStatusService and publishes the status change
 * via the MessageDispatcher to all clients that are subscribed to the client who just changed his staus.
 * 
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class PresenceStatusChangePublisher implements PresenceChangeListener {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(PresenceStatusChangePublisher.class);

    private ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link PresenceStatusChangePublisher}.
     * 
     * @param serviceLookup The servicelookup to use.
     */
    public PresenceStatusChangePublisher(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public void initialPresence(Presence presence) {
        allPresence(presence);

    }

    @Override
    public void normalPresence(Presence stanza) {
        allPresence(stanza);

    }

    @Override
    public void finalPresence(Presence stanza) {
        allPresence(stanza);
    }

    /**
     * Handles a presence by either sending a directed Presence if the incoming Presence contains a recipient ID or by retrieving the
     * contacts that are subscribed to the presence of the sender.
     * 
     * @param stanza The incoming Presence Stanza
     */
    private void allPresence(Presence stanza) {
        try {
            PresenceSubscriptionService presenceSubscriptionService = serviceLookup.getService(PresenceSubscriptionService.class);
            if (presenceSubscriptionService == null) {
                throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getSimpleName());
            }
            if (isDirectedPresence(stanza)) {
                sendPresenceUpdate(stanza.getTo(), stanza);
            } else {
                List<ID> subscribers = presenceSubscriptionService.getSubscribers(stanza.getFrom().toGeneralForm());
                for (ID id : subscribers) {
                    sendPresenceUpdate(id, stanza);
                }
            }
        } catch (OXException e) {
            // Not much we can do at this point except logging the error
            LOG.error(e);
        }
    }

    /**
     * Check if the Presence Stanza is directed to only one person.
     * 
     * @param presence The Stanza to check
     * @return true if it is directed at one person, false if it's a global contact list update
     */
    private boolean isDirectedPresence(Presence presence) {
        ID to = presence.getTo();
        if (to == null) {
            return false;
        }
        return true;
    }

    /**
     * Send a Presence Stanza update to a contact.
     * 
     * @param contact The contact that should receive the update
     * @param presence The Presence Stanza that caused the update.
     * @throws OXException If sending the Presence update fails
     */
    private void sendPresenceUpdate(ID contact, Presence presence) throws OXException {
        MessageDispatcher messageDispatcher = serviceLookup.getService(MessageDispatcher.class);
        if (messageDispatcher == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(MessageDispatcher.class.getSimpleName());
        }
        Presence updatePresence = new Presence(presence);
        updatePresence.setFrom(presence.getFrom());
        updatePresence.setTo(contact);
        messageDispatcher.send(updatePresence);
    }

}
