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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13214Test extends AbstractAJAXSession {

    public Bug13214Test(String name) {
        super(name);
    }

    public void testBugAsWritten() throws Exception {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = client.getValues().getTimeZone();
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            // Step 1
            // Prepare appointment
            appointment.setTitle("Bug 13214 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 30);
            appointment.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            appointment.setEndDate(calendar.getTime());

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = client.execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 2
            // Prepare update appointment
            Appointment updateAppointment = new Appointment();
            updateAppointment.setObjectID(objectId);
            updateAppointment.setParentFolderID(folderId);
            updateAppointment.setIgnoreConflicts(true);
            updateAppointment.setLastModified(lastModified);
            calendar.set(Calendar.HOUR_OF_DAY, 11);
            updateAppointment.setStartDate(calendar.getTime());

            // Update
            UpdateRequest updateRequest = new UpdateRequest(updateAppointment, tz, false);
            UpdateResponse updateResponse = client.execute(updateRequest);

            try {
                assertTrue("No Exception occurred.", updateResponse.hasError());
                OXException e = updateResponse.getException();
                assertTrue("Wrong Exception", e instanceof OXException);
                assertTrue(
                    "Wrong Exception", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
            } finally {
                if (!updateResponse.hasError()) {
                    updateAppointment.setLastModified(updateResponse.getTimestamp());
                    lastModified = updateAppointment.getLastModified();
                }
            }

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                client.execute(deleteRequest);
            }
        }
    }
}
