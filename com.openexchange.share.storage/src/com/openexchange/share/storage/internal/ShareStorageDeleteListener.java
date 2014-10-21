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

import static com.openexchange.share.storage.internal.SQL.logExecuteUpdate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.share.storage.mapping.ShareField;
import com.openexchange.share.storage.mapping.ShareTargetField;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ShareStorageDeleteListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareStorageDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link ShareStorageDeleteListener}.
     */
    public ShareStorageDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            try {
                deleteSharesInContext(writeCon, event.getContext().getContextId());
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (Exception e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            }
        } else if (DeleteEvent.TYPE_USER == event.getType() && event.getContext().getMailadmin() != event.getId()) {
            try {
                deleteSharesForGuest(writeCon, event.getContext().getContextId(), event.getId());
                reassignShares(writeCon, event.getContext().getContextId(), event.getId(), event.getContext().getMailadmin());
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (Exception e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            }
        }
    }

    private static int deleteSharesInContext(Connection connection, int cid) throws SQLException, OXException {
        int affectedRows = 0;
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM share WHERE ").append(SQL.SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=?;");
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            affectedRows += logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        stringBuilder = new StringBuilder()
            .append("DELETE FROM share_target WHERE ").append(SQL.TARGET_MAPPER.get(ShareTargetField.CONTEXT_ID).getColumnLabel()).append("=?;");
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            affectedRows += logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return affectedRows;
    }

    private static int deleteSharesForGuest(Connection connection, int cid, int guest) throws SQLException, OXException {
        int affectedRows = 0;
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM share_target WHERE ").append(SQL.TARGET_MAPPER.get(ShareTargetField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(SQL.TARGET_MAPPER.get(ShareTargetField.TOKEN).getColumnLabel()).append(" IN (")
            .append("SELECT ").append(SQL.SHARE_MAPPER.get(ShareField.TOKEN).getColumnLabel()).append(" FROM share WHERE ")
            .append(SQL.SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(SQL.SHARE_MAPPER.get(ShareField.CREATED_BY).getColumnLabel()).append("=?);")
        ;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            stmt.setInt(2, cid);
            stmt.setInt(3, guest);
            affectedRows += logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        stringBuilder = new StringBuilder()
            .append("DELETE FROM share WHERE ").append(SQL.SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(SQL.SHARE_MAPPER.get(ShareField.CREATED_BY).getColumnLabel()).append("=?;")
        ;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, cid);
            stmt.setInt(2, guest);
            affectedRows += logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return affectedRows;
    }

    private static int reassignShares(Connection connection, int cid, int createdBy, int newCreatedBy) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("UPDATE share SET ").append(SQL.SHARE_MAPPER.get(ShareField.CREATED_BY).getColumnLabel()).append("=? ")
            .append("WHERE ").append(SQL.SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=? ")
            .append("AND ").append(SQL.SHARE_MAPPER.get(ShareField.CREATED_BY).getColumnLabel()).append("=?;")
        ;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, newCreatedBy);
            stmt.setInt(2, cid);
            stmt.setInt(3, createdBy);
            return logExecuteUpdate(stmt);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
