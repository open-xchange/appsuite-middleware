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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Tools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ContactsAddUseCountColumnUpdateTask implements UpdateTask {

    private final String ADD_COLUMN = "ALTER TABLE prg_contacts ADD COLUMN useCount INT4 UNSIGNED DEFAULT 0";

    private final String ADD_COLUMN_DEL = "ALTER TABLE del_contacts ADD COLUMN useCount INT4 UNSIGNED";

    private final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactsAddUseCountColumnUpdateTask.class));

    @Override
    public int addedWithVersion() {
        return 50;
    }

    @Override
    public int getPriority() {
        return UpdateTask.UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(Schema schema, int contextId) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = Database.getNoTimeout(contextId, true);
            if (Tools.columnExists(con, "prg_contacts", "useCount")) {
                LOG.info("Column 'useCount' already exists in table 'prg_contacts'.");
            } else {
                stmt = con.prepareStatement(ADD_COLUMN);
                stmt.executeUpdate();
                stmt.close();
                stmt = con.prepareStatement(ADD_COLUMN_DEL);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (con != null) {
                Database.backNoTimeout(contextId, true, con);
            }
        }
    }

}
