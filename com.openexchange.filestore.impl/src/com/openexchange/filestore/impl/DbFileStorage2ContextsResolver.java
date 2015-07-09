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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.filestore.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage2ContextsResolver;
import com.openexchange.filestore.QuotaFileStorageExceptionCodes;
import com.openexchange.filestore.impl.osgi.Services;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link DbFileStorage2ContextsResolver} - Utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DbFileStorage2ContextsResolver implements FileStorage2ContextsResolver {

    /**
     * Initializes a new {@link DbFileStorage2ContextsResolver}.
     */
    public DbFileStorage2ContextsResolver() {
        super();
    }

    @Override
    public int[] getIdsOfContextsUsing(int fileStorageId) throws OXException {
        DatabaseService databaseService = Services.requireService(DatabaseService.class);

        TIntSet usingContexts = new TIntHashSet(256);

        int[] contextIds;
        {
            Connection configDBCon = databaseService.getReadOnly();
            try {
                contextIds = getAllContextIds(configDBCon);
                addContextsUsing(fileStorageId, usingContexts, configDBCon);
            } finally {
                databaseService.backReadOnly(configDBCon);
            }
        }

        TIntSet processed = new TIntHashSet(256);
        for (int contextId : contextIds) {
            if (processed.add(contextId)) {
                Connection schemaCon = databaseService.getReadOnly(contextId);
                try {
                    addContextsWithUsersUsing(fileStorageId, usingContexts, schemaCon);
                } finally {
                    databaseService.backReadOnly(contextId, schemaCon);
                }

                processed.addAll(databaseService.getContextsInSameSchema(contextId));
            }
        }

        return usingContexts.toArray();
    }

    private void addContextsUsing(int fileStorageId, TIntSet usingContexts, Connection configDBCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = configDBCon.prepareStatement("SELECT cid FROM context WHERE filestore_id=?");
            stmt.setInt(1, fileStorageId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                usingContexts.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void addContextsWithUsersUsing(int fileStorageId, TIntSet usingContexts, Connection schemaCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = schemaCon.prepareStatement("SELECT DISTINCT cid FROM user WHERE filestore_id=?");
            stmt.setInt(1, fileStorageId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                usingContexts.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private int[] getAllContextIds(Connection configDBCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = configDBCon.prepareStatement("SELECT cid FROM context ORDER BY cid");
            rs = stmt.executeQuery();
            TIntList cids = new TIntLinkedList();
            while (rs.next()) {
                cids.add(rs.getInt(1));
            }
            return cids.toArray();
        } catch (SQLException e) {
            throw QuotaFileStorageExceptionCodes.SQLSTATEMENTERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
