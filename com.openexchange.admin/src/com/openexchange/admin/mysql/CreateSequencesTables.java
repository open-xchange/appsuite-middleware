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
 * {@link CreateSequencesTables}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateSequencesTables extends AbstractCreateTableImpl {

    private static final String sequenceIdTableName = "sequence_id";
    private static final String sequencePrincipalTableName = "sequence_principal";
    private static final String sequenceResourceTableName = "sequence_resource";
    private static final String sequenceResourceGroupTableName = "sequence_resource_group";
    private static final String sequenceFolderTableName = "sequence_folder";
    private static final String sequenceCalendarTableName = "sequence_calendar";
    private static final String sequenceContactTableName = "sequence_contact";
    private static final String sequenceTaskTableName = "sequence_task";
    private static final String sequenceProjectTableName = "sequence_project";
    private static final String sequenceInfostoreTableName = "sequence_infostore";
    private static final String sequenceForumTableName = "sequence_forum";
    private static final String sequencePinboardTableName = "sequence_pinboard";
    private static final String sequenceAttachmentTableName = "sequence_attachment";
    private static final String sequenceGuiSettingTableName = "sequence_gui_setting";
    private static final String sequenceReminderTableName = "sequence_reminder";
    private static final String sequenceIcalTableName = "sequence_ical";
    private static final String sequenceWebdavTableName = "sequence_webdav";
    private static final String sequenceUidNumberTableName = "sequence_uid_number";
    private static final String sequenceGidNumberTableName = "sequence_gid_number";
    private static final String sequenceMailServiceTableName = "sequence_mail_service";

    private static final String createSequenceIdTable = "CREATE TABLE sequence_id ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequencePrincipalTable = "CREATE TABLE sequence_principal ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceResourceTable = "CREATE TABLE sequence_resource ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceResourceGroupTable = "CREATE TABLE sequence_resource_group ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceFolderTable = "CREATE TABLE sequence_folder ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceCalendarTable = "CREATE TABLE sequence_calendar ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceContactTable = "CREATE TABLE sequence_contact ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceTaskTable = "CREATE TABLE sequence_task ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceProjectTable = "CREATE TABLE sequence_project ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceInfostoreTable = "CREATE TABLE sequence_infostore ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceForumTable = "CREATE TABLE sequence_forum ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequencePinboardTable = "CREATE TABLE sequence_pinboard ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceAttachmentTable = "CREATE TABLE sequence_attachment ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceGuiSettingTable = "CREATE TABLE sequence_gui_setting ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceReminderTable = "CREATE TABLE sequence_reminder ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceIcalTable = "CREATE TABLE sequence_ical ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceWebdavTable = "CREATE TABLE sequence_webdav ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceUidNumberTable = "CREATE TABLE sequence_uid_number ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceGidNumberTable = "CREATE TABLE sequence_gid_number ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createSequenceMailServiceTable = "CREATE TABLE sequence_mail_service ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Initializes a new {@link CreateSequencesTables}.
     */
    public CreateSequencesTables() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { sequenceIdTableName, sequencePrincipalTableName, sequenceResourceTableName,
            sequenceResourceGroupTableName, sequenceFolderTableName, sequenceCalendarTableName, sequenceContactTableName,
            sequenceTaskTableName, sequenceProjectTableName, sequenceInfostoreTableName, sequenceForumTableName,
            sequencePinboardTableName, sequenceAttachmentTableName, sequenceGuiSettingTableName, sequenceReminderTableName,
            sequenceIcalTableName, sequenceWebdavTableName, sequenceUidNumberTableName, sequenceGidNumberTableName,
            sequenceMailServiceTableName };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { createSequenceIdTable, createSequencePrincipalTable, createSequenceResourceTable,
            createSequenceResourceGroupTable, createSequenceFolderTable, createSequenceCalendarTable, createSequenceContactTable,
            createSequenceTaskTable, createSequenceProjectTable, createSequenceInfostoreTable, createSequenceForumTable,
            createSequencePinboardTable, createSequenceAttachmentTable, createSequenceGuiSettingTable, createSequenceReminderTable,
            createSequenceIcalTable, createSequenceWebdavTable, createSequenceUidNumberTable, createSequenceGidNumberTable,
            createSequenceMailServiceTable };
    }

}
