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

package com.openexchange.data.conversion.ical.ical4j.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.data.conversion.ical.ical4j.internal.OXUserResolver;
import com.openexchange.user.UserService;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class UserServiceTrackerCustomizer implements ServiceTrackerCustomizer<UserService, UserService> {

    private final BundleContext context;

    private final OXUserResolver userResolver;

    /**
     * Default constructor.
     */
    public UserServiceTrackerCustomizer(final BundleContext context, final OXUserResolver userResolver) {
        super();
        this.context = context;
        this.userResolver = userResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserService addingService(final ServiceReference<UserService> reference) {
        final UserService userService = context.getService(reference);
        userResolver.setUserService(userService);
        return userService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifiedService(final ServiceReference<UserService> reference, final UserService service) {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removedService(final ServiceReference<UserService> reference, final UserService service) {
        userResolver.setUserService(null);
        context.ungetService(reference);
    }
}
