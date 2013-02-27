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

package com.openexchange.realtime.hazelcast.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import org.apache.commons.logging.Log;
import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.LocalMessageDispatcher;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.hazelcast.Services;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.channel.StanzaDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IQ;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GlobalMessageDispatcherImpl}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class GlobalMessageDispatcherImpl implements MessageDispatcher {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(GlobalMessageDispatcherImpl.class);

    private final ResourceDirectory resourceDirectory;

    public GlobalMessageDispatcherImpl(ResourceDirectory resourceDirectory) {
        super();
        this.resourceDirectory = resourceDirectory;
    }

    @Override
    public void send(Stanza stanza, ServerSession session) throws OXException {
        ID to = stanza.getTo();
        if (to == null) {
            /*
             * TODO: See http://tools.ietf.org/html/rfc3920#section-10: 
             * If the stanza possesses no 'to' attribute, the server SHOULD process
             * it on behalf of the entity that sent it. Because all stanzas received from other servers MUST possess a 'to' attribute, this
             * rule applies only to stanzas received from a registered entity (such as a client) that is connected to the server. If the
             * server receives a presence stanza with no 'to' attribute, the server SHOULD broadcast it to the entities that are subscribed
             * to the sending entity's presence, if applicable (the semantics of presence broadcast for instant messaging and presence
             * applications are defined in [XMPP-IM]). If the server receives an IQ stanza of type "get" or "set" with no 'to' attribute and
             * it understands the namespace that qualifies the content of the stanza, it MUST either process the stanza on behalf of the
             * sending entity (where the meaning of "process" is determined by the semantics of the qualifying namespace) or return an error
             * to the sending entity.
             */
            return;
        }

        if (isInboundStanza(stanza)) {
            if (applyPrivacyLists(stanza)) {
                sendInbound(stanza);
            }
        } else {
            // TODO: implement handling for outbound stanzas
        }
    }

    private void sendInbound(Stanza stanza) throws OXException {
        ID to = stanza.getTo();
        if (addressesValidOXUser(stanza)) {
            IDMap<Resource> resources = resourceDirectory.get(to);
            if (to.isGeneralForm()) {
                if (resources.isEmpty()) {
                    handleToGeneralOffline(stanza);
                } else {
                    handleToGeneralOnline(stanza, resources);
                }
            } else {
                if (resources.isEmpty()) {
                    handleConcreteOffline(stanza);
                } else {
                    handleConcreteOnline(stanza, resources);
                }
            }
        } else {
            /*
             * TODO: 
             * Else if the JID is of the form <user@domain> or <user@domain/resource> and the associated user account does not exist,
             * the recipient's server (a) SHOULD silently ignore the stanza (i.e., neither deliver it nor return an error) if it is a
             * presence stanza, (b) MUST return a <service-unavailable/> stanza error to the sender if it is an IQ stanza, and (c) SHOULD
             * return a <service-unavailable/> stanza error to the sender if it is a message stanza.
             */
        }
    }

    private void handleToGeneralOnline(Stanza stanza, IDMap<Resource> resources) throws OXException {
        /*
         * Else if the JID is of the form <user@domain> and there is at least one available resource available for the user, the recipient's
         * server MUST follow these rules:
         */
        if (Message.class.isInstance(stanza)) {
            /*
             * For message stanzas, the server SHOULD deliver the stanza to the highest-priority available resource (if the resource did not
             * provide a value for the <priority/> element, the server SHOULD consider it to have provided a value of zero). If two or more
             * available resources have the same priority, the server MAY use some other rule (e.g., most recent connect time, most recent
             * activity time, or highest availability as determined by some hierarchy of <show/> values) to choose between them or MAY
             * deliver the message to all such resources. However, the server MUST NOT deliver the stanza to an available resource with a
             * negative priority; if the only available resource has a negative priority, the server SHOULD handle the message as if there
             * were no available resources (defined below). In addition, the server MUST NOT rewrite the 'to' attribute (i.e., it MUST leave
             * it as <user@domain> rather than change it to <user@domain/resource>).
             */
            IDMap<Resource> receivers = new IDMap<Resource>();
            byte highest = 0;
            for (Entry<ID, Resource> entry : resources.entrySet()) {
                ID id = entry.getKey();
                Resource resource = entry.getValue();
                byte priority = resource.getPriority();
                if (priority == highest) {
                    receivers.put(id, resource);
                } else if (priority > highest) {
                    receivers.clear();
                    receivers.put(id, resource);
                }
            }
            
            if (receivers.isEmpty()) {
                handleToGeneralOffline(stanza);
            } else {
                for (Entry<ID, Resource> entry : receivers.entrySet()) {
                    IDMap<Resource> target = new IDMap<Resource>();
                    target.put(entry.getKey(), entry.getValue());
                    handleConcreteOnline(stanza, target);
                }
            }
        } else if (Presence.class.isInstance(stanza)) {
            /*
             * For presence stanzas other than those of type "probe", the server MUST deliver the stanza to all available resources; for
             * presence probes, the server SHOULD reply based on the rules defined in Presence Probes. In addition, the server MUST NOT
             * rewrite the 'to' attribute (i.e., it MUST leave it as <user@domain> rather than change it to <user@domain/resource>).
             * 
             * TODO: We have no probe-implementation yet
             */
            for (Entry<ID, Resource> entry : resources.entrySet()) {
                IDMap<Resource> target = new IDMap<Resource>();
                target.put(entry.getKey(), entry.getValue());
                handleConcreteOnline(stanza, target);
            }
        } else if (IQ.class.isInstance(stanza)) {
            /*
             * For IQ stanzas, the server itself MUST reply on behalf of the user with either an IQ result or an IQ error, and MUST NOT
             * deliver the IQ stanza to any of the available resources. Specifically, if the semantics of the qualifying namespace define a
             * reply that the server can provide, the server MUST reply to the stanza on behalf of the user; if not, the server MUST reply
             * with a <service-unavailable/> stanza error.
             * 
             * TODO: This should be handled by a corresponding IQ handler. Probably those stanzas even don't ever reach the dispatcher?
             */
        }
    }

    private void handleToGeneralOffline(Stanza stanza) {
        /*
         * Else if the JID is of the form <user@domain> and there are no available resources associated with the user, how the stanza is
         * handled depends on the stanza type:
         */
        if (Presence.class.isInstance(stanza)) {
            if (EnumSet.of(Presence.Type.SUBSCRIBE, Presence.Type.SUBSCRIBED, Presence.Type.UNSUBSCRIBE, Presence.Type.UNSUBSCRIBED).contains(
                ((Presence) stanza).getType())) {
                /*
                 * TODO:
                 * For presence stanzas of type "subscribe", "subscribed", "unsubscribe", and "unsubscribed", the server MUST maintain a
                 * record of the stanza and deliver the stanza at least once (i.e., when the user next creates an available resource); in
                 * addition, the server MUST continue to deliver presence stanzas of type "subscribe" until the user either approves or
                 * denies the subscription request (see also Presence Subscriptions).
                 */
            } else {
                /*
                 * For all other presence stanzas, the server SHOULD silently ignore the stanza by not storing it for later delivery or
                 * replying to it on behalf of the user.
                 */
                LOG.info("Silently ignoring stanza: " + stanza.toString());
            }
        } else if (Message.class.isInstance(stanza)) {
            /*
             * TODO:
             * For message stanzas, the server MAY choose to store the stanza on behalf of the user and deliver it when the user next
             * becomes available, or forward the message to the user via some other means (e.g., to the user's email account). However, if
             * offline message storage or message forwarding is not enabled, the server MUST return to the sender a <service-unavailable/>
             * stanza error. (Note: Offline message storage and message forwarding are not defined in XMPP, since they are strictly a matter
             * of implementation and service provisioning.)
             */
        } else if (IQ.class.isInstance(stanza)) {
            /*
             * TODO:
             * For IQ stanzas, the server itself MUST reply on behalf of the user with either an IQ result or an IQ error. Specifically, if
             * the semantics of the qualifying namespace define a reply that the server can provide, the server MUST reply to the stanza on
             * behalf of the user; if not, the server MUST reply with a <service-unavailable/> stanza error.
             */
        }
    }

    private void handleConcreteOnline(Stanza stanza, IDMap<Resource> target) throws OXException {
        /*
         * If the JID is of the form <user@domain/resource> and an available resource matches the full JID, the recipient's server MUST 
         * deliver the stanza to that resource.
         */
        Entry<ID, Resource> next = target.entrySet().iterator().next();
        ID id = next.getKey();
        Resource resource = next.getValue();
        Serializable routingInfo = resource.getRoutingInfo();
        if (routingInfo != null && Member.class.isInstance(routingInfo)) {
            Member localMember = HazelcastAccess.getHazelcastInstance().getCluster().getLocalMember();
            Member targetMember = (Member) routingInfo;
            if (localMember.equals(targetMember)) {
                LocalMessageDispatcher dispatcher = Services.getService(LocalMessageDispatcher.class);
                dispatcher.send(stanza, Collections.singleton(id));
            } else {
                ExecutorService executorService = HazelcastAccess.getHazelcastInstance().getExecutorService();
                FutureTask<Void> task = new DistributedTask<Void>(new StanzaDispatcher(stanza, id), targetMember);
                executorService.execute(task);
                try {
                    task.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw RealtimeExceptionCodes.UNEXPECTED_ERROR.create(e, "Execution interrupted");
                } catch (ExecutionException e) {
                    throw ThreadPools.launderThrowable(e, OXException.class);
                }
            }
        }
    }

    private void handleConcreteOffline(Stanza stanza) {
        /*
         * Else if the JID is of the form <user@domain/resource> and no available resource matches the full JID, the recipient's server
         */
        if (Presence.class.isInstance(stanza)) {
            /*
             * (a) SHOULD silently ignore the stanza (i.e., neither deliver it nor return an error) if it is a presence stanza,
             */
            LOG.info("Silently ignoring stanza: " + stanza.toString());
        } else if (IQ.class.isInstance(stanza)) {
            /*
             * (b) MUST return a <service-unavailable/> stanza error to the sender if it is an IQ stanza
             */
        } else if (Message.class.isInstance(stanza)) {
            /*
             * (c) SHOULD treat the stanza as if it were addressed to <user@domain> if it is a message stanza.
             */
        }
    }

    private boolean applyPrivacyLists(Stanza stanza) {
        /*
         * TODO:
         * If the hostname of the domain identifier portion of the JID contained in the 'to' attribute of an inbound stanza matches 
         * a hostname of the server itself and the JID contained in the 'to' attribute is of the form <user@example.com> or 
         * <user@example.com/resource>, the server MUST first apply any privacy lists that are in force
         */
        return true;
    }

    private boolean isInboundStanza(Stanza stanza) {
        // TODO: Really check if the id addresses a OX resource.
        return true;
    }

    private boolean addressesValidOXUser(Stanza stanza) {
        // TODO: Implement lookup
        return true;
    }

}
