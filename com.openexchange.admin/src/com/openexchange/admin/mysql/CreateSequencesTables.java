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
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequencePrincipalTable = "CREATE TABLE sequence_principal ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceResourceTable = "CREATE TABLE sequence_resource ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceResourceGroupTable = "CREATE TABLE sequence_resource_group ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceFolderTable = "CREATE TABLE sequence_folder ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceCalendarTable = "CREATE TABLE sequence_calendar ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceContactTable = "CREATE TABLE sequence_contact ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceTaskTable = "CREATE TABLE sequence_task ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceProjectTable = "CREATE TABLE sequence_project ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceInfostoreTable = "CREATE TABLE sequence_infostore ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceForumTable = "CREATE TABLE sequence_forum ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequencePinboardTable = "CREATE TABLE sequence_pinboard ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceAttachmentTable = "CREATE TABLE sequence_attachment ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceGuiSettingTable = "CREATE TABLE sequence_gui_setting ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceReminderTable = "CREATE TABLE sequence_reminder ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceIcalTable = "CREATE TABLE sequence_ical ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceWebdavTable = "CREATE TABLE sequence_webdav ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceUidNumberTable = "CREATE TABLE sequence_uid_number ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceGidNumberTable = "CREATE TABLE sequence_gid_number ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createSequenceMailServiceTable = "CREATE TABLE sequence_mail_service ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateSequencesTables}.
     */
    public CreateSequencesTables() {
        super();
    }

    /* (non-Javadoc)
     * @see com.openexchange.database.CreateTableService#requiredTables()
     */
    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    /* (non-Javadoc)
     * @see com.openexchange.database.CreateTableService#tablesToCreate()
     */
    @Override
    public String[] tablesToCreate() {
        return new String[] { sequenceIdTableName, sequencePrincipalTableName, sequenceResourceTableName,
            sequenceResourceGroupTableName, sequenceFolderTableName, sequenceCalendarTableName, sequenceContactTableName,
            sequenceTaskTableName, sequenceProjectTableName, sequenceInfostoreTableName, sequenceForumTableName,
            sequencePinboardTableName, sequenceAttachmentTableName, sequenceGuiSettingTableName, sequenceReminderTableName,
            sequenceIcalTableName, sequenceWebdavTableName, sequenceUidNumberTableName, sequenceGidNumberTableName,
            sequenceMailServiceTableName };
    }

    /* (non-Javadoc)
     * @see com.openexchange.database.AbstractCreateTableImpl#getCreateStatements()
     */
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
