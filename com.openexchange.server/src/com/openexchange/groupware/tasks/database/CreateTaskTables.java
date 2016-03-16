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

package com.openexchange.groupware.tasks.database;

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link CreateTaskTables}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateTaskTables extends AbstractCreateTableImpl {

    private static final String taskTableName = "task";
    private static final String taskFolderTableName = "task_folder";
    private static final String taskParticipantTableName = "task_participant";
    private static final String taskEParticipantTableName = "task_eparticipant";
    private static final String taskRemovedParticipantTableName = "task_removedparticipant";
    private static final String delTaskTableName = "del_task";
    private static final String delTaskFolderTableName = "del_task_folder";
    private static final String delTaskParticipantTableName = "del_task_participant";
    // TODO drop this table with the upcoming major release after 7.8.0.
    private static final String delTaskEParticipantTableName = "del_task_eparticipant";

    private static final String createTaskTable = "CREATE TABLE task ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "uid VARCHAR(255) NOT NULL,"
       + "filename VARCHAR(255),"
       + "private BOOLEAN NOT NULL,"
       + "creating_date DATETIME NOT NULL,"
       + "last_modified INT8 NOT NULL,"
       + "created_from INT4 UNSIGNED NOT NULL,"
       + "changed_from INT4 UNSIGNED NOT NULL,"
       + "start DATETIME,"
       + "end DATETIME,"
       + "full_time BOOLEAN NOT NULL DEFAULT 1,"
       + "completed DATETIME,"
       + "title VARCHAR(256),"
       + "description TEXT,"
       + "state INTEGER,"
       + "priority INTEGER,"
       + "progress INTEGER,"
       + "categories VARCHAR(255),"
       + "project INT4 UNSIGNED,"
       + "target_duration BIGINT,"
       + "actual_duration BIGINT,"
       + "target_costs DECIMAL(12,2),"
       + "actual_costs DECIMAL(12,2),"
       + "currency VARCHAR(10),"
       + "trip_meter VARCHAR(255),"
       + "billing VARCHAR(255),"
       + "companies VARCHAR(255),"
       + "color_label INT1 UNSIGNED,"
       + "recurrence_type INT1 UNSIGNED NOT NULL,"
       + "recurrence_interval INT4 UNSIGNED,"
       + "recurrence_days INT1 UNSIGNED,"
       + "recurrence_dayinmonth INT1 UNSIGNED,"
       + "recurrence_month INT1 UNSIGNED,"
       + "recurrence_until DATETIME,"
       + "recurrence_count INT2 UNSIGNED,"
       + "number_of_attachments INT1 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid,id),"
       + "INDEX (cid,last_modified)"
       + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createTaskFolderTable = "CREATE TABLE task_folder ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "folder INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid,id,folder),"
       + "INDEX (cid,folder),"
       + "FOREIGN KEY (cid, id) REFERENCES task (cid, id),"
       + "FOREIGN KEY (cid, folder) REFERENCES oxfolder_tree (cid, fuid)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createTaskParticipantTable = "CREATE TABLE task_participant ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "task INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "group_id INT4 UNSIGNED,"
       + "accepted INT1 UNSIGNED NOT NULL,"
       + "description VARCHAR(255),"
       + "PRIMARY KEY (cid,task,user),"
       + "FOREIGN KEY (cid, task) REFERENCES task (cid, id),"
       + "FOREIGN KEY (cid,user) REFERENCES user (cid,id)"
       + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createTaskEParticipantTable = "CREATE TABLE task_eparticipant ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "task INT4 UNSIGNED NOT NULL,"
       + "mail VARCHAR(255) NOT NULL,"
       + "display_name VARCHAR(255),"
       + "PRIMARY KEY (cid,task,mail),"
       + "FOREIGN KEY (cid,task) REFERENCES task (cid,id)"
       + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createTaskRemovedParticipantTable = "CREATE TABLE task_removedparticipant ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "task INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "group_id INT4 UNSIGNED,"
       + "accepted INT1 UNSIGNED NOT NULL,"
       + "description VARCHAR(255),"
       + "folder INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid,task,user),"
       + "INDEX (cid,folder),"
       + "FOREIGN KEY (cid, task) REFERENCES task (cid, id),"
       + "FOREIGN KEY (cid,user) REFERENCES user(cid,id)"
       + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelTaskTable = "CREATE TABLE del_task ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "uid VARCHAR(255) NOT NULL,"
       + "filename VARCHAR(255),"
       + "private BOOLEAN NOT NULL,"
       + "creating_date DATETIME NOT NULL,"
       + "last_modified INT8 NOT NULL,"
       + "created_from INT4 UNSIGNED NOT NULL,"
       + "changed_from INT4 UNSIGNED NOT NULL,"
       + "start DATETIME,"
       + "end DATETIME,"
       + "full_time BOOLEAN NOT NULL DEFAULT 1,"
       + "completed DATETIME,"
       + "title VARCHAR(256),"
       + "description TEXT,"
       + "state INTEGER,"
       + "priority INTEGER,"
       + "progress INTEGER,"
       + "categories VARCHAR(255),"
       + "project INT4 UNSIGNED,"
       + "target_duration BIGINT,"
       + "actual_duration BIGINT,"
       + "target_costs DECIMAL(12,2),"
       + "actual_costs DECIMAL(12,2),"
       + "currency VARCHAR(10),"
       + "trip_meter VARCHAR(255),"
       + "billing VARCHAR(255),"
       + "companies VARCHAR(255),"
       + "color_label INT1 UNSIGNED,"
       + "recurrence_type INT1 UNSIGNED NOT NULL,"
       + "recurrence_interval INT4 UNSIGNED,"
       + "recurrence_days INT1 UNSIGNED,"
       + "recurrence_dayinmonth INT1 UNSIGNED,"
       + "recurrence_month INT1 UNSIGNED,"
       + "recurrence_until DATETIME,"
       + "recurrence_count INT2 UNSIGNED,"
       + "number_of_attachments INT1 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid,id),"
       + "INDEX (cid,last_modified)"
       + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelTaskFolderTable = "CREATE TABLE del_task_folder ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "id INT4 UNSIGNED NOT NULL,"
       + "folder INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (cid,id,folder),"
       + "INDEX (cid,folder),"
       + "FOREIGN KEY (cid, id) REFERENCES del_task (cid, id)"
       + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelTaskParticipantTable = "CREATE TABLE del_task_participant ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "task INT4 UNSIGNED NOT NULL,"
       + "user INT4 UNSIGNED NOT NULL,"
       + "group_id INT4 UNSIGNED,"
       + "accepted INT1 UNSIGNED NOT NULL,"
       + "description VARCHAR(255),"
       + "PRIMARY KEY (cid,task,user),"
       + "FOREIGN KEY (cid, task) REFERENCES del_task (cid, id)"
       + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    // TODO drop this table with the upcoming major release after 7.8.0.
    private static final String createDelTaskEParticipantTable = "CREATE TABLE del_task_eparticipant ("
       + "cid INT4 UNSIGNED NOT NULL,"
       + "task INT4 UNSIGNED NOT NULL,"
       + "mail VARCHAR(255) NOT NULL,"
       + "display_name VARCHAR(255),"
       + "PRIMARY KEY (cid,task,mail),"
       + "FOREIGN KEY (cid, task) REFERENCES del_task (cid, id)"
       + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateTaskTables}.
     */
    public CreateTaskTables() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user", "oxfolder_tree" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { taskTableName, taskFolderTableName, taskParticipantTableName, taskEParticipantTableName,
            taskRemovedParticipantTableName, delTaskTableName, delTaskFolderTableName, delTaskParticipantTableName,
            delTaskEParticipantTableName };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { createTaskTable, createTaskFolderTable, createTaskParticipantTable, createTaskEParticipantTable,
            createTaskRemovedParticipantTable, createDelTaskTable, createDelTaskFolderTable, createDelTaskParticipantTable,
            createDelTaskEParticipantTable };
    }
}
