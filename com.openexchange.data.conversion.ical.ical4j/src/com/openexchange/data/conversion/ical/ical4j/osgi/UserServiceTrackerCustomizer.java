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
