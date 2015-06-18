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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.WorkingLevel;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DropStoredProceduresUpdateTask}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.8.0
 */
public class DropStoredProceduresUpdateTask extends UpdateTaskAdapter {

    // The list of stored procedures
    private final List<String> storedProcedures = Collections.unmodifiableList(Arrays.asList(
        "get_attachment_id",
        "get_calendar_id",
        "get_contact_id",
        "get_folder_id",
        "get_forum_id",
        "get_gid_number_id",
        "get_gui_setting_id",
        "get_ical_id",
        "get_infostore_id",
        "get_mail_service_id",
        "get_pinboard_id",
        "get_principal_id",
        "get_project_id",
        "get_resource_group_id",
        "get_resource_id",
        "get_task_id",
        "get_uid_number_id",
        "get_unique_id",
        "get_webdav_id"));

    @Override
    public void perform(PerformParameters params) throws OXException {
        int ctxId = params.getContextId();
        Connection conn = Database.getNoTimeout(ctxId, true);
        try {
            conn.setAutoCommit(false);
            for(String proc : storedProcedures) {
                if(DBUtils.procedureExists(conn, proc)) {
                    Statement stmt = null;
                    try {
                        stmt = conn.createStatement();
                        stmt.execute("DROP PROCEDURE " + proc);
                    } finally {
                        closeSQLStuff(null, stmt);
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            DBUtils.rollback(conn);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            DBUtils.rollback(conn);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(conn);
            Database.backNoTimeout(ctxId, true, conn);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BACKGROUND, WorkingLevel.SCHEMA);
    }
}
