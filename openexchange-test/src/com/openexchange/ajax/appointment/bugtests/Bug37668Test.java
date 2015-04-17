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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Calendar;
import java.util.Date;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug37668Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Bug37668Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;
    private Appointment app;

    /**
     * Initializes a new {@link Bug37668Test}.
     * 
     * @param name
     */
    public Bug37668Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ctm = new CalendarTestManager(client);
        app = new Appointment();
        app.setStartDate(D("14.01.2015 16:00"));
        app.setEndDate(D("14.01.2015 17:00"));
        app.setRecurrenceType(Appointment.YEARLY);
        app.setInterval(1);
        app.setDayInMonth(27);
        app.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        app.setIgnoreConflicts(true);
    }

    public void testBug37668_JAN() throws Exception {
        doTest(Calendar.JANUARY);
    }

    public void testBug37668_FEB() throws Exception {
        doTest(Calendar.FEBRUARY);
    }

    public void testBug37668_MAR() throws Exception {
        doTest(Calendar.MARCH);
    }

    public void testBug37668_APR() throws Exception {
        doTest(Calendar.APRIL);
    }

    public void testBug37668_MAY() throws Exception {
        doTest(Calendar.MAY);
    }

    public void testBug37668_JUN() throws Exception {
        doTest(Calendar.JUNE);
    }

    public void testBug37668_JUL() throws Exception {
        doTest(Calendar.JULY);
    }

    public void testBug37668_AUG() throws Exception {
        doTest(Calendar.AUGUST);
    }

    public void testBug37668_SEP() throws Exception {
        doTest(Calendar.SEPTEMBER);
    }

    public void testBug37668_OCT() throws Exception {
        doTest(Calendar.OCTOBER);
    }

    public void testBug37668_NOV() throws Exception {
        doTest(Calendar.NOVEMBER);
    }

    public void testBug37668_DEC() throws Exception {
        doTest(Calendar.DECEMBER);
    }

    private void doTest(int month) throws Exception {
        app.setMonth(month);
        app.setTitle("Bug 37668 Test (" + (month + 1) + ")");

        ctm.insert(app);
        Appointment delete = new Appointment();
        delete.setObjectID(app.getObjectID());
        delete.setParentFolderID(app.getParentFolderID());
        delete.setRecurrencePosition(1);
        delete.setLastModified(new Date(Long.MAX_VALUE));

        ctm.delete(delete);

        Appointment load = ctm.get(app.getParentFolderID(), app.getObjectID());
        assertEquals("Wrong date.", D("27." + (month + 1) + ".2015 17:00"), load.getEndDate());
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }
}
