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

package com.openexchange.realtime.atmosphere.presence;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.OXRTHandler;
import com.openexchange.realtime.atmosphere.StanzaSender;
import com.openexchange.realtime.atmosphere.presence.osgi.AtmospherePresenceServiceRegistry;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Payload;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.presence.PresenceData;
import com.openexchange.realtime.presence.PresenceService;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link OXRTPresenceHandler} - Handles Stanzas related to Presence and PresenceSubscription.
 * Features:
 * <ul>
 *   <li>Ask others to see their Status, iow. subscribe to their Presence</li>
 *   <li>Give others permission to view my Status, iow. subscribe to my Presence</li>
 *   <li>Get a list of IDs you are subscribed to and their status</li>
 *   <li>Change my status</li>
 * </ul>
 * 
 * <h3>Final unavailable presence</h3>
 * 
 * <pre>
 * {
 *  [namespace: 'default',]
 *  element: 'presence',
 *  type: unavailable
 *  [data: {
 *      message: 'Bye'
 *  }]
 * };
 * </pre>
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class OXRTPresenceHandler implements OXRTHandler {

    @Override
    public String getElementPath() {
        return Presence.ELEMENTPATH;
    }

    @Override
    public void incoming(Stanza stanza, ServerSession session) throws OXException {
        if (stanza == null || session == null || !(stanza instanceof Presence)) {
            throw new IllegalArgumentException();
        }
        Presence presence = (Presence) stanza;
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
     * Handle the incoming request of userA to subscribe to the presence of userB.
     * This transforms the payload of the incoming stanza and passes it to the PresenceSubscriptionService.
     * 
     * <pre>
     * {
     *  from: ox://userA@realtime
     *  to: ox://userB@realtime
     *  namespace: 'presence',
     *  element: 'presence'
     *  type: subscribe
     *  session: $session
     *  [data: {
     *      message: 'Hello B, please let me subscribe to your presence. WBR, A.',
     *  }]
     * };
     * </pre>
     * 
     * @param stanza the incoming Stanza representing the subscribe request
     * @throws OXException if the subscription fails, e.g. Stanza can't be read
     */
    private void handleSubscribe(Presence stanza, ServerSession session) throws OXException {
        Payload payload = stanza.getPayload();
        PresenceSubscriptionService subscriptionService = AtmospherePresenceServiceRegistry.getInstance().getService(
            PresenceSubscriptionService.class);
        if (payload != null) {
            PresenceData data = (PresenceData) stanza.getPayload().to("presenceData", session).getData();
            subscriptionService.subscribe(stanza, data.getMessage(), session);
        } else {
            subscriptionService.subscribe(stanza, "", session);
        }
    }

    /**
     * Handle userB's incoming approval to prior subscription request of userA.
     * 
     * <pre>
     * {
     *  from: userB@realtime
     *  to: userA@realtime
     *  [namespace: 'default',]
     *  element: 'presence'
     *  type: subscribed
     *  [data: {
     *      message: 'Hello A, just don't spam me with kitten pics!'
     *  }]
     * };
     * </pre>
     * 
     * @param stanza the incoming Stanza representing the approval
     * @throws OXException If stanza conversion fails or the subscription can't be approved
     */
    private void handleSubscribed(Presence stanza, ServerSession session) throws OXException {
        PresenceData data = (PresenceData) stanza.getPayload().to("presenceData", session).getData();
        PresenceSubscriptionService subscriptionService = AtmospherePresenceServiceRegistry.getInstance().getService(
            PresenceSubscriptionService.class);
        subscriptionService.approve(stanza, session);
    }

    /**
     * Handle userA's request to unsubscribe from the Presence of userB.
     * 
     * <pre>
     * {
     *  from: userA@realtime
     *  to: userB@realtime
     *  [namespace: 'default',]
     *  element: 'presence'
     *  type: unsubscribe
     *  [data: {
     *      message: 'If you don't want my kitten pics, there is no reason to subscribe to you!',
     *  }]
     * };
     * </pre>
     * 
     * @param stanza
     * @throws OXException
     */
    private void handleUnSubscribe(Presence stanza, ServerSession session) throws OXException {
        PresenceData data = (PresenceData) stanza.getPayload().to("presenceData", session).getData();
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
     *  from: userB@realtime
     *  [namespace: 'default',]
     *  element: 'presence'
     *  [type: none]
     *  data: {
     *      state: 'online',
     *      message: 'i am here',
     *      [priority: 0,]
     *  }
     * };
     * </pre>
     * 
     * @param stanza the Stanza representing the unsubscribed request.
     * @throws OXException If stanza conversion fails or the subscription can't be denied/canceled
     */
    private void handleUnSubscribed(Presence stanza, ServerSession session) throws OXException {
        PresenceData data = (PresenceData) stanza.getPayload().to("presenceData", session).getData();
        PresenceSubscriptionService subscriptionService = AtmospherePresenceServiceRegistry.getInstance().getService(
            PresenceSubscriptionService.class);
        // TODO:
        // subscriptionService.subscribe(stanza, data.getMessage(), session);
        subscriptionService.approve(stanza, session);
    }

    /**
     * Change the curren Presence status.
     * 
     * @param stanza Stanza containing the new Presence Status
     * @throws OXException If stanza conversion fails or the status can't be changed
     */
    private void handlePresence(Presence stanza, ServerSession session) throws OXException {
        PresenceData data = (PresenceData) stanza.getPayload().to("presenceData", session).getData();
        PresenceService presenceService = AtmospherePresenceServiceRegistry.getInstance().getService(PresenceService.class);
        presenceService.changeState(stanza.getFrom(), data.getState(), data.getMessage(), session);
    }

    /*
     * Transport status changes and subscribe requests
     */
    @Override
    public void outgoing(Stanza stanza, ServerSession session, StanzaSender sender) throws OXException {
        Payload payload = stanza.getPayload();
        if(payload != null) {
            stanza.setPayload(payload.to("json", session));
        }
        sender.send(stanza);

    }

    /**
     * Update the PresenceStatus of a client. This involves several steps:
     * <ol>
     * <li>Set status in central status registry</li>
     * <li>Notify the user about the successful status update by sending him the new status back</li>
     * <li>Get active users from the roster of the client that sent the status update</li>
     * <li>Notify active users about the status update</li>
     * </ol>
     * 
     * @param client The client that sent a new PresenceStatus
     * @param status The new PresenceStatus of the client that has to be set
     * @param session The server session associated with the update request
     */
    public void updatePresenceStatus(ID client, PresenceData status, ServerSession session) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Get a list of clients a given client is subscribed to from the PresenceSubscriptionService and query their status from the
     * PresenceStatus.
     * 
     * @param requester The client requesting thes status map of clients he is subscribed to
     * @param session The associated session
     * @return a map of clients and associated status that a given client is subscribed to
     */
    public IDMap<PresenceData> getSubscriptionStatus(ID requester, ServerSession session) {
        throw new UnsupportedOperationException("Not implemented.");
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
