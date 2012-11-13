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

package com.openexchange.realtime.atmosphere.presence.handler;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.StanzaSender;
import com.openexchange.realtime.atmosphere.impl.StanzaTransformer;
import com.openexchange.realtime.atmosphere.presence.initializer.PresenceInitializer;
import com.openexchange.realtime.atmosphere.presence.osgi.AtmospherePresenceServiceRegistry;
import com.openexchange.realtime.atmosphere.stanza.StanzaHandler;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.presence.PresenceData;
import com.openexchange.realtime.presence.PresenceStatusService;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link OXRTPresenceHandler} - Handles Stanzas related to Presence and PresenceSubscription. Features:
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
public class OXRTPresenceHandler implements StanzaHandler {

    @Override
    public Class<? extends Stanza> getStanzaClass() {
        return Presence.class;
    }

    @Override
    public void incoming(Stanza stanza, ServerSession session) throws OXException {
        if (stanza == null || session == null || !(stanza instanceof Presence)) {
            throw new IllegalArgumentException();
        }

        Presence presence = (Presence) stanza;
        PresenceInitializer initializer = new PresenceInitializer();
        presence = initializer.initialize(presence);

        Type type = presence.getType();
        if (Type.SUBSCRIBE == type) {
            handleSubscribe(presence, session);
        } else if (Type.SUBSCRIBED == type) {
            handleSubscribed(presence, session);
        } else if (Type.UNSUBSCRIBE == type) {
            handleUnSubscribe(presence, session);
        } else if (Type.UNSUBSCRIBED == type) {
            handleUnSubscribed(presence, session);
        } else if (Type.NONE == type || Type.UNAVAILABLE == type) {
            handlePresence(presence, session);
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
    private void handleSubscribe(Presence stanza, ServerSession session) throws OXException {
        PresenceSubscriptionService subscriptionService = AtmospherePresenceServiceRegistry.getInstance().getService(
            PresenceSubscriptionService.class);
        subscriptionService.subscribe(stanza, stanza.getMessage(), session);
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
    private void handleSubscribed(Presence stanza, ServerSession session) throws OXException {
        PresenceSubscriptionService subscriptionService = AtmospherePresenceServiceRegistry.getInstance().getService(
            PresenceSubscriptionService.class);
        subscriptionService.approve(stanza, session);
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
    private void handleUnSubscribe(Presence stanza, ServerSession session) throws OXException {
        PresenceSubscriptionService subscriptionService = AtmospherePresenceServiceRegistry.getInstance().getService(
            PresenceSubscriptionService.class);
        subscriptionService.approve(stanza, session);
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
    private void handleUnSubscribed(Presence stanza, ServerSession session) throws OXException {
        PresenceSubscriptionService subscriptionService = AtmospherePresenceServiceRegistry.getInstance().getService(
            PresenceSubscriptionService.class);
        subscriptionService.approve(stanza, session);
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
    private void handlePresence(Presence stanza, ServerSession session) throws OXException {
        AtmospherePresenceServiceRegistry serviceRegistry = AtmospherePresenceServiceRegistry.getInstance();
        PresenceSubscriptionService presenceSubscriptionService = serviceRegistry.getService(PresenceSubscriptionService.class, true);
        PresenceStatusService presenceStatusService = serviceRegistry.getService(PresenceStatusService.class, true);
        MessageDispatcher messageDispatcher = serviceRegistry.getService(MessageDispatcher.class, true);

        // Change the status of the incoming Stanza's client
        presenceStatusService.changePresenceStatus(stanza.getFrom(), new PresenceData(stanza.getState(), stanza.getMessage()), session);

        // Inform the client about status of his contacts
        if (isInitialPresence(stanza)) {
            List<ID> subscriptions = presenceSubscriptionService.getSubscriptions(session);
            IDMap<PresenceData> idToStatusMap = presenceStatusService.getPresenceStatus(subscriptions);
            for (ID id : idToStatusMap.keySet()) {
                PresenceData presenceData = idToStatusMap.get(id);

                // build Presence Stanza to tell joining client about the status of its contacts
                Presence presenceStanza = new Presence();
                presenceStanza.setFrom(id);
                presenceStanza.setTo(stanza.getFrom());
                presenceStanza.setState(presenceData.getState());
                presenceStanza.setMessage(presenceData.getMessage());
                // TODO: add delay from last statusChange to presenceStanza

                messageDispatcher.send(stanza, session);
            }
        }
        // TODO: PresenceService must honor priority
    }

    /**
     * Are we dealing with an initial Presence Stanza iow. was the client offline before?
     * 
     * @param stanza The incoming Presence Stanza that has to be insepcted
     * @return true if the client is sending an initial Presence, false otherwise
     * @throws OXException If the AtmospherePresenceService can't be queried
     */
    private boolean isInitialPresence(Presence stanza) throws OXException {
        boolean isInitial = false;
        AtmospherePresenceServiceRegistry serviceRegistry = AtmospherePresenceServiceRegistry.getInstance();
        PresenceStatusService presenceStatusService = serviceRegistry.getService(PresenceStatusService.class, true);
        PresenceData presenceData = presenceStatusService.getPresenceStatus(stanza.getFrom());
        if (PresenceState.OFFLINE.equals(presenceData.getState())) {
            isInitial = true;
        }
        return isInitial;
    }

    /**
     * Transport status changes and subscribe requests. Transform PayloadElements into JSON payloads and hand it over to the sender.
     */
    @Override
    public void outgoing(Stanza stanza, ServerSession session, StanzaSender sender) throws OXException {
        StanzaTransformer transformer = new StanzaTransformer();
        transformer.outgoing(stanza, session);
        sender.send(stanza);

    }

    /**
     * Get a list of clients a given client is subscribed to from the PresenceSubscriptionService and query their status from the
     * PresenceStatus.
     * 
     * @param requester The client requesting thes status map of clients he is subscribed to
     * @param session The associated session
     * @return a map of clients and associated status that a given client is subscribed to
     * @throws OXException
     */
    public IDMap<PresenceData> getSubscriptionStatus(ID requester, ServerSession session) throws OXException {
        AtmospherePresenceServiceRegistry serviceRegistry = AtmospherePresenceServiceRegistry.getInstance();
        PresenceSubscriptionService presenceSubscriptionService = serviceRegistry.getService(PresenceSubscriptionService.class, true);
        PresenceStatusService presenceStatusService = serviceRegistry.getService(PresenceStatusService.class, true);

        List<ID> subscribers = presenceSubscriptionService.getSubscribers(session);

        return null;
    }

    /**
     * Ask someone else for permission to subscribe/see his status. Done by triggering a method on the PresenceSubscriptionService.
     * 
     * @param requester The client asking for permission to subscribe to someone else
     * @param receiver The client the requester wants to ask for permission to subscribe
     * @param session The associated server session
     */
    public void askForSubscriptionAuthorization(ID requester, ID receiver, ServerSession session) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Give somebody else the permission to subscribe to me and see my status. This will persist the permission in the
     * PresenceSubscriptionService.
     * 
     * @param granter The client granting someone the permission to subscribe
     * @param receiver The client asking for permission to subscribe
     * @param session The associated serversession
     */
    public void grantSubscriptionAUthorization(ID granter, ID receiver, ServerSession session) {

    }

}
