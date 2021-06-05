/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.userfeedback.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.validate.ParameterValidator;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.FeedbackStoreListener;
import com.openexchange.userfeedback.FeedbackType;
import com.openexchange.userfeedback.FeedbackTypeRegistry;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
import com.openexchange.userfeedback.export.ExportResultConverter;
import com.openexchange.userfeedback.filter.FeedbackFilter;
import com.openexchange.userfeedback.osgi.Services;

/**
 * {@link FeedbackServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class FeedbackServiceImpl implements FeedbackService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FeedbackServiceImpl.class);
    
    private final ServiceSet<FeedbackStoreListener> storeListeners;
    
    /**
     * Initializes a new {@link FeedbackServiceImpl}.
     */
    public FeedbackServiceImpl(ServiceSet<FeedbackStoreListener> storeListeners) {
        this.storeListeners = storeListeners;
    }

    @Override
    public void store(Session session, Object feedback, Map<String, String> params) throws OXException {
        ParameterValidator.checkObject(params);
        ParameterValidator.checkObject(feedback);

        String type = params.get("type");
        ParameterValidator.checkString(type);

        // Get type service
        FeedbackTypeRegistry registry = FeedbackTypeRegistryImpl.getInstance();
        FeedbackType feedBackType = registry.getFeedbackType(type);

        LeanConfigurationService leanConfig = Services.getService(LeanConfigurationService.class);
        if (leanConfig == null) {
            throw ServiceExceptionCode.absentService(LeanConfigurationService.class);
        }
        String configuredTypeForUser = leanConfig.getProperty(session.getUserId(), session.getContextId(), UserFeedbackProperty.mode);
        if (feedBackType == null || !type.equalsIgnoreCase(configuredTypeForUser)) {
            throw FeedbackExceptionCodes.INVALID_FEEDBACK_TYPE.create(type);
        }

        DatabaseService dbService = Services.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }
        if (!dbService.isGlobalDatabaseAvailable()) {
            throw FeedbackExceptionCodes.GLOBAL_DB_NOT_CONFIGURED.create();
        }

        String hostname = params.get("hostname");
        hostname = hostname != null ? hostname : "";

        String serverVersion = "";
        if (Strings.isNotEmpty(hostname)) {
            ServerConfigService serverConfigService = Services.getService(ServerConfigService.class);
            if (serverConfigService != null) {
                ServerConfig serverConfig = serverConfigService.getServerConfig(hostname, session);
                if (serverConfig != null) {
                    serverVersion = serverConfig.getServerVersion();
                }
            }
        }
        String uiVersion = "";
        if (feedback instanceof JSONObject) {
            JSONObject jsonFeedback = (JSONObject) feedback;
            if (jsonFeedback.has("client_version")) {
                try {
                    uiVersion = jsonFeedback.getString("client_version");
                } catch (JSONException e) {
                    LOG.warn("Unable to retrieve client version.", e);
                }
            }
        } else {
            LOG.debug("Unable to retrieve ui version as provided feedback is from type {}.", feedback.getClass().getName());
        }

        // Get context group id
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        if (factory == null) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        String contextGroupId = view.opt("com.openexchange.context.group", String.class, null);
        Connection writeCon = dbService.getWritableForGlobal(contextGroupId);
        FeedbackMetaData metaData = null;
        int rollback = 0;
        try {
            // Store feedback and feedback metadata
            writeCon.setAutoCommit(false);
            rollback = 1;

            long fid = feedBackType.storeFeedback(feedback, writeCon);
            if (fid <= 0) {
                writeCon.rollback();
                throw FeedbackExceptionCodes.UNEXPECTED_ERROR.create("Unable to store feedback metadata.");
            }

            // @formatter:off
            metaData = FeedbackMetaData.builder()
                            .setCtxId(session.getContextId())
                            .setDate(System.currentTimeMillis())
                            .setLoginName(session.getLoginName())
                            .setServerVersion(serverVersion)
                            .setUiVersion(uiVersion)
                            .setType(type)
                            .setTypeId(fid)
                            .setUserId(session.getUserId())
                            .build();
            // @formatter:on

            saveFeedBackInternal(writeCon, metaData, contextGroupId == null ? "default" : contextGroupId);
            writeCon.commit();
            rollback = 2;
        } catch (SQLException e) {
            LOG.error("Unable to store feedback data.", e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(writeCon);
                }
                Databases.autocommit(writeCon);
            }
            dbService.backWritableForGlobal(contextGroupId, writeCon);
        }
        if (rollback == 2 && metaData != null) {
            notifyStoreListeners(session, feedBackType, feedback, metaData);
        }
    }

    private static final String INSERT_FEEDBACK_SQL = "INSERT INTO feedback (groupId, type, date, cid, user, login_name, typeId, client_version, server_version) VALUES (?,?,?,?,?,?,?,?,?);";
    private static final String SELECT_FEEDBACK_SQL = "SELECT date, cid, user, login_name, typeId, client_version, server_version FROM feedback WHERE groupId=? AND type=? AND date >? AND date <?";
    private static final String DELETE_FEEDBACK_SQL = "DELETE FROM feedback WHERE groupId = ? AND type = ? AND date > ? AND date < ?";
    private static final String TYPEID_FEEDBACK_SQL = "SELECT typeId FROM feedback WHERE groupId = ? AND type = ? AND date > ? AND date < ?";

    /**
     * @param writeCon The global db write connection
     * @param feedback The feedback to persist
     * @param groupId The global db group id assigned to the context
     * @throws SQLException
     */
    protected void saveFeedBackInternal(Connection writeCon, FeedbackMetaData feedbackMetaData, String groupId) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = writeCon.prepareStatement(INSERT_FEEDBACK_SQL);
            statement.setString(1, groupId);
            statement.setString(2, feedbackMetaData.getType());
            statement.setLong(3, feedbackMetaData.getDate());
            statement.setInt(4, feedbackMetaData.getCtxId());
            statement.setInt(5, feedbackMetaData.getUserId());
            String loginName = feedbackMetaData.getLoginName();
            if (loginName == null) {
                loginName = "";
            }
            statement.setString(6, loginName);
            statement.setLong(7, feedbackMetaData.getTypeId());
            statement.setString(8, feedbackMetaData.getUiVersion());
            statement.setString(9, feedbackMetaData.getServerVersion());
            statement.execute();
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    @Override
    public ExportResultConverter export(String ctxGroup, FeedbackFilter filter) throws OXException {
        return export(ctxGroup, filter, Collections.<String, String> emptyMap());
    }

    @Override
    public ExportResultConverter export(String ctxGroup, FeedbackFilter filter, Map<String, String> configuration) throws OXException {
        ParameterValidator.checkString(ctxGroup);
        ParameterValidator.checkObject(filter);

        FeedbackTypeRegistry registry = FeedbackTypeRegistryImpl.getInstance();
        FeedbackType feedBackType = registry.getFeedbackType(filter.getType());

        if (feedBackType == null) {
            throw FeedbackExceptionCodes.INVALID_FEEDBACK_TYPE.create(filter.getType());
        }

        DatabaseService dbService = Services.getService(DatabaseService.class);
        if (!dbService.isGlobalDatabaseAvailable()) {
            throw FeedbackExceptionCodes.GLOBAL_DB_NOT_CONFIGURED.create();
        }

        Connection readCon = dbService.getReadOnlyForGlobal(ctxGroup);
        try {

            List<FeedbackMetaData> metaDataList = loadFeedbackMetaData(readCon, filter, ctxGroup);

            List<FeedbackMetaData> filteredFeedback = new ArrayList<>();
            for (FeedbackMetaData meta : metaDataList) {
                if (filter.accept(meta)) {
                    filteredFeedback.add(meta);
                }
            }

            return feedBackType.getFeedbacks(filteredFeedback, readCon, configuration);
        } catch (SQLException e) {
            throw FeedbackExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            dbService.backReadOnlyForGlobal(ctxGroup, readCon);
        }
    }

    protected List<FeedbackMetaData> loadFeedbackMetaData(Connection readCon, FeedbackFilter filter, String groupId) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = readCon.prepareStatement(SELECT_FEEDBACK_SQL);
            statement.setString(1, groupId);
            statement.setString(2, filter.getType());
            statement.setLong(3, filter.start());
            statement.setLong(4, filter.end());
            ResultSet resultSet = statement.executeQuery();
            List<FeedbackMetaData> result = new ArrayList<>();
            while (resultSet.next()) {
                FeedbackMetaData data = FeedbackMetaData.builder().setType(filter.getType()).setDate(resultSet.getLong(1)).setCtxId(resultSet.getInt(2)).setUserId(resultSet.getInt(3)).setLoginName(resultSet.getString(4)).setTypeId(resultSet.getLong(5)).setUiVersion(resultSet.getString(6)).setServerVersion(resultSet.getString(7)).build();
                result.add(data);
            }
            return result;
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    @Override
    public void delete(String ctxGroup, FeedbackFilter filter) throws OXException {
        FeedbackTypeRegistry registry = FeedbackTypeRegistryImpl.getInstance();
        FeedbackType feedBackType = registry.getFeedbackType(filter.getType());

        if (feedBackType == null) {
            throw FeedbackExceptionCodes.INVALID_FEEDBACK_TYPE.create(filter.getType());
        }

        DatabaseService dbService = Services.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        if (!dbService.isGlobalDatabaseAvailable()) {
            throw FeedbackExceptionCodes.GLOBAL_DB_NOT_CONFIGURED.create();
        }

        Connection writeCon = dbService.getWritableForGlobal(ctxGroup);
        int rollback = 0;
        try {
            writeCon.setAutoCommit(false);
            rollback = 1;

            List<Long> typeIdsToDelete = getTypeIds(ctxGroup, filter, writeCon);
            if (typeIdsToDelete.size() > 0) {
                feedBackType.deleteFeedbacks(typeIdsToDelete, writeCon);
                deleteFeedback(writeCon, feedBackType, filter, ctxGroup);
            }

            writeCon.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw FeedbackExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(writeCon);
                }
                Databases.autocommit(writeCon);
            }
            dbService.backWritableForGlobal(ctxGroup, writeCon);
        }
    }

    private List<Long> getTypeIds(String ctxGroup, FeedbackFilter filter, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(TYPEID_FEEDBACK_SQL);
            stmt.setString(1, ctxGroup);
            stmt.setString(2, filter.getType());
            stmt.setLong(3, filter.start());
            stmt.setLong(4, filter.end());
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Long> results = new ArrayList<>();
            do {
                results.add(Long.valueOf(rs.getLong("typeId")));
            } while (rs.next());
            return results;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void deleteFeedback(Connection writeCon, FeedbackType feedbackType, FeedbackFilter filter, String ctxGroup) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(DELETE_FEEDBACK_SQL);
            stmt.setString(1, ctxGroup);
            stmt.setString(2, feedbackType.getType());
            stmt.setLong(3, filter.start());
            stmt.setLong(4, filter.end());
            stmt.execute();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Notifies {@link FeedbackStoreListener}s after a feedback was successfully stored
     *
     * @param session The user session
     * @param feedBackType The feedback type
     * @param feedback The stored feedback
     * @param metaData The feedback metadata
     */
    private void notifyStoreListeners(Session session, FeedbackType feedBackType, Object feedback, FeedbackMetaData metaData) {
        for (FeedbackStoreListener storeListener : storeListeners) {
            Runnable notifyStoreListener = () -> {
                try {
                    storeListener.onAfterStore(session, feedBackType, feedback, metaData);
                } catch (Exception e) {
                    LOG.warn("Unexpected error while notifying listener {}", storeListener, e);
                }
            };
            ThreadPools.submitElseExecute(ThreadPools.task(notifyStoreListener));
        }
    }
    
}
