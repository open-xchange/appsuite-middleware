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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
    private static final String getUniqueIdProcedureName = "get_unique_id";
    private static final String getResourceIdProcedureName = "get_resource_id";
    private static final String getResourceGroupIdProcedureName = "get_resource_group_id";
    private static final String getPrincipalIdProcedureName = "get_principal_id";
    private static final String getFolderIdProcedureName = "get_folder_id";
    private static final String getCalendarIdProcedureName = "get_calendar_id";
    private static final String getContactIdProcedureName = "get_contact_id";
    private static final String getTaskIdProcedureName = "get_task_id";
    private static final String getProjectIdProcedureName = "get_project_id";
    private static final String getInfostoreIdProcedureName = "get_infostore_id";
    private static final String getForumIdProcedureName = "get_forum_id";
    private static final String getPinboardIdProcedureName = "get_pinboard_id";
    private static final String getGuiSettingIdProcedureName = "get_gui_setting_id";
    private static final String getIcalIdProcedureName = "get_ical_id";
    private static final String getAttachmentIdProcedureName = "get_attachment_id";
    private static final String getWebdavIdProcedureName = "get_webdav_id";
    private static final String getUidNumberIdProcedureName = "get_uid_number_id";
    private static final String getGidNumberIdProcedureName = "get_gid_number_id";
    private static final String getMailServiceIdProcedureName = "get_mail_service_id";
    
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
    
    private static final String createGetUniqueIdProcedure = "CREATE PROCEDURE get_unique_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_id SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_id WHERE cid=context;"
       + "END ;";
    
    private static final String createGetResourceIdProcedure = "CREATE PROCEDURE get_resource_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_resource SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_resource WHERE cid=context;"
       + "END ;";
    
    private static final String createGetResourceGroupIdProcedure = "CREATE PROCEDURE get_resource_group_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_resource_group SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_resource_group WHERE cid=context;"
       + "END ;";
    
    private static final String createGetPrincipalIdProcedure = "CREATE PROCEDURE get_principal_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_principal SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_principal WHERE cid=context;"
       + "END ;";
    
    private static final String createGetFolderIdProcedure = "CREATE PROCEDURE get_folder_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_folder SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_folder WHERE cid=context;"
       + "END ;";
    
    private static final String createGetCalendarIdProcedure = "CREATE PROCEDURE get_calendar_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_calendar SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_calendar WHERE cid=context;"
       + "END ;";
    
    private static final String createGetContactIdProcedure = "CREATE PROCEDURE get_contact_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_contact SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_contact WHERE cid=context;"
       + "END ;";
    
    private static final String createGetTaskIdProcedure = "CREATE PROCEDURE get_task_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_task SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_task WHERE cid=context;"
       + "END ;";
    
    private static final String createGetProjectIdProcedure = "CREATE PROCEDURE get_project_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_project SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_project WHERE cid=context;"
       + "END ;";
    
    private static final String createGetInfostoreIdProcedure = "CREATE PROCEDURE get_infostore_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_infostore SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_infostore WHERE cid=context;"
       + "END ;";
    
    private static final String createGetForumIdProcedure = "CREATE PROCEDURE get_forum_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_forum SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_forum WHERE cid=context;"
       + "END ;";
    
    private static final String createGetPinboardIdProcedure = "CREATE PROCEDURE get_pinboard_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_pinboard SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_pinboard WHERE cid=context;"
       + "END ;";
    
    private static final String createGetGuiSettingIdProcedure = "CREATE PROCEDURE get_gui_setting_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_gui_setting SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_gui_setting WHERE cid=context;"
       + "END ;";
    
    private static final String createGetIcalIdProcedure = "CREATE PROCEDURE get_ical_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_ical SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_ical WHERE cid=context;"
       + "END ;";
    
    private static final String createGetAttachmentIdProcedure = "CREATE PROCEDURE get_attachment_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_attachment SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_attachment WHERE cid=context;"
       + "END ;";
    
    private static final String createGetWebdavIdProcedure = "CREATE PROCEDURE get_webdav_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_webdav SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_webdav WHERE cid=context;"
       + "END ;";
    
    private static final String createGetUidNumberIdProcedure = "CREATE PROCEDURE get_uid_number_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_uid_number SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_uid_number WHERE cid=context;"
       + "END ;";
    
    private static final String createGetGidNumberIdProcedure = "CREATE PROCEDURE get_gid_number_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE sequence_gid_number SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM sequence_gid_number WHERE cid=context;"
       + "END ;";
    
    private static final String createGetMailServiceIdProcedure = "CREATE PROCEDURE get_mail_service_id(IN context INT4 UNSIGNED) NOT DETERMINISTIC MODIFIES SQL DATA "
       + "BEGIN "
         + "UPDATE get_mail_service_id SET id=id+1 WHERE cid=context;"
         + "SELECT id FROM get_mail_service_id WHERE cid=context;"
       + "END ;";

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
            sequenceMailServiceTableName, getUniqueIdProcedureName, getResourceIdProcedureName, getResourceGroupIdProcedureName,
            getPrincipalIdProcedureName, getFolderIdProcedureName, getCalendarIdProcedureName, getContactIdProcedureName,
            getTaskIdProcedureName, getProjectIdProcedureName, getInfostoreIdProcedureName, getForumIdProcedureName,
            getPinboardIdProcedureName, getGuiSettingIdProcedureName, getIcalIdProcedureName, getAttachmentIdProcedureName,
            getWebdavIdProcedureName, getUidNumberIdProcedureName, getGidNumberIdProcedureName, getMailServiceIdProcedureName };
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
            createSequenceMailServiceTable, createGetUniqueIdProcedure, createGetResourceIdProcedure,
            createGetResourceGroupIdProcedure, createGetPrincipalIdProcedure, createGetFolderIdProcedure,
            createGetCalendarIdProcedure, createGetContactIdProcedure, createGetTaskIdProcedure, createGetProjectIdProcedure,
            createGetInfostoreIdProcedure, createGetForumIdProcedure, createGetPinboardIdProcedure, createGetGuiSettingIdProcedure,
            createGetIcalIdProcedure, createGetAttachmentIdProcedure, createGetWebdavIdProcedure, createGetUidNumberIdProcedure,
            createGetGidNumberIdProcedure, createGetMailServiceIdProcedure }; 
    }

}
