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

package com.openexchange.userfeedback.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.validate.ParameterValidator;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.FeedbackMetaData.Builder;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.FeedbackType;
import com.openexchange.userfeedback.FeedbackTypeRegistry;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
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

    @Override
    public void store(Session session, Object feedback, Map<String, String> params) throws OXException {
        ParameterValidator.checkObject(params);
        ParameterValidator.checkObject(feedback);

        // Get context group id
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        if (factory == null) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        String contextGroupId = view.opt("com.openexchange.context.group", String.class, null);

        String type = params.get("type");
        ParameterValidator.checkString(type);

        // Get type service
        FeedbackTypeRegistry registry = FeedbackTypeRegistryImpl.getInstance();
        FeedbackType feedBackType = registry.getFeedbackType(type);

        if (feedBackType == null) {
            throw FeedbackExceptionCodes.INVALID_FEEDBACK_TYPE.create(type);
        }

        DatabaseService dbService = Services.getService(DatabaseService.class);
        if (!dbService.isGlobalDatabaseAvailable()) {
            throw FeedbackExceptionCodes.GLOBAL_DB_NOT_CONFIGURED.create();
        }

        String hostname = params.get("hostname");
        hostname = hostname != null ? hostname : "";

        String serverVersion = "";
        if (Strings.isNotEmpty(hostname)) {
            ServerConfigService serverConfigService = Services.getService(ServerConfigService.class);
            ServerConfig serverConfig = serverConfigService.getServerConfig(hostname, session);
            if (serverConfig != null) {
                serverVersion = serverConfig.getServerVersion();
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

        Connection writeCon = dbService.getWritableForGlobal(contextGroupId);
        boolean rollback = false;
        try {
            // Store feedback and feedback metadata
            writeCon.setAutoCommit(false);
            rollback = true;

            long fid = feedBackType.storeFeedback(feedback, writeCon);
            if (fid <= 0) {
                writeCon.rollback();
                throw FeedbackExceptionCodes.UNEXPECTED_ERROR.create("Unable to store feedback metadata.");
            }

            Builder builder = FeedbackMetaData.builder().setCtxId(session.getContextId()).setDate(System.currentTimeMillis()).setLoginName(session.getLoginName()).setServerVersion(serverVersion).setUiVersion(uiVersion).setType(type).setTypeId(fid).setUserId(session.getUserId());

            saveFeedBackInternal(writeCon, builder.build(), contextGroupId == null ? "default" : contextGroupId);
            writeCon.commit();
            rollback = false;
        } catch (SQLException e) {
            LOG.error("Unable to store feedback data.", e);
        } finally {
            if (rollback) {
                Databases.rollback(writeCon);
            }
            Databases.autocommit(writeCon);
            dbService.backWritableForGlobal(contextGroupId, writeCon);
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
            DBUtils.closeSQLStuff(statement);
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
            throw FeedbackExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
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
            DBUtils.closeSQLStuff(statement);
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
        boolean rollback = false;
        try {
            writeCon.setAutoCommit(false);
            rollback = true;

            List<Long> typeIdsToDelete = getTypeIds(ctxGroup, filter, writeCon);
            if (typeIdsToDelete.size() > 0) {
                feedBackType.deleteFeedbacks(typeIdsToDelete, writeCon);
                deleteFeedback(writeCon, feedBackType, filter, ctxGroup);
            }

            writeCon.commit();
            rollback = false;
        } catch (SQLException e) {
            throw FeedbackExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            if (rollback) {
                DBUtils.rollback(writeCon);
            }
            DBUtils.autocommit(writeCon);
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
            stmt.setLong(3, filter.start().longValue());
            stmt.setLong(4, filter.end().longValue());
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Long> results = new LinkedList<>();
            do {
                results.add(Long.valueOf(rs.getLong("typeId")));
            } while (rs.next());
            return results;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
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
            DBUtils.closeSQLStuff(stmt);
        }
    }
}
