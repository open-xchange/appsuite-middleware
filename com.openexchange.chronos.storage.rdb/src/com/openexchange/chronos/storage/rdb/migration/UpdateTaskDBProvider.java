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

package com.openexchange.chronos.storage.rdb.migration;

import java.sql.Connection;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link UpdateTaskDBProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdateTaskDBProvider implements DBProvider {

    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link UpdateTaskDBProvider}.
     *
     * @param databaseService A reference to the database service
     */
    public UpdateTaskDBProvider(DatabaseService databaseService) {
        super();
        this.databaseService = databaseService;
    }

    @Override
    public Connection getReadConnection(Context ctx) throws OXException {
        return getWriteConnection(ctx);
    }

    @Override
    public void releaseReadConnection(Context ctx, Connection con) {
        releaseWriteConnectionAfterReading(ctx, con);
    }

    @Override
    public Connection getWriteConnection(Context ctx) throws OXException {
        return databaseService.getForUpdateTask(ctx.getContextId());
    }

    @Override
    public void releaseWriteConnection(Context ctx, Connection con) {
        databaseService.backForUpdateTask(ctx.getContextId(), con);
    }

    @Override
    public void releaseWriteConnectionAfterReading(Context ctx, Connection con) {
        databaseService.backForUpdateTaskAfterReading(ctx.getContextId(), con);
    }

}
