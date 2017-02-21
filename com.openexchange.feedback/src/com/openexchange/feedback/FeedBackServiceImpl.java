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

package com.openexchange.feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.GlobalDatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.feedback.exception.FeedbackExceptionCodes;
import com.openexchange.feedback.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FeedBackServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class FeedBackServiceImpl implements FeedbackService {

    @Override
    public void storeFeedback(Session session, String type, Object feedback) throws OXException {

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

        // Get db connection
        GlobalDatabaseService dbService = Services.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        Connection writeCon = dbService.getWritableForGlobal(contextGroupId);
        try {
            // Store feedback and feedback metadata
            writeCon.setAutoCommit(false);
            long fid = feedBackType.storeFeedback(feedback, writeCon);
            if(fid<=0){
                writeCon.rollback();
                throw FeedbackExceptionCodes.UNEXPECTED_ERROR.create("Unable to store feedback metadata.");
            }
            saveFeedBackInternal(writeCon, session.getUserId(), session.getContextId(), contextGroupId == null ? "default" : contextGroupId, session.getLoginName(), System.currentTimeMillis(), type, fid);
            writeCon.commit();
        } catch (SQLException e) {
            try{
                writeCon.rollback();
            }catch (SQLException sqlEx) {
                // ignore
            }
        } finally {
            if (writeCon != null) {
                DBUtils.autocommit(writeCon);
                dbService.backWritableForGlobal(contextGroupId, writeCon);
            }
        }
    }


    private static final String SAVE_FEEDBACK_SQL = "INSERT INTO feedback (groupId, type, date, cid, user, login_name, typeId) VALUES (?,?,?,?,?,?,?);";

    /**
     * @param writeCon The global db write connection
     * @param userId The user id
     * @param contextId The context id
     * @param loginName The login name
     * @param date The time of the feedback
     * @throws SQLException
     */
    private void saveFeedBackInternal(Connection writeCon, int userId, int contextId, String groupId, String loginName, long date, String type, long feedbackId) throws SQLException {
        PreparedStatement statement = writeCon.prepareStatement(SAVE_FEEDBACK_SQL);
        try {
            statement.setString(1, groupId);
            statement.setString(2, type);
            statement.setLong(3, date);
            statement.setInt(4, contextId);
            statement.setInt(5, userId);
            if(loginName==null){
                loginName="";
            }
            statement.setString(6, loginName);
            statement.setLong(7, feedbackId);
            statement.execute();
        } finally {
            DBUtils.closeSQLStuff(statement);
        }
    }
}
