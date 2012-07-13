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

package com.openexchange.file.storage.osgi;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link OSGIEventAdminLookup}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public class OSGIEventAdminLookup {

    final AtomicReference<EventAdmin> eventAdminRef;

    /**
     * The tracker instance.
     */
    private ServiceTracker<EventAdmin,EventAdmin> tracker;

    /**
     * Initializes a new {@link OSGIEventAdminLookup}.
     */
    public OSGIEventAdminLookup() {
        super();
        eventAdminRef = new AtomicReference<EventAdmin>();
    }

    /**
     * Starts the tracker.
     *
     * @param context The bundle context
     */
    public void start(final BundleContext context) {
        if (null == tracker) {
            tracker = new ServiceTracker<EventAdmin,EventAdmin>(context, EventAdmin.class, new Customizer(context));
            tracker.open();
        }
    }

    /**
     * Stops the tracker.
     */
    public void stop() {
        if (null != tracker) {
            tracker.close();
            tracker = null;
        }
    }

    /**
     * Gets the tracked {@link EventAdmin} service
     *
     * @return The tracked {@link EventAdmin} service or <code>null</code>
     */
    public EventAdmin getEventAdmin() {
        return eventAdminRef.get();
    }

    private final class Customizer implements ServiceTrackerCustomizer<EventAdmin,EventAdmin> {

        private final BundleContext context;

        Customizer(final BundleContext context) {
            super();
            this.context = context;
        }

        @Override
        public EventAdmin addingService(final ServiceReference<EventAdmin> reference) {
            final EventAdmin service = context.getService(reference);
            if ((service != null)) {
                eventAdminRef.set(service);
                return service;
            }
            /*
             * Adding to registry failed
             */
            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(final ServiceReference<EventAdmin> reference, final EventAdmin service) {
            // Nothing to do
        }

        @Override
        public void removedService(final ServiceReference<EventAdmin> reference, final EventAdmin service) {
            if (null != service) {
                try {
                    eventAdminRef.set(null);
                } finally {
                    context.ungetService(reference);
                }
            }
        }
    } // End of Customizer class

}
