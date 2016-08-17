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

package com.openexchange.context.osgi;

import java.util.Arrays;
import java.util.Collection;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.context.mbean.ContextMBean;
import com.openexchange.context.mbean.ContextMBeanImpl;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.contexts.impl.sql.ContextAttributeCreateTable;
import com.openexchange.groupware.contexts.impl.sql.ContextAttributeTableUpdateTask;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.management.ManagementService;
import com.openexchange.management.Managements;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ContextActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContextActivator extends HousekeepingActivator {

    public ContextActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{DatabaseService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(ContextActivator.class);

        final DatabaseService dbase = getService(DatabaseService.class);

        final ContextAttributeCreateTable createTable = new ContextAttributeCreateTable();
        registerService(CreateTableService.class, createTable);

        final ContextAttributeTableUpdateTask updateTask = new ContextAttributeTableUpdateTask(dbase);

        registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {

            @Override
            public Collection<? extends UpdateTaskV2> getUpdateTasks() {
                return Arrays.asList(updateTask);
            }

        });

        final BundleContext context = this.context;
        track(ManagementService.class, new ServiceTrackerCustomizer<ManagementService, ManagementService>() {

            @Override
            public ManagementService addingService(ServiceReference<ManagementService> reference) {
                final ManagementService service = context.getService(reference);
                try {
                    final ObjectName objectName = Managements.getObjectName(ContextMBean.class.getName(), ContextMBean.DOMAIN);
                    service.registerMBean(objectName, new ContextMBeanImpl());
                    return service;
                } catch (final Exception e) {
                    logger.warn("Could not register MBean '{}'", ContextMBean.class.getName());
                }
                context.ungetService(reference);
                return null;
            }

            @Override
            public void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
                // Ignore
            }

            @Override
            public void removedService(ServiceReference<ManagementService> reference, ManagementService service) {
                if (null != service) {
                    try {
                        service.unregisterMBean(Managements.getObjectName(ContextMBean.class.getName(), ContextMBean.DOMAIN));
                    } catch (final Exception e) {
                        logger.warn("Unregistering MBean '{}' failed.", ContextMBean.class.getName(), e);
                    } finally {
                        context.ungetService(reference);
                    }
                }
            }
        });

        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
    }
}
