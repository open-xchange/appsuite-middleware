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
 * {@link ContactFixUserDistListReferencesTask}
 *
 * Tries to correct references in 'prg_dlist' using the contact's user ID instead of it's object ID.
 * @see https://bugs.open-xchange.com/show_bug.cgi?id=24035
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class ContactFixUserDistListReferencesTask extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactFixUserDistListReferencesTask.class);

    /**
     * Initializes a new {@link ContactFixUserDistListReferencesTask}.
     */
    public ContactFixUserDistListReferencesTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextID = params.getContextId();
        DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection connection = databaseService.getForUpdateTask(contextID);
        try {
            connection.setAutoCommit(false);
            LOG.info("Trying to auto-correct wrong contact references in 'prg_dlist'...");
            int corrected = correctWrongReferences(connection);
            LOG.info("Auto-corrected {} contact references.", corrected);
            LOG.info("Deleting remaining wrong contact references in 'prg_dlist'...");
            int deleted = deleteWrongReferences(connection);
            LOG.info("Deleted {} contact references.", deleted);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(connection);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(connection);
            Database.backNoTimeout(contextID, true, connection);
        }
    }

    private int correctWrongReferences(Connection connection) throws SQLException {
        String sql =
            "UPDATE prg_dlist AS d LEFT JOIN prg_contacts AS c " +
            "ON d.intfield02 = c.userid AND d.cid = c.cid " +
            "SET d.intfield02 = c.intfield01 " +
            "WHERE d.intfield03=1 AND d.field04 = c.field65 " +
            "OR d.intfield03=2 AND d.field04 = c.field66 " +
            "OR d.intfield03=3 AND d.field04 = c.field67;"
        ;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            return statement.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(statement);
        }
    }

    private int deleteWrongReferences(Connection connection) throws SQLException {
        String sql =
            "UPDATE prg_dlist AS d LEFT JOIN prg_contacts AS c " +
            "ON d.intfield02 = c.intfield01 AND d.cid = c.cid " +
            "SET d.intfield02 = NULL, d.intfield03 = NULL, d.intfield04 = NULL " +
            "WHERE d.intfield03=1 AND d.field04 <> c.field65 " +
            "OR d.intfield03=2 AND d.field04 <> c.field66 " +
            "OR d.intfield03=3 AND d.field04 <> c.field67;"
        ;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            return statement.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(statement);
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND);
    }

}
