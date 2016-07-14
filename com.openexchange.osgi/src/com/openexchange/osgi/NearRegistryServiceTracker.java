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

package com.openexchange.osgi;

import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.java.ConcurrentList;

/**
 * {@link NearRegistryServiceTracker} - A near-registry service tracker.
 * <p>
 * Occurrences of specified service type are collected and available via {@link #getServiceList()}.<br>
 * This is intended to replace {@link #getServices()} since it requires to obtain tracker's mutex on each invocation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NearRegistryServiceTracker<S> extends ServiceTracker<S, S> implements ServiceListing<S> {

    private final List<S> services;

    /**
     * Initializes a new {@link NearRegistryServiceTracker}.
     *
     * @param context The bundle context
     * @param clazz The service class
     */
    public NearRegistryServiceTracker(final BundleContext context, final Class<S> clazz) {
        super(context, clazz, null);
        services = new ConcurrentList<S>();
    }

    @Override
    public List<S> getServiceList() {
        return services;
    }

    @Override
    public S addingService(final ServiceReference<S> reference) {
        S service = context.getService(reference);

        S serviceToAdd = onServiceAvailable(service);
        if (services.add(serviceToAdd)) {
            return service;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(final ServiceReference<S> reference, final S service) {
        services.remove(service);
        context.ungetService(reference);
    }

    /**
     * Invoked when a tracked service is available.
     *
     * @param service The available service
     * @return The service to add
     */
    protected S onServiceAvailable(S service) {
        return service;
    }

}
