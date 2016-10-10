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

package com.openexchange.filestore.swift.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.filestore.swift.impl.token.Token;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * A {@link EndpointPool} manages a set of end-points for the Swift client. The available
 * end-points are returned in a round-robin manner. If end-points become unavailable they can
 * be blacklisted. Every host on the blacklist is periodically checked by a heartbeat for
 * availability. If a formerly blacklisted host becomes available again, it is removed from
 * the blacklist and returned to the pool of available hosts. The process of blacklisting
 * an end-point is up to the client.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.2
 */
@ThreadSafe
public class EndpointPool {

    /** The logger constant */
    static final Logger LOG = LoggerFactory.getLogger(EndpointPool.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<Endpoint> available;
    private final List<Endpoint> blacklist;
    private final AtomicInteger counter;
    final String filestoreId;
    private final AtomicReference<Token> tokenRef;
    private volatile ScheduledTimerTask heartbeat;

    /**
     * Initializes a new {@link EndpointPool}.
     *
     * @param filestoreId The file storage identifier
     * @param endpointUris A list of end-point URIs to manage; must not be empty; URIs must always end with a trailing slash
     * @param httpClient
     * @param heartbeatInterval
     * @param timerService
     */
    public EndpointPool(String filestoreId, List<String> endpointUris, HttpClient httpClient, int heartbeatInterval, Token initialToken, TimerService timerService) {
        super();
        int size = endpointUris.size();
        if (size <= 0) {
            throw new IllegalArgumentException("Paramater 'hosts' must not be empty");
        }

        this.filestoreId = filestoreId;
        tokenRef = new AtomicReference<Token>();
        available = new ArrayList<Endpoint>(size);
        blacklist = new ArrayList<Endpoint>(size);
        counter = new AtomicInteger(size);

        EndpointFactory endpointFactory = EndpointFactory.getInstance();
        for (String endpointUri : endpointUris) {
            Endpoint endpoint = endpointFactory.createEndpointFor(endpointUri);
            if (null != initialToken) {
                endpoint.setToken(initialToken);
            }
            available.add(endpoint);
        }

        LOG.debug("Swift end-point pool [{}]: Scheduling heartbeat timer task", filestoreId);
        heartbeat = timerService.scheduleWithFixedDelay(new Heartbeat(httpClient), heartbeatInterval, heartbeatInterval);
    }

    /**
     * Gets an available end-point.
     *
     * @param prefix The client's prefix
     * @return The end-point or <code>null</code> if all end-points have been blacklisted
     */
    public Endpoint get(String prefix) {
        lock.readLock().lock();
        try {
            int size = available.size();
            if (size <= 0) {
                return null;
            }

            // Is there only one in pool?
            if (size == 1) {
                return available.get(0);
            }

            // Round-robin the next one
            int next = counter.incrementAndGet();
            if (next < 0) {
                int newNext = size;
                counter.compareAndSet(next, newNext);
                next = newNext;
            }

            Endpoint endpoint = available.get(next % size);
            LOG.debug("Swift end-point pool [{}]: Returning endpoint {}", filestoreId, endpoint);
            return endpoint;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Checks if this end-point pool has any working/available end-point left.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if there is any available end-point; otherwise <code>false</code>
     */
    public boolean hasAny(int contextId, int userId) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return !available.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Removes an end-point from the list of available ones and adds it to the blacklist.
     *
     * @param endpoint The end-point to add to black-list
     * @return <code>true</code> if there is any available end-point; otherwise <code>false</code>
     */
    public boolean blacklist(Endpoint endpoint) {
        lock.writeLock().lock();
        try {
            if (available.remove(endpoint)) {
                LOG.warn("Swift end-point pool [{}]: Endpoint {} is added to blacklist", filestoreId, endpoint);
                blacklist.add(endpoint);
            }
            return !available.isEmpty();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes an end-point from the blacklist and adds it back to list of available ones.
     *
     * @param endpoint The end-point to remove from black-list
     */
    public void unblacklist(Endpoint endpoint) {
        lock.writeLock().lock();
        try {
            if (blacklist.remove(endpoint)) {
                LOG.info("Swift end-point pool [{}]: Endpoint {} is removed from blacklist", filestoreId, endpoint);
                available.add(endpoint);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Closes this end-point pool instance. The blacklist heartbeat task is cancelled.
     */
    public synchronized void close() {
        ScheduledTimerTask heartbeat = this.heartbeat;
        if (heartbeat != null) {
            this.heartbeat = null;
            heartbeat.cancel();
        }
    }

    /**
     * Gets the currently blacklisted end-points.
     *
     * @return The currently blacklisted end-points
     */
    List<Endpoint> getBlacklist() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(blacklist);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Applies a new token to this end-point pool used for heart-beating blacklisted end-points.
     *
     * @param newToken The new token to apply
     */
    void applyNewToken(Token newToken) {
        tokenRef.set(newToken);
    }

    // -------------------------------------------------------------------------------------------------------------------------- //

    private final class Heartbeat implements Runnable {

        private final HttpClient httpClient;

        Heartbeat(HttpClient httpClient) {
            super();
            this.httpClient = httpClient;
        }

        @Override
        public void run() {
            try {
                List<Endpoint> blacklist = getBlacklist();
                int size = blacklist.size();
                if (size <= 0) {
                    LOG.debug("Swift end-point pool [{}]: Heartbeat - blacklist is empty, nothing to do", filestoreId);
                    return;
                }

                LOG.debug("Swift end-point pool [{}]: Heartbeat - blacklist contains {} endpoints", filestoreId, Integer.valueOf(size));
                for (Endpoint endpoint : blacklist) {
                    Boolean unavailable = Utils.endpointUnavailable(endpoint, httpClient);
                    if (null != unavailable) {
                        if (unavailable.booleanValue()) {
                            LOG.warn("Swift end-point pool [{}]: Endpoint {} is still unavailable", filestoreId, endpoint);
                        } else {
                            unblacklist(endpoint);
                        }
                    }
                }
            } catch (Throwable t) {
                LOG.error("Swift end-point pool [{}]: Error during heartbeat execution", filestoreId, t);
                ExceptionUtils.handleThrowable(t);
            }
        }

    }

}