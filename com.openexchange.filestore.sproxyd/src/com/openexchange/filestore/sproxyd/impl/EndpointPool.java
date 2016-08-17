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

package com.openexchange.filestore.sproxyd.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * A {@link EndpointPool} manages a set of endpoints for the sproxyd client. The available
 * endpoints are returned in a round-robin manner. If endpoints become unavailable they can
 * be blacklisted. Every host on the blacklist is periodically checked by a heartbeat for
 * availability. If a formerly blacklisted host becomes available again, it is removed from
 * the blacklist and returned to the pool of available hosts. The process of blacklisting
 * an endpoint is up to the client.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@ThreadSafe
public class EndpointPool {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointPool.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final List<String> available;

    private final List<String> blacklist;

    private final AtomicInteger counter;

    private final String filestoreId;

    private ScheduledTimerTask heartbeat;

    /**
     * Initializes a new {@link EndpointPool}.
     *
     * @param filestoreId The filestore ID
     * @param endpointUrls A list of endpoint URLs to manage; must not be empty; URLs must always end with a trailing slash
     * @param httpClient
     * @param heartbeatInterval
     * @param timerService
     */
    public EndpointPool(String filestoreId, List<String> endpointUrls, HttpClient httpClient, int heartbeatInterval, TimerService timerService) {
        super();
        this.filestoreId = filestoreId;
        int size = endpointUrls.size();
        available = new ArrayList<>(endpointUrls);
        blacklist = new ArrayList<>(size);
        counter = new AtomicInteger(size);
        if (endpointUrls.isEmpty()) {
            throw new IllegalArgumentException("Paramater 'endpointUrls' must not be empty");
        }

        LOG.debug("Sproxyd endpoint pool [{}]: Scheduling heartbeat timer task", filestoreId);
        heartbeat = timerService.scheduleWithFixedDelay(new Heartbeat(filestoreId, this, httpClient), heartbeatInterval, heartbeatInterval);
    }

    /**
     * Gets an available endpoint.
     *
     * @param contextId The context ID
     * @param userId The userID
     * @return The endpoint or <code>null</code> if all endpoints have been blacklisted
     */
    public Endpoint get(int contextId, int userId) {
        lock.readLock().lock();
        try {
            if (available.isEmpty()) {
                return null;
            }

            int next = counter.incrementAndGet();
            if (next < 0) {
                int newNext = available.size();
                counter.compareAndSet(next, newNext);
                next = newNext;
            }
            Endpoint endpoint = new Endpoint(available.get(next % available.size()), contextId, userId);
            LOG.debug("Sproxyd endpoint pool [{}]: Returning endpoint {}", filestoreId, endpoint);
            return endpoint;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Removes an endpoint from the list of available ones and adds it to the blacklist.
     *
     * @param url The base URL of the endpoint
     */
    public void blacklist(String url) {
        lock.writeLock().lock();
        try {
            if (available.remove(url)) {
                LOG.warn("Sproxyd endpoint pool [{}]: Endpoint {} is added to blacklist", filestoreId, url);
                blacklist.add(url);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes an endpoint from the blacklist and adds it back to list of available ones.
     *
     * @param url The base URL of the endpoint
     */
    public void unblacklist(String url) {
        lock.writeLock().lock();
        try {
            if (blacklist.remove(url)) {
                LOG.info("Sproxyd endpoint pool [{}]: Endpoint {} is removed from blacklist", filestoreId, url);
                available.add(url);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Closes this endpoint pool instance. The blacklist heartbeat task is cancelled.
     */
    public synchronized void close() {
        if (heartbeat != null) {
            heartbeat.cancel();
            heartbeat = null;
        }
    }

    private List<String> getBlacklist() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(blacklist);
        } finally {
            lock.readLock().unlock();
        }
    }

    private static final class Heartbeat implements Runnable {

        private final String filestoreId;
        private final EndpointPool endpoints;
        private final HttpClient httpClient;

        private Heartbeat(String filestoreId, EndpointPool endpoints, HttpClient httpClient) {
            super();
            this.filestoreId = filestoreId;
            this.endpoints = endpoints;
            this.httpClient = httpClient;
        }

        @Override
        public void run() {
            try {
                List<String> blacklist = endpoints.getBlacklist();
                if (blacklist.isEmpty()) {
                    LOG.debug("Sproxyd endpoint pool [{}]: Heartbeat - blacklist is empty, nothing to do", filestoreId);
                    return;
                }

                LOG.debug("Sproxyd endpoint pool [{}]: Heartbeat - blacklist contains {} endpoints", filestoreId, blacklist.size());
                for (String endpoint : blacklist) {
                    if (Utils.endpointUnavailable(endpoint, httpClient)) {
                        LOG.warn("Sproxyd endpoint pool [{}]: Endpoint {} is still unavailable", filestoreId, endpoint);
                    } else {
                        endpoints.unblacklist(endpoint);
                    }
                }
            } catch (Throwable t) {
                LOG.error("Sproxyd endpoint pool [{}]: Error during heartbeat execution", filestoreId, t);
                ExceptionUtils.handleThrowable(t);
            }
        }

    }

}