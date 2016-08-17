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

package com.openexchange.realtime.hazelcast.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.realtime.cleanup.AbstractRealtimeJanitor;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.RoutingInfo;
import com.openexchange.realtime.dispatch.DispatchExceptionCode;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.dispatch.Utils;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.directory.HazelcastResourceDirectory;
import com.openexchange.realtime.hazelcast.osgi.Services;
import com.openexchange.realtime.hazelcast.serialization.channel.PortableStanzaDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link GlobalMessageDispatcherImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class GlobalMessageDispatcherImpl extends AbstractRealtimeJanitor implements MessageDispatcher {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GlobalMessageDispatcherImpl.class);

    private final HazelcastResourceDirectory directory;

    private final ResponseChannel channel;

    public GlobalMessageDispatcherImpl(HazelcastResourceDirectory directory) {
        super();
        this.directory = directory;
        this.channel = new ResponseChannel(directory);
    }

    @Override
    public Stanza sendSynchronously(Stanza stanza, long timeout, TimeUnit unit) throws OXException {
        String uuid = UUIDs.getUnformattedString(UUID.randomUUID());
        stanza.trace("Send synchronously. UUID: " + uuid);
        ID id = channel.setUp(uuid, stanza);
        send(stanza);
        return channel.waitFor(id, timeout, unit);
    }

    @Override
    public void send(Stanza stanza) throws OXException {
        IDMap<Resource> idMap = directory.get(stanza.getTo());
        if(idMap == null || idMap.isEmpty()) {
            throw DispatchExceptionCode.RESOURCE_OFFLINE.create(stanza.getTo());
        }
        Map<ID, OXException> exceptions = send(stanza, idMap);
        if(!exceptions.isEmpty()) {
            throw exceptions.values().iterator().next();
        }
    }

    public ResponseChannel getChannel() {
        return channel;
    }

    @Override
    public Map<ID, OXException> send(Stanza stanza, IDMap<Resource> recipients) throws OXException {
        if (stanza == null) {
            throw new IllegalArgumentException("Parameter 'stanza' must not be null!");
        }

        if (recipients == null) {
            stanza.trace("Parameter 'recipients' must not be null!");
            throw new IllegalArgumentException("Parameter 'recipients' must not be null!");
        }

        if (recipients.isEmpty()) {
            String logString = String.format(
                "Received empty map of recipients when trying to send from %1$s, giving up. Resource missing for ID: %2$s."
                , stanza.getFrom(), stanza.getTo());
            LOG.warn(logString);
            stanza.trace(logString);
            return Collections.emptyMap();
        }

        Map<Member, Set<ID>> targets = new HashMap<Member, Set<ID>>();
        for (Entry<ID, Resource> recipient : recipients.entrySet()) {
            ID id = recipient.getKey();
            Resource resource = recipient.getValue();
            RoutingInfo routingInfo = resource.getRoutingInfo();
            if (routingInfo != null) {
                Member member = memberFromRoutingInfo(routingInfo);
                if(member != null) {
                    Set<ID> ids = targets.get(member);
                    if (ids == null) {
                        ids = new HashSet<ID>();
                        targets.put(member, ids);
                    }

                    ids.add(id);
                } else {
                    LOG.error("No member matches {}", routingInfo);
                }
            } else {
                LOG.error("RoutingInfo for {} was null", resource);
            }
        }

        return deliver(stanza, targets);
    }

    /**
     * Filter the set of cluster {@link Member}s for a node matching the given {@link RoutingInfo}.
     *
     * @param routingInfo The {@link RoutingInfo} to filter the cluster {@link Member}s
     * @return null if no matching {@link Member} can be found, otherwise the first matching {@link Member}
     * @throws OXException
     */
    private Member memberFromRoutingInfo(final RoutingInfo routingInfo) throws OXException {
        String uuid = routingInfo.getId();
        InetSocketAddress socketAddress = routingInfo.getSocketAddress();
        final HazelcastInstance hazelcastInstance = HazelcastAccess.getHazelcastInstance();
        Set<Member> members = hazelcastInstance.getCluster().getMembers();

        if(!Strings.isEmpty(uuid)) {
            for(Member member : members) {
                    if(uuid.equals(member.getUuid())) {
                        return member;
                    }
            }
        } else {
            for(Member member : members) {
                    if(socketAddress.equals(member.getSocketAddress())) {
                        return member;
                    }
            }
        }

        return null;
    }

    private Map<ID, OXException> deliver(Stanza stanza, Map<Member, Set<ID>> targets) throws OXException {
        final HazelcastInstance hazelcastInstance = HazelcastAccess.getHazelcastInstance();
        Member localMember = hazelcastInstance.getCluster().getLocalMember();
        Map<ID, OXException> exceptions = new HashMap<ID, OXException>();
        Set<ID> localIds = targets.remove(localMember);
        if (localIds != null) {
            // Send via local message dispatcher to locally reachable receivers
            ensureSequence(stanza, localMember);
            stanza.trace("Deliver locally");
            Map<ID, OXException> sent = Services.getService(LocalMessageDispatcher.class).send(stanza, localIds);
            if (Utils.shouldResend(sent, stanza)) {
                resend(stanza);
                //return empty map of exceptions when resending
                return exceptions;
            }
            exceptions.putAll(sent);
        }
        // Sent to remote receivers
        IExecutorService executorService = hazelcastInstance.getExecutorService("default");
        List<Future<IDMap<OXException>>> futures = new ArrayList<Future<IDMap<OXException>>>();
        for (Entry<Member, Set<ID>> receiverEntry : targets.entrySet()) {
            Member receiver = receiverEntry.getKey();
            Set<ID> ids = receiverEntry.getValue();
            LOG.debug("Sending to '{}' @ {}", stanza.getTo(), receiver);
            stanza.trace("Sending to '" + stanza.getTo() + "' @ " + receiver);
            ensureSequence(stanza, receiver);
            Future<IDMap<OXException>> task = executorService.submitToMember(new PortableStanzaDispatcher(stanza, ids), receiver);
            futures.add(task);
        }
        // Await completion of send requests and extract their exceptions (if any)
        for (Future<IDMap<OXException>> future : futures) {
            try {
                exceptions.putAll(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw RealtimeExceptionCodes.UNEXPECTED_ERROR.create(e, "Execution interrupted");
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
        if (!exceptions.isEmpty()) {
            stanza.trace(exceptions);
        }
        return exceptions;
    }

    /**
     * The Stanza wasn't delivered locally/remotely. If the addressed Resource isn't available anylonger we remove it from the ResourceDirectory and
     * try to send the Stanza again. This will succeed if the Channel can conjure the Resource.
     *
     * @param stanza The Stanza to resend
     * @throws OXException
     */
    private void resend(Stanza stanza) throws OXException {
        stanza.setResendCount(stanza.getResendCount()+1);
        send(stanza);
    }

    /*
     * Map which client (sequencePrincipal) should use which sequence when sending to a given recipient node. Nodes are addressed via
     * InetSocketAddress.
     */
    private final ConcurrentHashMap<ID, ConcurrentHashMap<String, AtomicLong>> peerMapPerID = new ConcurrentHashMap<ID, ConcurrentHashMap<String, AtomicLong>>();
    /**
     *
     * When delivering a Stanza to a different node, a new sequence number relative to that node is generated, so that stanza streams
     * directed at different nodes still work.Consider client0 and client1 being connected to node1 and client2 being connected to node2.
     * client0 wants to chat with client1 and client2 and sends messages with strictly ascending sequence numbers reaching node1 that he is
     * connected to:
     *
     * <pre>
     * Seq 0 delivered to client1 via node 1
     * Seq 1 delivered to client1 via node 1
     * Seq 2 delivered to client2 via node 2
     * Seq 3 delivered to client1 via node 1
     * Seq 4 delivered to client2 via node 2
     * </pre>
     *
     * Node 2 only sees messages 2 and 4 and would indefinetly wait for messages 0, 1 and 3 to form a valid order of sequences. Therefore
     * the realtime framework on node1 has to recast sequence numbers in a way that node2 sees a valid order.
     * <pre>
     * Seq 0 remains      Seq 0 for node 1
     * Seq 1 remains      Seq 1 for node 1
     * Seq 2 is recast as Seq 0 for node 2
     * Seq 3 is recast as Seq 2 for node 1
     * Seq 4 is recast as Seq 1 for node 2
     * </pre>
     *
     * @param stanza The Stanza to deliver to another node of the cluster
     * @param receiver The receiving node of the cluster
     * @throws OXException
     */
    private void ensureSequence(Stanza stanza, Member receiver) throws OXException {
        if (stanza.getSequenceNumber() != -1) {
            LOG.debug("peerMapsPerID before ensuring Sequence: {}", peerMapPerID);
            LOG.debug("SequencePrincipal for peerMapPerID lookup is: {}", stanza.getSequencePrincipal());
            ConcurrentHashMap<String, AtomicLong> peerMap = peerMapPerID.get(stanza.getSequencePrincipal());
            if (peerMap == null) {
                peerMap = new ConcurrentHashMap<String, AtomicLong>();
                ConcurrentHashMap<String, AtomicLong> otherPeerMap = peerMapPerID.putIfAbsent(stanza.getSequencePrincipal(), peerMap);
                if(otherPeerMap != null) {
                    LOG.debug("Found other peerMap for SequencePrincipal: {} with value {}", stanza.getSequencePrincipal(), otherPeerMap);
                    peerMap = otherPeerMap;
                }
            }
            AtomicLong nextNumber = peerMap.get(receiver.getUuid());
            if (nextNumber == null) {
                nextNumber = new AtomicLong(0);
                AtomicLong otherNextNumber = peerMap.putIfAbsent(receiver.getUuid(), nextNumber);
                nextNumber = (otherNextNumber != null) ? otherNextNumber : nextNumber;
                if(otherNextNumber != null) {
                    LOG.debug("Found other nextNumber to use for receiver: {}, nextNumber {}", receiver.getUuid(), otherNextNumber);
                    nextNumber = otherNextNumber;
                }
                LOG.debug("nextNumber for receiver {} was null, adding nextNumber: {}", receiver.getUuid(), nextNumber);
            }
            Long ensuredSequence = nextNumber.incrementAndGet() - 1;
            LOG.debug("Updating sequence number for {}: {}", receiver.getUuid(), ensuredSequence);
            stanza.setSequenceNumber(ensuredSequence);
            stanza.trace("Updating sequence number for " + receiver.getUuid() + ": " + ensuredSequence);
            if(LOG.isDebugEnabled()) {
                LOG.debug("peerMapsPerID after ensuring Sequence: {}", peerMapPerID);
            }
        }
    }

    @Override
    public void cleanupForId(ID id) {
        LOG.debug("Cleanup for ID: {}. Removing SequencePrincipal from peerMapPerID lookup table.", id);
        peerMapPerID.remove(id);
    }
}
