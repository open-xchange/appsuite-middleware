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
 *     Copyright (C) 2018-2020 OX Software GmbH
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
import com.openexchange.antivirus.impl.impl.AntiVirusCapabilityChecker;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // nothing

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
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
