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

package com.openexchange.mail.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.session.Session;

/**
 * Service tracker for transport providers
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TransportProviderServiceTracker implements ServiceTrackerCustomizer<TransportProvider,TransportProvider> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TransportProviderServiceTracker.class);

    private final BundleContext context;
    volatile ServiceRegistration<CapabilityChecker> capabilityChecker;
    private volatile ServiceTracker<CapabilityService, CapabilityService> capabilityServiceTracker;

    /**
     * Initializes a new {@link TransportProviderServiceTracker}
     */
    public TransportProviderServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public synchronized TransportProvider addingService(final ServiceReference<TransportProvider> reference) {
        final BundleContext context = this.context;
        final TransportProvider transportProvider = context.getService(reference);
        if (null == transportProvider) {
            LOG.warn("Added service is null!", new Throwable());
            context.ungetService(reference);
            return null;
        }
        final Object protocol = reference.getProperty("protocol");
        if (null == protocol) {
            LOG.error("Missing protocol in transport provider service: {}", transportProvider.getClass().getName());
            context.ungetService(reference);
            return null;
        }
        try {
            if (TransportProviderRegistry.registerTransportProvider(protocol.toString(), transportProvider)) {
                LOG.info("Transport provider for protocol '{}' successfully registered", protocol);
            } else {
                LOG.warn("Transport provider for protocol '{}' could not be added.Another provider which supports the protocol has already been registered.", protocol);
                context.ungetService(reference);
                return null;
            }
        } catch (final OXException e) {
            LOG.error("", e);
            context.ungetService(reference);
            return null;
        }

        capabilityServiceTracker = new ServiceTracker<CapabilityService, CapabilityService>(context, CapabilityService.class, null) {

            @Override
            public CapabilityService addingService(ServiceReference<CapabilityService> ref) {
                final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
                final String sCapability = "auto_publish_attachments";
                properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
                capabilityChecker = context.registerService(CapabilityChecker.class, new CapabilityChecker() {

                    @Override
                    public boolean isEnabled(String capability, Session ses) throws OXException {
                        if (sCapability.equals(capability)) {
                            return false;
                        }

                        return true;
                    }
                }, properties);

                CapabilityService capabilityService = context.getService(ref);
                capabilityService.declareCapability(sCapability);
                return capabilityService;
            }
        };
        capabilityServiceTracker.open();

        return transportProvider;
    }

    @Override
    public void modifiedService(final ServiceReference<TransportProvider> reference, final TransportProvider service) {
        // Nothing to do
    }

    @Override
    public synchronized void removedService(final ServiceReference<TransportProvider> reference, final TransportProvider service) {
        if (null != service) {
            try {
                try {
                    final TransportProvider provider = service;
                    TransportProviderRegistry.unregisterTransportProvider(provider);
                    LOG.info("Transport provider for protocol '{}' successfully unregistered", provider.getProtocol());
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            } finally {
                context.ungetService(reference);
            }

            if (TransportProviderRegistry.isEmpty()) {
                final ServiceRegistration<CapabilityChecker> capabilityChecker = this.capabilityChecker;
                if (capabilityChecker != null) {
                    capabilityChecker.unregister();
                    this.capabilityChecker = null;

                    final ServiceTracker<CapabilityService, CapabilityService> capabilityServiceTracker = this.capabilityServiceTracker;
                    if (capabilityServiceTracker != null) {
                        capabilityServiceTracker.close();
                        this.capabilityServiceTracker = null;
                    }
                }
            }
        }
    }

}
