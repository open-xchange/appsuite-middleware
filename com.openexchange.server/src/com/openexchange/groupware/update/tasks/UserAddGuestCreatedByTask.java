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
import static com.openexchange.tools.sql.DBUtils.rollback;
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
import com.openexchange.groupware.update.WorkingLevel;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link UserAddGuestCreatedByTask}
 *
 * Adds the column 'guestCreatedBy' to the tables 'user' and 'del_user', as well as an appropriate index.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class UserAddGuestCreatedByTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link UserAddGuestCreatedByTask}.
     */
    public UserAddGuestCreatedByTask() {
        super();
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING, WorkingLevel.SCHEMA);
    }

    @Override
    public String[] getDependencies() {
        return new String[] { UserClearDelTablesTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(UserAddGuestCreatedByTask.class);
        log.info("Performing update task {}", UserAddGuestCreatedByTask.class.getSimpleName());

        Connection connection = Database.getNoTimeout(params.getContextId(), true);
        boolean committed = false;
        try {
            connection.setAutoCommit(false);
            Column guestCreatedByColumn = new Column("guestCreatedBy", "int(10) unsigned NOT NULL DEFAULT 0");
            Tools.checkAndAddColumns(connection, "user", guestCreatedByColumn);
            Tools.checkAndAddColumns(connection, "del_user", guestCreatedByColumn);
            if (null == Tools.existsIndex(connection, "user", new String[] { "cid", "guestCreatedBy"})) {
                Tools.createIndex(connection, "user", "guestCreatedByIndex", new String[] { "cid", "guestCreatedBy"}, false);
            }
            connection.commit();
            committed = true;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (false == committed) {
                rollback(connection);
            }
            autocommit(connection);
            Database.backNoTimeout(params.getContextId(), true, connection);
        }

        log.info("{} successfully performed.", UserAddGuestCreatedByTask.class.getSimpleName());
    }

}
