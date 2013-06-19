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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.handle.HandleExceptionCode;
import com.openexchange.realtime.handle.impl.AbstractStrategyHandler;
import com.openexchange.realtime.handle.impl.HandlerStrategy;
import com.openexchange.realtime.handle.impl.Services;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
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
 * See http://xmpp.org/rfcs/rfc3921.html#presence
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class PresenceHandler extends AbstractStrategyHandler<Presence> {

    private final static Log LOG = com.openexchange.log.Log.loggerFor(PresenceHandler.class);

    public PresenceHandler(BlockingQueue<Presence> queue) {
        super(queue, new HandlerStrategy<Presence>());
    }

    @Override
    public void handleToIsNull(Presence stanza) {
        try {
            /*
             * If the server receives a presence stanza with no 'to' attribute, the server SHOULD broadcast it to the entities that are
             * subscribed to the sending entity's presence, if applicable (the semantics of presence broadcast for instant messaging and
             * presence applications are defined in [XMPP-IM]).
             */
            Type type = stanza.getType();
            if (Type.NONE == type || Type.UNAVAILABLE == type) {
                handleBroadcastPresence(stanza);
            } else {
                stanza.trace("Discarding. Only Stanzas of type NONE or UNAVAILABLE are suported for broadcasting.");
                throw new IllegalArgumentException("Only Stanzas of type NONE or UNAVAILABLE are suported for broadcasting.");
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void handleAccountNotExists(Presence stanza) {
        /*
         * Else if the JID is of the form <user@domain> or <user@domain/resource> and the associated user account does not exist, the
         * recipient's server (a) SHOULD silently ignore the stanza (i.e., neither deliver it nor return an error) if it is a presence
         * stanza [...]
         */
        stanza.trace("Discarding. Accound does not exist.");
        return;
    }

    @Override
    public void handleInboundStanzaWithConcreteRecipient(Presence presence) {
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
            handleDirectedPresence(presence);
        } else {
            presence.trace("Discarding. Handling of Type " + type + " isn't implemented yet!");
            throw new UnsupportedOperationException("Handling of Type " + type + " isn't implemented yet!");
        }
    }

    @Override
    public void handleInboundStanzaWithGeneralRecipient(Presence presence) {
        /*
         * If the JID is of the form <user@domain> and there are no available resources associated with the user, how the stanza is handled
         * depends on the stanza type: 1. For presence stanzas of type "subscribe", "subscribed", "unsubscribe", and "unsubscribed", the
         * server MUST maintain a record of the stanza and deliver the stanza at least once (i.e., when the user next creates an available
         * resource); in addition, the server MUST continue to deliver presence stanzas of type "subscribe" until the user either approves
         * or denies the subscription request (see also Presence Subscriptions). 2. For all other presence stanzas, the server SHOULD
         * silently ignore the stanza by not storing it for later delivery or replying to it on behalf of the user. Else if the JID is ofthe
         * form <user@domain> and there is at least one available resource available for the user, the recipient's server MUST follow these
         * rules: For presence stanzas other than those of type "probe", the server MUST deliver the stanza to all available resources; for
         * presence probes, the server SHOULD reply based on the rules defined in Presence Probes. In addition, the server MUST NOT rewrite
         * the 'to' attribute (i.e., it MUST leave it as <user@domain> rather than change it to <user@domain/resource>).
         */
        // TODO: implement probe
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
            handleDirectedPresence(presence);
        } else {
            throw new UnsupportedOperationException("Handling of Type " + type + " isn't implemented yet!");
        }
    }

    @Override
    public void handleOutboundStanza(Presence stanza) {
        try {
            stanza.trace("Unable to handle broadcast stanza. Not implemented yet.");
            throw new UnsupportedOperationException("Not implemented yet.");
        } catch (Exception e) {
            // TODO: send error stanza to stanza.from or other proper error handling for messages depending on this case.
            LOG.error("Unable to handle broadcast stanza.", e);
        }
    }

    @Override
    public boolean applyPrivacyLists(Presence stanza) {
        LOG.error("Not implemented yet");
        return true;
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
    private void handleSubscribe(Presence stanza) {
        try {
            stanza.trace("handle subscribe");
            PresenceSubscriptionService subscriptionService = Services.getService(PresenceSubscriptionService.class);
            if (subscriptionService == null) {
                throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
            }
            subscriptionService.subscribe(stanza, stanza.getMessage());
        } catch (OXException oxe) {
            handleError(stanza, oxe);
        }
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
    private void handleSubscribed(Presence stanza) {
        try {
            stanza.trace("handle subscribed");
            PresenceSubscriptionService subscriptionService = Services.getService(PresenceSubscriptionService.class);
            if (subscriptionService == null) {
                throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
            }
            subscriptionService.approve(stanza);
        } catch (OXException oxe) {
            handleError(stanza, oxe);
        }
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
    private void handleUnSubscribe(Presence stanza) {
        try {
            stanza.trace("handle unsubscribe");
            PresenceSubscriptionService subscriptionService = Services.getService(PresenceSubscriptionService.class);
            if (subscriptionService == null) {
                throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
            }
            subscriptionService.approve(stanza);
        } catch (OXException oxe) {
            handleError(stanza, oxe);
        }
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
    private void handleUnSubscribed(Presence stanza) {
        try {
            stanza.trace("handle unsubscribed");
            PresenceSubscriptionService subscriptionService = Services.getService(PresenceSubscriptionService.class);
            if (subscriptionService == null) {
                throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(PresenceSubscriptionService.class.getName());
            }
            subscriptionService.approve(stanza);
        } catch (OXException oxe) {
            handleError(stanza, oxe);
        }
    }

    /**
     * Handle directed Presence Stanzas by delivering them to all connected Resources of the general ID or simply discarding them if no
     * connected Resource can be found.
     * 
     * @param presence The directed Presence Stanza
     * @throws OXException If the lookup of the recipient fails due to errors
     */
    private void handleDirectedPresence(Presence presence) {
        try {
            PresenceSubscriptionService presenceSubscriptionService = Services.getService(PresenceSubscriptionService.class);
            ResourceDirectory resourceDirectory = getResourceDirectory();
            Map<ID, OXException> failedDeliveries = Collections.emptyMap();
            ID from = presence.getFrom().toGeneralForm();
            ID to = presence.getTo().toGeneralForm();
            presence.trace("Handle directed presence. From: " + from + " To: " + to);
            IDMap<Resource> idMap = resourceDirectory.get(to);

            if (idMap.isEmpty()) {
                /*
                 * From http://xmpp.org/rfcs/rfc3921.html#rules 11.1.2 If the JID is of the form <user@domain/resource> and no available
                 * resource matches the full JID, the recipient's server (a) SHOULD silently ignore the stanza (i.e., neither deliver it nor
                 * return an error) if it is a presence stanza [...]
                 */
                return;
            }

            /*
             * Case 1: If the user sends directed presence to a contact that is in the user's roster with a subscription type of "from" or
             * "both" after having sent initial presence and before sending unavailable presence broadcast, the user's server MUST route or
             * deliver the full XML of that presence stanza (subject to privacy lists) but SHOULD NOT otherwise modify the contact's status
             * regarding presence broadcast (i.e., it SHOULD include the contact's JID in any subsequent presence broadcasts initiated by
             * the user).
             */
            if (presenceSubscriptionService.getSubscribers(from).contains(to)) {
                MessageDispatcher messageDispatcher = getMessageDispatcher();
                failedDeliveries = messageDispatcher.send(presence, idMap);
            }
            /*
             * Case 2: If the user sends directed presence to an entity that is not in the user's roster with a subscription type of "from"
             * or "both" after having sent initial presence and before sending unavailable presence broadcast, the user's server MUST route
             * or deliver the full XML of that presence stanza to the entity but MUST NOT modify the contact's status regarding available
             * presence broadcast (i.e., it MUST NOT include the entity's JID in any subsequent broadcasts of available presence initiated
             * by the user); however, if the available resource from which the user sent the directed presence become unavailable, the
             * user's server MUST broadcast that unavailable presence to the entity (if the user has not yet sent directed unavailable
             * presence to that entity). Case 3: If the user sends directed presence without first sending initial presence or after having
             * sent unavailable presence broadcast (i.e., the resource is active but not available), the user's server MUST treat the
             * entities to which the user sends directed presence in the same way that it treats the entities listed in case #2 above.
             * TL/DR: Add unsubscribed entity to temporary Map<ID,List<ID>> {Sender -> Recipients} and inform recipient when sender sends
             * final unavailable Presence
             */
            else {
                throw new UnsupportedOperationException("Not implemented yet.");
            }
            /*
             * The user has no interest in being informed about ever single failed delivery but wants to be informed if the Presence
             * couldn't be delivered to any resource of the desired recipient.
             */
            if (failedDeliveries.entrySet().size() == idMap.size()) {
                throw HandleExceptionCode.DIRECT_PRESENCE_FAILED.create(presence.getTo().toGeneralForm());
            }
        } catch (OXException oxe) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(oxe);
            }
            handleError(presence, oxe);
        }
    }

    /**
     * Change the current Presence status of the client in the ResourceDirectory.
     * 
     * @param presence Stanza containing the new Presence Status
     * @throws OXException If stanza conversion fails or the status can't be changed
     */
    private void handleBroadcastPresence(Presence presence) throws OXException {
        presence.trace("Handle broadcast presence");
        ResourceDirectory resourceDirectory = getResourceDirectory();
        DefaultResource presenceResource = new DefaultResource(presence);
        Resource old = resourceDirectory.set(presence.getFrom(), presenceResource);
        if (old != null) {
            presence.trace(String.format(
                "Update Presence: Old was: %1$s, %2$s, %3$d, %4$tT, %5$s",
                old.getPresence().getState(),
                old.getPresence().getMessage(),
                old.getPresence().getPriority(),
                old.getTimestamp(),
                old.getRoutingInfo()));
            
            if (LOG.isDebugEnabled()) {
                if (old.getPresence() != null) {
                    LOG.debug(String.format(
                        "Update Presence: Old was: %1$s, %2$s, %3$d, %4$tT, %5$s",
                        old.getPresence().getState(),
                        old.getPresence().getMessage(),
                        old.getPresence().getPriority(),
                        old.getTimestamp(),
                        old.getRoutingInfo()));
                } else {
                    LOG.debug(String.format("Update Presence: Old was: %1$tT, %2$s", old.getTimestamp(), old.getRoutingInfo()));
                }
            }
        }
    }

    /**
     * Inform the Sender about an error while handling his Presence Stanza.
     * 
     * @param presence The presence that couldn't be handled
     * @param oxException The OXException that cuased the handling to fail
     */
    private void handleError(Presence presence, OXException oxException) {
        try {
            presence.trace("error", oxException);
            MessageDispatcher messageDispatcher = getMessageDispatcher();
            Presence errorPresence = new Presence(presence);
            errorPresence.setTo(presence.getFrom());
            errorPresence.setError(RealtimeExceptionCodes.UNEXPECTED_ERROR.create(oxException, new Object[0]));
            if (presence.traceEnabled()) {
                errorPresence.addLogMessages(presence.getLogEntries());
                errorPresence.setTracer(presence.getTracer());
            }
            messageDispatcher.send(errorPresence);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

}
