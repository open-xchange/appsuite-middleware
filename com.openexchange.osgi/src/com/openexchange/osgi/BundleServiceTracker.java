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
import com.openexchange.server.ServiceHolder;

/**
 * {@link BundleServiceTracker} - Tracks a bundle service and fills or empties corresponding {@link ServiceHolder} instance
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class BundleServiceTracker<S> implements ServiceTrackerCustomizer<S, S> {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(BundleServiceTracker.class);

    protected final BundleContext context;

    protected final ServiceHolder<S> serviceHolder;

    protected final Class<S> serviceClass;

    /**
     * Initializes a new bundle service tracker
     *
     * @param context The bundle context
     * @param serviceClass The service's class (used for dynamic type comparison and casts)
     */
    public BundleServiceTracker(final BundleContext context, final Class<S> serviceClass) {
        this(context, null, serviceClass);
    }

    /**
     * Initializes a new bundle service tracker
     *
     * @param context The bundle context
     * @param serviceHolder The service holder
     * @param serviceClass The service's class (used for dynamic type comparison and casts)
     */
    public BundleServiceTracker(final BundleContext context, final ServiceHolder<S> serviceHolder, final Class<S> serviceClass) {
        super();
        this.context = context;
        this.serviceClass = serviceClass;
        this.serviceHolder = serviceHolder;
    }

    @Override
    public final S addingService(final ServiceReference<S> reference) {
        final S addedService = context.getService(reference);
        if (null == addedService) {
            LOG.warn("added service is null! {}", serviceClass.getName(), new Throwable());
        }
        if (serviceClass.isInstance(addedService)) {
            try {
                final S service = serviceClass.cast(addedService);
                if (serviceHolder != null) {
                    serviceHolder.setService(service);
                }
                addingServiceInternal(service);
            } catch (final Exception e) {
                LOG.error("", e);
            }
        }
        return addedService;
    }

    /**
     * Invoked when service is added
     *
     * @param service The service
     */
    protected void addingServiceInternal(final S service) {
        LOG.trace("BundleServiceTracker.addingServiceInternal(): {}", service);
    }

    @Override
    public final void modifiedService(final ServiceReference<S> reference, final S service) {
        // Nope
    }

    @Override
    public final void removedService(final ServiceReference<S> reference, final S service) {
        try {
            if (serviceClass.isInstance(service)) {
                try {
                    if (serviceHolder != null) {
                        serviceHolder.removeService();
                    }
                    removedServiceInternal(serviceClass.cast(service));
                } catch (final Exception e) {
                    LOG.error("", e);
                }
            }
        } finally {
            /*
             * Release service
             */
            context.ungetService(reference);
        }
    }

    /**
     * Invoked when service is added
     *
     * @param service The service
     */
    protected void removedServiceInternal(final S service) {
        LOG.trace("BundleServiceTracker.removedServiceInternal()");
    }

}
