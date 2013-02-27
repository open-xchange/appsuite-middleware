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

package com.openexchange.realtime.dispatch.impl;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.dispatch.StanzaHandler;
import com.openexchange.realtime.dispatch.StanzaSender;
import com.openexchange.realtime.dispatch.osgi.RealtimeServiceRegistry;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.presence.PresenceStatusService;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;

/**
 * {@link PresenceHandler} - Handles Stanzas related to Presence and PresenceSubscription. Features:
 * <ul>
 * <li>Ask others to see their Status, iow. subscribe to their Presence</li>
 * <li>Give others permission to view my Status, iow. subscribe to my Presence</li>
 * <li>Deny or revoke others permission to view my Status</li>
 * <li>Get a list of IDs you are subscribed to and their status</li>
 * <li>Change my status</li>
 * </ul>
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PresenceHandler implements StanzaHandler {

    private final RealtimeServiceRegistry serviceRegistry;
    
    public PresenceHandler() {
        this.serviceRegistry = RealtimeServiceRegistry.getInstance();
    }
    
    @Override
    public Class<? extends Stanza> getStanzaClass() {
        return Presence.class;
    }

    @Override
    public void incoming(Stanza stanza) throws OXException {
        if (stanza == null || !(stanza instanceof Presence)) {
            throw new IllegalArgumentException();
        }

        Presence presence = (Presence) stanza;

        Type type = presence.getType();
        if (Type.SUBSCRIBE == type) {
            handleSubscribe(presence);
        } else if (Type.SUBSCRIBED == type) {
            handleSubscribed(presence);
        } else if (Type.UNSUBSCRIBE == type) {
            handleUnSubscribe(presence);
        } else if (Type.UNSUBSCRIBED == type) {
            handleUnSubscribed(presence);
        } else if (Type.NONE == type || Type.UNAVAILABLE == type) {
            handlePresence(presence);
        } else {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
    }

    /**
     * Handle the incoming request of userA to subscribe to the presence of userB. This transforms the payload of the incoming stanza and
     * passes it to the PresenceSubscriptionService.
     *
     * <pre>
     * {
     *   "session" : "$session",
     *   "element" : "presence",
     *   "to" : "ox://marens@1337",
     *   "type" : "subscribe",
     *   "data" : {
     *     "message" : "Hello marens, please let me subscribe to your presence, WBR., Mr. X"
     *   }
     * }
     *
     * </pre>
     *
     * @param stanza the incoming Stanza representing the subscribe request
     * @throws OXException if the subscription fails, e.g. Stanza can't be read
     */
    private void handleSubscribe(Presence stanza) throws OXException {
        PresenceSubscriptionService subscriptionService = serviceRegistry.getService(PresenceSubscriptionService.class);
        if(subscriptionService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
        }
        subscriptionService.subscribe(stanza, stanza.getMessage());
    }

    /**
     * Handle userB's incoming approval to prior subscription request of userA.
     *
     * <pre>
     * {
     *   "session" : "$session",
     *   "element" : "presence",
     *   "to" : "ox://mrx@1337",
     *   "type" : "subscribed",
     *   "data" : {
     *     "message" : "Hello Mr. X!"
     *   }
     * }
     * </pre>
     *
     * @param stanza the incoming Stanza representing the approval
     * @throws OXException If stanza conversion fails or the subscription can't be approved
     */
    private void handleSubscribed(Presence stanza) throws OXException {
        PresenceSubscriptionService subscriptionService = serviceRegistry.getService(PresenceSubscriptionService.class);
        if(subscriptionService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
        }
        subscriptionService.approve(stanza);
    }

    /**
     * Handle userA's request to unsubscribe from the Presence of userB.
     *
     * <pre>
     * {
     *   "session" : "$session",
     *   "element" : "presence",
     *   "to" : "ox://marens@1337",
     *   "type" : "unsubscribe"
     * }
     * </pre>
     *
     * @param stanza
     * @throws OXException
     */
    private void handleUnSubscribe(Presence stanza) throws OXException {
        PresenceSubscriptionService subscriptionService = serviceRegistry.getService(PresenceSubscriptionService.class);
        if(subscriptionService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
        }
        subscriptionService.approve(stanza);
    }

    /**
     * Used:
     * <ul>
     * <li>by userB to deny a prior subscription request made by userA</li>
     * <li>by userB to cancel the previously granted subscription to userA</li>
     * </ul>
     *
     * <pre>
     * {
     *   "session" : "$session",
     *   "element" : "presence",
     *   "to" : "ox://mrx@1337",
     *   "type" : "unsubscribed",
     *   "data" : {
     *     "message" : "Bye Mr. X!"
     *   }
     * }
     * </pre>
     *
     * @param stanza the Stanza representing the unsubscribed request.
     * @throws OXException If stanza conversion fails or the subscription can't be denied/canceled
     */
    private void handleUnSubscribed(Presence stanza) throws OXException {
        PresenceSubscriptionService subscriptionService = serviceRegistry.getService(PresenceSubscriptionService.class);
        if(subscriptionService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
        }
        subscriptionService.approve(stanza);
    }

    /**
     * Change the current Presence status.
     * <ol>
     * <li>Change the Status in the PresenceStatusService</li>
     * <li>Check if the Status changes from offline to * and send status of the IDs contacts back to the ID</li>
     * </ol>
     *
     * @param stanza Stanza containing the new Presence Status
     * @throws OXException If stanza conversion fails or the status can't be changed
     */
    private void handlePresence(Presence stanza) throws OXException {
        PresenceStatusService presenceStatusService = serviceRegistry.getService(PresenceStatusService.class);
        if(presenceStatusService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceStatusService.class.getName());
        }
        // TODO: add delay from last statusChange to presenceStanza
        // Change the status of the incoming Stanza's client
        presenceStatusService.changePresenceStatus(stanza);
    }

    /**
     * Transport status changes and subscribe requests. Transform PayloadElements into JSON payloads and hand it over to the sender.
     */
    @Override
    public void outgoing(Stanza stanza, StanzaSender sender) throws OXException {
//        StanzaTransformer transformer = new StanzaTransformer();
//        transformer.outgoing(stanza);
//        sender.send(stanza);
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
