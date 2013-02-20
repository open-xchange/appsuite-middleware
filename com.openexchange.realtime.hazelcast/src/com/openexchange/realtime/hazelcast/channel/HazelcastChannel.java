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

package com.openexchange.realtime.hazelcast.channel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import org.apache.commons.logging.Log;
import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link HazelcastChannel}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class HazelcastChannel implements Channel {

    private static Log LOG = LogFactory.getLog(HazelcastChannel.class);
    private static final int PRIORTIY = 12;
    private static final String PROTOCOL = "hz";

    private final ResourceDirectory directory;

    /**
     * Initializes a new {@link HazelcastChannel}.
     *
     * @param directory The resource directory used for ID lookups
     */
    public HazelcastChannel(ResourceDirectory directory) {
        super();
        this.directory = directory;
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public boolean canHandle(Set<ElementPath> elementPaths, ID recipient, ServerSession session) throws OXException {
        return isConnected(recipient, session);
    }

    @Override
    public int getPriority() {
        return PRIORTIY;
    }

    @Override
    public boolean isConnected(ID id, ServerSession session) throws OXException {
        //TODO: maybe too expensive, since it's repeated shortly afterwards in send
        Set<Member> receivers = getReceivers(id);
        return null != receivers && 0 < receivers.size();
    }

    @Override
    public void send(Stanza stanza, ServerSession session) throws OXException {
        List<FutureTask<Void>> futures = initiateSend(stanza);
        for (FutureTask<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw RealtimeExceptionCodes.UNEXPECTED_ERROR.create(e, "Execution interrupted");
            } catch (ExecutionException e) {
                throw ThreadPools.launderThrowable(e, OXException.class);
            }
        }
    }

    private List<FutureTask<Void>> initiateSend(Stanza stanza) throws OXException {
        Set<Member> receivers = getReceivers(stanza.getTo());
        if (null == receivers || 0 == receivers.size()) {
            //TODO: check exception parameter usage
            throw RealtimeExceptionCodes.NO_APPROPRIATE_CHANNEL.create(stanza.getTo(), stanza.getId());
        }
        ExecutorService executorService = HazelcastAccess.getHazelcastInstance().getExecutorService();
        List<FutureTask<Void>> futures = new ArrayList<FutureTask<Void>>();
        for (Member receiver : receivers) {
            LOG.debug("Sending to '" + stanza.getTo() + "' @ " + receiver);
            FutureTask<Void> task = new DistributedTask<Void>(new StanzaDispatcher(stanza), receiver);
            executorService.execute(task);
            futures.add(task);
        }
        return futures;
    }

    /**
     * Gets all possible receivers for the supplied ID. If the ID is in general form, all matching members are looked up. The returned
     * collection will never contain the local member.
     *
     * @param id The ID to lookup
     * @return All possible receivers, or an empty set if none were found
     * @throws OXException
     */
    private Set<Member> getReceivers(ID id) throws OXException {
        Set<Member> members = new HashSet<Member>();
        Member localMember = HazelcastAccess.getHazelcastInstance().getCluster().getLocalMember();
        if (id.isGeneralForm()) {
            for (Member member : directory.getAll(id)) {
                if (null != member && false == member.equals(localMember)) {
                    members.add(member);
                }
            }
        } else {
            Member member = directory.get(id);
            if (null != member && false == member.equals(localMember)) {
                members.add(member);
            }
        }
        return members;
    }

}
