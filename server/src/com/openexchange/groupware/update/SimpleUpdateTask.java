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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.groupware.update;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.update.UpdateTask.UpdateTaskPriority;


/**
 * A {@link SimpleUpdateTask} handles connection management for update tasks. Implement the {@link #perform(Connection)} method
 * with your update logic. You may want to implement {@link #shouldRun(Connection)} if you can determine whether the UT has any work to do. 
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class SimpleUpdateTask implements UpdateTaskV2 {

    public void perform(PerformParameters params) throws AbstractOXException {
        perform(params.getSchema(), params.getContextId());
    }

    public void perform(Schema schema, int contextId) throws AbstractOXException {
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            con.setAutoCommit(false);
            if (!shouldRun(con)) {
                return;
            }
            perform(con);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    protected abstract void perform(Connection con) throws SQLException;

    protected boolean shouldRun(Connection con) {
        return true;
    }
    
//  The following methods can be overridden, if different values make sense here
    
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    public String[] getDependencies() {
        return new String[0];
    }

    public int addedWithVersion() {
        return NO_VERSION;
    }

    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

}
