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

package com.openexchange.oauth.impl.osgi;

import java.util.Arrays;
import java.util.Collection;
import com.openexchange.database.CreateTableService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.oauth.impl.internal.groupware.CreateOAuthAccountTable;
import com.openexchange.oauth.impl.internal.groupware.DropForeignKeyFromOAuthAccountTask;
import com.openexchange.oauth.impl.internal.groupware.OAuthAccountsTableUtf8Mb4UpdateTask;
import com.openexchange.oauth.impl.internal.groupware.OAuthAccountsTableUtf8Mb4UpdateTaskV2;
import com.openexchange.oauth.impl.internal.groupware.OAuthAddExpiryDateColumnTask;
import com.openexchange.oauth.impl.internal.groupware.OAuthAddIdentityColumnTaskV2;
import com.openexchange.oauth.impl.internal.groupware.OAuthAddScopeColumnTask;
import com.openexchange.oauth.impl.internal.groupware.OAuthCreateTableTask;
import com.openexchange.oauth.impl.internal.groupware.OAuthCreateTableTask2;
import com.openexchange.oauth.impl.internal.groupware.OAuthDeleteListener;
import com.openexchange.oauth.impl.internal.groupware.RemoveLinkedInScopeUpdateTask;
import com.openexchange.oauth.impl.internal.groupware.RenameMigrateLinkedInServiceIdUpdateTask;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * Registers the services necessary for the administration daemon.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LowLevelServicesActivator extends HousekeepingActivator {

    public LowLevelServicesActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        // Nothing to do
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(CreateTableService.class, new CreateOAuthAccountTable(), null);
        registerService(DeleteListener.class, new OAuthDeleteListener(), null);
        registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {

            @Override
            public Collection<UpdateTaskV2> getUpdateTasks() {
                //@formatter:off
                return Arrays.asList(((UpdateTaskV2) new OAuthCreateTableTask()), new OAuthCreateTableTask2(),
                    new OAuthAddScopeColumnTask(), new RenameMigrateLinkedInServiceIdUpdateTask(),
                    new DropForeignKeyFromOAuthAccountTask(), new OAuthAddIdentityColumnTaskV2(),
                    new OAuthAccountsTableUtf8Mb4UpdateTask(), new OAuthAccountsTableUtf8Mb4UpdateTaskV2(),
                    new RemoveLinkedInScopeUpdateTask(), new OAuthAddExpiryDateColumnTask());
                //@formatter:on
            }
        });
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterServices();
        super.stopBundle();
    }
}
