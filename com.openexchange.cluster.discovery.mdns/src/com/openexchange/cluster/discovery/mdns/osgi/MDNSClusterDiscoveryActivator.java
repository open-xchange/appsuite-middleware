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
import java.util.Dictionary;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.cluster.discovery.ClusterDiscoveryService;
import com.openexchange.cluster.discovery.ClusterListener;
import com.openexchange.cluster.discovery.ClusterMember;
import com.openexchange.cluster.discovery.mdns.MDNSClusterDiscoveryService;
import com.openexchange.config.ConfigurationService;
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

    private final class ClusterAwareMdnsServiceListener implements MDNSServiceListener {

        private final String serviceId;
        private final MDNSClusterDiscoveryService service;

        protected ClusterAwareMdnsServiceListener(final String serviceId, final MDNSClusterDiscoveryService service) {
            super();
            this.service = service;
            this.serviceId = serviceId;
        }

        @Override
        public void onServiceRemoved(final String serviceId, final MDNSServiceEntry entry) {
            LOG.debug("Removed: " + entry);
            if (this.serviceId.equals(serviceId)) {
                /*
                 * Notify listeners
                 */
                for (final ClusterListener listener : service.getListeners()) {
                    for (final InetAddress inetAddress : entry.getAddresses()) {
                        listener.removed(ClusterMember.valueOf(inetAddress, entry.getPort()));
                        LOG.info("Notified ClusterListener '" + listener.getClass().getName() + "' about disappeared Open-Xchange node: " + inetAddress);
                    }
                }
            }
        }

        @Override
        public void onServiceAdded(final String serviceId, final MDNSServiceEntry entry) {
            LOG.debug("Added: " + entry);
            if (this.serviceId.equals(serviceId)) {
                /*
                 * Notify listeners
                 */
                for (final ClusterListener listener : service.getListeners()) {
                    for (final InetAddress inetAddress : entry.getAddresses()) {
                        listener.added(ClusterMember.valueOf(inetAddress, entry.getPort()));
                        LOG.info("Notified ClusterListener '" + listener.getClass().getName() + "' about appeared Open-Xchange node: " + inetAddress);
                    }
                }
            }
        }
    }

    /**
     * Reference for RegisteringListener.
     */
    protected final AtomicReference<ClusterAwareMdnsServiceListener> registeringListenerRef;

    /**
     * Initializes a new {@link MDNSClusterDiscoveryActivator}.
     */
    public MDNSClusterDiscoveryActivator() {
        super();
        registeringListenerRef = new AtomicReference<ClusterAwareMdnsServiceListener>();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new  Class<?>[] { ConfigurationService.class };
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service, final Dictionary<String, ?> props) {
        super.registerService(clazz, service, props);
    }

    @Override
    public void unregisterServices() {
        super.unregisterServices();
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        // Form service ID from cluster name
        final String serviceID = getService(ConfigurationService.class).getProperty("com.openexchange.cluster.name");
        if (null == serviceID || 0 == serviceID.trim().length()) {
            throw new BundleException(
                "Cluster name is mandatory. Please set a valid identifier through property \"com.openexchange.cluster.name\".",
                BundleException.ACTIVATOR_ERROR);
        } else if ("ox".equalsIgnoreCase(serviceID)) {
            LOG.warn("\n\tThe configuration value for \"com.openexchange.cluster.name\" has not been changed from it's default value " + "\"ox\". Please do so to make this warning disappear.\n");
        }
        LOG.info("Cluster Discovery will track services with ID \"" + serviceID + "\".");
        // Create service instance
        final MDNSClusterDiscoveryService mdnsClusterDiscoveryService = new MDNSClusterDiscoveryService(serviceID, context);
        rememberTracker(mdnsClusterDiscoveryService);
        // Tracker for MDNSService
        track(MDNSService.class, new ServiceTrackerCustomizer<MDNSService, MDNSService>() {

            @Override
            public MDNSService addingService(final ServiceReference<MDNSService> reference) {
                final MDNSService service = context.getService(reference);
                mdnsClusterDiscoveryService.setMDNSService(service);
                service.addListener(new ClusterAwareMdnsServiceListener(serviceID, mdnsClusterDiscoveryService));
                return service;
            }

            @Override
            public void modifiedService(final ServiceReference<MDNSService> reference, final MDNSService service) {
                // Ignore
            }

            @Override
            public void removedService(final ServiceReference<MDNSService> reference, final MDNSService service) {
                context.ungetService(reference);
            }
        });
        openTrackers();
        // Register MDNS-based ClusterDiscoveryService
        registerService(ClusterDiscoveryService.class, mdnsClusterDiscoveryService);
    }

}
