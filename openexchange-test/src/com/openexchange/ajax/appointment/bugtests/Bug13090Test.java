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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Date;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;


/**
 * {@link Bug13090Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug13090Test extends AbstractAJAXSession {

    private Appointment appointment;
    private FolderObject folder;
    private Appointment exception;
    private Appointment updateAppointment;

    /**
     * Initializes a new {@link Bug13090Test}.
     * @param name
     */
    public Bug13090Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        folder = Create.folder(
            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
            "Bug 13090 Folder " + System.currentTimeMillis(),
            FolderObject.CALENDAR,
            FolderObject.PRIVATE,
            ocl(
                getClient().getValues().getUserId(),
                false,
                true,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION));

        CommonInsertResponse response = getClient().execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);

        appointment = new Appointment();
        appointment.setStartDate(D("11.04.2011 08:00"));
        appointment.setEndDate(D("11.04.2011 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(3);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug 13090 Test");

        InsertRequest insertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
        insertResponse.fillAppointment(appointment);

        exception = new Appointment();
        exception.setObjectID(appointment.getObjectID());
        exception.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        exception.setStartDate(D("12.04.2011 09:00"));
        exception.setEndDate(D("12.04.2011 10:00"));
        exception.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception.setIgnoreConflicts(true);
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setRecurrencePosition(2);

        UpdateRequest updateRequest = new UpdateRequest(exception, getClient().getValues().getTimeZone());
        getClient().execute(updateRequest);

        updateAppointment = new Appointment();
        updateAppointment.setObjectID(appointment.getObjectID());
        updateAppointment.setParentFolderID(folder.getObjectID());
        updateAppointment.setIgnoreConflicts(true);
        updateAppointment.setLastModified(new Date(Long.MAX_VALUE));
    }

    public void testErrorMessag() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(getClient().getValues().getPrivateAppointmentFolder(), updateAppointment, getClient().getValues().getTimeZone(), false);
        UpdateResponse updateResponse = getClient().execute(updateRequest);
        if (updateResponse.hasError()) {
            assertTrue("Wrong exception code.", updateResponse.getException().similarTo(OXCalendarExceptionCodes.RECURRING_FOLDER_MOVE.create()));
        } else {
            fail("Error expected.");
        }
    }

    @Override
    public void tearDown() throws Exception {
        getClient().execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder));
        appointment.setLastModified(new Date(Long.MAX_VALUE));
        getClient().execute(new DeleteRequest(appointment));

        super.tearDown();
    }

}
