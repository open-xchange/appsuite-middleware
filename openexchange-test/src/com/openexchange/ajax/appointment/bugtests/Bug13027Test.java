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
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.HasRequest;
import com.openexchange.ajax.appointment.action.HasResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13027Test extends AbstractAJAXSession {

    public Bug13027Test(String name) {
        super(name);
    }

    public void testNegativeTimeZone() throws Exception {
        AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = TimeZone.getTimeZone("America/New York");
        String formerTimeZone = "Europe/Berlin";
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            GetRequest getRequest = new GetRequest(Tree.TimeZone);
            GetResponse getResponse = getClient().execute(getRequest);
            formerTimeZone = getResponse.getString();
            SetRequest setRequest = new SetRequest(Tree.TimeZone, "America/New_York");
            client.execute(setRequest);

            // Step 1
            clear(tz, folderId, client);

            // Step 2
            // Prepare appointment
            appointment.setTitle("Bug 13027 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            appointment.setStartDate(calendar.getTime());
            appointment.setEndDate(calendar.getTime());
            appointment.setFullTime(true);

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = client.execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 3
            final HasRequest hasRequest = new HasRequest(new Date(1230508800000L), new Date(1230854400000L), tz); // 29.12.08 - 02.01.09
            final HasResponse hasResponse = client.execute(hasRequest);
            boolean[] values = hasResponse.getValues();
            assertFalse("No appointment expected.", values[1]); // 30.12.08
            assertFalse("No appointment expected.", values[2]); // 31.12.08
            assertTrue("Appointment expected.", values[3]); // 01.01.09

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                client.execute(deleteRequest);
            }

            SetRequest setRequest = new SetRequest(Tree.TimeZone, formerTimeZone);
            client.execute(setRequest);
        }
    }

    public void testPositiveTimeZone() throws Exception {
        AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        String formerTimeZone = "Europe/Berlin";
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            GetRequest getRequest = new GetRequest(Tree.TimeZone);
            GetResponse getResponse = getClient().execute(getRequest);
            formerTimeZone = getResponse.getString();
            SetRequest setRequest = new SetRequest(Tree.TimeZone, "Europe/Berlin");
            client.execute(setRequest);

            // Step 1
            clear(tz, folderId, client);

            // Step 2
            // Prepare appointment
            appointment.setTitle("Bug 13027 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(TimeZone.getTimeZone("UTC"));
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            appointment.setStartDate(calendar.getTime());
            appointment.setEndDate(calendar.getTime());
            appointment.setFullTime(true);

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = client.execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 3
            final HasRequest hasRequest = new HasRequest(new Date(1230508800000L), new Date(1230854400000L), tz); // 29.12.08 - 02.01.09
            final HasResponse hasResponse = client.execute(hasRequest);
            boolean[] values = hasResponse.getValues();
            assertFalse("No appointment expected.", values[1]); // 30.12.08
            assertFalse("No appointment expected.", values[2]); // 31.12.08
            assertTrue("Appointment expected.", values[3]); // 01.01.09

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                client.execute(deleteRequest);
            }

            SetRequest setRequest = new SetRequest(Tree.TimeZone, formerTimeZone);
            client.execute(setRequest);
        }
    }

    public void testUTC() throws Exception {
        AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = TimeZone.getTimeZone("UTC");
        String formerTimeZone = "Europe/Berlin";
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            GetRequest getRequest = new GetRequest(Tree.TimeZone);
            GetResponse getResponse = getClient().execute(getRequest);
            formerTimeZone = getResponse.getString();
            SetRequest setRequest = new SetRequest(Tree.TimeZone, "Europe/London");
            client.execute(setRequest);

            // Step 1
            clear(tz, folderId, client);

            // Step 2
            // Prepare appointment
            appointment.setTitle("Bug 13027 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            appointment.setStartDate(calendar.getTime());
            appointment.setEndDate(calendar.getTime());
            appointment.setFullTime(true);

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = client.execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 3
            final HasRequest hasRequest = new HasRequest(new Date(1230508800000L), new Date(1230854400000L), tz); // 29.12.08 - 02.01.09
            final HasResponse hasResponse = client.execute(hasRequest);
            boolean[] values = hasResponse.getValues();
            assertFalse("No appointment expected.", values[1]); // 30.12.08
            assertFalse("No appointment expected.", values[2]); // 31.12.08
            assertTrue("Appointment expected.", values[3]); // 01.01.09

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                client.execute(deleteRequest);
            }

            SetRequest setRequest = new SetRequest(Tree.TimeZone, formerTimeZone);
            client.execute(setRequest);
        }
    }

    public void testBugAsWritten() throws Exception {
        AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = TimeZone.getTimeZone("America/New York");
        String formerTimeZone = "Europe/Berlin";
        final Appointment appointment = new Appointment();
        int objectId = 0;
        Date lastModified = null;

        try {
            GetRequest getRequest = new GetRequest(Tree.TimeZone);
            GetResponse getResponse = getClient().execute(getRequest);
            formerTimeZone = getResponse.getString();
            SetRequest setRequest = new SetRequest(Tree.TimeZone, "America/New_York");
            client.execute(setRequest);

            // Step 1
            clear(tz, folderId, client);

            // Step 2
            // Prepare appointment
            appointment.setTitle("Bug 13027 Test");
            appointment.setParentFolderID(folderId);
            appointment.setIgnoreConflicts(true);
            final Calendar calendar = TimeTools.createCalendar(tz);
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            appointment.setStartDate(calendar.getTime());
            appointment.setEndDate(calendar.getTime());
            appointment.setFullTime(true);

            // Insert
            final InsertRequest insertRequest = new InsertRequest(appointment, tz, false);
            final AppointmentInsertResponse insertResponse = client.execute(insertRequest);
            appointment.setObjectID(insertResponse.getId());
            appointment.setLastModified(insertResponse.getTimestamp());
            objectId = appointment.getObjectID();
            appointment.setObjectID(objectId);
            lastModified = appointment.getLastModified();

            // Step 3
            final HasRequest hasRequest = new HasRequest(new Date(1230508800000L), new Date(1230854400000L), tz); // 29.12.08 - 02.01.09
            final HasResponse hasResponse = client.execute(hasRequest);
            boolean[] values = hasResponse.getValues();
            assertFalse("No appointment expected.", values[1]); // 30.12.08
            assertFalse("No appointment expected.", values[2]); // 31.12.08
            assertTrue("Appointment expected.", values[3]); // 01.01.09

        } finally {
            if (objectId != 0 && lastModified != null) {
                final DeleteRequest deleteRequest = new DeleteRequest(objectId, folderId, lastModified);
                client.execute(deleteRequest);
            }

            SetRequest setRequest = new SetRequest(Tree.TimeZone, formerTimeZone);
            client.execute(setRequest);
        }
    }

    private void clear(TimeZone tz, int folderId, AJAXClient client) throws Exception {
        Calendar start = new GregorianCalendar();
        start.setTimeZone(tz);
        start.set(Calendar.YEAR, 2008);
        start.set(Calendar.MONTH, Calendar.DECEMBER);
        start.set(Calendar.DAY_OF_MONTH, 30);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = new GregorianCalendar();
        end.setTimeZone(tz);
        end.set(Calendar.YEAR, 2009);
        end.set(Calendar.MONTH, Calendar.JANUARY);
        end.set(Calendar.DAY_OF_MONTH, 2);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        AllRequest request = new AllRequest(
            folderId,
            new int[] { Appointment.OBJECT_ID, Appointment.LAST_MODIFIED },
            start.getTime(),
            end.getTime(),
            tz);
        CommonAllResponse response = client.execute(request);

        Object[][] responseColumns = response.getArray();
        for (Object[] obj : responseColumns) {
            client.execute(new DeleteRequest(((Integer) obj[0]).intValue(), folderId, new Date(Long.MAX_VALUE)));
        }
    }

}
