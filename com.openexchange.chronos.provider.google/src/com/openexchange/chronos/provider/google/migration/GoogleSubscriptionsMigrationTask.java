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

package com.openexchange.chronos.provider.google.migration;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.provider.google.GoogleCalendarConfigField;
import com.openexchange.chronos.provider.google.GoogleCalendarProvider;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.Databases;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.WorkingLevel;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionStorage;

/**
 * {@link GoogleSubscriptionsMigrationTask}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleSubscriptionsMigrationTask extends UpdateTaskAdapter {

    private final static Logger LOG = LoggerFactory.getLogger(GoogleSubscriptionsMigrationTask.class);

    private static final String SQL_READ = " SELECT id, user_id, configuration_id, source_id, folder_id, last_update, created, enabled FROM subscriptions WHERE cid=? AND source_id='com.openexchange.subscribe.google.calendar'";

    @Override
    public void perform(PerformParameters params) throws OXException {

        GenericConfigurationStorageService storageService = Services.getService(GenericConfigurationStorageService.class);
        if (storageService == null) {
            throw ServiceExceptionCode.absentService(GenericConfigurationStorageService.class);
        }

        SubscriptionStorage subscriptionStorage = AbstractSubscribeService.STORAGE.get();
        if (subscriptionStorage == null) {
            throw ServiceExceptionCode.absentService(SubscriptionStorage.class);
        }

        Connection writeCon = params.getConnection();
        int[] contextsInSameSchema = params.getContextsInSameSchema();

        for (int ctxId : contextsInSameSchema) {

            /*
             * Step 1: Load existing subscriptions
             */
            ResultSet rs = null;
            PreparedStatement stmt = null;
            final List<Subscription> subscriptions = new LinkedList<Subscription>();
            Context ctx = new ContextImpl(ctxId);
            try {
                stmt = writeCon.prepareStatement(SQL_READ);
                stmt.setInt(1, ctxId);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    final Subscription subscription = new Subscription();
                    subscription.setFolderId(rs.getString("folder_id"));
                    subscription.setId(rs.getInt("id"));
                    subscription.setUserId(rs.getInt("user_id"));
                    subscription.setEnabled(rs.getBoolean("enabled"));
                    subscription.setContext(ctx);
                    final Map<String, Object> content = new HashMap<String, Object>();
                    storageService.fill(writeCon, subscription.getContext(), rs.getInt("configuration_id"), content);
                    subscription.setConfiguration(content);
                    subscriptions.add(subscription);
                }
            } catch (SQLException e) {
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }

            /*
             * Step 2: Create accounts for the existing subscriptions
             */
            Iterator<Subscription> iterator = subscriptions.iterator();
            try {
                writeCon.setAutoCommit(false);
                CalendarStorage calendarStorage = Services.getService(CalendarStorageFactory.class).create(ctx, -1, null);
                while (iterator.hasNext()) {
                    Subscription sub = iterator.next();
                    try {
                        JSONObject config = new JSONObject();
                        Object oauthAccount = sub.getConfiguration().get("account");
                        if (oauthAccount == null) {
                            // Skip bad configured subscriptions
                            iterator.remove();
                            continue;
                        }
                        config.put(GoogleCalendarConfigField.OAUTH_ID, oauthAccount);
                        config.put(GoogleCalendarConfigField.MIGRATED, true);
                        config.put(GoogleCalendarConfigField.OLD_FOLDER, sub.getFolderId());
                        int id = IDGenerator.getId(sub.getContext(), Types.SUBSCRIPTION, writeCon);
                        insertAccount(writeCon, ctxId, new DefaultCalendarAccount(GoogleCalendarProvider.PROVIDER_ID, id, sub.getUserId(), sub.isEnabled(), config, config, new Date()));
                        calendarStorage.getAccountStorage().invalidateAccount(sub.getUserId(), -1);
                    } catch (JSONException | SQLException e) {
                        LOG.error("Error during migration of google subscriptions. Subscription with id " + sub.getId() + " in context " + ctxId + " could not be migrated to a calendar account because of: " + e.getMessage());
                        // remove the subscription so it does not get deleted
                        iterator.remove();
                    } catch (OXException e) {
                        LOG.warn("Problem during migration of google subscriptions. Cache could not be invalidated for user with id " + sub.getUserId());
                    }
                }
                writeCon.commit();
            } catch (SQLException e) {
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e.getMessage());
            } finally {
                try {
                    writeCon.setAutoCommit(true);
                } catch (SQLException e) {
                    // ignore
                }
            }

            /*
             * Step 3: Remove old subscriptions
             */
            for (Subscription sub : subscriptions) {
                subscriptionStorage.forgetSubscription(sub);
            }
        }
    }

    private static int insertAccount(Connection connection, int cid, CalendarAccount account) throws SQLException {
        String sql = "INSERT INTO calendar_account (cid,id,provider,user,enabled,modified,internalConfig,userConfig) VALUES (?,?,?,?,?,?,?,?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            InputStream internalConfigStream = null;
            InputStream userConfigStream = null;
            try {
                internalConfigStream = serialize(account.getInternalConfiguration());
                userConfigStream = serialize(account.getUserConfiguration());
                stmt.setInt(1, cid);
                stmt.setInt(2, account.getAccountId());
                stmt.setString(3, account.getProviderId());
                stmt.setInt(4, account.getUserId());
                stmt.setBoolean(5, account.isEnabled());
                stmt.setLong(6, account.getLastModified().getTime());
                stmt.setBinaryStream(7, internalConfigStream);
                stmt.setBinaryStream(8, userConfigStream);
                return stmt.executeUpdate();
            } finally {
                Streams.close(internalConfigStream, userConfigStream);
            }
        }
    }

    /**
     * Serializes a JSON object (as used in an account's configuration) to an input stream.
     *
     * @param data The JSON object serialize, or <code>null</code>
     * @return The serialized JSON object, or <code>null</code> if the passed object was <code>null</code>
     */
    private static InputStream serialize(JSONObject data) {
        if (null == data) {
            return null;
        }
        return new JSONInputStream(data, Charsets.US_ASCII.name());
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING, WorkingLevel.SCHEMA);
    }

}
