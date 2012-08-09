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

    private static final String SERVICE_ID = "openexchange.service.hazelcast";

    private abstract class AbstractRegisteringListener implements ClusterDiscoveryService, MDNSServiceListener {
        protected final List<ClusterListener> clusterListeners;
        protected final String serviceId;
        protected final AtomicReference<MDNSService> serviceRef;

        AbstractRegisteringListener(final String serviceId, final AtomicReference<MDNSService> serviceRef) {
            super();
            clusterListeners = new CopyOnWriteArrayList<ClusterListener>();
            this.serviceId = serviceId;
            this.serviceRef = serviceRef;
        }

        @Override
        public void addListener(final ClusterListener listener) {
            clusterListeners.add(listener);
        }

        @Override
        public void removeListener(final ClusterListener listener) {
            clusterListeners.remove(listener);
        }

        @Override
        public List<InetAddress> getNodes() {
            return Collections.emptyList();
        }

        public void close() {
            unregisterServices();
        }
    }

    private final class ImmediateRegisteringListener extends AbstractRegisteringListener {

        ImmediateRegisteringListener(final String serviceId, final AtomicReference<MDNSService> serviceRef) {
            super(serviceId, serviceRef);
            registerService(ClusterDiscoveryService.class, new MDNSClusterDiscoveryService(serviceId, serviceRef, this));
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
                        LOG.info("Notified ClusterListener '" + listener.getClass().getName() + "' about disappeared Open-Xchange node: " + inetAddress);
                    }
                }
            }
        }

        @Override
        public void onServiceAdded(final String serviceId, final MDNSServiceEntry entry) {
            if (this.serviceId.equals(serviceId)) {
                /*
                 * Notify listeners
                 */
                for (final ClusterListener listener : clusterListeners) {
                    for (final InetAddress inetAddress : entry.getAddresses()) {
                        listener.added(inetAddress);
                        LOG.info("Notified ClusterListener '" + listener.getClass().getName() + "' about appeared Open-Xchange node: " + inetAddress);
                    }
                }
            }
        }
    }

    private final class DelayedRegisteringListener extends AbstractRegisteringListener {

        private final AtomicBoolean registered;

        DelayedRegisteringListener(final String serviceId, final AtomicReference<MDNSService> serviceRef) {
            super(serviceId, serviceRef);
            registered = new AtomicBoolean();
        }

        @Override
        public void close() {
            super.close();
            registered.set(false);
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
                        LOG.info("Notified ClusterListener '" + listener.getClass().getName() + "' about disappeared Open-Xchange node: " + inetAddress);
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
                            LOG.info("Detected last Open-Xchange node disappeared. Therefore de-registered MDNS based ClusterDiscoveryService.");
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
                    LOG.info("Detected first Open-Xchange node. Therefore registered MDNS based ClusterDiscoveryService.");
                }
                /*
                 * Notify listeners
                 */
                for (final ClusterListener listener : clusterListeners) {
                    for (final InetAddress inetAddress : entry.getAddresses()) {
                        listener.added(inetAddress);
                        LOG.info("Notified ClusterListener '" + listener.getClass().getName() + "' about appeared Open-Xchange node: " + inetAddress);
                    }
                }
            }
        }

    }

    volatile AbstractRegisteringListener registeringListener;

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

    private boolean delayedRegistration() {
        return false;
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        final AtomicReference<MDNSService> serviceRef = new AtomicReference<MDNSService>();
        if (delayedRegistration()) {
            final DelayedRegisteringListener delayedRegisteringListener = new DelayedRegisteringListener(SERVICE_ID, serviceRef);
            this.registeringListener = delayedRegisteringListener;
            track(MDNSService.class, new ServiceTrackerCustomizer<MDNSService, MDNSService>() {

                @Override
                public MDNSService addingService(final ServiceReference<MDNSService> reference) {
                    final MDNSService service = context.getService(reference);
                    try {
                        serviceRef.set(service);
                        service.addListener(delayedRegisteringListener);
                        return service;
                    } catch (final Exception e) {
                        // Failure
                        LOG.error("Failed registration of MDNSClusterDiscoveryService.", e);
                        serviceRef.set(null);
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
                    service.removeListener(delayedRegisteringListener);
                    serviceRef.set(null);
                    context.ungetService(reference);
                }
            });
            openTrackers();
        } else {
            track(MDNSService.class, new ServiceTrackerCustomizer<MDNSService, MDNSService>() {

                @Override
                public MDNSService addingService(final ServiceReference<MDNSService> reference) {
                    final MDNSService service = context.getService(reference);
                    try {
                        serviceRef.set(service);
                        final ImmediateRegisteringListener registeringListener = new ImmediateRegisteringListener(SERVICE_ID, serviceRef);
                        service.addListener(registeringListener);
                        MDNSClusterDiscoveryActivator.this.registeringListener = registeringListener;
                        return service;
                    } catch (final Exception e) {
                        // Failure
                        LOG.error("Failed registration of MDNSClusterDiscoveryService.", e);
                        serviceRef.set(null);
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
                    final AbstractRegisteringListener registeringListener = MDNSClusterDiscoveryActivator.this.registeringListener;
                    if (null != registeringListener) {
                        service.removeListener(registeringListener);
                        registeringListener.close();
                        MDNSClusterDiscoveryActivator.this.registeringListener = null;
                    }
                    serviceRef.set(null);
                    context.ungetService(reference);
                }
            });
            openTrackers();
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final AbstractRegisteringListener registeringListener = this.registeringListener;
        if (null != registeringListener) {
            registeringListener.close();
            this.registeringListener = null;
        }
        super.stopBundle();
    }
    
}
