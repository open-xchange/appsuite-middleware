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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.config.ConfigurationService;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 * Loads the data of multiple context in a fast multithreaded manner.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContextLoader implements Filter<Integer, Context> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContextLoader.class);

    private final AdminCache cache;

    private final boolean failOnMissing;

    public ContextLoader(final AdminCache cache, final boolean failOnMissing) {
        super();
        this.cache = cache;
        this.failOnMissing = failOnMissing;
    }

    @Override
    public Context[] filter(final Collection<Integer> input) throws PipesAndFiltersException {
        List<Context> contexts;
        try {
            contexts = loadContexts(input);
        } catch (StorageException e) {
            throw new PipesAndFiltersException(e);
        }
        return contexts.toArray(new Context[contexts.size()]);
    }

    private static final String SQL = "SELECT context.cid,context.name,context.enabled,context.reason_id,context.filestore_id,context.filestore_name,context.quota_max,context_server2db_pool.write_db_pool_id,context_server2db_pool.read_db_pool_id,context_server2db_pool.db_schema FROM context JOIN context_server2db_pool ON context.cid=context_server2db_pool.cid JOIN server ON context_server2db_pool.server_id=server.server_id WHERE server.name=? AND context.cid IN (";

    private List<Context> loadContexts(final Collection<Integer> cids) throws StorageException {
        final Connection con;
        try {
            con = cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e);
        }
        final List<Context> retval = new ArrayList<Context>(cids.size());
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(getIN(SQL, cids.size()) + " ORDER BY context_server2db_pool.read_db_pool_id,context_server2db_pool.db_schema");
            int pos = 1;
            final String serverName = AdminServiceRegistry.getInstance().getService(ConfigurationService.class).getProperty(AdminProperties.Prop.SERVER_NAME, "local");
            stmt.setString(pos++, serverName);
            for (final Integer cid : cids) {
                stmt.setInt(pos++, cid.intValue());
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                final Context cs = new Context();
                cs.setId(I(rs.getInt(1)));
                cs.setName(rs.getString(2));
                cs.setEnabled(B(rs.getBoolean(3)));
                final int reasonId = rs.getInt(4);
                if (-1 != reasonId) {
                    cs.setMaintenanceReason(new MaintenanceReason(I(reasonId)));
                }
                cs.setFilestoreId(I(rs.getInt(5)));
                cs.setFilestore_name(rs.getString(6));
                long quotaMax = rs.getLong(7);
                if (quotaMax != -1) {
                    // value is in MB
                    quotaMax = quotaMax >> 20;
                    cs.setMaxQuota(L(quotaMax));
                }
                final String dbSchema = rs.getString(10);
                cs.setReadDatabase(new Database(rs.getInt(9), dbSchema));
                cs.setWriteDatabase(new Database(rs.getInt(8), dbSchema));
                retval.add(cs);
            }
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                cache.pushReadConnectionForConfigDB(con);
            } catch (PoolException e) {
                LOG.error("", e);
            }
        }
        if (cids.size() != retval.size()) {
            final List<Integer> missing = new ArrayList<Integer>(cids);
            for (final Context context : retval) {
                missing.remove(context.getId());
            }
            if (failOnMissing) {
                throw new StorageException("Can not load the following contexts: " + missing.toString());
            }
            LOG.warn("Can not load the following contexts: {}", missing);
        }
        return retval;
    }
}
