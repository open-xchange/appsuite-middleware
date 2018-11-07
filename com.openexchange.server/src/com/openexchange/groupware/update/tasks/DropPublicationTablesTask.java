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

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.WorkingLevel;
import com.openexchange.tools.update.Tools;

/**
 * 
 * {@link DropPublicationTablesTask} - Deletes the tables "sequence_publications", "publications" and "publication_users".
 * If any publication is found, data will be logged. Afterwards the data will be removed!
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 * @see <a href="https://jira.open-xchange.com/browse/MW-1089">MW-1089</a>
 */
public class DropPublicationTablesTask implements UpdateTaskV2 {

    /*
     * @formatter:off
     * ------------------------- OLD TABLE DEFINITIONS -------------------------
     * 
     * mysql> desc publications;
     * +------------------+------------------+------+-----+---------+-------+
     * | Field            | Type             | Null | Key | Default | Extra |
     * +------------------+------------------+------+-----+---------+-------+
     * | cid              | int(10) unsigned | NO   | PRI | NULL    |       |
     * | id               | int(10) unsigned | NO   | PRI | NULL    |       |
     * | user_id          | int(10) unsigned | NO   |     | NULL    |       |
     * | entity           | int(10) unsigned | NO   |     | NULL    |       |
     * | module           | varchar(255)     | NO   |     | NULL    |       |
     * | configuration_id | int(10) unsigned | NO   |     | NULL    |       |
     * | target_id        | varchar(255)     | NO   |     | NULL    |       |
     * | enabled          | tinyint(1)       | NO   |     | 1       |       |
     * | created          | bigint(20)       | NO   |     | 0       |       |
     * | lastModified     | bigint(20)       | NO   |     | 0       |       |
     * +------------------+------------------+------+-----+---------+-------+
     * 
     * --------------------------------------------------------------------------
     * 
     * mysql> desc publication_users;
     * +--------------+------------------+------+-----+---------+-------+
     * | Field        | Type             | Null | Key | Default | Extra |
     * +--------------+------------------+------+-----+---------+-------+
     * | cid          | int(10) unsigned | NO   | PRI | NULL    |       |
     * | id           | int(10) unsigned | NO   | PRI | NULL    |       |
     * | name         | varchar(255)     | NO   | PRI | NULL    |       |
     * | password     | varchar(255)     | NO   |     | NULL    |       |
     * | created      | bigint(20)       | NO   |     | 0       |       |
     * | lastModified | bigint(20)       | NO   |     | 0       |       |
     * +--------------+------------------+------+-----+---------+-------+
     * 
     * --------------------------------------------------------------------------
     * @formatter:on 
     */

    private final static Logger LOGGER = LoggerFactory.getLogger(DropPublicationTablesTask.class);

    public DropPublicationTablesTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        boolean rollback = false;

        LOGGER.info("Start purging publication related tables.");

