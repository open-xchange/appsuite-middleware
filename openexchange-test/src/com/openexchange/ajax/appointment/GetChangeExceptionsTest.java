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

package com.openexchange.ajax.appointment;

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetChangeExceptionsRequest;
import com.openexchange.ajax.appointment.action.GetChangeExceptionsResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.cache.OXCachingExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link GetChangeExceptionsTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class GetChangeExceptionsTest extends AbstractAJAXSession {

    private Appointment appointment;

    private Appointment exception1;

    private Appointment exception2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        /*
         * reset permissions in default calendar folder of user a if required
         */
        com.openexchange.ajax.folder.actions.GetResponse folderGetResponse = getClient().execute(
            new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_OLD, getClient().getValues().getPrivateAppointmentFolder()));
        FolderObject calendarFolder = folderGetResponse.getFolder();
        if (1 < calendarFolder.getPermissions().size()) {
            FolderObject folderUpdate = new FolderObject(calendarFolder.getObjectID());
            folderUpdate.setLastModified(folderGetResponse.getTimestamp());
            folderUpdate.setPermissionsAsArray(new OCLPermission[] { ocl(
                getClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION) });
            getClient().execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, folderUpdate)).getResponse();
        }

        appointment = new Appointment();
        appointment.setStartDate(D("01.05.2013 08:00"));
        appointment.setEndDate(D("01.05.2013 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setAlarm(30);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Change Exception test");

        InsertRequest insertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
        insertResponse.fillAppointment(appointment);

        exception1 = new Appointment();
        exception1.setObjectID(appointment.getObjectID());
        exception1.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        exception1.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception1.setIgnoreConflicts(true);
        exception1.setLastModified(new Date(Long.MAX_VALUE));
        exception1.setRecurrencePosition(2);
        exception1.setAlarm(30);
        exception1.setTitle("Exception 1");

        UpdateRequest updateRequest = new UpdateRequest(exception1, getClient().getValues().getTimeZone());
        getClient().execute(updateRequest);

        exception2 = new Appointment();
        exception2.setObjectID(appointment.getObjectID());
        exception2.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        exception2.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception2.setIgnoreConflicts(true);
        exception2.setLastModified(new Date(Long.MAX_VALUE));
        exception2.setRecurrencePosition(5);
        exception2.setAlarm(30);
        exception2.setTitle("Exception 2");

        updateRequest = new UpdateRequest(exception2, getClient().getValues().getTimeZone());
        getClient().execute(updateRequest);
    }

    @Test
    public void testGetChangeExceptions() throws Exception {
        int[] columns = new int[] { Appointment.OBJECT_ID, Appointment.RECURRENCE_ID, Appointment.TITLE, Appointment.ALARM };
        GetChangeExceptionsRequest request = new GetChangeExceptionsRequest(appointment.getParentFolderID(), appointment.getObjectID(), columns);
        GetChangeExceptionsResponse response = getClient().execute(request);

        List<Appointment> exceptions = response.getAppointments(getClient().getValues().getTimeZone());

        assertEquals("Wrong amount of returned exceptions.", 2, exceptions.size());

        boolean foundFirst = false;
        boolean foundSecond = false;
        for (Appointment exception : exceptions) {
            assertEquals("Wrong recurrence id.", appointment.getObjectID(), exception.getRecurrenceID());
            if (exception.getTitle().equals(exception1.getTitle())) {
                foundFirst = true;
            }
            if (exception.getTitle().equals(exception2.getTitle())) {
                foundSecond = true;
            }
        }

        assertTrue("Missing exception.", foundFirst);
        assertTrue("Missing exception.", foundSecond);
    }

    @Test
    public void testPermission() throws Exception {
        int[] columns = new int[] { Appointment.OBJECT_ID, Appointment.RECURRENCE_ID, Appointment.TITLE };
        GetChangeExceptionsRequest request = new GetChangeExceptionsRequest(appointment.getParentFolderID(), appointment.getObjectID(), columns, false);

        GetChangeExceptionsResponse response = getClient2().execute(request);
        assertTrue("Missing error.", response.hasError());
        OXException oxException = response.getException();
        assertEquals("Wrong error.", OXCachingExceptionCode.CATEGORY_PERMISSION_DENIED, oxException.getCategory());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (appointment != null) {
                appointment.setLastModified(new Date(Long.MAX_VALUE));
                getClient().execute(new DeleteRequest(appointment));
            }
        } finally {
            super.tearDown();
        }
    }

}
