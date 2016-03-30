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

package com.openexchange.contact.vcard.storage.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.contact.vcard.storage.VCardStorageMetadataStore;
import com.openexchange.contact.vcard.storage.VCardStorageExceptionCodes;
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
            stmt = con.prepareStatement("DELETE FROM prg_contacts WHERE cid = ? AND vCardId = ?");
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
