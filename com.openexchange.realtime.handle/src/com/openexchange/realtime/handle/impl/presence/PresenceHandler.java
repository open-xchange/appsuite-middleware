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

package com.openexchange.realtime.handle.impl.presence;

import java.util.concurrent.BlockingQueue;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.handle.impl.AbstractStrategyHandler;
import com.openexchange.realtime.handle.impl.HandlerStrategy;
import com.openexchange.realtime.handle.impl.Services;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.presence.PresenceStatusService;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.realtime.util.IDMap;

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
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class PresenceHandler extends AbstractStrategyHandler<Presence> {

    public PresenceHandler(BlockingQueue<Presence> queue) {
        super(queue, new HandlerStrategy<Presence>());
    }

    @Override
    public void handleToIsNull(Presence stanza) throws OXException {
        /*
         * If the server receives a
         * presence stanza with no 'to' attribute, the server SHOULD broadcast
         * it to the entities that are subscribed to the sending entity's
         * presence, if applicable (the semantics of presence broadcast for
         * instant messaging and presence applications are defined in
         * [XMPP-IM]).
         */
        Type type = stanza.getType();
        if (Type.SUBSCRIBE == type) {
            handleSubscribe(stanza);
        } else if (Type.SUBSCRIBED == type) {
            handleSubscribed(stanza);
        } else if (Type.UNSUBSCRIBE == type) {
            handleUnSubscribe(stanza);
        } else if (Type.UNSUBSCRIBED == type) {
            handleUnSubscribed(stanza);
        } else if (Type.NONE == type || Type.UNAVAILABLE == type) {
            handlePresence(stanza);
        } else {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
    }

    @Override
    public void handleAccountNotExists(Presence stanza) throws OXException {
        /*
         * Else if the JID is of the form <user@domain> or <user@domain/resource> and 
         * the associated user account does not exist, the recipient's server (a) 
         * SHOULD silently ignore the stanza (i.e., neither deliver it nor return an error) 
         * if it is a presence stanza [...]
         */
        return;
    }

    @Override
    public void handleInboundStanzaWithConcreteRecipient(Presence stanza) throws OXException {
        ResourceDirectory resourceDirectory = getResourceDirectory();
        ID to = stanza.getTo();
        IDMap<Resource> idMap = resourceDirectory.get(to);
        if (idMap.isEmpty()) {
            /*
             * Else if the JID is of the form <user@domain/resource> and no available resource 
             * matches the full JID, the recipient's server (a) SHOULD silently ignore the stanza 
             * (i.e., neither deliver it nor return an error) if it is a presence stanza [...]
             */
            return;
        }

        MessageDispatcher messageDispatcher = getMessageDispatcher();
        messageDispatcher.send(stanza, idMap);
    }

    @Override
    public void handleInboundStanzaWithGeneralRecipient(Presence stanza) throws OXException {
        ResourceDirectory resourceDirectory = getResourceDirectory();
        ID to = stanza.getTo();
        IDMap<Resource> idMap = resourceDirectory.get(to);
        if (idMap.isEmpty()) {
            /*
             * Else if the JID is of the form <user@domain> and there are no available resources 
             * associated with the user, how the stanza is handled depends on the stanza type:
             */
            Type type = stanza.getType();
            if (type == Type.SUBSCRIBE || type == Type.SUBSCRIBED || type == Type.UNSUBSCRIBE || type == Type.UNSUBSCRIBED) {
                /*
                 * 1. For presence stanzas of type "subscribe", "subscribed", "unsubscribe", and "unsubscribed", 
                 * the server MUST maintain a record of the stanza and deliver the stanza at least once 
                 * (i.e., when the user next creates an available resource); in addition, the server MUST 
                 * continue to deliver presence stanzas of type "subscribe" until the user either approves 
                 * or denies the subscription request (see also Presence Subscriptions). 
                 */
                storeStanzaForDelayedDelivery(stanza);
            } else {
                /*
                 * 2. For all other presence stanzas, the server SHOULD silently ignore the stanza by not 
                 * storing it for later delivery or replying to it on behalf of the user.
                 */
                return;
            }
        } else {
            /*
             * Else if the JID is of the form <user@domain> and there is at least one available resource 
             * available for the user, the recipient's server MUST follow these rules:
             * 
             * For presence stanzas other than those of type "probe", the server MUST deliver the stanza 
             * to all available resources; for presence probes, the server SHOULD reply based on the rules 
             * defined in Presence Probes. In addition, the server MUST NOT rewrite the 'to' attribute 
             * (i.e., it MUST leave it as <user@domain> rather than change it to <user@domain/resource>).
             * 
             * TODO: implement probe
             */
            MessageDispatcher messageDispatcher = getMessageDispatcher();
            messageDispatcher.send(stanza, idMap);
        }
    }

    @Override
    public void handleOutboundStanza(Presence stanza) throws OXException {
        // TODO Implement me
    }

    @Override
    public boolean applyPrivacyLists(Presence stanza) throws OXException {
        // TODO Implement me
        return true;
    }

    private void storeStanzaForDelayedDelivery(Presence stanza) {
        // TODO: Implement me
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
        PresenceSubscriptionService subscriptionService = Services.getService(PresenceSubscriptionService.class);
        if (subscriptionService == null) {
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
        PresenceSubscriptionService subscriptionService = Services.getService(PresenceSubscriptionService.class);
        if (subscriptionService == null) {
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
        PresenceSubscriptionService subscriptionService = Services.getService(PresenceSubscriptionService.class);
        if (subscriptionService == null) {
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
        PresenceSubscriptionService subscriptionService = Services.getService(PresenceSubscriptionService.class);
        if (subscriptionService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
        }
        subscriptionService.approve(stanza);
    }

    /**
     * Change the current Presence status of the client in the ResourceDirectory.
     * @param stanza Stanza containing the new Presence Status
     * @throws OXException If stanza conversion fails or the status can't be changed
     */
    private void handlePresence(Presence stanza) throws OXException {
        PresenceStatusService presenceStatusService = Services.getService(PresenceStatusService.class);
        if (presenceStatusService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceStatusService.class.getName());
        }
        ResourceDirectory resourceDirectory = getResourceDirectory();
        Resource resource = new DefaultResource(stanza.getState());
        resourceDirectory.set(stanza.getFrom(), resource);
        
    }
}
