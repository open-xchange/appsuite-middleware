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

package com.openexchange.admin.plugin.hosting.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.database.Databases;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * Executes some SQL statements searching for context identifier with a separate thread.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContextSearcher extends AbstractTask<Collection<Integer>> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContextSearcher.class);

    private final AdminCacheExtended cache;
    private final String sql;
    private final String pattern;

    /**
     * Initializes a new {@link ContextSearcher}.
     *
     * @param cache The cache reference used to acquire/release a connection
     * @param sql The SQL statement to execute
     * @param pattern The search pattern to use
     */
    public ContextSearcher(AdminCacheExtended cache, String sql, String pattern) {
        super();
        this.cache = cache;
        this.sql = sql;
        this.pattern = pattern;
    }

    @Override
    public void setThreadName(ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix("listContext searcher");
    }

    @Override
    public Collection<Integer> call() throws StorageException {
        Connection con = acquireConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(sql);
            if (null != pattern) {
                stmt.setString(1, pattern);
            }
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Integer> cids = new ArrayList<>();
            do {
                cids.add(I(rs.getInt(1)));
            } while (rs.next());
            return cids;
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            releaseConnection(con);
        }
    }

    private void releaseConnection(Connection con) {
        try {
            cache.pushReadConnectionForConfigDB(con);
        } catch (Exception x) {
            LOG.error("Failed to push ConfigDB connection back to pool.", x);
        }
    }

    private Connection acquireConnection() throws StorageException {
        try {
            return cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e);
        }
    }

}
