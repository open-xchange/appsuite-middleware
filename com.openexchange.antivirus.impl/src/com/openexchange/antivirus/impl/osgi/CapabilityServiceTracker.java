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

package com.openexchange.antivirus.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.antivirus.impl.AntiVirusCapabilityChecker;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CapabilityServiceTracker} - A multi-service-tracker that probes for the
 * availability of {@link AntiVirusService} and {@link CapabilityService} and
 * declares/undeclares the {@link AntiVirusCapabilityChecker#CAPABILITY}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class CapabilityServiceTracker implements ServiceTrackerCustomizer<Object, Object> {

    private final Class<?>[] neededServices;
    private final BundleContext context;
    private boolean antiVirusServicePresent;
    private boolean capabilityDeclared;
    private CapabilityService capabilityService;
    private ServiceRegistration<CapabilityChecker> checkerRegistration;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link CapabilityServiceTracker}.
     */
    public CapabilityServiceTracker(BundleContext context, ServiceLookup services) {
        super();
        this.context = context;
        this.services = services;
        this.neededServices = new Class<?>[] { AntiVirusService.class, CapabilityService.class };
    }

    /**
     * Creates a service {@link Filter} for this tracker's {@link #neededServices}
     * 
     * @return The {@link Filter}
     * @throws InvalidSyntaxException if the filter's syntax is invalid
     */
    public Filter getServiceFilter() throws InvalidSyntaxException {
        return Tools.generateServiceFilter(context, neededServices);
    }

    @Override
    public Object addingService(ServiceReference<Object> reference) {
        Object service = context.getService(reference);
        if (service instanceof AntiVirusService) {
            antiVirusServicePresent = true;
            declareCapability();
        } else if (service instanceof CapabilityService) {
            capabilityService = (CapabilityService) service;
            declareCapability();
        } else {
            context.ungetService(reference);
            return null;
        }
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // nothing

    }

    @Override
    public void removedService(ServiceReference<Object> reference, Object service) {
        if (service instanceof AntiVirusService) {
            undeclareCapability();
            antiVirusServicePresent = false;
        } else if (service instanceof CapabilityService) {
            undeclareCapability();
        }
        context.ungetService(reference);
    }

    ///////////////////////// HELPERS ///////////////////////////////

    /**
     * Declares the capability
     */
    private void declareCapability() {
        if (!antiVirusServicePresent || capabilityService == null || capabilityDeclared == true) {
            return;
        }
        Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, AntiVirusCapabilityChecker.CAPABILITY);

        checkerRegistration = context.registerService(CapabilityChecker.class, new AntiVirusCapabilityChecker(services), properties);
        capabilityService.declareCapability(AntiVirusCapabilityChecker.CAPABILITY);
        capabilityDeclared = true;
    }

    /**
     * Un-declares the capability
     */
    private void undeclareCapability() {
        if (!capabilityDeclared || capabilityService == null) {
            return;
        }
        ServiceRegistration<CapabilityChecker> checkerRegistration = this.checkerRegistration;
        if (null != checkerRegistration) {
            this.checkerRegistration = null;
            checkerRegistration.unregister();
        }

        capabilityService.undeclareCapability(AntiVirusCapabilityChecker.CAPABILITY);
        capabilityDeclared = false;
    }
}
