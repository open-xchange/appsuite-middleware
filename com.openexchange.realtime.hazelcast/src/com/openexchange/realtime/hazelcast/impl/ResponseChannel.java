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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanup;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.hazelcast.osgi.Services;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link ResponseChannel} - Used to "transport" messages addressed to call:// IDs used during sychronous Stanza sending. Received messages
 * are collected in a response map and the appropriate condition is signaled. As a result clients that are "synchronously" waiting for this
 * response in {@link ResponseChannel#waitFor(String, long, TimeUnit)} will receive the Stanza.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ResponseChannel implements Channel {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseChannel.class);

    private final ConcurrentHashMap<ID, Stanza> responses = new ConcurrentHashMap<ID, Stanza>();
    private final ConcurrentHashMap<ID, Condition> condition = new ConcurrentHashMap<ID, Condition>();
    private final ConcurrentHashMap<ID, Lock> locks = new ConcurrentHashMap<ID, Lock>();

    private final ResourceDirectory directory;

    public ResponseChannel(ResourceDirectory directory) {
        this.directory = directory;
    }

    /**
     * Set up/prepare the channel for sending a Stanza. This includes replacing the from ID with a channel specific internal ID
     * @param uuid the unique id to use for sending this Stanza
     * @param stanza the Stanza to send
     * @return A generated id that has to be reused in {@link ResponseChannel#waitFor(ID, long, TimeUnit)}
     * @throws OXException
     */
    public ID setUp(String uuid, Stanza stanza) throws OXException {
        ID id = getId(uuid);
        ReentrantLock lock = new ReentrantLock();
        locks.put(id, lock);
        condition.put(id, lock.newCondition());
        Resource res = new DefaultResource();
        directory.set(id, res);
        stanza.setFrom(id);
        return id;
    }

    /**
     * Wait "synchronously" for the arrival of a response Stanza addressed for a given unique id.
     * @param id the id created during {@link ResponseChannel#setUp(String, Stanza)}
     * @param timeout duration to wait for the arrival or the expected response
     * @param unit timout unit
     * @return the Stanza representing the response that we are waiting for
     * @throws OXException if no Stanza was received within the given timout
     */
    public Stanza waitFor(ID id, long timeout, TimeUnit unit) throws OXException {
        try {
            locks.get(id).lock();
            Stanza stanza = responses.get(id);
            if (stanza != null) {
                LOG.debug("Returning stanza without waiting on condition: {}", stanza);
                return stanza;
            }
            condition.get(id).await(timeout, unit);
            stanza = responses.get(id);
            if (stanza == null) {
                RealtimeException re = RealtimeExceptionCodes.RESPONSE_AWAIT_TIMEOUT.create();
                LOG.error("Didn't get a response in time",re);
                throw re;
            }

            LOG.debug("Returning stanza after waiting on condition: {}", stanza);
            return stanza;
        } catch (InterruptedException e) {
            throw new OXException(e);
        } finally {
            directory.remove(id);
            condition.remove(id);
            responses.remove(id);
            locks.remove(id).unlock();
            LocalRealtimeCleanup localRealtimeCleanup = Services.getService(LocalRealtimeCleanup.class);
            if (localRealtimeCleanup != null) {
                localRealtimeCleanup.cleanForId(id);
            } else {
                LOG.error(
                    "Error while trying to cleanup for ResponseChannel ID: {}",
                    id,
                    RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(LocalRealtimeCleanup.class.getName()));
            }
        }

    }

    /**
     * Generate a channel specific internal ID. Example: call://894ae710b4f447d494818b56f663d0ca@internal
     * @param uuid the uuid to use for ID creation
     * @return an channel specific internal ID
     */
    private ID getId(String uuid) {
        ID id = new ID(getProtocol(), uuid, ID.INTERNAL_CONTEXT, "");
        return id;
    }

    @Override
    public String getProtocol() {
        return "call";
    }

    @Override
    public boolean canHandle(Set<ElementPath> elementPaths, ID recipient) throws OXException {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isConnected(ID id) throws OXException {
        return condition.containsKey(id);
    }

    /*
     * Recipients are one-time UUIDs that are created before sending a message on behalf of some client and removed either after Stanza
     * retrieval or the given timeout. Recipients can't be conjured for this channel.
     */
    @Override
    public boolean conjure(ID id) throws OXException {
        return false;
    }

    /**
     *
     */
    @Override
    public void send(Stanza stanza, ID recipient) throws OXException {
        stanza.trace("Delivering synchronously. ResponseChannel.");
        Lock lock = locks.get(recipient);
        if(lock == null) {
            throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create("Missing lock for recipient: " + recipient);
        }
        try {
            lock.lock();
            if (condition.get(recipient) != null) {
                responses.put(recipient, stanza);
                condition.get(recipient).signal();
            } else {
                throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create("Missing condition for recipient: " + recipient);
            }
        } finally {
            lock.unlock();
        }
    }

}
