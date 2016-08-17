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

package com.openexchange.rest.client.endpointpool.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.http.client.HttpClient;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.rest.client.endpointpool.Endpoint;
import com.openexchange.rest.client.endpointpool.EndpointAvailableStrategy;
import com.openexchange.rest.client.endpointpool.EndpointManager;
import com.openexchange.rest.client.endpointpool.EndpointAvailableStrategy.AvailableResult;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link EndpointManagerImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class EndpointManagerImpl implements EndpointManager {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<Endpoint> available;
    private final List<Endpoint> blacklist;
    private final AtomicInteger counter;
    private volatile ScheduledTimerTask heartbeat;

    /**
     * Initializes a new {@link EndpointManagerImpl}.
     *
     * @throws IllegalArgumentException If initialization fails
     */
    public EndpointManagerImpl(List<URI> endpointUris, HttpClient httpClient, EndpointAvailableStrategy availableStrategy, long heartbeatIntervalMillis, TimerService timerService) {
        super();
        if (null == endpointUris) {
            throw new IllegalArgumentException("End-points must not be null");
        }

        int size = endpointUris.size();
        if (size == 0) {
            throw new IllegalArgumentException("End-points must not be empty");
        }

        available = new ArrayList<Endpoint>(size);
        for (URI uri : endpointUris) {
            available.add(new EndpointImpl(uri));
        }
        blacklist = new ArrayList<Endpoint>(size);
        counter = new AtomicInteger(size);
        heartbeat = timerService.scheduleWithFixedDelay(new Heartbeat(httpClient, availableStrategy), heartbeatIntervalMillis, heartbeatIntervalMillis);
    }

    @Override
    public Endpoint get() {
        lock.readLock().lock();
        try {
            int size = available.size();
            if (size == 0) {
                return null;
            }

            if (size == 1) {
                return available.get(0);
            }

            int next = counter.incrementAndGet();
            if (next < 0) {
                int newNext = size;
                counter.compareAndSet(next, newNext);
                next = newNext;
            }

            return available.get(next % size);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean hasAny() {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return !available.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean blacklist(Endpoint endpoint) {
        lock.writeLock().lock();
        try {
            if (available.remove(endpoint)) {
                blacklist.add(endpoint);
            }
            return !available.isEmpty();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unblacklist(Endpoint endpoint) {
        lock.writeLock().lock();
        try {
            if (blacklist.remove(endpoint)) {
                available.add(endpoint);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public synchronized void close() {
        ScheduledTimerTask heartbeat = this.heartbeat;
        if (heartbeat != null) {
            this.heartbeat = null;
            heartbeat.cancel();
        }
    }

    @Override
    public List<Endpoint> getBlacklist() {
        lock.readLock().lock();
        try {
            return new ArrayList<Endpoint>(blacklist);
        } finally {
            lock.readLock().unlock();
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------- //

    private final class Heartbeat implements Runnable {

        private final HttpClient httpClient;
        private final EndpointAvailableStrategy availableStrategy;

        Heartbeat(HttpClient httpClient, EndpointAvailableStrategy availableStrategy) {
            super();
            this.httpClient = httpClient;
            this.availableStrategy = availableStrategy;
        }

        @Override
        public void run() {
            try {
                List<Endpoint> blacklist = getBlacklist();
                if (blacklist.isEmpty()) {
                    return;
                }

                for (Endpoint endpoint : blacklist) {
                    AvailableResult availableResult = availableStrategy.isEndpointAvailable(endpoint, httpClient);
                    if (AvailableResult.AVAILABLE == availableResult) {
                        unblacklist(endpoint);
                    }
                }
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
            }
        }
    }

}
