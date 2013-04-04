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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import org.apache.commons.logging.Log;
import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.hazelcast.Services;
import com.openexchange.realtime.hazelcast.channel.HazelcastAccess;
import com.openexchange.realtime.hazelcast.channel.StanzaDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link GlobalMessageDispatcherImpl}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class GlobalMessageDispatcherImpl implements MessageDispatcher {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(GlobalMessageDispatcherImpl.class);

    private final ResourceDirectory directory;

    public GlobalMessageDispatcherImpl(ResourceDirectory directory) {
        super();
        this.directory = directory;
    }

    @Override
    public Map<ID, OXException> send(Stanza stanza) throws OXException {
        return send(stanza, directory.get(stanza.getTo()));
    }

    @Override
    public Map<ID, OXException> send(Stanza stanza, IDMap<Resource> recipients) throws OXException {
        if (stanza == null) {
            throw new IllegalArgumentException("Parameter 'stanza' must not be null!");
        }

        if (recipients == null) {
            throw new IllegalArgumentException("Parameter 'recipients' must not be null!");
        }

        if (recipients.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Member, Set<ID>> targets = new HashMap<Member, Set<ID>>();
        for (Entry<ID, Resource> recipient : recipients.entrySet()) {
            ID id = recipient.getKey();
            Resource resource = recipient.getValue();
            Serializable routingInfo = resource.getRoutingInfo();
            if (routingInfo != null && Member.class.isInstance(routingInfo)) {
                Member member = (Member) routingInfo;
                Set<ID> ids = targets.get(member);
                if (ids == null) {
                    ids = new HashSet<ID>();
                    targets.put(member, ids);
                }

                ids.add(id);
            }
        }

        return deliver(stanza, targets);
    }

    private Map<ID, OXException> deliver(Stanza stanza, Map<Member, Set<ID>> targets) throws OXException {
        final HazelcastInstance hazelcastInstance = HazelcastAccess.getHazelcastInstance();
        Member localMember = hazelcastInstance.getCluster().getLocalMember();
        Map<ID, OXException> exceptions = new HashMap<ID, OXException>();
        Set<ID> localIds = targets.remove(localMember);
        if (localIds != null) {
            // Send via local message dispatcher to locally reachable receivers
            Map<ID, OXException> sent = Services.getService(LocalMessageDispatcher.class).send(stanza, localIds);
            exceptions.putAll(sent);
        }
        // Sent to remote receivers
        ExecutorService executorService = hazelcastInstance.getExecutorService();
        List<FutureTask<Map<ID, OXException>>> futures = new ArrayList<FutureTask<Map<ID, OXException>>>();
        for (Member receiver : targets.keySet()) {
            Set<ID> ids = targets.get(receiver);
            LOG.debug("Sending to '" + stanza.getTo() + "' @ " + receiver);

            FutureTask<Map<ID, OXException>> task = new DistributedTask<Map<ID, OXException>>(new StanzaDispatcher(stanza, ids), receiver);
            executorService.execute(task);
            futures.add(task);
        }
        // Await completion of send requests and extract their exceptions (if any)
       
        for (FutureTask<Map<ID, OXException>> future : futures) {
            try {
                exceptions.putAll(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw RealtimeExceptionCodes.UNEXPECTED_ERROR.create(e, "Execution interrupted");
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }

        return exceptions;
    }

}
