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

package com.openexchange.admin.autocontextid.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.admin.autocontextid.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.autocontextid.storage.sqlStorage.OXAutoCIDSQLStorage;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.groupware.impl.IDGenerator;

/**
 * @author choeger
 */
public final class OXAutoCIDMySQLStorage extends OXAutoCIDSQLStorage {

    private static AdminCache cache = null;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXAutoCIDMySQLStorage.class);

    static {
        cache = ClientAdminThreadExtended.autocontextidCache;
    }

    public OXAutoCIDMySQLStorage() {
        super();
    }

    @Override
    public int generateContextId() throws StorageException {
        final Connection con;
        try {
            con = cache.getWriteConnectionForConfigDB();
        } catch (PoolException e) {
            log.error("", e);
            throw new StorageException(e.getMessage());
        }
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            int id = IDGenerator.getId(con, -2);
            con.commit();
            rollback = false;
            return id;
        } catch (SQLException e) {
            throw new StorageException(e.getMessage());
        } finally {
            if (rollback) {
                doRollback(con);
            }
            cache.closeWriteConfigDBSqlStuff(con, null);
        }
    }

    private static void doRollback(final Connection con) {
        if (null != con) {
            try {
                con.rollback();
            } catch (SQLException e2) {
                log.error("Error doing rollback", e2);
            }
        }
    }

}
