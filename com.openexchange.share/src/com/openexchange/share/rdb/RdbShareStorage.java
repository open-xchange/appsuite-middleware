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

package com.openexchange.share.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.internal.DefaultShare;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link RdbShareStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class RdbShareStorage implements ShareStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbShareStorage.class);

    private static final String SELECT_SHARE_STMT =
        "SELECT module,folder,item,created,createdBy,lastModified,modifiedBy,expires,guest,auth,displayName " +
        "FROM share " +
        "WHERE cid=? AND token=?;"
    ;

    private static final String SELECT_SHARES_CREATED_BY_STMT =
        "SELECT HEX(token),module,folder,item,created,lastModified,modifiedBy,expires,guest,auth,displayName " +
        "FROM share " +
        "WHERE cid=? AND createdBy=?;"
    ;

    private static final String INSERT_SHARES_STMT(int count) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO share (token,  uuid,cid,folder,file,version,sequence,checksum) ");
        if (0 < count) {
            stringBuilder.append("VALUES (UNHEX(?),?,REVERSE(?),REVERSE(?),?,?,UNHEX(?))");
        }
        for (int i = 1; i < count; i++) {
            stringBuilder.append(",(UNHEX(?),?,REVERSE(?),REVERSE(?),?,?,UNHEX(?))");
        }
        stringBuilder.append(';');
        return stringBuilder.toString();
    }


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
    public Share loadShare(int contextID, String token) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectShare(connection, contextID, token);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public void storeShare(Share share) {
    }

    @Override
    public void updateShare(Share share) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Share> loadSharesCreatedBy(int contextID, int createdBy) throws OXException {
        Connection connection = databaseService.getReadOnly(contextID);
        try {
            return selectSharesCreatedBy(connection, contextID, createdBy);
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    private static DefaultShare selectShare(Connection connection, int cid, String token) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SELECT_SHARE_STMT);
            stmt.setInt(1, cid);
            stmt.setBytes(2, UUIDs.toByteArray(UUIDs.fromUnformattedString(token)));
            ResultSet resultSet = logExecuteQuery(stmt);
            if (resultSet.next()) {
                DefaultShare share = new DefaultShare();
                share.setToken(token);
                share.setContextID(cid);
                share.setModule(resultSet.getInt(1));
                share.setFolder(resultSet.getString(2));
                share.setItem(resultSet.getString(3));
                share.setCreated(new Date(resultSet.getLong(4)));
                share.setCreatedBy(resultSet.getInt(5));
                share.setLastModified(new Date(resultSet.getLong(6)));
                share.setModifiedBy(resultSet.getInt(7));
                long expires = resultSet.getLong(8);
                if (false == resultSet.wasNull()) {
                    share.setExpires(new Date(expires));
                }
                share.setGuest(resultSet.getInt(9));
                share.setAuthentication(resultSet.getInt(10));
                share.setDisplayName(resultSet.getString(11));
                return share;
            } else {
                return null;
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static List<Share> selectSharesCreatedBy(Connection connection, int cid, int createdBy) throws SQLException {
        List<Share> shares = new ArrayList<Share>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SELECT_SHARES_CREATED_BY_STMT);
            stmt.setInt(1, cid);
            stmt.setInt(2, createdBy);
            ResultSet resultSet = logExecuteQuery(stmt);
            if (resultSet.next()) {
                DefaultShare share = new DefaultShare();
                share.setContextID(cid);
                share.setCreatedBy(createdBy);
                share.setToken(resultSet.getString(1));
                share.setModule(resultSet.getInt(2));
                share.setFolder(resultSet.getString(3));
                share.setItem(resultSet.getString(4));
                share.setCreated(new Date(resultSet.getLong(5)));
                share.setLastModified(new Date(resultSet.getLong(6)));
                share.setModifiedBy(resultSet.getInt(7));
                long expires = resultSet.getLong(8);
                if (false == resultSet.wasNull()) {
                    share.setExpires(new Date(expires));
                }
                share.setGuest(resultSet.getInt(9));
                share.setAuthentication(resultSet.getInt(10));
                share.setDisplayName(resultSet.getString(11));
                shares.add(share);
            } else {
                return null;
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return shares;
    }

    private static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: {} - {} ms elapsed.", stmt.toString(), (System.currentTimeMillis() - start));
            return resultSet;
        }
    }

    private static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", stmt.toString(), rowCount, (System.currentTimeMillis() - start));
            return rowCount;
        }
    }

}
