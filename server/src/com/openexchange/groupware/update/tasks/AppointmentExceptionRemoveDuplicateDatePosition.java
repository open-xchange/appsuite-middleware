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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.database.DatabaseServiceImpl;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.tools.sql.DBUtils;

/**
 * Removes the duplicate recurrence date positions from appointment exceptions.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public final class AppointmentExceptionRemoveDuplicateDatePosition implements
    UpdateTask {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(
        AppointmentExceptionRemoveDuplicateDatePosition.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(AppointmentExceptionRemoveDuplicateDatePosition.class);

    /**
     * Default constructor.
     */
    public AppointmentExceptionRemoveDuplicateDatePosition() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public int addedWithVersion() {
        return 22;
    }

    /**
     * {@inheritDoc}
     */
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    /**
     * {@inheritDoc}
     */
    @OXThrowsMultiple(category = { Category.CODE_ERROR },
        desc = { "" },
        exceptionId = { 1 },
        msg = { "An SQL error occurred: %1$s." }
    )
    public void perform(final Schema schema, final int contextId)
        throws AbstractOXException {
        LOG.info("Performing update task to remove duplicate date recurrence "
            + "position from appointment change exceptions on schema "
            + schema.getSchema());
        final Connection con = DatabaseServiceImpl.get(contextId, true);
        Statement st = null;
        try {
            con.setAutoCommit(false);
            st = con.createStatement();
            st.executeUpdate("UPDATE prg_dates SET "
                + "field08=SUBSTR(field08,1,LOCATE(',', field08)-1) "
                + "WHERE intfield01!=intfield02 AND field08 LIKE '%,%';");
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            closeSQLStuff(null, st);
            if (con != null) {
                DatabaseServiceImpl.back(contextId, true, con);
            }
        }
        LOG.info("Update task to remove duplicate date recurrence position from"
            + " appointment change exceptions performed.");
    }
}
