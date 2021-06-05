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

package com.openexchange.contact.vcard.storage.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.contact.vcard.storage.VCardStorageExceptionCodes;
import com.openexchange.contact.vcard.storage.VCardStorageMetadataStore;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;

/**
 *
 * {@link DefaultVCardStorageMetadataStore}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DefaultVCardStorageMetadataStore implements VCardStorageMetadataStore {

    private final DatabaseService databaseService;

    public DefaultVCardStorageMetadataStore(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * Loads all filestore references for the given context.
     *
     * @param contextId The context id.
     * @return A set containing all found reference ids.
     * @throws OXException
     */
    @Override
    public Set<String> loadRefIds(int contextId) throws OXException {
        Set<String> refIds = new HashSet<String>();
        Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT vCardId FROM prg_contacts WHERE cid=? AND vCardid IS NOT NULL");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                refIds.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw VCardStorageExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
        return refIds;
    }

    @Override
    public Set<String> loadRefIds(int contextId, int userId) throws OXException {
        Set<String> refIds = new HashSet<String>();
        Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT vCardId FROM prg_contacts WHERE cid=? AND created_from=? AND vCardid IS NOT NULL");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                refIds.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw VCardStorageExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
        return refIds;
    }

    /**
     * Deletes a set of records in a context for a given set of reference ids.
     *
     * @param contextId The context id.
     * @param refIds The reference ids.
     * @throws OXException if deletion fails.
     */
    @Override
    public void removeByRefId(int contextId, Set<String> refIds) throws OXException {
        Connection con = databaseService.getWritable(contextId);
        try {
            removeByRefIds(con, contextId, refIds);
        } catch (SQLException e) {
            throw VCardStorageExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Convenience method to re-use an existing database connection.
     *
     * @param con The writable database connection in a transactional state.
     * @throws SQLException
     * @see {@link DefaultVCardStorageMetadataStore#removeByRefId(int, Set)}
     */
    private void removeByRefIds(Connection con, int contextId, Set<String> refIds) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE prg_contacts SET vCardId=NULL WHERE cid=? AND vCardId=?");
            stmt.setInt(1, contextId);
            for (String refId : refIds) {
                stmt.setString(2, refId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }
}
