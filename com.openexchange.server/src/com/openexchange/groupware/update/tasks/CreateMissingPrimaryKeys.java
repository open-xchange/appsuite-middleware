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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Tools;

/**
 * {@link CreateMissingPrimaryKeys} - Creates missing primary keys on various tables.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CreateMissingPrimaryKeys extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link CreateMissingPrimaryKeys}.
     */
    public CreateMissingPrimaryKeys() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    private void initTasks(final Connection con, final List<Callable<Void>> tasks) {
        class Splitter {

            private final Pattern split = Pattern.compile(" *, *");

            private final Pattern repl = Pattern.compile("[\\(\\)`]");

            String[] split(final String keys) {
                return split.split(repl.matcher(keys).replaceAll(""), 0);
            }
        }
        final Splitter splitter = new Splitter();
        /*
         * PRIMARY KEY for "genconf_attributes_bools"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "genconf_attributes_bools", splitter.split("(`cid`,`id`,`name`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE genconf_attributes_bools ADD PRIMARY KEY (`cid`,`id`,`name`), DROP KEY cid");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE genconf_attributes_bools ADD PRIMARY KEY (`cid`,`id`,`name`), DROP KEY cid";
            }
        });
        /*
         * PRIMARY KEY for "genconf_attributes_strings"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "genconf_attributes_strings", splitter.split("(`cid`,`id`,`name`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE genconf_attributes_strings ADD PRIMARY KEY (`cid`,`id`,`name`), DROP KEY cid");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE genconf_attributes_strings ADD PRIMARY KEY (`cid`,`id`,`name`), DROP KEY cid";
            }
        });
        /*
         * PRIMARY KEY for "user_setting_server"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "user_setting_server", splitter.split("(`cid`,`user`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE user_setting_server ADD PRIMARY KEY (`cid`,`user`), DROP KEY cid");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE user_setting_server ADD PRIMARY KEY (`cid`,`user`), DROP KEY cid";
            }
        });
        /*
         * PRIMARY KEY for "user_attribute" + DROP redundant KEY
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "user_attribute", splitter.split("(`cid`,`id`,`name`,`value`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE user_attribute ADD PRIMARY KEY (`cid`,`id`,`name`,`value`(128)), DROP KEY cid_2");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE user_attribute ADD PRIMARY KEY (`cid`,`id`,`name`,`value`(128)), DROP KEY cid_2";
            }
        });
        /*
         * PRIMARY KEY for "ical_principal"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "ical_principal", splitter.split("(`cid`,`object_id`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE ical_principal ADD PRIMARY KEY (`cid`,`object_id`), ADD INDEX `indexPrincipal` (`cid`,`principal`(64))");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE ical_principal ADD PRIMARY KEY (`cid`,`object_id`), ADD INDEX `indexPrincipal` (`cid`,`principal`(64))";
            }
        });
        /*
         * PRIMARY KEY for "ical_ids"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "ical_ids", splitter.split("(`cid`,`object_id`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE ical_ids ADD PRIMARY KEY (`cid`,`object_id`), ADD INDEX `indexPrincipal` (`cid`,`principal_id`)");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE ical_ids ADD PRIMARY KEY (`cid`,`object_id`), ADD INDEX `indexPrincipal` (`cid`,`principal_id`)";
            }
        });
        /*
         * PRIMARY KEY for "vcard_principal"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "vcard_principal", splitter.split("(`cid`,`object_id`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE vcard_principal ADD PRIMARY KEY (`cid`,`object_id`), ADD INDEX `indexPrincipal` (`cid`,`principal`(64))");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE vcard_principal ADD PRIMARY KEY (`cid`,`object_id`), ADD INDEX `indexPrincipal` (`cid`,`principal`(64))";
            }
        });
        /*
         * PRIMARY KEY for "vcard_ids"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "vcard_ids", splitter.split("(`cid`,`object_id`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE vcard_ids ADD PRIMARY KEY (`cid`,`object_id`), ADD INDEX `indexPrincipal` (`cid`,`principal_id`)");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE vcard_ids ADD PRIMARY KEY (`cid`,`object_id`), ADD INDEX `indexPrincipal` (`cid`,`principal_id`)";
            }
        });
        /*
         * PRIMARY KEY for "infostoreReservedPaths"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "infostoreReservedPaths", splitter.split("(`cid`,`folder`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE infostoreReservedPaths ADD PRIMARY KEY (`cid`,`folder`)");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE infostoreReservedPaths ADD PRIMARY KEY (`cid`,`folder`)";
            }
        });
        /*
         * PRIMARY KEY for "updateTask"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "updateTask", splitter.split("(`cid`,`taskName`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE updateTask ADD PRIMARY KEY (`cid`,`taskName`(255)), DROP KEY full");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE updateTask ADD PRIMARY KEY (`cid`,`taskName`(255)), DROP KEY full";
            }
        });
        /*
         * PRIMARY KEY for "prg_links"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(
                    con,
                    "prg_links",
                    splitter.split("(`cid`,`firstid`,`firstmodule`,`firstfolder`,`secondid`,`secondmodule`,`secondfolder`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE prg_links ADD PRIMARY KEY (`cid`,`firstid`,`firstmodule`,`firstfolder`,`secondid`,`secondmodule`,`secondfolder`)");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE prg_links ADD PRIMARY KEY (`cid`,`firstid`,`firstmodule`,`firstfolder`,`secondid`,`secondmodule`,`secondfolder`)";
            }
        });
        /*
         * PRIMARY KEY for "prg_contacts_linkage"
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                if (!Tools.existsPrimaryKey(con, "prg_contacts_linkage", splitter.split("(`cid`,`intfield01`,`intfield02`)"))) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE prg_contacts_linkage ADD PRIMARY KEY (`cid`,`intfield01`,`intfield02`)");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE prg_contacts_linkage ADD PRIMARY KEY (`cid`,`intfield01`,`intfield02`)";
            }
        });
        /*
         * TODO: PRIMARY KEY for "aggregatingContacts"
         */

        /*-
         * ########################################################################################
         * ################################ TIDY UP; See Bug #21882 ###############################
         * ########################################################################################
         */

        /*-
         * cid is a left-prefix of reminder_unique
         * Key definitions:
         *   KEY `cid` (`cid`,`target_id`),
         *   UNIQUE KEY `reminder_unique` (`cid`,`target_id`,`module`,`userid`),
         * Column types:
         *     `cid` int(10) unsigned not null
         *     `target_id` varchar(255) collate utf8_unicode_ci not null
         *     `module` tinyint(3) unsigned not null
         *     `userid` int(10) unsigned not null
         * To remove this duplicate index, execute:
         * ALTER TABLE `oxdatabase_6`.`reminder` DROP INDEX `cid`;
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                final String name = Tools.existsIndex(con, "reminder", splitter.split("(`cid`,`target_id`)"));
                if (null != name) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE reminder DROP INDEX `" + name + "`");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE reminder DROP INDEX `cid`";
            }
        });
        /*-
         * cid is a left-prefix of PRIMARY
         * Key definitions:
         *   KEY `cid` (`cid`,`tree`,`user`,`folderId`)
         *   PRIMARY KEY (`cid`,`tree`,`user`,`folderId`,`entity`),
         * Column types:
         *     `cid` int(10) unsigned not null
         *     `tree` int(10) unsigned not null
         *     `user` int(10) unsigned not null
         *     `folderid` varchar(192) collate utf8_unicode_ci not null
         *     `entity` int(10) unsigned not null
         * To remove this duplicate index, execute:
         * ALTER TABLE `oxdatabase_6`.`virtualBackupPermission` DROP INDEX `cid`;
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                final String name = Tools.existsIndex(con, "virtualBackupPermission", splitter.split("(`cid`,`tree`,`user`,`folderId`)"));
                if (null != name) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE virtualBackupPermission DROP INDEX `" + name + "`");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE virtualBackupPermission DROP INDEX `cid`";
            }
        });
        /*-
         * cid is a left-prefix of PRIMARY
         * Key definitions:
         *   KEY `cid` (`cid`,`tree`,`user`,`folderId`)
         *   PRIMARY KEY (`cid`,`tree`,`user`,`folderId`,`entity`),
         * Column types:
         *     `cid` int(10) unsigned not null
         *     `tree` int(10) unsigned not null
         *     `user` int(10) unsigned not null
         *     `folderid` varchar(192) collate utf8_unicode_ci not null
         *     `entity` int(10) unsigned not null
         * To remove this duplicate index, execute:
         * ALTER TABLE `oxdatabase_6`.`virtualPermission` DROP INDEX `cid`;
         */
        tasks.add(new Callable<Void>() {

            @Override
            public Void call() throws SQLException {
                final String name = Tools.existsIndex(con, "virtualPermission", splitter.split("(`cid`,`tree`,`user`,`folderId`)"));
                if (null != name) {
                    Statement stmt = null;
                    try {
                        stmt = con.createStatement();
                        stmt.execute("ALTER TABLE virtualPermission DROP INDEX `" + name + "`");
                    } finally {
                        closeSQLStuff(stmt);
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "ALTER TABLE virtualPermission DROP INDEX `cid`";
            }
        });
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int contextId = params.getContextId();
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            DBUtils.startTransaction(con);
            /*
             * Gather tasks to perform
             */
            final List<Callable<Void>> tasks = new LinkedList<Callable<Void>>();
            initTasks(con, tasks);
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateMissingPrimaryKeys.class);
            for (final Callable<Void> task : tasks) {
                try {
                    task.call();
                } catch (final SQLException e) {
                    log.warn("ALTER TABLE failed with: >>{}<<\nStatement: >>{}<<", e.getMessage(), task);
                }
            }
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } catch (final Exception e) {
            rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }
}
