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
import static com.openexchange.share.storage.internal.SQL.TARGET_MAPPER;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.Share;
import com.openexchange.share.storage.mapping.ShareField;
import com.openexchange.share.storage.mapping.ShareTargetField;

/**
 * {@link SelectShareBuilder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SelectShareBuilder {

    static final String ALIAS_SHARE = "s";
    static final String ALIAS_SHARE_TARGET = "t";

    private final int contextID;
    private final ShareField[] shareFields;
    private final ShareTargetField[] targetFields;
    private String[] tokens;
    private int createdBy;
    private int[] guests;
    private Share target;
    private Date expiredAfter;

    /**
     * Initializes a new {@link SelectShareBuilder}.
     *
     * @param contextID
     * @param shareFields
     * @param targetFields
     */
    public SelectShareBuilder(int contextID, ShareField[] shareFields, ShareTargetField[] targetFields) {
        super();
        this.contextID = contextID;
        this.shareFields = shareFields;
        this.targetFields = targetFields;
    }

    /**
     * Adds one or more tokens to restrict the results to.
     *
     * @param tokens The tokens of the shares to retrieve, or <code>null</code> to not filter by token
     * @return The builder
     */
    public SelectShareBuilder tokens(String[] tokens) {
        this.tokens = tokens;
        return this;
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
     * @param targte The target
     * @return The builder
     */
    public SelectShareBuilder target(Share target) {
        this.target = target;
        return this;
    }

    /**
     * Prepares the statement and sets all required parameters.
     *
     * @param connection The connection to use
     * @return The prepared statement
     */
    public PreparedStatement prepare(Connection connection) throws SQLException, OXException {
        /*
         * build query
         */
        String prefixShare = ALIAS_SHARE + '.';
        String prefixTarget = ALIAS_SHARE_TARGET + '.';
        StringBuilder statementBuilder = new StringBuilder(256)
            .append("SELECT ").append(SHARE_MAPPER.getColumns(shareFields, prefixShare)).append(',')
            .append(TARGET_MAPPER.getColumns(targetFields, prefixTarget))
            .append(" FROM share AS ").append(ALIAS_SHARE).append(" LEFT JOIN share_target AS ").append(ALIAS_SHARE_TARGET)
            .append(" ON ").append(SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel(prefixShare))
            .append('=').append(TARGET_MAPPER.get(ShareTargetField.CONTEXT_ID).getColumnLabel(prefixTarget))
            .append(" AND ").append(SHARE_MAPPER.get(ShareField.TOKEN).getColumnLabel(prefixShare))
            .append('=').append(TARGET_MAPPER.get(ShareTargetField.TOKEN).getColumnLabel(prefixTarget))
            .append(" WHERE ").append(SHARE_MAPPER.get(ShareField.CONTEXT_ID).getColumnLabel(prefixShare)).append("=?")
        ;
        if (null != tokens && 0 < tokens.length) {
            statementBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.TOKEN).getColumnLabel(prefixShare));
            if (1 == tokens.length) {
                statementBuilder.append("=?");
            } else {
                statementBuilder.append(" IN (?");
                for (int i = 1; i < tokens.length; i++) {
                    statementBuilder.append(",?");
                }
                statementBuilder.append(')');
            }
        }
        if (0 < createdBy) {
            statementBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.CREATED_BY).getColumnLabel(prefixShare)).append("=?");
        }
        if (null != guests && 0 < guests.length) {
            statementBuilder.append(" AND ").append(SHARE_MAPPER.get(ShareField.GUEST_ID).getColumnLabel(prefixShare));
            if (1 == guests.length) {
                statementBuilder.append("=?");
            } else {
                statementBuilder.append(" IN (?");
                for (int i = 1; i < guests.length; i++) {
                    statementBuilder.append(",?");
                }
                statementBuilder.append(')');
            }
        }
        if (null != expiredAfter) {
            statementBuilder.append(" AND EXISTS (SELECT 1 FROM share_target WHERE ")
                .append(TARGET_MAPPER.get(ShareTargetField.CONTEXT_ID).getColumnLabel()).append("=?")
                .append(" AND ").append(TARGET_MAPPER.get(ShareTargetField.TOKEN).getColumnLabel()).append('=')
                .append(SHARE_MAPPER.get(ShareField.TOKEN).getColumnLabel(prefixShare))
                .append(" AND ").append(TARGET_MAPPER.get(ShareTargetField.EXPIRY_DATE).getColumnLabel()).append(" IS NOT NULL")
                .append(" AND ").append(TARGET_MAPPER.get(ShareTargetField.EXPIRY_DATE).getColumnLabel()).append("<?)")
            ;
        }
        if (null != target) {
            statementBuilder.append(" AND EXISTS (SELECT 1 FROM share_target WHERE ")
                .append(TARGET_MAPPER.get(ShareTargetField.CONTEXT_ID).getColumnLabel()).append("=?")
                .append(" AND ").append(TARGET_MAPPER.get(ShareTargetField.TOKEN).getColumnLabel()).append('=')
                .append(SHARE_MAPPER.get(ShareField.TOKEN).getColumnLabel(prefixShare))
                .append(" AND ").append(TARGET_MAPPER.get(ShareTargetField.MODULE).getColumnLabel()).append("=?")
                .append(" AND ").append(TARGET_MAPPER.get(ShareTargetField.FOLDER).getColumnLabel()).append("=?")
            ;
            if (null != target.getItem()) {
                statementBuilder.append(" AND ").append(TARGET_MAPPER.get(ShareTargetField.ITEM).getColumnLabel()).append("=?");
            }
            statementBuilder.append(')');
        }
        /*
         * prepare statement
         */
        PreparedStatement stmt = connection.prepareStatement(statementBuilder.toString());
        int parameterIndex = 1;
        stmt.setInt(parameterIndex++, contextID);
        if (null != tokens) {
            for (String token : tokens) {
                stmt.setBytes(parameterIndex++, UUIDs.toByteArray(UUIDs.fromUnformattedString(token)));
            }
        }
        if (0 < createdBy) {
            stmt.setInt(parameterIndex++, createdBy);
        }
        if (null != guests) {
            for (int guest : guests) {
                stmt.setInt(parameterIndex++, guest);
            }
        }
        if (null != expiredAfter) {
            stmt.setInt(parameterIndex++, contextID);
            stmt.setLong(parameterIndex++, expiredAfter.getTime());
        }
        if (null != target) {
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, target.getModule());
            stmt.setString(parameterIndex++, target.getFolder());
            if (null != target.getItem()) {
                stmt.setString(parameterIndex++, target.getItem());
            }
        }
        return stmt;
    }

}

