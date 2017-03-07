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
import java.util.List;
import org.json.JSONObject;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.validate.ParameterValidator;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.FeedbackMetaData;
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

    @Override
    public void store(Session session, String type, JSONObject feedback) throws OXException {
        ParameterValidator.checkString(type);
        ParameterValidator.checkJSON(feedback);

        // Get context group id
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        if (factory == null) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        String contextGroupId = view.opt("com.openexchange.context.group", String.class, null);

        // Get type service
        FeedbackTypeRegistry registry = FeedbackTypeRegistryImpl.getInstance();
        FeedbackType feedBackType = registry.getFeedbackType(type);

        if (feedBackType == null) {
            throw FeedbackExceptionCodes.INVALID_FEEDBACK_TYPE.create();
        }

        DatabaseService dbService = Services.getService(DatabaseService.class);
        if (!dbService.isGlobalDatabaseAvailable()) {
            throw FeedbackExceptionCodes.GLOBAL_DB_NOT_CONFIGURED.create();
        }

        Connection writeCon = null;
        try {
            writeCon = dbService.getWritableForGlobal(contextGroupId);
            // Store feedback and feedback metadata
            writeCon.setAutoCommit(false);
            long fid = feedBackType.storeFeedback(feedback, writeCon);
            if (fid <= 0) {
                writeCon.rollback();
                throw FeedbackExceptionCodes.UNEXPECTED_ERROR.create("Unable to store feedback metadata.");
            }
            saveFeedBackInternal(writeCon, session.getUserId(), session.getContextId(), contextGroupId == null ? "default" : contextGroupId, session.getLoginName(), System.currentTimeMillis(), type, fid);
            writeCon.commit();
        } catch (SQLException e) {
            try {
                writeCon.rollback();
            } catch (SQLException sqlEx) {
                // ignore
            }
        } finally {
            if (writeCon != null) {
                DBUtils.autocommit(writeCon);
                dbService.backWritableForGlobal(contextGroupId, writeCon);
            }
        }
    }

    private static final String INSERT_FEEDBACK_SQL = "INSERT INTO feedback (groupId, type, date, cid, user, login_name, typeId) VALUES (?,?,?,?,?,?,?);";
    private static final String SELECT_FEEDBACK_SQL = "SELECT date, cid, user, login_name, typeId FROM feedback WHERE groupId=? AND type=? AND date >? AND date <?";
    private static final String DELETE_FEEDBACK_SQL = "DELETE FROM feedback WHERE groupId = ? AND type = ? AND date > ? AND date < ?";
    private static final String TYPEID_FEEDBACK_SQL = "SELECT typeId FROM feedback WHERE groupId = ? AND type = ? AND date > ? AND data < ?";

    /**
     * @param writeCon The global db write connection
     * @param userId The user id
     * @param contextId The context id
     * @param loginName The login name
     * @param date The time of the feedback
     * @throws SQLException
     */
    protected void saveFeedBackInternal(Connection writeCon, int userId, int contextId, String groupId, String loginName, long date, String type, long feedbackId) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = writeCon.prepareStatement(INSERT_FEEDBACK_SQL);
            statement.setString(1, groupId);
            statement.setString(2, type);
            statement.setLong(3, date);
            statement.setInt(4, contextId);
            statement.setInt(5, userId);
            if (loginName == null) {
                loginName = "";
            }
            statement.setString(6, loginName);
            statement.setLong(7, feedbackId);
            statement.execute();
        } finally {
            DBUtils.closeSQLStuff(statement);
        }
    }

    @Override
    public ExportResultConverter export(String ctxGroup, FeedbackFilter filter) throws OXException {
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

        Connection readCon = null;
        try {
            readCon = dbService.getReadOnlyForGlobal(ctxGroup);

            List<FeedbackMetaData> metaDataList = loadFeedbackMetaData(readCon, filter, ctxGroup);

            List<FeedbackMetaData> filteredFeedback = new ArrayList<>();
            for (FeedbackMetaData meta : metaDataList) {
                if (filter.accept(meta)) {
                    filteredFeedback.add(meta);
                }
            }

            return feedBackType.getFeedbacks(filteredFeedback, readCon);
        } catch (SQLException e) {
            throw FeedbackExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        } finally {
            if (readCon != null) {
                dbService.backReadOnlyForGlobal(ctxGroup, readCon);
            }
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
                result.add(new FeedbackMetaData(filter.getType(), resultSet.getLong(1), resultSet.getInt(2), resultSet.getInt(3), resultSet.getString(4), resultSet.getLong(5)));
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
            throw FeedbackExceptionCodes.INVALID_FEEDBACK_TYPE.create();
        }

        DatabaseService dbService = Services.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        if (!dbService.isGlobalDatabaseAvailable()) {
            throw FeedbackExceptionCodes.GLOBAL_DB_NOT_CONFIGURED.create();
        }

        Connection writeCon = null;
        try {
            writeCon = dbService.getWritableForGlobal(ctxGroup);
            writeCon.setAutoCommit(false);
            List<Long> typeIdsToDelete = getTypeIds(ctxGroup, filter, writeCon);
            feedBackType.deleteFeedbacks(typeIdsToDelete, writeCon);
            deleteFeedback(writeCon, feedBackType, filter, ctxGroup);
            writeCon.commit();
        } catch (SQLException e) {
            try {
                writeCon.rollback();
            } catch (SQLException x) {
                // ignore
            }
            throw FeedbackExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (null != writeCon) {
                DBUtils.autocommit(writeCon);
                dbService.backWritableForGlobal(ctxGroup, writeCon);
            }
        }
    }

    private List<Long> getTypeIds(String ctxGroup, FeedbackFilter filter, Connection con) throws SQLException {
        List<Long> result = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(TYPEID_FEEDBACK_SQL);
            stmt.setString(1, ctxGroup);
            stmt.setString(2, filter.getType());
            stmt.setLong(3, filter.start());
            stmt.setLong(4, filter.end());
            rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong("typeId"));
            }
            return result;
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
