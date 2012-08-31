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

package com.openexchange.mdns.internal;

import static com.openexchange.java.util.UUIDs.getUnformattedString;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.JmDNSImpl;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.mdns.MDNSExceptionCodes;
import com.openexchange.mdns.MDNSService;
import com.openexchange.mdns.MDNSServiceEntry;
import com.openexchange.mdns.MDNSServiceInfo;
import com.openexchange.mdns.MDNSServiceListener;

/**
 * {@link MDNSServiceImpl} - The mDNS service implementation backed by <a href="http://sourceforge.net/projects/jmdns/">JmDNS</a>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MDNSServiceImpl implements MDNSService, MDNSReregisterer {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MDNSServiceImpl.class));

    /**
     * Maps service identifiers to available services
     */
    private final ConcurrentMap<String, ConcurrentMap<UUID, MDNSServiceEntry>> map;

    private final ConcurrentMap<Key, ServiceInfo> registeredServicesSet;

    private final JmDNS jmdns;

    private final ServiceListener serviceListener;

    private final Lock rlock;

    private final Lock wlock;

    private final List<MDNSServiceListener> listeners;

    /**
     * Initializes a new {@link MDNSServiceImpl}.
     * 
     * @throws OXException If initialization fails
     */
    public MDNSServiceImpl() throws OXException {
        super();
        try {
            jmdns = JmDNS.create();
            /*
             * Register the "_openexchange._tcp.local." service type
             */
            final String serviceType = Constants.SERVICE_TYPE;
            if (!((JmDNSImpl) jmdns).getServiceTypes().containsKey(serviceType)) {
                jmdns.registerServiceType(serviceType);
            }
            map = new NonBlockingHashMap<String, ConcurrentMap<UUID, MDNSServiceEntry>>();
            registeredServicesSet = new NonBlockingHashMap<Key, ServiceInfo>();
            /*
             * Add service listener for "_openexchange._tcp.local."
             */
            jmdns.addServiceListener(serviceType, (serviceListener = new MDNSListener(map, this)));
            final ReadWriteLock rw = new ReentrantReadWriteLock();
            rlock = rw.readLock();
            wlock = rw.writeLock();
            listeners = new CopyOnWriteArrayList<MDNSServiceListener>();
        } catch (final IOException e) {
            throw MDNSExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    private UUID getIdentifierFor(/* final String serviceId */) {
        return UUID.randomUUID();
    }

    /**
     * Closes this mDNS service.
     */
    public void close() {
        wlock.lock();
        try {
            map.clear();
            registeredServicesSet.clear();
            jmdns.removeServiceListener(Constants.SERVICE_TYPE, serviceListener);
            jmdns.unregisterAllServices();
            jmdns.close();
        } catch (final Exception e) {
            LOG.error("Closing JmDNS instance failed.", e);
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public void serviceAdded(final String serviceId, final MDNSServiceEntry entry) {
        for (final MDNSServiceListener listener : listeners) {
            try {
                listener.onServiceAdded(serviceId, entry);
            } catch (final Exception e) {
                LOG.warn("Listener '" + listener.getClass().getName() + "' failed.", e);
            }
        }
    }

    @Override
    public void serviceRemoved(final String serviceId, final MDNSServiceEntry entry) {
        for (final MDNSServiceListener listener : listeners) {
            try {
                listener.onServiceRemoved(serviceId, entry);
            } catch (final Exception e) {
                LOG.warn("Listener '" + listener.getClass().getName() + "' failed.", e);
            }
        }
    }

    @Override
    public void addListener(final MDNSServiceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(final MDNSServiceListener listener) {
        listeners.remove(listener);
    }

    @Override
    public List<MDNSServiceEntry> listByService(final String serviceId) throws OXException {
        rlock.lock();
        try {
            final ConcurrentMap<UUID, MDNSServiceEntry> inner = map.get(serviceId);
            if (null == inner || inner.isEmpty()) {
                return Collections.<MDNSServiceEntry> emptyList();
            }
            return new ArrayList<MDNSServiceEntry>(inner.values());
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public MDNSServiceInfo registerService(final String serviceId, final int port, final String info) throws OXException {
        wlock.lock();
        try {
            final UUID id = getIdentifierFor(/* serviceId */);
            final String name = new StringBuilder().append(getUnformattedString(id)).append('/').append(serviceId).toString();
            final ServiceInfo sinfo = ServiceInfo.create(Constants.SERVICE_TYPE, name, port, null == info ? "" : info);
            jmdns.registerService(sinfo);
            if (LOG.isInfoEnabled()) {
                LOG.info(new StringBuilder(64).append("Registered new service: ").append(sinfo).toString());
            }
            registeredServicesSet.put(new Key(id, serviceId), sinfo);
            return new MDNSServiceInfoImpl(id, serviceId, port, info);
        } catch (final IOException e) {
            throw MDNSExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public void unregisterService(final MDNSServiceInfo serviceInfo) throws OXException {
        wlock.lock();
        try {
            final ServiceInfo sinfo = registeredServicesSet.remove(new Key(serviceInfo.getId(), serviceInfo.getServiceId()));
            if (null == sinfo) {
                return;
            }
            jmdns.unregisterService(sinfo);
            if (LOG.isInfoEnabled()) {
                LOG.info(new StringBuilder(64).append("Un-Registered service: ").append(sinfo).toString());
            }
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public void reregisterServices() {
        /*-
         *
        wlock.lock();
        try {
            for (final ServiceInfo sinfo : registeredServicesSet.values()) {
                try {
                    jmdns.registerService(sinfo);
                } catch (final IOException e) {
                    LOG.error(new StringBuilder(64).append("Re-registration failed for service: ").append(sinfo).toString(), e);
                }
            }
        } finally {
            wlock.unlock();
        }
         */
    }

    @Override
    public boolean contains(final UUID id, final String serviceId) {
        return registeredServicesSet.containsKey(new Key(id, serviceId));
    }

    /*-
     * -------------------------- Key class ------------------------------
     */

    private static final class Key {

        private final UUID id;

        private final String serviceId;

        private final int hash;

        public Key(final UUID id, final String serviceId) {
            super();
            this.id = id;
            this.serviceId = serviceId;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            if (serviceId == null) {
                if (other.serviceId != null) {
                    return false;
                }
            } else if (!serviceId.equals(other.serviceId)) {
                return false;
            }
            return true;
        }

    }

}
