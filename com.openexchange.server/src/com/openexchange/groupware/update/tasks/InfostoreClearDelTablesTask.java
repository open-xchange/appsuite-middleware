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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link InfostoreClearDelTablesTask} - Removes obsolete data from the 'del_infostore_document' table.
 * We don't need to clean up 'del_infostore' as it does only contain the metadata we have to keep anyway.<br>
 * <br>
 * Columns that will be cleaned up:
 * <ul>
 * <li>title</li>
 * <li>url</li>
 * <li>description</li>
 * <li>categories</li>
 * <li>filename</li>
 * <li>file_store_location</li>
 * <li>file_size</li>
 * <li>file_mimetype</li>
 * <li>file_md5sum</li>
 * <li>file_version_comment</li>
 * </ul>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreClearDelTablesTask extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreClearDelTablesTask.class);

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection con = databaseService.getForUpdateTask(contextId);
        PreparedStatement stmt = null;
        try {
            con.setAutoCommit(false);
            LOG.info("Clearing obsolete fields in 'del_infostore_document'...");

            String query = "UPDATE " +
                               "del_infostore_document " +
                           "SET " +
                               "title = NULL, " +
                               "url = NULL, " +
                               "description = NULL, " +
                               "categories = NULL, " +
                               "filename = NULL, " +
                               "file_store_location = NULL, " +
                               "file_size = NULL, " +
                               "file_mimetype = NULL, " +
                               "file_md5sum = NULL, " +
                               "file_version_comment = NULL" +
                           ";";

            stmt = con.prepareStatement(query);
            int cleared = stmt.executeUpdate();
            LOG.info("Cleared {} rows in 'del_infostore_document'.", cleared);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND);
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

}
