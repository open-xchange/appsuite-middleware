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

package com.openexchange.admin.reseller.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.database.Databases;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 */
public class ResellerContextFilter implements Filter<Integer, Integer> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResellerContextFilter.class);

    private final AdminCache cache;
    private final ResellerAdmin admin;

    public ResellerContextFilter(AdminCache cache, ResellerAdmin admin) {
        super();
        this.cache = cache;
        this.admin = admin;
    }

    @Override
    public Integer[] filter(Collection<Integer> input) throws PipesAndFiltersException {
        List<Integer> cids;
        if ( null == admin ) {
            return input.toArray(new Integer[input.size()]);
        }
        try {
            cids = filterContexts(input);
        } catch (StorageException e) {
            throw new PipesAndFiltersException(e);
        }
        return cids.toArray(new Integer[cids.size()]);
    }

    private List<Integer> filterContexts(Collection<Integer> cids) throws StorageException {
        Connection con = null;
        try {
            con = cache.getReadConnectionForConfigDB();

            String sqlStmt = buildSqlInStatement(con);

            List<Integer> retval = new ArrayList<>(cids.size());
            for (Collection<Integer> partition : partition(cids, Databases.IN_LIMIT)) {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement(Databases.getIN(sqlStmt, partition.size()));
                    int pos = 1;
                    for (Integer cid : partition) {
                        stmt.setInt(pos++, cid.intValue());
                    }
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        retval.add(Integer.valueOf(rs.getInt(1)));
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
            }
            return retval;
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            if (null != con) {
                try {
                    cache.pushReadConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Error pushing connection to pool!", e);
                }
            }
        }
    }

    private String buildSqlInStatement(Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // determine all subadmin sids having me as a parent
            stmt = con.prepareStatement("SELECT sid FROM subadmin WHERE pid = ?");
            stmt.setInt(1, admin.getId().intValue());
            rs = stmt.executeQuery();

            StringBuilder stmtBuilder = new StringBuilder("SELECT cid FROM context2subadmin WHERE sid IN (").append(admin.getId());
            while (rs.next()) {
                stmtBuilder.append(", ").append(rs.getInt(1));
            }
            stmtBuilder.append(") AND cid IN (");
            return stmtBuilder.toString();
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Generates consecutive subsets of a set, each of the same size (the final list may be smaller).
     *
     * @param original The set to return consecutive subsets of
     * @param partitionSize The desired size for each subset
     * @return A list of consecutive subsets
     * @throws IllegalArgumentException if {@code partitionSize} is not positive
     */
    private static <T> List<Collection<T>> partition(Collection<T> original, int partitionSize) {
        int total = original.size();
        if (partitionSize >= total) {
            return java.util.Collections.singletonList(original);
        }

        // Create a list of collections to return.
        List<Collection<T>> result = new LinkedList<Collection<T>>();

        // Create an iterator for the original collection.
        Iterator<T> it = original.iterator();

        // Create each new collection.
        Collection<T> s = new ArrayList<T>(partitionSize);
        s.add(it.next());
        for (int i = 1; i < total; i++) {
            if ((i % partitionSize) == 0) {
                result.add(s);
                s = new ArrayList<T>(partitionSize);
            }
            s.add(it.next());
        }
        result.add(s);
        return result;
    }

}
