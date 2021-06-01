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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

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
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            LOG.info("Trying to auto-correct wrong contact references in 'prg_dlist'...");
            int corrected = correctWrongReferences(connection);
            LOG.info("Auto-corrected {} contact references.", I(corrected));
            LOG.info("Deleting remaining wrong contact references in 'prg_dlist'...");
            int deleted = deleteWrongReferences(connection);
            LOG.info("Deleted {} contact references.", I(deleted));

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
            if (rollback == 1) {
                Databases.rollback(connection);
            }
            Databases.autocommit(connection);
            }
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
            Databases.closeSQLStuff(statement);
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
            Databases.closeSQLStuff(statement);
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND);
    }

}
