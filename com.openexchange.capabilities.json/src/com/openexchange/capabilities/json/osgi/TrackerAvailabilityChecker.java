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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.capabilities.json.osgi;

import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.capabilities.json.AvailabilityChecker;

/**
 * {@link TrackerAvailabilityChecker} - The {@link AvailabilityChecker} backed by a {@link ServiceTracker}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TrackerAvailabilityChecker<S> extends ServiceTracker<S, S> implements com.openexchange.capabilities.json.AvailabilityChecker {

    /**
     * Gets the checker for specified service.
     *
     * @param clazz The service's class
     * @param defaultAvailability The default availability value
     * @param bundleContext The bundle context
     * @return The checker for given service
     */
    public static <S> AvailabilityChecker getAvailabilityCheckerFor(final Class<S> clazz, final boolean defaultAvailability, final BundleContext bundleContext) {
        if (null == bundleContext) {
            return AvailabilityChecker.TRUE_AVAILABILITY_CHECKER;
        }
        final TrackerAvailabilityChecker<S> checker = new TrackerAvailabilityChecker<S>(clazz, defaultAvailability, bundleContext);
        checker.open();
        return checker;
    }

    private volatile AtomicBoolean available;
    private final boolean defaultAvailability;

    /**
     * Initializes a new {@link TrackerAvailabilityChecker}.
     *
     * @param clazz The service's class to track
     * @param defaultAvailability The default availability value
     * @param bundleContext The bundle context
     */
    private TrackerAvailabilityChecker(final Class<S> clazz, final boolean defaultAvailability, final BundleContext bundleContext) {
        super(bundleContext, clazz, null);
        this.defaultAvailability = defaultAvailability;
    }

    @Override
    public S addingService(final ServiceReference<S> reference) {
        AtomicBoolean available = this.available;
        if (null == available) {
            synchronized (this) {
                available = this.available;
                if (null == available) {
                    available = new AtomicBoolean();
                    this.available = available;
                }
            }
        }
        // CAS
        if (available.compareAndSet(false, true)) {
            return super.addingService(reference);
        }
        return null;
    }

    @Override
    public void removedService(final ServiceReference<S> reference, final S service) {
        if (null != service) {
            final AtomicBoolean available = this.available;
            if (null != available) {
                available.compareAndSet(true, false);
                this.available = null;
            }
            context.ungetService(reference);
        }
    }

    @Override
    public boolean isAvailable() {
        final AtomicBoolean available = this.available;
        return null == available ? defaultAvailability : available.get();
    }

}
