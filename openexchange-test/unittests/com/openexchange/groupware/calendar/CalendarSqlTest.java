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
package com.openexchange.groupware.calendar;

import junit.framework.TestCase;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.configuration.AJAXConfig;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.sql.SQLException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarSqlTest extends TestCase {

    private CalendarSql calendar = null;
    private List<CalendarDataObject> clean = new ArrayList<CalendarDataObject>();
    private int privateFolder;
    private Context ctx;

    public void setUp() throws Exception {
        Init.startServer();
        AJAXConfig.init();

        String user = AJAXConfig.getProperty(AJAXConfig.Property.LOGIN);
        CalendarContextToolkit tools = new CalendarContextToolkit();
        ctx = tools.getDefaultContext();
        int userId = tools.resolveUser(user,ctx);
        privateFolder = new CalendarFolderToolkit().getStandardFolder(userId, ctx);
        calendar = new CalendarSql( tools.getSessionForUser(user, ctx) );
        
    }

    public void tearDown() throws OXException, SQLException {
        for(CalendarDataObject cdao : clean) {
            //calendar.deleteAppointmentObject(cdao,privateFolder,new Date(Long.MAX_VALUE));
        }
        Init.stopServer();
    }

    // Bug #11148
    public void testUpdateWithInvalidRecurrencePatternShouldFail() throws OXException, SQLException {
        CalendarDataObject cdao = buildRecurringAppointment();
        save( cdao );
        clean.add( cdao );

        CalendarDataObject modified = new CalendarDataObject();
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setRecurrenceType(CalendarDataObject.MONTHLY);
        modified.setDays(CalendarObject.TUESDAY);
        modified.setDayInMonth(666); // Must be between 1 and 5, usually, so 666 is invalid
        modified.setContext(cdao.getContext());
        try {
            save( modified );
            fail("Could save invalid dayInMonth value");
        } catch (OXException x) {
            // Passed. The invalid value wasn't accepted.       
        } catch (SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }

    public void testShouldRebuildEntireRecurrencePatternOnUpdate() throws SQLException, OXException {
        CalendarDataObject cdao = buildRecurringAppointment();
        save( cdao );
        clean.add( cdao );

        CalendarDataObject modified = new CalendarDataObject();
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setContext(cdao.getContext());
        modified.setRecurrenceType(CalendarDataObject.MONTHLY);
        modified.setDayInMonth(12);
        modified.setInterval(2);
        modified.setRecurrenceCount(3); // Every 12th of every 2nd month for 3 appointments

        try {
            save( modified );
            
            CalendarDataObject reloaded = reload( modified );

            assertEquals(0, reloaded.getDays());
            assertEquals(12, reloaded.getDayInMonth());
            assertEquals(2, reloaded.getInterval());
            assertEquals(CalendarObject.MONTHLY, reloaded.getRecurrenceType());
        } catch (OXException x) {
            x.printStackTrace();
            fail(x.toString());
        } catch (SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }

    public void testShouldOnlyUpdateRecurrencePatternIfNeeded() throws SQLException, OXException {
        CalendarDataObject cdao = buildRecurringAppointment();
        save( cdao );
        clean.add( cdao );

        CalendarDataObject modified = new CalendarDataObject();
        modified.setObjectID(cdao.getObjectID());
        modified.setParentFolderID(cdao.getParentFolderID());
        modified.setContext(cdao.getContext());
        modified.setLocation("updated location");

        try {
            save( modified );

            CalendarDataObject reloaded = reload( modified );

            assertEquals(cdao.getDays(), reloaded.getDays());
            assertEquals(cdao.getDayInMonth(), reloaded.getDayInMonth());
            assertEquals(cdao.getInterval(), reloaded.getInterval());
            assertEquals(cdao.getRecurrenceType(), reloaded.getRecurrenceType());
        } catch (OXException x) {
            x.printStackTrace();
            fail(x.toString());
        } catch (SQLException x) {
            x.printStackTrace();
            fail(x.toString());
        }
    }

    private void save(CalendarDataObject cdao) throws OXException, SQLException {
        if(cdao.containsObjectID()) {
            calendar.updateAppointmentObject(cdao, cdao.getParentFolderID(), new Date(Long.MAX_VALUE));
        } else {
            calendar.insertAppointmentObject(cdao);            
        }
    }

    private CalendarDataObject reload(CalendarDataObject which) throws SQLException, OXException {
        return calendar.getObjectById(which.getObjectID(), which.getParentFolderID());
    }

    private CalendarDataObject buildRecurringAppointment() {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("recurring");
        cdao.setParentFolderID(privateFolder);
        cdao.setIgnoreConflicts(true);
        cdao.setStartDate(new Date(0));
        cdao.setEndDate(new Date(60*60*1000));
        cdao.setRecurrenceType(CalendarObject.MONTHLY);
        cdao.setRecurrenceCount(5);
        cdao.setDayInMonth(3);
        cdao.setInterval(2);
        cdao.setDays(CalendarObject.TUESDAY);
        assertTrue(cdao.isSequence());

        cdao.setContext(ctx);
        return cdao;
    }

}
