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

package com.openexchange.groupware.update.osgi;

import java.util.Stack;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.internal.CreateUpdateTaskTable;

/**
 * This {@link Activator} currently is only used to initialize some structures within the database update component. Lateron this may used
 * to start up the bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Activator implements BundleActivator {

    // private static final String APPLICATION_ID = "com.openexchange.groupware.update";

    private final Stack<ServiceTracker<?, ?>> trackers = new Stack<ServiceTracker<?, ?>>();

    private ServiceRegistration<CreateTableService> createTableRegistration;

    public Activator() {
        super();
    }

    @Override
    public void start(final BundleContext context) {
        createTableRegistration = context.registerService(CreateTableService.class, new CreateUpdateTaskTable(), null);
        trackers.push(new ServiceTracker<ConfigurationService, ConfigurationService>(
            context,
            ConfigurationService.class,
            new ConfigurationCustomizer(context)));
        trackers.push(new ServiceTracker<UpdateTaskProviderService, UpdateTaskProviderService>(
            context,
            UpdateTaskProviderService.class,
            new UpdateTaskCustomizer(context)));
        trackers.push(new ServiceTracker<CacheService, CacheService>(context, CacheService.class.getName(), new CacheCustomizer(context)));
        for (final ServiceTracker<?, ?> tracker : trackers) {
            tracker.open();
        }
    }

    @Override
    public void stop(final BundleContext context) {
        while (!trackers.isEmpty()) {
            trackers.pop().close();
        }
        createTableRegistration.unregister();
    }
}
