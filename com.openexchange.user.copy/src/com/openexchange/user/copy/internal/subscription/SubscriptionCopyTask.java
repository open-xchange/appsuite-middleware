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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.user.copy.internal.CopyTools.getIntOrNegative;
import static com.openexchange.user.copy.internal.CopyTools.replaceIdsInQuery;
import static com.openexchange.user.copy.internal.CopyTools.setIntOrNull;
import static com.openexchange.user.copy.internal.CopyTools.setStringOrNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
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
   
   private static final String SELECT_ATTRIBUTES = 
       "SELECT " +
           "name, value " +
       "FROM " +
           "genconf_attributes_#TYPE# " +
       "WHERE " +
           "cid = ? " +
       "AND " +
           "id = ?";
   
   private static final String SELECT_ACCOUNTS =
       "SELECT " +
           "id, displayName, accessToken, accessSecret, " +
           "serviceId FROM oauthAccounts " +
       "WHERE " +
           "cid = ? " +
       "AND " +
           "user = ? " +
       "AND " +
           "id IN (#IDS#)";
   
   private static final String INSERT_ACCOUNTS =
       "INSERT INTO " +
           "oauthAccounts " +
           "(cid, user, id, displayName, accessToken, accessSecret, serviceId) " +
       "VALUES " +
           "(?, ?, ?, ?, ?, ?, ?)";
   
   private static final String INSERT_ATTRIBUTES =
       "INSERT INTO " +
           "genconf_attributes_#TYPE# " +
           "(cid, id, name, value) " +
       "VALUES " +
           "(?, ?, ?, ?)";
   
   private static final String INSERT_SUBSCRIPTION =
       "INSERT INTO " +
           "subscriptions " +
           "(id, cid, user_id, configuration_id, source_id, folder_id, last_update, enabled, created, lastModified) " +
       "VALUES " +
           "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

   private final IDGeneratorService idService;
    
   
   public SubscriptionCopyTask(final IDGeneratorService idService) {
       super();
       this.idService = idService;
   }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    public String getObjectName() {
        return "subscription";
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
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
                final Map<Integer, Subscription> subscriptions = loadSubscriptionsFromDB(srcCon, i(srcUsrId), i(srcCtxId));
                fillSubscriptionsWithAttributes(subscriptions, srcCon, i(srcCtxId));
                
                final boolean hasOAuth = DBUtils.tableExists(srcCon, "oauthAccounts");
                if (hasOAuth) {
                    final List<OAuthAccount> accounts = loadOAuthAccountsFromDB(subscriptions, srcCon, i(srcUsrId), i(srcCtxId));
                    final Map<Integer, Integer> oAuthMapping = exchangeOAuthIds(dstCon, accounts, dstCtx);                    
                    writeOAuthAccountsToDB(accounts, dstCon, i(dstCtxId), i(dstUsrId));
                    setAccountIdsForSubscriptions(subscriptions, oAuthMapping);
                }                
                                
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
                writeAttributesToDB(subscription.getBoolAttributes(), con, subscription.getConfigId(), cid, ConfAttribute.BOOL);
                writeAttributesToDB(subscription.getStringAttributes(), con, subscription.getConfigId(), cid, ConfAttribute.STRING);
                
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
    
    private void writeAttributesToDB(final List<ConfAttribute> attributes, final Connection con, final int id, final int cid, final int type) throws OXException {
        final String sql = replaceTableInAttributeStatement(INSERT_ATTRIBUTES, type);
        PreparedStatement stmt = null;
        try {            
            stmt = con.prepareStatement(sql);
            for (final ConfAttribute attribute : attributes) {
                int i = 1;
                stmt.setInt(i++, cid);
                stmt.setInt(i++, id);
                setStringOrNull(i++, stmt, attribute.getName());
                if (type == ConfAttribute.BOOL) {
                    setIntOrNull(i++, stmt, Integer.parseInt(attribute.getValue()));
                } else {
                    setStringOrNull(i++, stmt, attribute.getValue());
                }
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
    
    private void writeOAuthAccountsToDB(final List<OAuthAccount> accounts, final Connection con, final int cid, final int uid) throws OXException {
        PreparedStatement stmt = null;
        try {            
            stmt = con.prepareStatement(INSERT_ACCOUNTS);
            for (final OAuthAccount account : accounts) {
                int i = 1;
                stmt.setInt(i++, cid);
                stmt.setInt(i++, uid);
                stmt.setInt(i++, account.getId());
                stmt.setString(i++, account.getDisplayName());
                stmt.setString(i++, account.getAccessToken());
                stmt.setString(i++, account.getAccessSecret());
                stmt.setString(i++, account.getServiceId());
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }
    
    private Map<Integer, Integer> exchangeOAuthIds(final Connection con, final List<OAuthAccount> accounts, final Context ctx) throws OXException {
        final Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();
        for (final OAuthAccount account : accounts) {
            final int oldId = account.getId();
            final int newId = idService.getId("com.openexchange.oauth.account", ctx.getContextId());
            account.setId(newId);
            
            mapping.put(oldId, newId);
        }
        
        return mapping;
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
    
    List<OAuthAccount> loadOAuthAccountsFromDB(final Map<Integer, Subscription> subscriptions, final Connection con, final int uid, final int cid) throws OXException {
        final List<OAuthAccount> accounts = new ArrayList<OAuthAccount>();
        final List<Integer> ids = prepareAccountIds(subscriptions);
        if (!ids.isEmpty()) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                final String sql = replaceIdsInQuery("#IDS#", SELECT_ACCOUNTS, ids);
                stmt = con.prepareStatement(sql);
                stmt.setInt(1, cid);
                stmt.setInt(2, uid);

                rs = stmt.executeQuery();
                while (rs.next()) {
                    int i = 1;
                    final OAuthAccount account = new OAuthAccount();
                    account.setId(rs.getInt(i++));
                    account.setDisplayName(rs.getString(i++));
                    account.setAccessToken(rs.getString(i++));
                    account.setAccessSecret(rs.getString(i++));
                    account.setServiceId(rs.getString(i++));

                    accounts.add(account);
                }
            } catch (final SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
        }
        return accounts;
    }
    
    List<Integer> prepareAccountIds(final Map<Integer, Subscription> subscriptions) {
        final List<Integer> ids = new ArrayList<Integer>();
        for (final Subscription subscription : subscriptions.values()) {
            final List<ConfAttribute> stringAttributes = subscription.getStringAttributes();
            for (final ConfAttribute attribute : stringAttributes) {
                if (attribute.getName() != null && attribute.getName().equals("account")) {
                    try {
                        final int id = Integer.parseInt(attribute.getValue());
                        ids.add(id);
                    } catch (final NumberFormatException e) {
                        // Skip this one
                    }
                    
                    break;
                }
            }
        }
        
        return ids;
    }
    
    void setAccountIdsForSubscriptions(final Map<Integer, Subscription> subscriptions, final Map<Integer, Integer> accountMapping) {
        for (final Subscription subscription : subscriptions.values()) {
            final List<ConfAttribute> stringAttributes = subscription.getStringAttributes();
            for (final ConfAttribute attribute : stringAttributes) {
                if (attribute.getName() != null && attribute.getName().equals("account")) {
                    try {
                        final int oldId = Integer.parseInt(attribute.getValue());
                        final Integer newId = accountMapping.get(oldId);
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
            final List<ConfAttribute> boolAttributes = loadAttributesFromDB(con, subscription.getConfigId(), cid, ConfAttribute.BOOL);
            final List<ConfAttribute> stringAttributes = loadAttributesFromDB(con, subscription.getConfigId(), cid, ConfAttribute.STRING);
            subscription.setBoolAttributes(boolAttributes);
            subscription.setStringAttributes(stringAttributes);
        }
    }
    
    List<ConfAttribute> loadAttributesFromDB(final Connection con, final int id, final int cid, final int type) throws OXException {
        final List<ConfAttribute> attributes = new ArrayList<ConfAttribute>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final boolean isBool = (type == ConfAttribute.BOOL);
            final String sql = replaceTableInAttributeStatement(SELECT_ATTRIBUTES, type);
            
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, cid);
            stmt.setInt(2, id);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                final ConfAttribute attribute = new ConfAttribute(type);
                attribute.setName(rs.getString(1));
                if (isBool) {
                    attribute.setValue(String.valueOf(getIntOrNegative(2, rs)));
                } else {
                    attribute.setValue(rs.getString(2));
                }
                
                attributes.add(attribute);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
        
        return attributes;
    }
    
    String replaceTableInAttributeStatement(final String statement, final int type) {
        final boolean isBool = (type == ConfAttribute.BOOL);
        final String sql;
        if (isBool) {
            sql = statement.replaceFirst("#TYPE#", "bools");
        } else {                
            sql = statement.replaceFirst("#TYPE#", "strings");
        }
        
        return sql;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
