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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.AssignmentFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.internal.AssignmentFactoryImpl;
import com.openexchange.database.internal.DatabaseServiceImpl;
import com.openexchange.database.internal.Initialization;
import com.openexchange.database.migration.DBMigrationExecutorService;

/**
 * Injects the {@link ConfigurationService} and publishes the DatabaseService.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DatabaseServiceRegisterer implements ServiceTrackerCustomizer<Object, Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabaseServiceRegisterer.class);

    private final BundleContext context;
    private final Lock lock = new ReentrantLock();

    private ConfigurationService configService;
    private ConfigViewFactory configViewFactory;
    private DBMigrationExecutorService migrationService;

    private ServiceRegistration<DatabaseService> serviceRegistration;

    public DatabaseServiceRegisterer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        final boolean needsRegistration;
        lock.lock();
        try {
            if (obj instanceof ConfigurationService) {
                configService = (ConfigurationService) obj;
            }
            if (obj instanceof ConfigViewFactory) {
                configViewFactory = (ConfigViewFactory) obj;
            }
            if (obj instanceof DBMigrationExecutorService) {
                migrationService = (DBMigrationExecutorService) obj;
            }
            needsRegistration = null != configService && null != configViewFactory && null != migrationService;
        } finally {
            lock.unlock();
        }
        if (needsRegistration && !Initialization.getInstance().isStarted()) {
            DatabaseServiceImpl databaseService = null;
            try {
                Initialization.setConfigurationService(configService);
                databaseService = Initialization.getInstance().start(configService, configViewFactory, migrationService);
                LOG.info("Publishing DatabaseService.");
                serviceRegistration = context.registerService(DatabaseService.class, databaseService, null);
            } catch (final Exception e) {
                LOG.error("Publishing the DatabaseService failed.", e);
            }
            try {
                if (databaseService != null) {
                    AssignmentFactoryImpl assignmentFactoryImpl = new AssignmentFactoryImpl(databaseService);
                    assignmentFactoryImpl.reload();
                    LOG.info("Publishing AssignmentFactory.");
                    context.registerService(AssignmentFactory.class, assignmentFactoryImpl, null);
                } else {
                    LOG.error("Publishing AssignmentFactory failed due to missing DatabaseService.");
                }
            } catch (final Exception e) {
                LOG.error("Publishing AssignmentFactory failed. This is normal until a server has been registered.", e);
            }
        }
        return obj;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        if (null != serviceRegistration) {
            LOG.info("Unpublishing DatabaseService.");
            serviceRegistration.unregister();
            serviceRegistration = null;
            Initialization.getInstance().stop();
            Initialization.setConfigurationService(null);
        }
        context.ungetService(reference);
    }
}
