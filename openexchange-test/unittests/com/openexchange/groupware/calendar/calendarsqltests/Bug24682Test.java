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

package com.openexchange.groupware.calendar.calendarsqltests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Arrays;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link Bug24682Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug24682Test extends CalendarSqlTest {

    private int sharedFolderId1;

    private CalendarCollectionService calendarCollectionService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folders.sharePrivateFolder(session, ctx, secondUserId);
        folders.sharePrivateFolder(session, ctx, thirdUserId);
        sharedFolderId1 = folders.getStandardFolder(userId, ctx);
        calendarCollectionService = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
    }

    public void testBug24682() throws Exception {
        appointments.switchUser(secondUser);
        CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user, fourthUser);
        appointment.setStartDate(D("01.01.2013 08:00"));
        appointment.setEndDate(D("01.01.2013 10:00"));
        appointment.setRecurrenceType(CalendarDataObject.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(10);
        appointment.setParentFolderID(sharedFolderId1);
        appointments.save(appointment);
        clean.add(appointment);

        CalendarDataObject exception = appointments.createIdentifyingCopy(appointment);
        exception.setStartDate(D("03.01.2013 09:00"));
        exception.setEndDate(D("03.01.2013 11:00"));
        exception.setTitle("Exception");
        exception.setRecurrencePosition(3);
        appointments.save(exception);

        appointments.switchUser(thirdUser);
        int[] columns = new int[] { CalendarDataObject.OBJECT_ID, CalendarDataObject.FOLDER_ID };
        CalendarDataObject[] changeExceptionsByRecurrence = calendarCollectionService.getChangeExceptionsByRecurrence(appointment.getObjectID(), columns, appointments.getSession());
        assertEquals("Wrong folder id"+System.getProperty("line.separator")+Arrays.toString(changeExceptionsByRecurrence), sharedFolderId1, changeExceptionsByRecurrence[0].getParentFolderID());
        // System.out.println(changeExceptionsByRecurrence.toString());
    }

    @Override
    public void tearDown() throws Exception {
        folders.unsharePrivateFolder(session, ctx);
        super.tearDown();
    }

}
