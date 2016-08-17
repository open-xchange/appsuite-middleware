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

package com.openexchange.admin.mysql;

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link CreateLdap2SqlTables}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateLdap2SqlTables extends AbstractCreateTableImpl {

    private static final String groupsTableName = "groups";
    private static final String delGroupsTableName = "del_groups";
    private static final String userTableName = "user";
    private static final String delUserTableName = "del_user";
    private static final String groupsMemberTableName = "groups_member";
    private static final String login2UserTableName = "login2user";
    private static final String userAttributeTableName = "user_attribute";
    private static final String resourceTableName = "resource";
    private static final String delResourceTableName = "del_resource";
    private static final String aliasTableName = "user_alias";

    private static final String createGroupsTable = "CREATE TABLE groups ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "identifier VARCHAR(128) NOT NULL,"
       + "displayName VARCHAR(128) NOT NULL,"
       + "lastModified INT8 NOT NULL,"
       + "gidNumber INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelGroupsTable = "CREATE TABLE del_groups ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "identifier VARCHAR(128) NOT NULL,"
       + "displayName VARCHAR(128) NOT NULL,"
       + "lastModified INT8 NOT NULL,"
       + "gidNumber INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createUserTable = "CREATE TABLE user ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "imapServer VARCHAR(128),"
       + "imapLogin VARCHAR(128),"
       + "mail VARCHAR(256) NOT NULL,"
       + "mailDomain VARCHAR(128),"
       + "mailEnabled boolean NOT NULL,"
       + "preferredLanguage VARCHAR(10) NOT NULL,"
       + "shadowLastChange INTEGER NOT NULL,"
       + "smtpServer VARCHAR(128),"
       + "timeZone VARCHAR(128) NOT NULL,"
       + "userPassword VARCHAR(128),"
       + "contactId INT4 UNSIGNED NOT NULL,"
       + "passwordMech VARCHAR(32) NOT NULL,"
       + "uidNumber INT4 UNSIGNED NOT NULL,"
       + "gidNumber INT4 UNSIGNED NOT NULL,"
       + "homeDirectory VARCHAR(128) NOT NULL,"
       + "loginShell VARCHAR(128) NOT NULL,"
       + "guestCreatedBy INT4 UNSIGNED NOT NULL DEFAULT 0,"
       + "filestore_id INT4 unsigned NOT NULL DEFAULT 0,"
       + "filestore_owner INT4 unsigned NOT NULL DEFAULT 0,"
       + "filestore_name VARCHAR(32) DEFAULT NULL,"
       + "filestore_login VARCHAR(32) DEFAULT NULL,"
       + "filestore_passwd VARCHAR(32) DEFAULT NULL,"
       + "quota_max BIGINT(20) DEFAULT NULL,"
       + "PRIMARY KEY (cid, id),"
       + "INDEX `mailIndex` (cid, mail(255)),"
       + "INDEX `guestCreatedByIndex` (cid, guestCreatedBy)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelUserTable = "CREATE TABLE del_user ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "imapServer VARCHAR(128) DEFAULT NULL,"
       + "imapLogin VARCHAR(128) DEFAULT NULL,"
       + "mail VARCHAR(256) NOT NULL DEFAULT '',"
       + "mailDomain VARCHAR(128) DEFAULT NULL,"
       + "mailEnabled boolean NOT NULL DEFAULT false,"
       + "preferredLanguage VARCHAR(10) NOT NULL DEFAULT '',"
       + "shadowLastChange INTEGER NOT NULL DEFAULT -1,"
       + "smtpServer VARCHAR(128) DEFAULT NULL,"
       + "timeZone VARCHAR(128) NOT NULL DEFAULT '',"
       + "userPassword VARCHAR(128) DEFAULT NULL,"
       + "contactId INT4 UNSIGNED NOT NULL,"
       + "passwordMech VARCHAR(32) NOT NULL DEFAULT '',"
       + "uidNumber INT4 UNSIGNED NOT NULL,"
       + "gidNumber INT4 UNSIGNED NOT NULL,"
       + "homeDirectory VARCHAR(128) NOT NULL DEFAULT '',"
       + "loginShell VARCHAR(128) NOT NULL DEFAULT '',"
       + "guestCreatedBy INT4 UNSIGNED NOT NULL DEFAULT 0,"
       + "filestore_id INT4 unsigned NOT NULL DEFAULT 0,"
       + "filestore_owner INT4 unsigned NOT NULL DEFAULT 0,"
       + "filestore_name VARCHAR(32) DEFAULT NULL,"
       + "filestore_login VARCHAR(32) DEFAULT NULL,"
       + "filestore_passwd VARCHAR(32) DEFAULT NULL,"
       + "quota_max BIGINT(20) DEFAULT NULL,"
       + "PRIMARY KEY (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createGroupsMemberTable = "CREATE TABLE groups_member ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "member INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid, id, member),"
       + "FOREIGN KEY (cid, id) REFERENCES groups(cid, id),"
       + "FOREIGN KEY (cid, member) REFERENCES user(cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createLogin2UserTable = "CREATE TABLE login2user ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "uid VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,"
       + "PRIMARY KEY (cid, uid),"
       + "FOREIGN KEY (cid, id) REFERENCES user(cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createUserAttributeTablePrimaryKey = "CREATE TABLE user_attribute ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "id INT4 UNSIGNED NOT NULL,"
        + "name VARCHAR(128) NOT NULL,"
        + "value TEXT NOT NULL,"
        + "uuid BINARY(16) NOT NULL,"
        + "PRIMARY KEY (cid, uuid),"
        + "INDEX `attributeIndex` (cid,name,value(20)),"
        + "FOREIGN KEY (cid, id) REFERENCES user(cid, id)"
      + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createResourceTable = "CREATE TABLE resource ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "identifier VARCHAR(128) NOT NULL,"
       + "displayName VARCHAR(128) NOT NULL,"
       + "mail VARCHAR(256),"
       + "available boolean NOT NULL,"
       + "description TEXT,"
       + "lastModified INT8 NOT NULL,"
       + "PRIMARY KEY (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelResourceTable = "CREATE TABLE del_resource ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "identifier VARCHAR(128) NOT NULL DEFAULT '',"
       + "displayName VARCHAR(128) NOT NULL DEFAULT '',"
       + "mail VARCHAR(256) DEFAULT NULL,"
       + "available boolean NOT NULL DEFAULT false,"
       + "description TEXT DEFAULT NULL,"
       + "lastModified INT8 NOT NULL,"
       + "PRIMARY KEY (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createAliasTable = "CREATE TABLE user_alias ( " // ---> Also specified in com.openexchange.groupware.update.tasks.MigrateAliasUpdateTask
        + "cid INT4 UNSIGNED NOT NULL, "
        + "user INT4 UNSIGNED NOT NULL, "
        + "alias VARCHAR(255) NOT NULL, "
        + "`uuid` BINARY(16) DEFAULT NULL," 
        + "PRIMARY KEY (`cid`, `user`, `alias`) " 
      + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateLdap2SqlTables}.
     */
    public CreateLdap2SqlTables() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { groupsTableName, delGroupsTableName, userTableName, delUserTableName,
            groupsMemberTableName, login2UserTableName, userAttributeTableName, resourceTableName,
            delResourceTableName, aliasTableName };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { createGroupsTable, createDelGroupsTable, createUserTable, createDelUserTable, createGroupsMemberTable,
            createLogin2UserTable, createUserAttributeTablePrimaryKey, createResourceTable, createDelResourceTable, createAliasTable };
    }

}
