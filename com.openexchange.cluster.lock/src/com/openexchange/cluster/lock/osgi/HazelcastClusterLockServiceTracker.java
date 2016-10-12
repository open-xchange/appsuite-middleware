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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.cluster.lock.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.internal.ClusterLockServiceHazelcastImpl;
import com.openexchange.cluster.lock.internal.Unregisterer;
import com.openexchange.server.ServiceLookup;

/**
 * {@link HazelcastClusterLockServiceTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class HazelcastClusterLockServiceTracker implements ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> {

    private BundleContext bundleContext;
    private ServiceLookup services;
    private Unregisterer unregisterer;
    private ServiceRegistration<ClusterLockService> registration;

    /**
     * Initialises a new {@link HazelcastClusterLockServiceTracker}.
     */
    public HazelcastClusterLockServiceTracker(BundleContext bundleContext, ServiceLookup services, Unregisterer unregisterer) {
        super();
        this.bundleContext = bundleContext;
        this.services = services;
        this.unregisterer = unregisterer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> ref) {
        HazelcastInstance hzInstance = bundleContext.getService(ref);
        registration = bundleContext.registerService(ClusterLockService.class, new ClusterLockServiceHazelcastImpl(services, unregisterer), null);
        return hzInstance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
        // no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @Override
    public void removedService(ServiceReference<HazelcastInstance> ref, HazelcastInstance service) {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        bundleContext.ungetService(ref);
    }
}
