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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.groupware.calendar.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * AlterMailAddressLength
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class AlterMailAddressLength implements UpdateTask {

    private static final String UPDATE_DEL_DATE_RIGHTS = "alter table del_date_rights change column ma ma VARCHAR(286)";
    private static final String UPDATE_PRG_DATE_RIGHTS = "alter table prg_date_rights change column ma ma VARCHAR(286)";


    @Override
    public int addedWithVersion() {
        return 2;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
        Connection writecon = null;
        Statement stmt = null;
        try {
            writecon = Database.get(contextId, true);
            try {
                stmt = writecon.createStatement();
            } catch (final SQLException ex) {
                throw OXCalendarExceptionCodes.UPDATE_EXCEPTION.create(ex.getMessage());
            }
            if (stmt != null) {
                try {
                    stmt.executeUpdate(UPDATE_PRG_DATE_RIGHTS);
                } catch (final SQLException ex) {
                    throw OXCalendarExceptionCodes.UPDATE_EXCEPTION.create(ex.getMessage());
                }
                try {
                    stmt.executeUpdate(UPDATE_DEL_DATE_RIGHTS);
                } catch (final SQLException ex) {
                   throw OXCalendarExceptionCodes.UPDATE_EXCEPTION.create(ex.getMessage());
                }
            }
        } finally {
            if (stmt != null) {
                ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class).closeStatement(stmt);
            }
            if (writecon != null) {
                Database.back(contextId, true, writecon);
            }
        }
    }

}
