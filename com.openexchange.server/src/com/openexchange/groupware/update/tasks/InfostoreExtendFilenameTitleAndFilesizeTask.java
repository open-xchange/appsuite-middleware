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

import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.update.Tools.checkAndModifyColumns;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;

/**
 * {@link InfostoreExtendFilenameTitleAndFilesizeTask}
 *
 * Extends the sizes of the 'filename', 'title' and 'file_size' columns in the 'infostore_document' table. The 'del_infostore_document'
 * table is not touched since the affected columns are about to be removed anyway, as a follow-up to the changes made by the previous
 * {@link InfostoreClearDelTablesTask}.
 *
 * @author <a href="mailto:tobias.Friedrich@open-xchange.com">Tobias Friedruch</a>
 */
public final class InfostoreExtendFilenameTitleAndFilesizeTask extends UpdateTaskAdapter {

    /**
     * Default constructor.
     */
    public InfostoreExtendFilenameTitleAndFilesizeTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(InfostoreExtendFilenameTitleAndFilesizeTask.class);
        log.info("Performing update task {}", InfostoreExtendFilenameTitleAndFilesizeTask.class.getSimpleName());
        Column[] colums = {
            new Column("title", "varchar(767)"),
            new Column("filename", "varchar(767)"),
            new Column("file_size", "bigint(20)")
        };
        Connection connnection = Database.getNoTimeout(params.getContextId(), true);
        try {
            connnection.setAutoCommit(false);
            checkAndModifyColumns(connnection, "infostore_document", colums);
            connnection.commit();
        } catch (SQLException e) {
            rollback(connnection);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (Exception e) {
            rollback(connnection);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(connnection);
            Database.backNoTimeout(params.getContextId(), true, connnection);
        }
        log.info("{} successfully performed.", InfostoreExtendFilenameTitleAndFilesizeTask.class.getSimpleName());
    }

}
