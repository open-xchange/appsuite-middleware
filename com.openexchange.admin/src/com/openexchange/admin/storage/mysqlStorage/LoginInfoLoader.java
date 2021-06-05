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
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 * {@link LoginInfoLoader}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LoginInfoLoader implements Filter<Context, Context> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginInfoLoader.class);

    private final AdminCache cache;

    public LoginInfoLoader(final AdminCache cache) {
        super();
        this.cache = cache;
    }

    @Override
    public Context[] filter(final Collection<Context> input) throws PipesAndFiltersException {
        final Map<Integer, Context> contexts = new HashMap<Integer, Context>(input.size());
        for (final Context context : input) {
            contexts.put(context.getId(), context);
        }
        try {
            loadLoginInfo(contexts);
        } catch (StorageException e) {
            throw new PipesAndFiltersException(e);
        }
        return contexts.values().toArray(new Context[contexts.size()]);
    }

    private static final String SQL = "SELECT cid,login_info FROM login2context WHERE cid IN (";

    private void loadLoginInfo(final Map<Integer, Context> contexts) throws StorageException {
        final Connection con;
        try {
            con = cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(getIN(SQL, contexts.size()));
            int pos = 1;
            for (final Integer cid : contexts.keySet()) {
                stmt.setInt(pos++, cid.intValue());
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                final int cid = rs.getInt(1);
                final String loginMapping = rs.getString(2);
                // Do not return the context identifier as a mapping. This can cause errors if changing login mappings afterwards! See
                // bug 11094 for details!
                final Context context = contexts.get(I(cid));
                if (!context.getIdAsString().equals(loginMapping)) {
                    context.addLoginMapping(loginMapping);
                }
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
    }
}
