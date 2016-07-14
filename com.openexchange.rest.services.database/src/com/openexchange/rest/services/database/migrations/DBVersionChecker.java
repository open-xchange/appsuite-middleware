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

package com.openexchange.rest.services.database.migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.database.DatabaseRESTErrorCodes;
import com.openexchange.tools.sql.DBUtils;

/**
 * The {@link DBVersionChecker} implements the VersionChecker interface. It caches version data for a schema and module for 30 minutes.
 *
 * @see VersionChecker
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DBVersionChecker implements VersionChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DBVersionChecker.class);

    private final Cache<Key, String> versionCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    @Override
    public String isUpToDate(Object id, final Connection con, final String module, String versionId) throws OXException {
        try {
            Key key = new Key(id, module);
            LoadVersion loader = new LoadVersion(con, module);
            String version = versionCache.get(key, loader);
            if (version.equals(versionId)) {
                return null;
            }
            // Get fresh from DB
            versionCache.invalidate(key);
            version = versionCache.get(key, loader);
            if (version.equals(versionId)) {
                return null;
            }

            return version;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof OXException) {
                throw (OXException) e.getCause();
            }
            LOG.error(e.getCause().getMessage(), e.getCause());
        }

        return "";
    }

    @Override
    public String updateVersion(Connection con, String module, String oldVersionId, String newVersionId) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (oldVersionId.equals("")) {
                stmt = con.prepareStatement("INSERT IGNORE INTO serviceSchemaVersion (module, version) VALUES (?, ?)");
                stmt.setString(1, module);
                stmt.setString(2, newVersionId);

                int update = stmt.executeUpdate();
                DBUtils.closeSQLStuff(stmt);

                if (update == 1) {
                    return null;
                }
            }

            stmt = con.prepareStatement("UPDATE serviceSchemaVersion SET version = ? WHERE module = ? AND version = ?");
            stmt.setString(1, newVersionId);
            stmt.setString(2, module);
            stmt.setString(3, oldVersionId);

            if (stmt.executeUpdate() > 0) {
                return null;
            }

            DBUtils.closeSQLStuff(stmt);
            stmt = con.prepareStatement("SELECT version FROM serviceSchemaVersion WHERE module = ?");
            stmt.setString(1, module);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("version");
            }

            throw DatabaseRESTErrorCodes.VERSION_MUST_BE_KNOWN.create(module);
        } catch (SQLException x) {
            try {
                con.rollback();
            } catch (SQLException e) {
                // IGNORE
            }
            throw DatabaseRESTErrorCodes.SQL_ERROR.create(x.getMessage());
        } finally {
            try {
                con.commit();
            } catch (SQLException x) {
                // IGNORE
            }
            DBUtils.closeSQLStuff(stmt);
            DBUtils.closeSQLStuff(rs);
        }
    }

    private static final class LoadVersion implements Callable<String> {

        private final Connection con;
        private final String module;

        public LoadVersion(Connection con, String module) {
            super();
            this.con = con;
            this.module = module;
        }

        @Override
        public String call() throws Exception {
            PreparedStatement stmt = null;
            ResultSet query = null;
            try {
                stmt = con.prepareStatement("SELECT version FROM serviceSchemaVersion WHERE module = ?");
                stmt.setString(1, module);
                query = stmt.executeQuery();
                if (query.next()) {
                    return query.getString("version");
                }
                return "";
            } catch (SQLException x) {
                throw DatabaseRESTErrorCodes.SQL_ERROR.create(x.getMessage());
            } finally {
                DBUtils.closeSQLStuff(query, stmt);
            }
        }

    }

    private static final class Key {

        private final String module;
        private final Object connectionKey;

        public Key(Object connectionKey, String module) {
            this.module = module;
            this.connectionKey = connectionKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((connectionKey == null) ? 0 : connectionKey.hashCode());
            result = prime * result + ((module == null) ? 0 : module.hashCode());
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
            Key other = (Key) obj;
            if (connectionKey == null) {
                if (other.connectionKey != null) {
                    return false;
                }
            } else if (!connectionKey.equals(other.connectionKey)) {
                return false;
            }
            if (module == null) {
                if (other.module != null) {
                    return false;
                }
            } else if (!module.equals(other.module)) {
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean lock(Connection con, String module, long now, long expires) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // DELETE STALE LOCKS
            stmt = con.prepareStatement("DELETE FROM serviceSchemaMigrationLock WHERE module = ? AND expires <= ?");
            stmt.setString(1, module);
            stmt.setLong(2, now);
            stmt.executeUpdate();

            // FIND LOCK
            stmt.close();
            stmt = con.prepareStatement("SELECT 1 FROM serviceSchemaMigrationLock WHERE module = ?");
            stmt.setString(1, module);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return false;
            }
            DBUtils.closeSQLStuff(stmt);

            //   CREATE LOCK
            stmt = con.prepareStatement("INSERT IGNORE INTO serviceSchemaMigrationLock (module, expires) VALUES (?, ?)");
            stmt.setString(1, module);
            stmt.setLong(2, expires);
            // RETURN TRUE IF INSERT WAS SUCCESSFUL
            return stmt.executeUpdate() == 1;

        } catch (SQLException x) {
            throw DatabaseRESTErrorCodes.SQL_ERROR.create(x.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void unlock(Connection con, String module) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM serviceSchemaMigrationLock WHERE module = ?");
            stmt.setString(1, module);
            stmt.executeUpdate();
        } catch (SQLException x) {
            throw DatabaseRESTErrorCodes.SQL_ERROR.create(x.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean touchLock(Connection con, String module, long expires) throws OXException {

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE serviceSchemaMigrationLock SET expires = ? WHERE module = ?");
            stmt.setLong(1, expires);
            stmt.setString(2, module);
            return stmt.executeUpdate() > 0;
        } catch (SQLException x) {
            throw DatabaseRESTErrorCodes.SQL_ERROR.create(x.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
}