        try {
            startTransaction(con);
            rollback = true;

            boolean existsPublication = Tools.tableExists(con, "publications");

            /*
             * Get all publication users
             */
            Map<Integer, Map<Integer, List<PublicationUser>>> contextToPub = new HashMap<>();
            if (Tools.tableExists(con, "publication_users")) {
                // Only fetch data if we can log something useful
                if (existsPublication) {
                    executeQuerry(con, "SELECT cid, id, name, created, lastModified FROM publication_users", (ResultSet resultSet) -> {
                        int i = 1;
                        do {
                            Integer contextId = I(resultSet.getInt(i++));
                            Integer publicationId = I(resultSet.getInt(i++));

                            PublicationUser publicationUser = new PublicationUser();
                            publicationUser.name = resultSet.getString(i++);
                            publicationUser.created = new Date(resultSet.getInt(i++));
                            publicationUser.lastModified = new Date(resultSet.getInt(i++));

                            if (contextToPub.containsKey(contextId)) {
                                // Known context
                                Map<Integer, List<PublicationUser>> pubToUser = contextToPub.get(contextId);
                                if (pubToUser.containsKey(publicationId)) {
                                    // New user in publication
                                    pubToUser.get(publicationId).add(publicationUser);
                                } else {
                                    // New publication
                                    ArrayList<PublicationUser> list = new ArrayList<>();
                                    list.add(publicationUser);
                                    pubToUser.put(publicationId, list);
                                }
                            } else {
                                // New context
                                ArrayList<PublicationUser> list = new ArrayList<>();
                                list.add(publicationUser);
                                Map<Integer, List<PublicationUser>> pubToUser = new HashMap<>();
                                pubToUser.put(publicationId, list);
                                contextToPub.put(contextId, pubToUser);
                            }
                        } while (resultSet.next());
                    });
                }
                Tools.dropTable(con, "publication_users");
            }

            /*
             * Get all publications and print out the information that is going to be deleted
             */
            if (existsPublication) {
                executeQuerry(con, "SELECT cid, id, user_id, entity, module, configuration_id, target_id, enabled, created, lastModified FROM publications", (ResultSet rs) -> {
                    do {
                        int i = 1;
                        Integer contextId = I(rs.getInt(i++));
                        Integer publicationId = I(rs.getInt(i++));

                        //@formatter:off         
                        LOGGER.info(
                            "Removing the publication with ID %s for user %s in context %s.\nThe publication has following attributes:\n" + 
                            "##############################################################\n" +
                            "entity:\t%s\nmodule:\t%s\nconfiguration_id:\t%s\ntarget_id:\t%s\nenabled:\t%s\ncreated:\t%s\nlastModified:\t%s\n" + 
                            "users:\t[%s]\n\n",
                            publicationId, 
                            rs.getString(i++), // user ID
                            contextId,
                            I(rs.getInt(i++)), // entity
                            rs.getString(i++), // module
                            I(rs.getInt(i++)), // configuration_id
                            rs.getString(i++), // target_id
                            0 == rs.getInt(i++) ? Boolean.FALSE : Boolean.TRUE, // enabled
                            new Date(rs.getInt(i++)), // created
                            new Date(rs.getInt(i++)), // lastModified
                            printPublicationUsers(contextToPub, contextId, publicationId)
                            );
                        //@formatter:on         

                    } while (rs.next());
                });
                Tools.dropTable(con, "publications");
            }

            // Nothing to log, just remove the table
            if (Tools.tableExists(con, "sequence_publications")) {
                Tools.dropTable(con, "sequence_publications");
            }

            con.commit();
            rollback = false;

            LOGGER.info("Publications have been removed successfully!");
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
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

    // -----------------------------------------------------------------------------------------------------------------------------

    /**
     * Pretty prints the publication users for a publication
     * 
     * @param contextToPub Holding the data
     * @param contextId The context ID
     * @param publicationId The ID of the publication
     * @return Pretty printed users or <code>none</code> if no data exist
     */
    private String printPublicationUsers(Map<Integer, Map<Integer, List<PublicationUser>>> contextToPub, Integer contextId, Integer publicationId) {
        StringBuilder sb = new StringBuilder();
        if (contextToPub.isEmpty() || false == contextToPub.containsKey(contextId) || false == contextToPub.get(contextId).containsKey(publicationId)) {
            return "none";
        }
        contextToPub.get(contextId).get(publicationId).forEach(u -> sb.append(u.toString()).append(","));
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Executes the query and logs
     * 
     * @param con The {@link Connection} to use
     * @param query The query to execute
     * @param logResults The logging to to
     * @throws SQLException If the query fails
     */
    private static void executeQuerry(final Connection con, String query, SQLConsumer<ResultSet> logResults) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                logResults.accept(rs);
            } else {
                LOGGER.debug("No result. Skip processing.");
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @FunctionalInterface
    private interface SQLConsumer<T> {

        void accept(T t) throws SQLException;
    }

    private class PublicationUser {

        String name;

        Date created;
        Date lastModified;

        public PublicationUser() {
            super();
        }

        @Override
        public String toString() {
            return new StringBuilder("name: ").append(name).append(", created: ").append(created).append(", lastModified: ").append(lastModified).toString();
        }
    }
}
