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

package com.openexchange.context.osgi;

import java.rmi.Remote;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.context.rmi.ContextRMIServiceImpl;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.contexts.impl.sql.ChangePrimaryKeyForContextAttribute;
import com.openexchange.groupware.contexts.impl.sql.ContextAttributeCreateTable;
import com.openexchange.groupware.contexts.impl.sql.ContextAttributeTableUpdateTask;
import com.openexchange.groupware.update.UpdateTaskProviderService;
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
        return new Class[] { DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        DatabaseService dbase = getService(DatabaseService.class);

        ContextAttributeCreateTable createTable = new ContextAttributeCreateTable();
        registerService(CreateTableService.class, createTable);

        ContextAttributeTableUpdateTask updateTask = new ContextAttributeTableUpdateTask(dbase);

        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put("RMI_NAME", ContextRMIServiceImpl.RMI_NAME);
        registerService(Remote.class, new ContextRMIServiceImpl(), serviceProperties);
        ChangePrimaryKeyForContextAttribute changePrimaryKeyForContextAttribute = new ChangePrimaryKeyForContextAttribute();

        registerService(UpdateTaskProviderService.class, () -> Arrays.asList(updateTask, changePrimaryKeyForContextAttribute));

        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
    }
}
