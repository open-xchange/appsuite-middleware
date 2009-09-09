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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.threadpool.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link AddingTrackerCustomizer} - The {@link ServiceTrackerCustomizer customizer} for adding into {@link ThreadPoolServiceRegistry
 * service registry}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AddingTrackerCustomizer<S> implements ServiceTrackerCustomizer {

    private final BundleContext context;

    private final Class<S> serviceClass;

    /**
     * Initializes a new {@link AddingTrackerCustomizer}.
     * 
     * @param context The bundle context
     * @param clazz The service class
     */
    public AddingTrackerCustomizer(final BundleContext context, final Class<S> clazz) {
        super();
        this.context = context;
        this.serviceClass = clazz;
    }

    public Object addingService(final ServiceReference reference) {
        final Object service = context.getService(reference);
        if (!serviceClass.isInstance(service)) {
            context.ungetService(reference);
            return null;
        }
        ThreadPoolServiceRegistry.getServiceRegistry().addService(serviceClass, service);
        return service;
    }

    public void modifiedService(final ServiceReference reference, final Object service) {
        // Nothing to do
    }

    public void removedService(final ServiceReference reference, final Object service) {
        if (null != service && serviceClass.isInstance(service)) {
            try {
                ThreadPoolServiceRegistry.getServiceRegistry().removeService(serviceClass);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
