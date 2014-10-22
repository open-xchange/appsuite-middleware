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
import static com.openexchange.share.storage.internal.SQL.logExecuteQuery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.share.DefaultShare;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.storage.mapping.ShareField;
import com.openexchange.share.storage.mapping.ShareTargetField;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ShareSelector}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareSelector {

    private static final ShareField[] DEFAULT_SHARE_FIELDS = {
        ShareField.TOKEN, ShareField.CREATION_DATE, ShareField.CREATED_BY, ShareField.LAST_MODIFIED, ShareField.MODIFIED_BY,
        ShareField.GUEST_ID, ShareField.AUTHENTICATION
    };

    private static final ShareTargetField[] DEFAULT_TARGET_FIELDS = {
        ShareTargetField.MODULE, ShareTargetField.FOLDER, ShareTargetField.ITEM, ShareTargetField.EXPIRY_DATE, ShareTargetField.META
    };

    private final SelectShareBuilder builder;
    private final ShareField[] shareFields;
    private final ShareTargetField[] targetFields;
    private final int contextID;

    /**
     * Initializes a new {@link ShareSelector}, selecting a set of default fields.
     *
     * @param contextID The context ID.
     */
    public ShareSelector(int contextID) {
        this(contextID, DEFAULT_SHARE_FIELDS, DEFAULT_TARGET_FIELDS);
    }

    /**
     * Initializes a new {@link ShareSelector}.
     *
     * @param contextID The context ID.
     * @param shareFields The share fields to select
     * @param targetFields The share target fields to select
     */
    public ShareSelector(int contextID, ShareField[] shareFields, ShareTargetField[] targetFields) {
        super();
        this.contextID = contextID;
        this.shareFields = shareFields;
        this.targetFields = targetFields;
        this.builder = new SelectShareBuilder(contextID, shareFields, targetFields);
    }

    /**
     * Adds one or more tokens to restrict the results to.
     *
     * @param tokens The tokens of the shares to retrieve, or <code>null</code> to not filter by token
     * @return The builder
     */
    public ShareSelector tokens(String[] tokens) {
        builder.tokens(tokens);
        return this;
    }

    /**
     * Adds the user ID of the share creator to restrict the results to.
     *
     * @param createdBy The ID of the user who created the shares, or <code>0</code> to not filter by the creating user
     * @return The builder
     */
    public ShareSelector createdBy(int createdBy) {
        builder.createdBy(createdBy);
        return this;
    }

    /**
     * Adds one or more guest user IDs to restrict the results to.
     *
     * @param guests The IDs of the guests assigned to the shares, or <code>null</code> to not filter by the guest users
     * @return The builder
     */
    public ShareSelector guests(int[] guests) {
        builder.guests(guests);
        return this;
    }

    /**
     * Adds a date to filter by share targets with an earlier expiry date.
     *
     * @param expiredAfter The expiry date
     * @return The builder
     */
    public ShareSelector expiredAfter(Date expiredAfter) {
        builder.expiredAfter(expiredAfter);
        return this;
    }

    /**
     * Adds a groupware target definition to restrict the results to.
     *
     * @param target The target
     * @return The builder
     */
    public ShareSelector target(ShareTarget target) {
        builder.target(target);
        return this;
    }

    /**
     * Performs the <code>SELECT</code> query and reads out the matching shares.
     *
     * @param connection The database connection
     * @return The shares
     * @throws OXException
     */
    public Collection<DefaultShare> select(Connection connection) throws OXException {
        Map<String, DefaultShare> sharesByToken = new HashMap<String, DefaultShare>();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = builder.prepare(connection);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                DefaultShare currentShare = SHARE_MAPPER.fromResultSet(resultSet, shareFields, SelectShareBuilder.ALIAS_SHARE + '.');
                DefaultShare share = sharesByToken.get(currentShare.getToken());
                if (null == share) {
                    share = currentShare;
                    share.setTargets(new ArrayList<ShareTarget>());
                    share.setContextID(contextID);
                    sharesByToken.put(share.getToken(), share);
                }
                RdbShareTarget target = TARGET_MAPPER.fromResultSet(resultSet, targetFields, SelectShareBuilder.ALIAS_SHARE_TARGET + '.');
                if (0 < target.getModule()) {
                    share.getTargets().add(target);
                }
            }
            return sharesByToken.values();
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

}

