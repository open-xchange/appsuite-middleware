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

package com.openexchange.admin.storage.sqlStorage;

import java.sql.Connection;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.SchemaInfo;
import com.openexchange.exception.OXException;

public class OXAdminPoolDBPoolExtension extends OXAdminPoolDBPool implements OXAdminPoolInterfaceExtension {

    public OXAdminPoolDBPoolExtension() {
        super();
    }

    @Override
    public int getDBPoolIdForContextId(final int contextId) throws PoolException {
        return new DatabaseServiceCallable<Integer>() {

            @Override
            protected Integer doPerform(DatabaseService databaseService) throws OXException {
                return Integer.valueOf(databaseService.getWritablePool(contextId));
            }
        }.perform(getService()).intValue();
    }

    @Override
    public Connection getWRITEConnectionForPoolId(final int poolId, final String schema) throws PoolException {
        return new DatabaseServiceCallable<Connection>() {

            @Override
            protected Connection doPerform(DatabaseService databaseService) throws OXException {
                return databaseService.get(poolId, schema);
            }
        }.perform(getService());
    }

    @Override
    public void pushWRITEConnectionForPoolId(final int poolId, final Connection con) throws PoolException {
        new DatabaseServiceCallable<Void>() {

            @Override
            protected Void doPerform(DatabaseService databaseService) throws OXException {
                databaseService.back(poolId, con);
                return null;
            }
        }.perform(getService());
    }

    @Override
    public Connection getWRITENoTimeoutConnectionForPoolId(final int poolId, final String schema) throws PoolException {
        return new DatabaseServiceCallable<Connection>() {

            @Override
            protected Connection doPerform(DatabaseService databaseService) throws OXException {
                return databaseService.getNoTimeout(poolId, schema);
            }
        }.perform(getService());
    }

    @Override
    public void pushWRITENoTimeoutConnectionForPoolId(final int poolId, final Connection con) throws PoolException {
        new DatabaseServiceCallable<Void>() {

            @Override
            protected Void doPerform(DatabaseService databaseService) throws OXException {
                databaseService.backNoTimeoout(poolId, con);
                return null;
            }
        }.perform(getService());
    }

    @Override
    public void resetPoolMappingForContext(final int contextId) throws PoolException {
        new DatabaseServiceCallable<Void>() {

            @Override
            protected Void doPerform(DatabaseService databaseService) throws OXException {
                databaseService.invalidate(contextId);
                return null;
            }
        }.perform(getService());
    }

    @Override
    public String getSchemeForContextId(final int contextId) throws PoolException {
        return new DatabaseServiceCallable<String>() {

            @Override
            protected String doPerform(DatabaseService databaseService) throws OXException {
                return databaseService.getSchemaName(contextId);
            }
        }.perform(getService());
    }

    @Override
    public SchemaInfo getSchemaInfoForContextId(final int context_id) throws PoolException {
        return new DatabaseServiceCallable<SchemaInfo>() {

            @Override
            protected SchemaInfo doPerform(DatabaseService databaseService) throws OXException {
                return databaseService.getSchemaInfo(context_id);
            }
        }.perform(getService());
    }
}
