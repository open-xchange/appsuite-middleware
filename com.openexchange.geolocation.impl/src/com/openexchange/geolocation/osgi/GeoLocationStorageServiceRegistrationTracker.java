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

package com.openexchange.geolocation.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.geolocation.GeoLocationRMIService;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.geolocation.GeoLocationStorageService;
import com.openexchange.geolocation.impl.GeoLocationRMIServiceImpl;
import com.openexchange.geolocation.impl.GeoLocationServiceImpl;
import com.openexchange.geolocation.impl.GeoLocationStorageServiceRegistry;
import com.openexchange.server.ServiceLookup;

/**
 * {@link GeoLocationStorageServiceRegistrationTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class GeoLocationStorageServiceRegistrationTracker implements ServiceTrackerCustomizer<GeoLocationStorageService, GeoLocationStorageService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationStorageServiceRegistrationTracker.class);

    private final BundleContext context;
    private final ServiceLookup services;
    private ServiceRegistration<GeoLocationService> serviceRegistration;
    private ServiceRegistration<Remote> rmiServiceRegistration;

    /**
     * Initialises a new {@link GeoLocationStorageServiceRegistrationTracker}.
     */
    public GeoLocationStorageServiceRegistrationTracker(BundleContext context, ServiceLookup services) {
        super();
        this.context = context;
        this.services = services;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public GeoLocationStorageService addingService(ServiceReference<GeoLocationStorageService> reference) {
        GeoLocationStorageService storage = context.getService(reference);
        GeoLocationStorageServiceRegistry.getInstance().registerServiceProvider(storage.getProviderId(), storage);
        registerServiceIfNeeded();
        LOGGER.info("Registered the GeoLocationStorageService provider {}.", storage.getProviderId());
        return storage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference<GeoLocationStorageService> reference, GeoLocationStorageService service) {
        // no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference<GeoLocationStorageService> reference, GeoLocationStorageService service) {
        GeoLocationStorageServiceRegistry.getInstance().unregisterServiceProvider(service.getProviderId());
        context.ungetService(reference);
        LOGGER.info("Unregistered the GeoLocationStorageService provider {}.", service.getProviderId());
        if (false == GeoLocationStorageServiceRegistry.getInstance().hasStorages() && serviceRegistration != null) {
            rmiServiceRegistration.unregister();
            rmiServiceRegistration = null;
            LOGGER.info("Unregistered the GeoLocationRMIService.");

            serviceRegistration.unregister();
            serviceRegistration = null;
            LOGGER.info("Unregistered the GeoLocationService.");
        }
    }

    /**
     * Registers the {@link GeoLocationService} and its RMI counterpart if needed
     */
    private void registerServiceIfNeeded() {
        if (serviceRegistration != null) {
            return;
        }
        Dictionary<String, Object> props = new Hashtable<String, Object>(2);
        props.put("RMIName", GeoLocationRMIService.RMI_NAME);
        serviceRegistration = context.registerService(GeoLocationService.class, new GeoLocationServiceImpl(services), null);
        LOGGER.info("Registered the GeoLocationService.");
        rmiServiceRegistration = context.registerService(Remote.class, new GeoLocationRMIServiceImpl(services), props);
        LOGGER.info("Registered the GeoLocationRMIService.");
    }
}
