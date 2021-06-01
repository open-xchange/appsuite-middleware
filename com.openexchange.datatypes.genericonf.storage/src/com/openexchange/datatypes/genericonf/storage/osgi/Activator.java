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

package com.openexchange.datatypes.genericonf.storage.osgi;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.datatypes.genericonf.storage.impl.ClearGenConfTables;
import com.openexchange.datatypes.genericonf.storage.impl.CreateGenConfTables;
import com.openexchange.datatypes.genericonf.storage.impl.GenConfConvertUtf8ToUtf8mb4UpdateTask;
import com.openexchange.datatypes.genericonf.storage.impl.MySQLGenericConfigurationStorage;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {



    public static final AtomicReference<GenericConfigurationStorageService> REF =
        new AtomicReference<GenericConfigurationStorageService>();

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DBProvider.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final MySQLGenericConfigurationStorage mySQLGenericConfigurationStorage = new MySQLGenericConfigurationStorage();
        mySQLGenericConfigurationStorage.setDBProvider(getService(DBProvider.class));
        registerService(DeleteListener.class, new ClearGenConfTables(), null);
        registerService(CreateTableService.class, new CreateGenConfTables(), null);
        registerService(GenericConfigurationStorageService.class, mySQLGenericConfigurationStorage, null);
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new GenConfConvertUtf8ToUtf8mb4UpdateTask()));
        REF.set(mySQLGenericConfigurationStorage);
    }

    @Override
    public void stopBundle() throws Exception {
        REF.set(null);
        super.stopBundle();
    }

}
