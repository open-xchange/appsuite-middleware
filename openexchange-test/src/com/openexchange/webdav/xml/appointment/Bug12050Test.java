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

package com.openexchange.webdav.xml.appointment;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug12050Test extends AppointmentTest {

    private int objectId = -1;
    private Appointment appointment;
    private Appointment exception;
    private Appointment exceptionUpdate;

    public Bug12050Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        FolderTest.clearFolder(webCon, new int[] {appointmentFolderId}, new String[] {"calendar"}, new Date(), PROTOCOL + hostName, login, password, context);

        appointment = new Appointment();
        appointment.setTitle("testBug12050");
        appointment.setStartDate(startTime);
        appointment.setEndDate(endTime);
        appointment.setParentFolderID(appointmentFolderId);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(3);
        appointment.setIgnoreConflicts(true);

        exception = new Appointment();
        exception.setParentFolderID(appointmentFolderId);
        exception.setRecurrencePosition(2);
        exception.setTitle("testBug12050 - Exception");

        exceptionUpdate = new Appointment();
        exceptionUpdate.setParentFolderID(appointmentFolderId);
        Date exceptionStart = new Date(startTime.getTime() + 3600 * 25 * 1000);
        exceptionUpdate.setStartDate(exceptionStart);
        Date exceptionEnd = new Date(endTime.getTime() + 3600 * 25 * 1000);
        exceptionUpdate.setEndDate(exceptionEnd);
    }

    public void testBug12050() throws Exception {
        createAppointment();
        createException();
        updateException();
    }

    @Override
    public void tearDown() throws Exception {
        if (objectId  != -1) {
            deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        }

        super.tearDown();
    }

    private void createAppointment() throws Exception {
        objectId = insertAppointment(getWebConversation(), appointment, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        appointment.setObjectID(objectId);
    }

    private void createException() throws Exception {
        exception.setLastModified(appointment.getLastModified());
        int exceptionId = updateAppointment(getWebConversation(), exception, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword(), context);
        exception.setObjectID(exceptionId);
    }

    private void updateException() throws Exception {
        exceptionUpdate.setLastModified(exception.getLastModified());
        exceptionUpdate.setObjectID(exception.getObjectID());
        try {
            updateAppointment(getWebConversation(), exceptionUpdate, exception.getObjectID(), appointmentFolderId, getHostName(), getLogin(), getPassword(), context);
        } catch (OXException e) {
            fail(e.getMessage());
        }
    }

}
