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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.tools.update.Tools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public class CreatePublicationTablesTask implements UpdateTask {

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(CreatePublicationTablesTask.class);
    
    private final String CREATE_TABLE_PUBLICATIONS = "CREATE TABLE publications (" +
        "id INT(10) UNSIGNED NOT NULL, " + 
        "cid INT(10) UNSIGNED NOT NULL, " +
        "user_id INT(10) UNSIGNED NOT NULL, " +
        "entity VARCHAR(255) NOT NULL, " +
        "module VARCHAR(255) NOT NULL, " +
        "configuration_id INT(10) UNSIGNED NOT NULL, " + 
        "target_id VARCHAR(255) NOT NULL, " +
        "PRIMARY KEY (id), " +
        "FOREIGN KEY(cid, user_id) REFERENCES user(cid, id)) " + 
        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    
    private final String CREATE_TABLE_SEQUENCE_PUBLICATIONS = "CREATE TABLE sequence_publications (" +
        "cid int(10) unsigned NOT NULL, " +
        "id int(10) unsigned NOT NULL, " +
        "PRIMARY KEY  (cid)) " +
        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    
    private final String INSERT_IN_SEQUENCE = "INSERT INTO sequence_publications (cid, id) VALUES (?, 0)";

    public int addedWithVersion() {
        return 42;
    }

    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
        Connection con = null;
        try {
            con = Database.getNoTimeout(contextId, true);
            if(!Tools.tableExists(con, "publications")) {
                Tools.exec(con, CREATE_TABLE_PUBLICATIONS);
            }
            if(!Tools.tableExists(con, "sequence_publications")) {
                Tools.exec(con, CREATE_TABLE_SEQUENCE_PUBLICATIONS);
            }
            for(final int ctxId : Tools.getContextIDs(con)) {
                if(!Tools.hasSequenceEntry("sequence_publications", con, ctxId)) {
                    Tools.exec(con, INSERT_IN_SEQUENCE, ctxId);
                }
            }
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            if(con != null) {
                Database.backNoTimeout(contextId, true, con);
            }
        }
    }
    
    @OXThrowsMultiple(
        category = { Category.CODE_ERROR },
        desc = { "" },
        exceptionId = { 1 },
        msg = { "A SQL error occurred while performing task CreatePublicationTablesTask: %1$s." }
    )
    private static UpdateException createSQLError(final SQLException e) {
        return EXCEPTION.create(1, e, e.getMessage());
    }

}
