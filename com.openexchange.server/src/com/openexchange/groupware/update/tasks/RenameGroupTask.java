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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.I18nServices;

/**
 * This implementation of an {@link UpdateTask} renames the group with identifier 1 to "Standard group" or the translated name for according
 * locale of the context administrator.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class RenameGroupTask extends UpdateTaskAdapter {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(RenameGroupTask.class));

    /**
     * Initializes a new {@link RenameGroupTask}.
     */
    public RenameGroupTask() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 52;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    private static final String[] DEPENDENCIES = { ContactsAddUseCountColumnUpdateTask.class.getName() };

    @Override
    public String[] getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            con.setAutoCommit(false);
            int[] ctxIds = Database.getContextsInSameSchema(contextId);
            ProgressState state = params.getProgressState();
            state.setTotal(ctxIds.length);
            for (int context : ctxIds) {
                String adminLanguage = getContextAdminLanguage(con, context);
                String groupName = I18nServices.getInstance().translate(adminLanguage, Groups.STANDARD_GROUP);
                updateGroupName(con, context, groupName);
                state.incrementState();
            }
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    private void updateGroupName(Connection con, int context, String groupName) {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE groups SET displayName=? WHERE cid=? AND id=1");
            stmt.setString(1, groupName);
            stmt.setInt(2, context);
            int count = stmt.executeUpdate();
            if (1 != count) {
                LOG.warn("Was not able to update display name of standard group.");
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private String getContextAdminLanguage(Connection con, int contextId) {
        PreparedStatement stmt = null;
        ResultSet result = null;
        String language = "en_US";
        try {
            stmt = con.prepareStatement("SELECT u.preferredLanguage FROM user u JOIN user_setting_admin a ON u.cid=a.cid AND u.id=a.user WHERE a.cid=?");
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();
            if (result.next()) {
                language = result.getString(1);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return language;
    }
}
