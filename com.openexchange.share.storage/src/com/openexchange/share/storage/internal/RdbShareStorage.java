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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.storage.internal;

import static com.openexchange.share.storage.internal.SQL.SHARE_MAPPER;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.share.storage.internal.ConnectionProvider.ConnectionMode;
import com.openexchange.share.storage.mapping.RdbShare;
import com.openexchange.share.storage.mapping.ShareField;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbShareStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class RdbShareStorage implements ShareStorage {

    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link RdbShareStorage}.
     *
     * @param databaseService The database service
     */
    public RdbShareStorage(DatabaseService databaseService) {
        super();
        this.databaseService = databaseService;
    }

    @Override
    public List<Share> loadShares(int contextID, int guest, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return new ShareSelector(contextID).guests(new int[] { guest }).select(provider.get());
        } finally {
            provider.close();
        }
    }

    @Override
    public List<Share> loadShares(int contextID, int[] guests, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return new ShareSelector(contextID).guests(guests).select(provider.get());
        } finally {
            provider.close();
        }
    }

    @Override
    public List<Share> loadSharesCreatedBy(int contextID, int createdBy, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getReadProvider(contextID, parameters);
        try {
            return new ShareSelector(contextID).createdBy(createdBy).select(provider.get());
        } finally {
            provider.close();
        }
    }

    @Override
    public void storeShares(int contextID, List<Share> shares, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getWriteProvider(contextID, parameters);
        try {
            insertShares(provider.get(), contextID, shares);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public int deleteShares(int contextID, List<ShareTarget> targets, int[] guestIDs, StorageParameters parameters) throws OXException {
        ConnectionProvider provider = getWriteProvider(contextID, parameters);
        try {
            return new ShareSelector(contextID).guests(guestIDs).targets(targets).delete(provider.get());
        } finally {
            provider.close();
        }
    }

    private static int[] insertShares(Connection connection, int contextID, List<Share> shares) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("INSERT INTO share (").append(SHARE_MAPPER.getColumns(ShareField.values())).append(") VALUES (")
            .append(SHARE_MAPPER.getParameters(ShareField.values().length)).append(");")
        ;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            for (Share share : shares) {
                SHARE_MAPPER.setParameters(stmt, new RdbShare(contextID, share), ShareField.values());
                stmt.addBatch();
            }
            return SQL.logExecuteBatch(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private ConnectionProvider getReadProvider(int contextId, StorageParameters parameters) throws OXException {
        return new ConnectionProvider(databaseService, parameters, ConnectionMode.READ, contextId);
    }

    private ConnectionProvider getWriteProvider(int contextId, StorageParameters parameters) throws OXException {
        return new ConnectionProvider(databaseService, parameters, ConnectionMode.WRITE, contextId);
    }

}
