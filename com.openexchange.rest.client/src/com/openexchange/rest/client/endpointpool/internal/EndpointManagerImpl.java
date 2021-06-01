/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.rest.client.endpointpool.EndpointAvailableStrategy.AvailableResult;
import com.openexchange.rest.client.endpointpool.EndpointManager;
import com.openexchange.rest.client.httpclient.HttpClientService;
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
    private final int total;
    private volatile ScheduledTimerTask heartbeat;

    /**
     * Initializes a new {@link EndpointManagerImpl}.
     *
     * @throws IllegalArgumentException If initialization fails
     */
    public EndpointManagerImpl(List<URI> endpointUris, String httpClientId, EndpointAvailableStrategy availableStrategy, long heartbeatIntervalMillis, TimerService timerService, HttpClientService httpClientService) {
        super();
        if (null == endpointUris) {
            throw new IllegalArgumentException("End-points must not be null");
        }

        int size = endpointUris.size();
        if (size == 0) {
            throw new IllegalArgumentException("End-points must not be empty");
        }

        total = size;
        available = new ArrayList<>(size);
        for (URI uri : endpointUris) {
            available.add(new EndpointImpl(uri));
        }
        blacklist = new ArrayList<>(size);
        counter = new AtomicInteger(size);
        heartbeat = timerService.scheduleWithFixedDelay(new Heartbeat(httpClientService.getHttpClient(httpClientId), availableStrategy), heartbeatIntervalMillis, heartbeatIntervalMillis);
    }

    @Override
    public int getNumberOfEndpoints() {
        return total;
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
            return new ArrayList<>(blacklist);
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
