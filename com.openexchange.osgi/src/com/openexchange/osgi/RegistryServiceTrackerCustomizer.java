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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link RegistryServiceTrackerCustomizer} can be used to remember discovered services in an {@link AbstractServiceRegistry}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class RegistryServiceTrackerCustomizer<T> implements ServiceTrackerCustomizer<T, T> {

    /** The bundle context */
    protected final BundleContext context;

    /** The service registry to add the tracked service to */
    protected final AbstractServiceRegistry registry;

    /** The class of the service to track */
    protected final Class<T> serviceClass;

    /**
     * Initializes a new {@link RegistryServiceTrackerCustomizer}.
     *
     * @param context The bundle context
     * @param registry The registry
     * @param clazz The service class to track
     */
    public RegistryServiceTrackerCustomizer(final BundleContext context, final AbstractServiceRegistry registry, final Class<T> clazz) {
        super();
        this.context = context;
        this.registry = registry;
        this.serviceClass = clazz;
    }

    @Override
    public T addingService(final ServiceReference<T> reference) {
        final T tmp = context.getService(reference);
        if (serviceClass.isInstance(tmp)) {
            registry.addService(serviceClass, tmp);
            serviceAcquired(tmp);
            return tmp;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<T> reference, final T service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<T> reference, final T service) {
        if (null != service) {
            final T removedService = registry.removeService(serviceClass);
            if (null != removedService) {
                context.ungetService(reference);
                serviceReleased(removedService);
            }
        }
    }

    /**
     * A hook for additional actions for newly tracked service instance.
     * <p>
     * Sub-classes may cast service using {@link #serviceClass} member.
     *
     * @param service The newly tracked service
     */
    protected void serviceAcquired(final T service) {
        // Nothing to do in basic implementation
    }

    /**
     * A hook for additional actions for a removed tracked service instance.
     * <p>
     * Sub-classes may cast service using {@link #serviceClass} member.
     *
     * @param service The removed tracked service
     */
    protected void serviceReleased(final T service) {
        // Nothing to do in basic implementation
    }

}
