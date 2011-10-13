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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.adapter.solrj.management;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexUrl;
import com.openexchange.mail.smal.SMALExceptionCodes;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.json.TrustAllAdapter;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link CommonsHttpSolrServerManagement}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CommonsHttpSolrServerManagement {

    private final ConcurrentMap<IndexUrl, Wrapper> map;

    private final int maxLifeMillis;

    private final ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link CommonsHttpSolrServerManagement}.
     */
    public CommonsHttpSolrServerManagement(final int maxCapacity, final int maxLifeMillis) {
        super();
        final Lock lock = new ReentrantLock();
        map = new LockBasedConcurrentMap<IndexUrl, Wrapper>(lock, lock, new MaxCapacityLinkedHashMap<IndexUrl, Wrapper>(maxCapacity));
        this.maxLifeMillis = maxLifeMillis;
        final Runnable task = new Runnable() {
            
            @Override
            public void run() {
                shrink();
            }
        };
        final int delay = maxLifeMillis / 3;
        timerTask = SMALServiceLookup.getInstance().getService(TimerService.class).scheduleWithFixedDelay(task, delay, delay);
    }

    /**
     * Puts specified mapping into this cache (if not already present)
     * 
     * @param indexUrl The index URL
     * @param solrServer The Solr server
     * @return <code>true</code> for successful put; otherwise <code>false</code>
     */
    public boolean putSolrServerIfAbsent(final IndexUrl indexUrl, final CommonsHttpSolrServer solrServer) {
        final Wrapper wrapper = new Wrapper(solrServer);
        Wrapper prev = map.putIfAbsent(indexUrl, wrapper);
        if (null == prev) {
            // Successfully put into map
            return true;
        }
        if (prev.elapsed(maxLifeMillis)) {
            synchronized (map) {
                prev = map.get(indexUrl);
                if (prev.elapsed(maxLifeMillis)) {
                    shrink();
                    map.put(indexUrl, wrapper);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes elapsed entries from map.
     */
    public void shrink() {
        final List<IndexUrl> removeKeys = new ArrayList<IndexUrl>(16);
        final long now = System.currentTimeMillis();
        for (final Entry<IndexUrl, Wrapper> entry : map.entrySet()) {
            final Wrapper wrapper = entry.getValue();
            if ((now - wrapper.getLastAccessed()) > maxLifeMillis) {
                removeKeys.add(entry.getKey());
            }
        }
        for (final IndexUrl key : removeKeys) {
            final Wrapper wrapper = map.remove(key);
            if (null != wrapper) {
                closeSolrServer(wrapper.getValueUnsafe());
            }
        }
    }

    /**
     * Closes specified Solr server.
     * 
     * @param server The Solr server to close
     */
    public static void closeSolrServer(final CommonsHttpSolrServer server) {
        if (null != server) {
            try {
                final HttpClient client = server.getHttpClient();
                closeSolrServer((CommonsHttpSolrServer) client.getParams().getParameter("solr.infinite-server"));
                ((MultiThreadedHttpConnectionManager) client.getHttpConnectionManager()).shutdown();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Checks is there is a mapping for specified index URL.
     * 
     * @param indexUrl The index URL
     * @return <code>true</code> if there is a mapping; otherwise <code>false</code>
     */
    public boolean containsSolrServer(final IndexUrl indexUrl) {
        return map.containsKey(indexUrl);
    }

    /**
     * Gets a new Solr server associated with specified index URL.
     * 
     * @param indexUrl The index URL
     * @return The new Solr server
     * @throws OXException If creation of a new Solr server fails
     */
    public CommonsHttpSolrServer newSolrServer(final IndexUrl indexUrl) throws OXException {
        return newCommonsHttpSolrServer(indexUrl, -1);
    }

    /**
     * Gets a new Solr server with a SO_TIMEOUT set which is associated with specified index URL either from cache or newly established.
     * 
     * @param indexUrl The index URL
     * @return The new Solr server without a SO_TIMEOUT applied
     * @throws OXException If creation of a new Solr server fails
     */
    public CommonsHttpSolrServer newNoTimeoutSolrServer(final IndexUrl indexUrl) throws OXException {
        return newCommonsHttpSolrServer(indexUrl, 0);
    }

    /**
     * Gets the Solr server associated with specified index URL either from cache or newly established.
     * 
     * @param indexUrl The index URL
     * @return The Solr server
     * @throws OXException If creation of a new Solr server fails
     */
    public CommonsHttpSolrServer getSolrServer(final IndexUrl indexUrl) throws OXException {
        final Wrapper wrapper = map.get(indexUrl);
        if (null == wrapper) {
            final CommonsHttpSolrServer solrServer = newCommonsHttpSolrServer(indexUrl, -1);
            map.put(indexUrl, new Wrapper(solrServer));
            return solrServer;
        }
        CommonsHttpSolrServer solrServer = wrapper.getValue(maxLifeMillis);
        if (null == solrServer) {
            map.remove(indexUrl);
            shrink();
            solrServer = newCommonsHttpSolrServer(indexUrl, -1);
            map.put(indexUrl, new Wrapper(solrServer));
        }
        return solrServer;
    }

    private CommonsHttpSolrServer newCommonsHttpSolrServer(final IndexUrl indexUrl, final int timeout) throws OXException {
        try {
            final CommonsHttpSolrServer server = new CommonsHttpSolrServer(indexUrl.getUrl());
            server.setSoTimeout(timeout < 0 ? indexUrl.getSoTimeout() : timeout);  // socket read timeout
            server.setConnectionTimeout(indexUrl.getConnectionTimeout());
            server.setDefaultMaxConnectionsPerHost(indexUrl.getMaxConnectionsPerHost());
            server.setMaxTotalConnections(indexUrl.getMaxConnectionsPerHost());
            server.setFollowRedirects(false);  // defaults to false
            // allowCompression defaults to false.
            // Server side must support gzip or deflate for this to have any effect.
            server.setAllowCompression(true);
            server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
            server.setParser(new XMLResponseParser()); // Otherwise binary parser is used by default
            // Configure HttpClient
            configureHttpClient(server.getHttpClient(), indexUrl, timeout);
            return server;
        } catch (final MalformedURLException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * The HTTP protocol constant.
     */
    private static final Protocol PROTOCOL_HTTP = Protocol.getProtocol("http");

    /**
     * The HTTPS identifier constant.
     */
    private static final String HTTPS = "https";

    private void configureHttpClient(final HttpClient client, final IndexUrl indexUrl, final int timeout) throws OXException {
        try {
            final int httpTimeout = timeout < 0 ? indexUrl.getSoTimeout() : timeout;
            final HttpClientParams clientParams = client.getParams();
            clientParams.setParameter("solr.index-url", indexUrl);
            clientParams.setParameter("solr.server-management", this);
            clientParams.setSoTimeout(httpTimeout);
            clientParams.setIntParameter("http.connection.timeout", httpTimeout);
            clientParams.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            /*
             * Create host configuration or URI
             */
            final java.net.URI uri = new java.net.URI(indexUrl.getUrl());
            final String host = uri.getHost();
            final HostConfiguration hostConfiguration;
            if (HTTPS.equalsIgnoreCase(uri.getScheme())) {
                int port = uri.getPort();
                if (port == -1) {
                    port = 443;
                }
                /*
                 * Own HTTPS host configuration and relative URI
                 */
                final Protocol httpsProtocol = new Protocol(HTTPS, ((ProtocolSocketFactory) new TrustAllAdapter()), port);
                hostConfiguration = new HostConfiguration();
                hostConfiguration.setHost(host, port, httpsProtocol);
            } else {
                int port = uri.getPort();
                if (port == -1) {
                    port = 80;
                }
                /*
                 * HTTP host configuration and relative URI
                 */
                hostConfiguration = new HostConfiguration();
                hostConfiguration.setHost(host, port, PROTOCOL_HTTP);
            }
            client.setHostConfiguration(hostConfiguration);
        } catch (final URISyntaxException e) {
            throw SMALExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the Solr server associated with specified index URL.
     * 
     * @param indexUrl The index URL
     * @return The Solr server or <code>null</code> if none available, yet
     */
    public CommonsHttpSolrServer optSolrServer(final IndexUrl indexUrl) {
        final Wrapper wrapper = map.get(indexUrl);
        if (null == wrapper) {
            return null;
        }
        final CommonsHttpSolrServer solrServer = wrapper.getValue(maxLifeMillis);
        if (null == solrServer) {
            map.remove(indexUrl);
            shrink();
            return null;
        }
        return solrServer;
    }

    /**
     * Shuts-down the cache.
     */
    public void shutDown() {
        timerTask.cancel(false);
        for (final Iterator<Wrapper> it = map.values().iterator(); it.hasNext();) {
            final Wrapper wrapper = it.next();
            if (null != wrapper) {
                closeSolrServer(wrapper.getValueUnsafe());
            }
            it.remove();
        }
        map.clear();
    }

    private static final class Wrapper {

        private final CommonsHttpSolrServer value;

        private final Lock readLock;
        
        private final Lock writeLock;

        private volatile long lastAccessed;

        public Wrapper(final CommonsHttpSolrServer value) {
            super();
            this.value = value;
            this.lastAccessed = System.currentTimeMillis();
            final ReadWriteLock rwLock = new ReentrantReadWriteLock();
            readLock = rwLock.readLock();
            writeLock = rwLock.writeLock();
        }

        public long getLastAccessed() {
            final Lock lock = readLock;
            lock.lock();
            try {
                return lastAccessed;
            } finally {
                lock.unlock();
            }
        }

        public boolean elapsed(final int maxLifeMillis) {
            final Lock lock = readLock;
            lock.lock();
            try {
                return (System.currentTimeMillis() - lastAccessed) > maxLifeMillis;
            } finally {
                lock.unlock();
            }
        }

        public CommonsHttpSolrServer getValueUnsafe() {
            return value;
        }

        public CommonsHttpSolrServer getValue(final int maxLifeMillis) {
            final Lock lock = writeLock;
            lock.lock();
            try {
                final long now = System.currentTimeMillis();
                if ((now - lastAccessed) > maxLifeMillis) {
                    return null;
                }
                this.lastAccessed = now;
                return value;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public String toString() {
            return value.getHttpClient().toString();
        }

    } // End of class Wrapper

}
