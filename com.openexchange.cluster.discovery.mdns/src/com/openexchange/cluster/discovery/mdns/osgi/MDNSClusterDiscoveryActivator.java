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

package com.openexchange.cluster.discovery.mdns.osgi;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.cluster.discovery.ClusterDiscoveryService;
import com.openexchange.cluster.discovery.ClusterListener;
import com.openexchange.cluster.discovery.mdns.MDNSClusterDiscoveryService;
import com.openexchange.exception.OXException;
import com.openexchange.mdns.MDNSService;
import com.openexchange.mdns.MDNSServiceEntry;
import com.openexchange.mdns.MDNSServiceListener;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MDNSClusterDiscoveryActivator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MDNSClusterDiscoveryActivator extends HousekeepingActivator {

    static final Log LOG = com.openexchange.log.Log.loggerFor(MDNSClusterDiscoveryActivator.class);

    private final class Listener implements MDNSServiceListener, ClusterDiscoveryService {

        private final List<ClusterListener> clusterListeners;
        private final String serviceId;
        private final AtomicReference<MDNSService> serviceRef;
        private final AtomicBoolean registered;

        Listener(final String serviceId, final AtomicReference<MDNSService> serviceRef) {
            super();
            registered = new AtomicBoolean();
            clusterListeners = new CopyOnWriteArrayList<ClusterListener>();
            this.serviceId = serviceId;
            this.serviceRef = serviceRef;
        }

        @Override
        public void onServiceRemoved(final String serviceId, final MDNSServiceEntry entry) {
            if (this.serviceId.equals(serviceId)) {
                /*
                 * Notify listeners
                 */
                for (final ClusterListener listener : clusterListeners) {
                    for (final InetAddress inetAddress : entry.getAddresses()) {
                        listener.removed(inetAddress);
                    }
                }
                /*
                 * Check
                 */
                final MDNSService mdnsService = serviceRef.get();
                if (null != mdnsService) {
                    try {
                        final List<MDNSServiceEntry> tmp = mdnsService.listByService(serviceId);
                        if (null == tmp || tmp.isEmpty()) {
                            unregisterServices();
                        }
                    } catch (final OXException e) {
                        LOG.error("Unregistration failed.", e);
                    }
                }
            }
        }

        @Override
        public void onServiceAdded(final String serviceId, final MDNSServiceEntry entry) {
            if (this.serviceId.equals(serviceId)) {
                /*
                 * Register service
                 */
                if (registered.compareAndSet(false, true)) {
                    registerService(ClusterDiscoveryService.class, new MDNSClusterDiscoveryService(serviceId, serviceRef, this));
                }
                /*
                 * Notify listeners
                 */
                for (final ClusterListener listener : clusterListeners) {
                    for (final InetAddress inetAddress : entry.getAddresses()) {
                        listener.added(inetAddress);
                    }
                }
            }
        }

        @Override
        public List<InetAddress> getNodes() {
            return Collections.emptyList();
        }

        @Override
        public void addListener(final ClusterListener listener) {
            clusterListeners.add(listener);
        }

        @Override
        public void removeListener(final ClusterListener listener) {
            clusterListeners.remove(listener);
        }
    }

    /**
     * Initializes a new {@link MDNSClusterDiscoveryActivator}.
     */
    public MDNSClusterDiscoveryActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    public void unregisterServices() {
        super.unregisterServices();
    }

    @Override
    protected void startBundle() throws Exception {
        final AtomicReference<MDNSService> serviceRef = new AtomicReference<MDNSService>();
        final String cServiceId = "openexchange.service.lookup";
        final MDNSServiceListener listener = new Listener(cServiceId, serviceRef);
        final BundleContext context = this.context;
        track(MDNSService.class, new ServiceTrackerCustomizer<MDNSService, MDNSService>() {

            @Override
            public MDNSService addingService(final ServiceReference<MDNSService> reference) {
                final MDNSService service = context.getService(reference);
                try {
                    service.addListener(listener);
                    serviceRef.set(service);
                    return service;
                } catch (final Exception e) {
                    // Failure
                    LOG.error("Failed registration of MDNSClusterDiscoveryService.", e);
                    context.ungetService(reference);
                    return null;
                }
            }

            @Override
            public void modifiedService(final ServiceReference<MDNSService> reference, final MDNSService service) {
                // Ignore
            }

            @Override
            public void removedService(final ServiceReference<MDNSService> reference, final MDNSService service) {
                if (null == service) {
                    return;
                }
                service.removeListener(listener);
                serviceRef.set(null);
                context.ungetService(reference);
            }
        });
    }

}
