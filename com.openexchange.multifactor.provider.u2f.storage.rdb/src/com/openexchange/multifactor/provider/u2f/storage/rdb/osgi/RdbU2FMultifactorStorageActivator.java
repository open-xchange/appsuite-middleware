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

package com.openexchange.multifactor.provider.u2f.storage.rdb.osgi;

import org.slf4j.Logger;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.multifactor.provider.u2f.storage.U2FMultifactorDeviceStorage;
import com.openexchange.multifactor.provider.u2f.storage.rdb.impl.CreateMultifactorU2FTable;
import com.openexchange.multifactor.provider.u2f.storage.rdb.impl.CreateMultifactorU2FTableTask;
import com.openexchange.multifactor.provider.u2f.storage.rdb.impl.RdbU2FMultifactorDeviceStorage;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link RdbU2FMultifactorStorageActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class RdbU2FMultifactorStorageActivator extends HousekeepingActivator {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RdbU2FMultifactorStorageActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ DatabaseService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        logger.info("Starting bundle {}", context.getBundle().getSymbolicName());
        registerService(CreateTableService.class, new CreateMultifactorU2FTable());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CreateMultifactorU2FTableTask()));
        registerService(U2FMultifactorDeviceStorage.class, new RdbU2FMultifactorDeviceStorage(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        logger.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
