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

package com.openexchange.groupware.contexts.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Strings;
import com.openexchange.tools.update.Tools;

/**
 * {@link ChangePrimaryKeyForContextAttribute} - Changes the PRIMARY KEY of the <code>"contextAttribute"</code> table from ("cid", "name", "value") to ("cid", "name").
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public final class ChangePrimaryKeyForContextAttribute extends UpdateTaskAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangePrimaryKeyForContextAttribute.class);

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.CreateIndexOnContextAttributesTask.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        // Initialize connection
        Connection con = params.getConnection();

        // Start task processing
        boolean restoreAutocommit = false;
        boolean rollback = false;
        try {
            if (Tools.existsPrimaryKey(con, "contextAttribute", new String[] { "cid", "name" })) {
                // PRIMARY KEY already changed
                return;
            }

            Databases.startTransaction(con);
            restoreAutocommit = true;
            rollback = true;

            doPerform(con);

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            if (restoreAutocommit) {
                Databases.autocommit(con);
            }
        }
    }

    private void doPerform(Connection con) throws SQLException {
        checkAndResolveDuplicateEntries(con);

        // Reset PRIMARY KEY
        if (Tools.hasPrimaryKey(con, "contextAttribute")) {
            Tools.dropPrimaryKey(con, "contextAttribute");
        }
        Tools.createPrimaryKey(con, "contextAttribute", new String[] { "cid", "name" });
    }

    private void checkAndResolveDuplicateEntries(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Extract affected context identifiers
            stmt = con.prepareStatement("select cid,name from contextAttribute group by cid,name having count(*)>1;");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No rows available in connection-associated schema
                return;
            }

            List<ContextAttribute> affectedAttributes = new ArrayList<ContextAttribute>();
            {
                do {
                    int cid = rs.getInt(1);
                    String name = rs.getString(2);
                    affectedAttributes.add(new ContextAttribute(cid, name));
                } while (rs.next());
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            for (ContextAttribute contextAttribute : affectedAttributes) {
                List<String> dupValues = new ArrayList<String>();
                stmt = con.prepareStatement("SELECT value FROM contextAttribute WHERE cid=? and name=?");
                int pos = 1;
                stmt.setInt(pos++, contextAttribute.contextId);
                stmt.setString(pos++, contextAttribute.name);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    String value = rs.getString(1);
                    dupValues.add(value);
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                // Handle multiple values associated with the same name for a context
                if (dupValues.size() > 1) { // just to double-check
                    handleDuplicateValues(contextAttribute, dupValues, con);
                }
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void handleDuplicateValues(ContextAttribute attribute, List<String> dupValues, Connection con) throws SQLException {
        List<String> values = new ArrayList<>(dupValues);

        if (attribute.name.equals("taxonomy/types")) {
            Set<String> taxonomies = new HashSet<>();
            for (String value : dupValues) {
                String[] splitBySemiColon = Strings.splitByComma(value.trim());
                for (int i = 0; i < splitBySemiColon.length; i++) {
                    String taxonomy = splitBySemiColon[i];
                    if (Strings.isNotEmpty(taxonomy)) {
                        taxonomies.add(taxonomy.trim());
                    }
                }
            }
            deleteAllButLast(attribute, values, con);

            String newValue = taxonomies.stream().collect(Collectors.joining(","));
            update(attribute, newValue, con);
            LOGGER.warn("Found multiple values for 'taxonomy/types' in context {}. The values found {} will be merged to '{}'.", attribute.contextId, Strings.concat(";", dupValues), newValue);
        } else {
            String newValue = deleteAllButLast(attribute, values, con);
            if (attribute.name.startsWith("config/")) {
                LOGGER.warn("Found duplicate configuration for config '{}' in context {}. Will keep value '{}'.", attribute.name, attribute.contextId, newValue);
                return;
            }
            LOGGER.warn("Found multiple values for setting '{}' in context {}. Previous values {} will be reduced to '{}'.", attribute.name, attribute.contextId, Strings.concat(";", dupValues), newValue);
        }
    }

    private String deleteAllButLast(ContextAttribute attribute, List<String> values, Connection con) throws SQLException {
        // remove the last element
        String lastValue = values.remove(values.size() - 1);
        // Delete rest
        delete(attribute, values, con);

        return lastValue;
    }

    private void delete(ContextAttribute attribute, List<String> values, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(Databases.getIN("DELETE FROM contextAttribute WHERE cid=? AND name=? AND value IN (", values.size()));
            int pos = 1;
            stmt.setInt(pos++, attribute.contextId);
            stmt.setString(pos++, attribute.name);
            for (Iterator<String> iter = values.iterator(); iter.hasNext();) {
                String valueToRemove = iter.next();
                stmt.setString(pos++, valueToRemove);
            }
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void update(ContextAttribute attribute, String value, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE contextAttribute SET value=? WHERE cid=? AND name=?");
            int pos = 1;
            stmt.setString(pos++, value);
            stmt.setInt(pos++, attribute.contextId);
            stmt.setString(pos++, attribute.name);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    // ------------------------------------------------------------- Helper classes ---------------------------------------------------------

    private static class ContextAttribute {

        final int contextId;
        final String name;
        private final int hash;

        ContextAttribute(int contextId, String name) {
            super();
            this.contextId = contextId;
            String lowerCaseName = Strings.asciiLowerCase(name);
            this.name = lowerCaseName;

            int prime = 31;
            int result = prime * 1 + contextId;
            result = prime * result + ((lowerCaseName == null) ? 0 : lowerCaseName.hashCode());
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ContextAttribute other = (ContextAttribute) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{contextId=").append(contextId).append(", ");
            if (name != null) {
                builder.append("name=").append(name);
            }
            builder.append("}");
            return builder.toString();
        }
    }
}
