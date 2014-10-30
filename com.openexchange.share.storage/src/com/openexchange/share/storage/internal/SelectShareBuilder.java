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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.storage.mapping.ShareField;

/**
 * {@link SelectShareBuilder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SelectShareBuilder {

    private final int contextID;
    private final ShareField[] shareFields;
    private int createdBy;
    private int[] guests;
    private List<ShareTarget> targets;
    private Date expiredAfter;

    /**
     * Initializes a new {@link SelectShareBuilder}.
     *
     * @param contextID
     * @param shareFields
     * @param targetFields
     */
    public SelectShareBuilder(int contextID, ShareField[] shareFields) {
        super();
        this.contextID = contextID;
        this.shareFields = shareFields;
    }

    /**
     * Adds the user ID of the share creator to restrict the results to.
     *
     * @param createdBy The ID of the user who created the shares, or <code>0</code> to not filter by the creating user
     * @return The builder
     */
    public SelectShareBuilder createdBy(int createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Adds a date to filter by share targets with an earlier expiry date.
     *
     * @param expiredAfter The expiry date
     * @return The builder
     */
    public SelectShareBuilder expiredAfter(Date expiredAfter) {
        this.expiredAfter = expiredAfter;
        return this;
    }

    /**
     * Adds one or more guest user IDs to restrict the results to.
     *
     * @param guests The IDs of the guests assigned to the shares, or <code>null</code> to not filter by the guest users
     * @return The builder
     */
    public SelectShareBuilder guests(int[] guests) {
        this.guests = guests;
        return this;
    }

    /**
     * Adds a share target definition to restrict the results to.
     *
     * @param targets The targets
     * @return The builder
     */
    public SelectShareBuilder targets(List<ShareTarget> targets) {
        this.targets = targets;
        return this;
    }

    /**
     * Prepares the statement and sets all required parameters.
     *
     * @param connection The connection to use
     * @return The prepared statement
     */
    public PreparedStatement prepareSelect(Connection connection) throws SQLException, OXException {
        return prepare(connection, "SELECT " + SHARE_MAPPER.getColumns(shareFields) + " FROM share ");
    }

    /**
     * Prepares the statement and sets all required parameters.
     *
     * @param connection The connection to use
     * @return The prepared statement
     */
    public PreparedStatement prepareDelete(Connection connection) throws SQLException, OXException {
        return prepare(connection, "DELETE FROM share ");
    }

    /**
     * Prepares the statement and sets all required parameters.
     *
     * @param connection The connection to use
     * @return The prepared statement
     */
    private PreparedStatement prepare(Connection connection, String sqlBeforeWhere) throws SQLException, OXException {
        /*
         * build query
         */
        StringBuilder stringBuilder = new StringBuilder(256)
            .append(sqlBeforeWhere).append(" WHERE ").append(SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel()).append("=?")
        ;
        if (0 < createdBy) {
            stringBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.CREATED_BY).getColumnLabel()).append("=?");
        }
        if (null != guests && 0 < guests.length) {
            stringBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.GUEST).getColumnLabel());
            if (1 == guests.length) {
                stringBuilder.append("=?");
            } else {
                stringBuilder.append(" IN (?");
                for (int i = 1; i < guests.length; i++) {
                    stringBuilder.append(",?");
                }
                stringBuilder.append(')');
            }
        }
        if (null != expiredAfter) {
            stringBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.EXPIRES).getColumnLabel()).append(" IS NOT NULL")
                .append(" AND ").append(SHARE_MAPPER.get(ShareField.EXPIRES).getColumnLabel()).append("<?")
            ;
        }
        if (null != targets && 0 < targets.size()) {
            if (1 == targets.size()) {
                stringBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.MODULE).getColumnLabel()).append("=?")
                    .append(" AND ").append(SHARE_MAPPER.get(ShareField.FOLDER).getColumnLabel()).append("=?")
                    .append(" AND ").append(SHARE_MAPPER.get(ShareField.ITEM).getColumnLabel()).append("=?")
                ;
            } else {
                stringBuilder.append(" AND (").append(SHARE_MAPPER.get(ShareField.MODULE).getColumnLabel()).append("=?")
                    .append(" AND ").append(SHARE_MAPPER.get(ShareField.FOLDER).getColumnLabel()).append("=?")
                    .append(" AND ").append(SHARE_MAPPER.get(ShareField.ITEM).getColumnLabel()).append("=?");
                for (int i = 1; i < targets.size(); i++) {
                    stringBuilder.append(" OR ").append(SHARE_MAPPER.get(ShareField.MODULE).getColumnLabel()).append("=?")
                        .append(" AND ").append(SHARE_MAPPER.get(ShareField.FOLDER).getColumnLabel()).append("=?")
                        .append(" AND ").append(SHARE_MAPPER.get(ShareField.ITEM).getColumnLabel()).append("=?");
                }
                stringBuilder.append(')');
            }
        }
        /*
         * prepare statement
         */
        PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString());
        int parameterIndex = 1;
        stmt.setInt(parameterIndex++, contextID);
        if (0 < createdBy) {
            stmt.setInt(parameterIndex++, createdBy);
        }
        if (null != guests) {
            for (int guest : guests) {
                stmt.setInt(parameterIndex++, guest);
            }
        }
        if (null != expiredAfter) {
            stmt.setLong(parameterIndex++, expiredAfter.getTime());
        }
        if (null != targets) {
            for (ShareTarget target : targets) {
                stmt.setInt(parameterIndex++, target.getModule());
                stmt.setString(parameterIndex++, null != target.getFolder() ? target.getFolder() : "");
                stmt.setString(parameterIndex++, null != target.getItem() ? target.getItem() : "");
            }
        }
        return stmt;
    }

}

