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

package com.openexchange.realtime.handle.impl.message;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.DispatchExceptionCode;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.handle.StanzaStorage;
import com.openexchange.realtime.handle.TimedStanza;
import com.openexchange.realtime.handle.impl.AbstractStrategyHandler;
import com.openexchange.realtime.handle.impl.HandlerStrategy;
import com.openexchange.realtime.handle.impl.Services;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link MessageHandler} - Handles Message Stanzas.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class MessageHandler extends AbstractStrategyHandler<Message> {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageHandler.class);

    public MessageHandler(BlockingQueue<Message> queue) {
        super(queue, new HandlerStrategy<Message>());
    }

    @Override
    public void handleToIsNull(Message stanza) {
        stanza.trace("Can not send to null recipients. Discarding");
        try {
            throw new UnsupportedOperationException("Not implemented yet.");
        } catch (Exception e) {
            // TODO: send error stanza to stanza.from or other proper error handling for messages depending on this case.
            LOG.error("Unable to handle broadcast stanza.", e);            
        }
    }

    @Override
    public void handleAccountNotExists(Message stanza) {
        stanza.trace("Discarding. Account doesn't exist");
        try {
            throw new UnsupportedOperationException("Not implemented yet.");
        } catch (Exception e) {
            // TODO: send error stanza to stanza.from or other proper error handling for messages depending on this case.
            LOG.error("Unable to handle stanza for non existant account ", e);            
        }
    }

    @Override
    public void handleInboundStanzaWithConcreteRecipient(Message stanza) {
        try {
            ResourceDirectory resourceDirectory = getResourceDirectory();
            ID to = stanza.getTo();
            stanza.trace("Handle inbound");
            IDMap<Resource> resources = resourceDirectory.get(to);
            stanza.trace("Resource directory said to deliver to: " + resources);
            if (resources.isEmpty()) {
                /*
                 * Else if the JID is of the form <user@domain/resource> and no available resource matches the full JID, the recipient's
                 * server SHOULD treat the stanza as if it were addressed to <user@domain> if it is a message stanza.
                 */
                handleInboundStanzaWithGeneralRecipient0(stanza, to.toGeneralForm());
            } else {
                MessageDispatcher messageDispatcher = getMessageDispatcher();
                Map<ID, OXException> failed = messageDispatcher.send(stanza, resources);
                if (!failed.isEmpty()) {
                    OXException oxException = failed.values().iterator().next();
                    if (DispatchExceptionCode.RESOURCE_OFFLINE.equals(oxException) || DispatchExceptionCode.UNKNOWN_CHANNEL.equals(oxException)) {
                        storeMessage(to, stanza);
                    } else {
                        sendServiceUnavailable(stanza);
                    }
                }
            }
        } catch (Exception e) {
            // TODO: send error stanza to stanza.from  or other proper error handling for messages depending on this case.
            LOG.error("Unable to handle inbound stanza.", e);
        }
    }

    @Override
    public void handleInboundStanzaWithGeneralRecipient(Message stanza) {
        handleInboundStanzaWithGeneralRecipient0(stanza, stanza.getTo());
    }

    private void handleInboundStanzaWithGeneralRecipient0(Message stanza, ID recipient) {
        try {
            IDMap<Resource> receivers = DestinationSelector.select(recipient);
            stanza.trace("Trying with general recipient. DestinationSelector chose: " + receivers);
            if (receivers.isEmpty()) {
                /*
                 * For message stanzas, the server MAY choose to store the stanza on behalf of the user and deliver it when the user next
                 * becomes available, or forward the message to the user via some other means (e.g., to the user's email account). However,
                 * if offline message storage or message forwarding is not enabled, the server MUST return to the sender a
                 * <service-unavailable/> stanza error. (Note: Offline message storage and message forwarding are not defined in XMPP, since
                 * they are strictly a matter of implementation and service provisioning.)
                 */
                storeMessage(recipient, stanza);
            } else {
                /*
                 * For message stanzas, the server SHOULD deliver the stanza to the highest-priority available resource (if the resource did
                 * not provide a value for the <priority/> element, the server SHOULD consider it to have provided a value of zero). If two
                 * or more available resources have the same priority, the server MAY use some other rule (e.g., most recent connect time,
                 * most recent activity time, or highest availability as determined by some hierarchy of <show/> values) to choose between
                 * them or MAY deliver the message to all such resources. However, the server MUST NOT deliver the stanza to an available
                 * resource with a negative priority; if the only available resource has a negative priority, the server SHOULD handle the
                 * message as if there were no available resources (defined below). In addition, the server MUST NOT rewrite the 'to'
                 * attribute (i.e., it MUST leave it as <user@domain> rather than change it to <user@domain/resource>).
                 */
                MessageDispatcher messageDispatcher = getMessageDispatcher();
                Map<ID, OXException> failed = messageDispatcher.send(stanza, receivers);
                if (failed.size() == receivers.size()) {
                    for (Map.Entry<ID, OXException> failure : failed.entrySet()) {
                        ID id = failure.getKey();
                        OXException oxException = failure.getValue();
                        if (DispatchExceptionCode.RESOURCE_OFFLINE.equals(oxException) || DispatchExceptionCode.UNKNOWN_CHANNEL.equals(oxException)) {
                            storeMessage(id, stanza);
                        } else {
                            sendServiceUnavailable(stanza);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: send error stanza to stanza.from  or other proper error handling for messages depending on this case.
            LOG.error("Unable to handle inbound stanza.", e);
        }
    }

    @Override
    public void handleOutboundStanza(Message stanza) {
        stanza.trace("Discarding outbound message");
        try {
            throw new UnsupportedOperationException("Not implemented yet.");
        } catch (Exception e) {
            // TODO: send error stanza to stanza.from or other proper error handling for messages depending on this case.
            LOG.error("Unable to handle inbound stanza.", e);            
        }
    }

    @Override
    public boolean applyPrivacyLists(Message stanza) {
        return true;
    }
    
    private void storeMessage(ID recipient, Message stanza) throws OXException {
        stanza.trace("Couldn't deliver message. Storing.");
        StanzaStorage stanzaStorage = Services.getService(StanzaStorage.class);
        stanzaStorage.pushStanza(recipient, new TimedStanza(System.currentTimeMillis(), stanza));
    }

    private void sendServiceUnavailable(Message stanza) {
        stanza.trace("Service unavailable. Discarding message");
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
