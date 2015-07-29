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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.timer.TimerService;

/**
 * {@link EndpointPool}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class EndpointPool {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointPool.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final List<Endpoint> available;

    private final List<Endpoint> blacklist;

    private final AtomicInteger counter;

    private final String filestoreId;

    public EndpointPool(String filestoreId, List<Endpoint> endpointUrls, DefaultHttpClient httpClient, TimerService timerService) {
        super();
        this.filestoreId = filestoreId;
        available = new ArrayList<>(endpointUrls);
        blacklist = new ArrayList<>(endpointUrls.size());
        counter = new AtomicInteger(endpointUrls.size());
        if (endpointUrls.size() > 1) {
            LOG.debug("Sproxyd endpoint pool [{}]: Scheduling heartbeat timer task", filestoreId);
            timerService.scheduleWithFixedDelay(new Heartbeat(filestoreId, this, httpClient), 60000l, 60000l);
        }
    }

    public Endpoint get() {
        lock.readLock().lock();
        try {
            int next = counter.incrementAndGet();
            if (next < 0) {
                int newNext = available.size();
                counter.compareAndSet(next, newNext);
                next = newNext;
            }
            Endpoint endpoint = available.get(next % available.size());
            LOG.debug("Sproxyd endpoint pool [{}]: Returning endpoint {}", filestoreId, endpoint);
            return endpoint;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void blacklist(Endpoint endpoint) {
        lock.writeLock().lock();
        try {
            // only blacklist if there are more endpoints available
            if (available.size() > 1 && available.remove(endpoint)) {
                LOG.warn("Sproxyd endpoint pool [{}]: Endpoint {} is added to blacklist", filestoreId, endpoint);
                blacklist.add(endpoint);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void unblacklist(Endpoint endpoint) {
        lock.writeLock().lock();
        try {
            if (blacklist.remove(endpoint)) {
                LOG.info("Sproxyd endpoint pool [{}]: Endpoint {} is removed from blacklist", filestoreId, endpoint);
                available.add(endpoint);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Endpoint> getBlacklist() {
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
                List<Endpoint> blacklist = endpoints.getBlacklist();
                if (blacklist.isEmpty()) {
                    LOG.debug("Sproxyd endpoint pool [{}]: Heartbeat - blacklist is empty, nothing to do", filestoreId);
                    return;
                }

                LOG.debug("Sproxyd endpoint pool [{}]: Heartbeat - blacklist contains {} endpoints", filestoreId, blacklist.size());
                for (Endpoint endpoint : blacklist) {
                    HttpGet get = null;
                    HttpResponse response = null;
                    try {
                        get = new HttpGet(endpoint.getConfUrl());
                        response = httpClient.execute(get);
                        int status = response.getStatusLine().getStatusCode();
                        if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_PARTIAL_CONTENT == status) {
                            endpoints.unblacklist(endpoint);
                        }
                    } catch (ClientProtocolException e) {
                        LOG.warn("Sproxyd endpoint pool [{}]: Endpoint {} is still unavailable", filestoreId, endpoint);
                    } catch (IOException e) {
                        LOG.warn("Sproxyd endpoint pool [{}]: Endpoint {} is still unavailable", filestoreId, endpoint);
                    } finally {
                        Utils.close(get, response);
                    }
                }
            } catch (Throwable t) {
                LOG.info("Sproxyd endpoint pool [{}]: Error during heartbeat execution", filestoreId, t);
                ExceptionUtils.handleThrowable(t);
            }
        }

    }

}