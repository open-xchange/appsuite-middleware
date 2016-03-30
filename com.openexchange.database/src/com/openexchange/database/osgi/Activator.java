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

package com.openexchange.database.osgi;

import java.util.Stack;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.internal.CreateReplicationTable;
import com.openexchange.database.internal.reloadable.GenericReloadable;
import com.openexchange.database.internal.reloadable.GlobalDbConfigsReloadable;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.management.ManagementService;
import com.openexchange.timer.TimerService;

/**
 * Activator for the database bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Activator implements BundleActivator {

    private final Stack<ServiceTracker<?, ?>> trackers = new Stack<ServiceTracker<?, ?>>();
    private volatile ServiceRegistration<CreateTableService> createTableRegistration;
    private volatile ServiceRegistration<Reloadable> reloadableRegistration;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        createTableRegistration = context.registerService(CreateTableService.class, new CreateReplicationTable(), null);
        final Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + ConfigurationService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + ConfigViewFactory.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + DBMigrationExecutorService.class.getName() + "))");
        trackers.push(new ServiceTracker<Object, Object>(context, filter, new DatabaseServiceRegisterer(context)));
        trackers.push(new ServiceTracker<ManagementService, ManagementService>(context, ManagementService.class, new ManagementServiceCustomizer(context)));
        trackers.push(new ServiceTracker<TimerService, TimerService>(context, TimerService.class, new TimerServiceCustomizer(context)));
        trackers.push(new ServiceTracker<CacheService, CacheService>(context, CacheService.class, new CacheServiceCustomizer(context)));
        for (final ServiceTracker<?, ?> tracker : trackers) {
            tracker.open();
        }
        reloadableRegistration = context.registerService(Reloadable.class, GenericReloadable.getInstance(), null);
        context.registerService(Reloadable.class, new GlobalDbConfigsReloadable(), null);
    }

    @Override
    public void stop(final BundleContext context) {
        while (!trackers.isEmpty()) {
            trackers.pop().close();
        }
        final ServiceRegistration<CreateTableService> createTableRegistration = this.createTableRegistration;
        if (null != createTableRegistration) {
            createTableRegistration.unregister();
            this.createTableRegistration = null;
        }
        final ServiceRegistration<Reloadable> reloadableRegistration = this.reloadableRegistration;
        if (null != reloadableRegistration) {
            reloadableRegistration.unregister();
            this.reloadableRegistration = null;
        }
    }
}
