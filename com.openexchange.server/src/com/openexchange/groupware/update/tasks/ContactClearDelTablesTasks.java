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
 * {@link ContactClearDelTablesTasks}
 *
 * Removes obsolete data from the 'del_contacts', 'del_dlist' and 'del_contacts_image' tables.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class ContactClearDelTablesTasks extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactClearDelTablesTasks.class);

    /**
     * Initializes a new {@link ContactClearDelTablesTasks}.
     */
    public ContactClearDelTablesTasks() {
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

            LOG.info("Clearing obsolete fields in 'del_dlist'...");
            int cleared = clearDeletedDistributionLists(connection);
            LOG.info("Cleared {} rows in 'del_dlist'.", I(cleared));
            LOG.info("Clearing obsolete fields in 'del_contacts_image'...");
            cleared = clearDeletedContactImages(connection);
            LOG.info("Cleared {} rows in 'del_contacts_image'.", I(cleared));
            LOG.info("Clearing obsolete fields in 'del_contacts'...");
            cleared = clearDeletedContacts(connection);
            LOG.info("Cleared {} rows in 'del_contacts'.", I(cleared));

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

    private static int clearDeletedContacts(Connection connection) throws SQLException {
//        String[] columsToRemain = {
//            "cid", "fid", "intfield01", "userid", "uid", "filename", "changing_date", "creating_date", "created_from", "changed_from", "pflag"
//        };
        String[] columsToClear = {
            "timestampfield01", "timestampfield02", "intfield02", "intfield03", "intfield04", "intfield05", "intfield06", "intfield07",
            "intfield08", "field01", "field02", "field03", "field04", "field05", "field06", "field07", "field08", "field09", "field10",
            "field11", "field12", "field13", "field14", "field15", "field16", "field17", "field18", "field19", "field20", "field21",
            "field22", "field23", "field24", "field25", "field26", "field27", "field28", "field29", "field30", "field31", "field32",
            "field33", "field34", "field35", "field36", "field37", "field38", "field39", "field40", "field41", "field42", "field43",
            "field44", "field45", "field46", "field47", "field48", "field49", "field50", "field51", "field52", "field53", "field54",
            "field55", "field56", "field57", "field58", "field59", "field60", "field61", "field62", "field63", "field64", "field65",
            "field66", "field67", "field68", "field69", "field70", "field71", "field72", "field73", "field74", "field75", "field76",
            "field77", "field78", "field79", "field80", "field81", "field82", "field83", "field84", "field85", "field86", "field87",
            "field88", "field89", "field90", "useCount", "yomiFirstName", "yomiLastName", "yomiCompany", "homeAddress", "businessAddress",
            "otherAddress"
        };
        StringBuilder StringBuilder = new StringBuilder("UPDATE del_contacts SET ").append(columsToClear[0]).append("=NULL");
        for (int i = 1; i < columsToClear.length; i++) {
            StringBuilder.append(',').append(columsToClear[i]).append("=NULL");
        }
        StringBuilder.append(';');
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(StringBuilder.toString());
            return statement.executeUpdate();
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    private static int clearDeletedDistributionLists(Connection connection) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("DELETE FROM del_dlist;");
            return statement.executeUpdate();
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    private static int clearDeletedContactImages(Connection connection) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("DELETE FROM del_contacts_image;");
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
