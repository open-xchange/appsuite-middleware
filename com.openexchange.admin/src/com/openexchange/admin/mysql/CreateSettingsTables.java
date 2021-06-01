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
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

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
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createUserSettingMailSignatureTable = "CREATE TABLE user_setting_mail_signature ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "id VARCHAR(64) NOT NULL,"
       + "signature VARCHAR(1024) NOT NULL,"
       + "PRIMARY KEY (cid, user, id),"
       + "FOREIGN KEY (cid, user) REFERENCES user_setting_mail (cid, user)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createUserSettingSpellcheckTable = "CREATE TABLE user_setting_spellcheck ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "user_dic TEXT,"
       + "PRIMARY KEY (cid, user),"
       + "FOREIGN KEY (cid, user) REFERENCES user (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createUserSettingAdminTable = "CREATE TABLE user_setting_admin ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid, user),"
       + "FOREIGN KEY (cid, user) REFERENCES user (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createUserSettingTable = "CREATE TABLE user_setting ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "user_id INT4 UNSIGNED NOT NULL,"
       + "path_id INT4 UNSIGNED NOT NULL,"
       + "value MEDIUMTEXT,"
       + "PRIMARY KEY (cid, user_id, path_id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

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
      + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

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
