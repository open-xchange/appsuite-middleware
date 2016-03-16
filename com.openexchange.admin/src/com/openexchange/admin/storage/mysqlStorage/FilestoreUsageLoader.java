/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 * {@link FilestoreUsageLoader}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FilestoreUsageLoader implements Filter<Context, Context> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FilestoreUsageLoader.class);

    private final AdminCache cache;
    private final long averageSize;
    private final boolean failOnMissing;

    public FilestoreUsageLoader(AdminCache cache, long averageSize, boolean failOnMissing) {
        super();
        this.cache = cache;
        this.averageSize = averageSize;
        this.failOnMissing = failOnMissing;
    }

    @Override
    public Context[] filter(Collection<Context> contexts) throws PipesAndFiltersException {
        Map<PoolIdTuple, Map<String, Map<Integer, Context>>> poolMap = new HashMap<PoolIdTuple, Map<String, Map<Integer, Context>>>();
        for (Context context : contexts) {
            PoolIdTuple tuple = new PoolIdTuple(context.getWriteDatabase().getId(), context.getReadDatabase().getId());
            Map<String, Map<Integer, Context>> schemaMap = poolMap.get(tuple);
            if (null == schemaMap) {
                schemaMap = new HashMap<String, Map<Integer, Context>>();
                poolMap.put(tuple, schemaMap);
            }
            String schema = context.getReadDatabase().getScheme();
            Map<Integer, Context> cidMap = schemaMap.get(schema);
            if (null == cidMap) {
                cidMap = new HashMap<Integer, Context>();
                schemaMap.put(schema, cidMap);
            }
            cidMap.put(context.getId(), context);
        }
        List<Context> retval = new ArrayList<Context>();
        for (Entry<PoolIdTuple, Map<String, Map<Integer, Context>>> poolEntry : poolMap.entrySet()) {
            for (Entry<String, Map<Integer, Context>> schemaEntry : poolEntry.getValue().entrySet()) {
                try {
                    retval.addAll(loadUsage(poolEntry.getKey().getWritePoolId(), poolEntry.getKey().getReadPoolId(), schemaEntry.getKey(), schemaEntry.getValue()));
                } catch (StorageException e) {
                    throw new PipesAndFiltersException(e);
                }
            }
        }
        return retval.toArray(new Context[retval.size()]);
    }

    private Collection<Context> loadUsage(int writePoolId, int readPoolId, String schema, Map<Integer, Context> contexts) throws StorageException {
        if (OXToolStorageInterface.getInstance().schemaBeingLockedOrNeedsUpdate(writePoolId, schema)) {
            throw new StorageException(new DatabaseUpdateException("Database with pool-id " + writePoolId + " and schema \"" + schema + "\" needs update. Please run \"runupdate\" for that database."));
        }

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getPool().getConnection(readPoolId, schema);
            stmt = con.prepareStatement("SELECT cid,used FROM filestore_usage WHERE user=0");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Context context = contexts.get(I(rs.getInt(1)));
                if (null != context) {
                    context.setUsedQuota(L(rs.getLong(2) >> 20));
                    context.setAverage_size(L(averageSize));
                }
            }
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                cache.getPool().pushConnection(readPoolId, con);
            } catch (PoolException e) {
                LOG.error("", e);
            }
        }
        if (failOnMissing) {
            for (Context context : contexts.values()) {
                if (!context.isUsedQuotaset()) {
                    throw new StorageException("Was not able to find a filestore usage for context " + context.getId()
                        + ". Please consider running update tasks on all existing schemas or at least on schema "
                        + context.getReadDatabase().getScheme());
                }
            }
        }
        return contexts.values();
    }

    private class PoolIdTuple {
        private final int writePoolId, readPoolId;
        PoolIdTuple(int writePoolId, int readPoolId) {
            super();
            this.writePoolId = writePoolId;
            this.readPoolId = readPoolId;
        }
        public int getWritePoolId() {
            return writePoolId;
        }
        public int getReadPoolId() {
            return readPoolId;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + writePoolId;
            result = prime * result + readPoolId;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PoolIdTuple other = (PoolIdTuple) obj;
            if (writePoolId != other.writePoolId) {
                return false;
            }
            if (readPoolId != other.readPoolId) {
                return false;
            }
            return true;
        }
    }
}
