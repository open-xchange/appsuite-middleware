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

package com.openexchange.database.cleanup.impl.osgi;

import java.rmi.Remote;
import org.slf4j.Logger;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.database.cleanup.impl.DatabaseCleanUpServiceImpl;
import com.openexchange.database.cleanup.impl.groupware.DatabaseCleanUpCreateTableService;
import com.openexchange.database.cleanup.impl.groupware.DatabaseCleanUpCreateTableTask;
import com.openexchange.database.cleanup.impl.rmi.DatabaseCleanUpRMIServiceImpl;
import com.openexchange.database.cleanup.impl.storage.RdbDatabaseCleanUpExecutionManagement;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link DatabaseCleanUpActivator} - The activator for clean-up jobs.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class DatabaseCleanUpActivator extends HousekeepingActivator {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabaseCleanUpActivator.class);

    private DatabaseCleanUpServiceImpl serviceImpl;

    /**
     * Initializes a new {@link DatabaseCleanUpActivator}.
     */
    public DatabaseCleanUpActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ThreadPoolService.class, TimerService.class, ContextService.class, UserService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());
        Services.setServiceLookup(this);

        registerService(CreateTableService.class, new DatabaseCleanUpCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DatabaseCleanUpCreateTableTask()));

        DatabaseCleanUpServiceImpl serviceImpl = new DatabaseCleanUpServiceImpl(new RdbDatabaseCleanUpExecutionManagement(this), this);
        this.serviceImpl = serviceImpl;
        registerService(DatabaseCleanUpService.class, serviceImpl);
        registerService(Remote.class, new DatabaseCleanUpRMIServiceImpl(serviceImpl));
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
        DatabaseCleanUpServiceImpl serviceImpl = this.serviceImpl;
        if (serviceImpl != null) {
            this.serviceImpl = null;
            serviceImpl.stop();
        }
        LOG.info("Stopped bundle {}", context.getBundle().getSymbolicName());
    }

}
