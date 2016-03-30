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

package com.openexchange.user.copy.internal.subscription;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.genconf.ConfAttribute;
import com.openexchange.user.copy.internal.genconf.GenconfCopyTool;
import com.openexchange.user.copy.internal.oauth.OAuthAccount;
import com.openexchange.user.copy.internal.oauth.OAuthCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link SubscriptionCopyTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SubscriptionCopyTask implements CopyUserTaskService {

   private static final String SELECT_SUBSCRIPTIONS =
       "SELECT " +
           "id, configuration_id, source_id, folder_id, " +
           "last_update, enabled, created, lastModified " +
       "FROM " +
           "subscriptions " +
       "WHERE " +
           "cid = ? " +
       "AND " +
           "user_id = ?";

   private static final String INSERT_SUBSCRIPTION =
       "INSERT INTO " +
           "subscriptions " +
           "(id, cid, user_id, configuration_id, source_id, folder_id, last_update, enabled, created, lastModified) " +
       "VALUES " +
           "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


   public SubscriptionCopyTask() {
       super();
   }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName(),
            OAuthCopyTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return "subscription";
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public ObjectMapping<?> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Integer srcCtxId = copyTools.getSourceContextId();
        final Integer dstCtxId = copyTools.getDestinationContextId();
        final Context dstCtx = copyTools.getDestinationContext();
        final Integer srcUsrId = copyTools.getSourceUserId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();
        final ObjectMapping<FolderObject> folderMapping = copyTools.getFolderMapping();

        try {
            final boolean hasSubscriptions = DBUtils.tableExists(srcCon, "subscriptions");
            if (hasSubscriptions) {
                final ObjectMapping<Integer> accountMapping = copyTools.checkAndExtractGenericMapping(OAuthAccount.class.getName());
                final Map<Integer, Subscription> subscriptions = loadSubscriptionsFromDB(srcCon, i(srcUsrId), i(srcCtxId));
                fillSubscriptionsWithAttributes(subscriptions, srcCon, i(srcCtxId));
                setAccountIdsForSubscriptions(subscriptions, accountMapping);
                exchangeSubscriptionIds(subscriptions, dstCon, dstCtx, folderMapping);
                writeSubscriptionsToDB(subscriptions, dstCon, i(dstUsrId), i(dstCtxId));
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        }

        return null;
    }

    private void writeSubscriptionsToDB(final Map<Integer, Subscription> subscriptions, final Connection con, final int uid, final int cid) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_SUBSCRIPTION);
            for (final Subscription subscription : subscriptions.values()) {
                GenconfCopyTool.writeAttributesToDB(subscription.getBoolAttributes(), con, subscription.getConfigId(), cid, ConfAttribute.BOOL);
                GenconfCopyTool.writeAttributesToDB(subscription.getStringAttributes(), con, subscription.getConfigId(), cid, ConfAttribute.STRING);

                int i = 1;
                stmt.setInt(i++, subscription.getId());
                stmt.setInt(i++, cid);
                stmt.setInt(i++, uid);
                stmt.setInt(i++, subscription.getConfigId());
                stmt.setString(i++, subscription.getSourceId());
                stmt.setString(i++, subscription.getFolderId());
                stmt.setLong(i++, subscription.getLastUpdate());
                stmt.setInt(i++, subscription.getEnabled());
                stmt.setLong(i++, subscription.getCreated());
                stmt.setLong(i++, subscription.getLastModified());

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private void exchangeSubscriptionIds(final Map<Integer, Subscription> subscriptions, final Connection con, final Context ctx, final ObjectMapping<FolderObject> folderMapping) throws OXException {
        for (final Subscription subscription : subscriptions.values()) {
            try {
                final int id = IDGenerator.getId(ctx, Types.SUBSCRIPTION, con);
                final int configId = IDGenerator.getId(ctx, Types.GENERIC_CONFIGURATION, con);
                subscription.setId(id);
                subscription.setConfigId(configId);
                final String oldFolderId = subscription.getFolderId();
                try {
                    final int parsed = Integer.parseInt(oldFolderId);
                    final FolderObject sourceFolder = folderMapping.getSource(parsed);
                    if (sourceFolder != null) {
                        final FolderObject destinationFolder = folderMapping.getDestination(sourceFolder);
                        subscription.setFolderId(String.valueOf(destinationFolder.getObjectID()));
                    }
                } catch (final NumberFormatException e) {
                    // Keep the old folder
                }
            } catch (final SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            }
        }
    }

    void setAccountIdsForSubscriptions(final Map<Integer, Subscription> subscriptions, final ObjectMapping<Integer> accountMapping) {
        for (final Subscription subscription : subscriptions.values()) {
            final List<ConfAttribute> stringAttributes = subscription.getStringAttributes();
            for (final ConfAttribute attribute : stringAttributes) {
                if (attribute.getName() != null && attribute.getName().equals("account")) {
                    try {
                        final int oldId = Integer.parseInt(attribute.getValue());
                        final Integer newId = accountMapping.getDestination(I(oldId));
                        if (newId != null) {
                            attribute.setValue(String.valueOf(newId));
                        }
                    } catch (final NumberFormatException e) {
                        // Skip this one
                    }

                    break;
                }
            }
        }
    }

    Map<Integer, Subscription> loadSubscriptionsFromDB(final Connection con, final int uid, final int cid) throws OXException {
        final Map<Integer, Subscription> subscriptions = new HashMap<Integer, Subscription>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT_SUBSCRIPTIONS);
            stmt.setInt(1, cid);
            stmt.setInt(2, uid);

            rs = stmt.executeQuery();
            while (rs.next()) {
                int i = 1;
                final int id = rs.getInt(i++);
                final Subscription subscription = new Subscription();
                subscription.setId(id);
                subscription.setConfigId(rs.getInt(i++));
                subscription.setSourceId(rs.getString(i++));
                subscription.setFolderId(rs.getString(i++));
                subscription.setLastUpdate(rs.getLong(i++));
                subscription.setEnabled(rs.getInt(i++));
                subscription.setCreated(rs.getLong(i++));
                subscription.setLastModified(rs.getLong(i++));

                subscriptions.put(id, subscription);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return subscriptions;
    }

    void fillSubscriptionsWithAttributes(final Map<Integer, Subscription> subscriptions, final Connection con, final int cid) throws OXException {
        for (final Subscription subscription : subscriptions.values()) {
            final List<ConfAttribute> boolAttributes = GenconfCopyTool.loadAttributesFromDB(con, subscription.getConfigId(), cid, ConfAttribute.BOOL);
            final List<ConfAttribute> stringAttributes = GenconfCopyTool.loadAttributesFromDB(con, subscription.getConfigId(), cid, ConfAttribute.STRING);
            subscription.setBoolAttributes(boolAttributes);
            subscription.setStringAttributes(stringAttributes);
        }
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
