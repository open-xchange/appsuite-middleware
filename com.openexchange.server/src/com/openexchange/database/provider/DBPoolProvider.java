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

package com.openexchange.database.provider;

import static com.openexchange.database.Databases.autocommit;
import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;

public class DBPoolProvider implements DBProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBPoolProvider.class);

    @Override
    public Connection getReadConnection(final Context ctx) throws OXException {
        try {
            return DBPool.pickup(ctx);
        } catch (OXException e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void releaseReadConnection(final Context ctx, final Connection con) {
        if (con != null) {
            DBPool.closeReaderSilent(ctx, con); //FIXME
        }
    }

    @Override
    public Connection getWriteConnection(final Context ctx) throws OXException {
        return DBPool.pickupWriteable(ctx);
    }

    @Override
    public void releaseWriteConnection(final Context ctx, final Connection con) {
        if (con == null) {
            return;
        }
        autocommit(con);
        DBPool.closeWriterSilent(ctx, con);
    }

    @Override
    public void releaseWriteConnectionAfterReading(final Context ctx, final Connection con) {
        if (con == null) {
            return;
        }
        autocommit(con);
        DBPool.closeWriterAfterReading(ctx, con);
    }
}
