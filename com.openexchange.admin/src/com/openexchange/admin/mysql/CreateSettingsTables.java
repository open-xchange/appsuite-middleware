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
 * {@link CreateSettingsTables}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateSettingsTables extends AbstractCreateTableImpl {

    private static final String userConfigurationTableName = "user_configuration";
    private static final String userSettingMailTableName = "user_setting_mail";
    private static final String userSettingMailSignatureTableName= "user_setting_mail_signature";
    private static final String userSettingSpellcheckTableName = "user_setting_spellcheck";
    private static final String userSettingAdminTableName = "user_setting_admin";
    private static final String userSettingTableName = "user_setting";
    private static final String userSettingServerTableName = "user_setting_server";

    private static final String createUserConfigurationTable = "CREATE TABLE user_configuration ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "permissions INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid, user),"
       + "FOREIGN KEY (cid, user) REFERENCES user (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createUserSettingMailTable = "CREATE TABLE user_setting_mail ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "bits INT4 UNSIGNED DEFAULT 0,"
       + "send_addr VARCHAR(256) NOT NULL,"
       + "reply_to_addr VARCHAR(256) DEFAULT NULL,"
       + "msg_format TINYINT(4) UNSIGNED DEFAULT 1,"
       + "display_msg_headers VARCHAR(256) DEFAULT NULL,"
       + "auto_linebreak INT4 UNSIGNED DEFAULT 80,"
       + "std_trash VARCHAR(128) NOT NULL,"
       + "std_sent VARCHAR(128) NOT NULL,"
       + "std_drafts VARCHAR(128) NOT NULL,"
       + "std_spam VARCHAR(128) NOT NULL,"
       + "confirmed_spam VARCHAR(128) NOT NULL,"
       + "confirmed_ham VARCHAR(128) NOT NULL,"
       + "upload_quota INT4 DEFAULT -1,"
       + "upload_quota_per_file INT4 DEFAULT -1,"
       + "PRIMARY KEY (cid, user),"
       + "FOREIGN KEY (cid, user) REFERENCES user (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createUserSettingMailSignatureTable = "CREATE TABLE user_setting_mail_signature ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "id VARCHAR(64) NOT NULL,"
       + "signature VARCHAR(1024) NOT NULL,"
       + "PRIMARY KEY (cid, user, id),"
       + "FOREIGN KEY (cid, user) REFERENCES user_setting_mail (cid, user)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createUserSettingSpellcheckTable = "CREATE TABLE user_setting_spellcheck ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "user_dic TEXT,"
       + "PRIMARY KEY (cid, user),"
       + "FOREIGN KEY (cid, user) REFERENCES user (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createUserSettingAdminTable = "CREATE TABLE user_setting_admin ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid, user),"
       + "FOREIGN KEY (cid, user) REFERENCES user (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createUserSettingTable = "CREATE TABLE user_setting ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user_id INT4 UNSIGNED NOT NULL,"
       + "path_id INT4 UNSIGNED NOT NULL,"
       + "value MEDIUMTEXT,"
       + "PRIMARY KEY (cid, user_id, path_id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createUserSettingServerTablePrimaryKey = "CREATE TABLE user_setting_server ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "user INT4 UNSIGNED NOT NULL,"
        + "contact_collect_folder INT4 UNSIGNED,"
        + "contact_collect_enabled BOOL,"
        + "defaultStatusPrivate INT4 UNSIGNED DEFAULT 0,"
        + "defaultStatusPublic INT4 UNSIGNED DEFAULT 0,"
        + "contactCollectOnMailTransport BOOL DEFAULT TRUE,"
        + "contactCollectOnMailAccess BOOL DEFAULT TRUE,"
        + "folderTree INT4,"
        + "uuid BINARY(16) NOT NULL,"
        + "PRIMARY KEY (cid, user, uuid),"
        + "FOREIGN KEY(cid, user) REFERENCES user(cid, id)"
      + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateSettingsTables}.
     */
    public CreateSettingsTables() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { userConfigurationTableName, userSettingMailTableName, userSettingMailSignatureTableName,
            userSettingSpellcheckTableName, userSettingAdminTableName, userSettingTableName, userSettingServerTableName };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { createUserConfigurationTable, createUserSettingMailTable, createUserSettingMailSignatureTable,
                createUserSettingSpellcheckTable, createUserSettingAdminTable, createUserSettingTable,
                createUserSettingServerTablePrimaryKey };
    }

}
