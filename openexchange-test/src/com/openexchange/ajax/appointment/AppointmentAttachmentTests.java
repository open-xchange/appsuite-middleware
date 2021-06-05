/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * Attachment tests for appointments.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AppointmentAttachmentTests extends AbstractAJAXSession {

    private int folderId;

    private TimeZone tz;

    private Appointment appointment;

    private int attachmentId;

    private Date creationDate;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderId = getClient().getValues().getPrivateAppointmentFolder();
        tz = getClient().getValues().getTimeZone();
        appointment = new Appointment();
        appointment.setTitle("Test appointment for testing attachments");
        Calendar calendar = TimeTools.createCalendar(tz);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setParentFolderID(folderId);
        appointment.setIgnoreConflicts(true);
        getClient().execute(new InsertRequest(appointment, tz)).fillAppointment(appointment);
        attachmentId = getClient().execute(new AttachRequest(appointment, "test.txt", new ByteArrayInputStream("Test".getBytes()), "text/plain")).getId();
        com.openexchange.ajax.attach.actions.GetResponse response = getClient().execute(new com.openexchange.ajax.attach.actions.GetRequest(appointment, attachmentId));
        long timestamp = response.getAttachment().getCreationDate().getTime();
        creationDate = new Date(timestamp - tz.getOffset(timestamp));
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithGet() throws Throwable {
        GetResponse response = getClient().execute(new GetRequest(appointment.getParentFolderID(), appointment.getObjectID()));
        appointment.setLastModified(response.getTimestamp());
        Appointment test = response.getAppointment(tz);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithAll() throws Throwable {
        Date rangeStart = TimeTools.getAPIDate(tz, appointment.getStartDate(), 0);
        Date rangeEnd = TimeTools.getAPIDate(tz, appointment.getEndDate(), 1);
        CommonAllResponse response = getClient().execute(new AllRequest(appointment.getParentFolderID(), new int[] { Appointment.OBJECT_ID, Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }, rangeStart, rangeEnd, tz, true));
        appointment.setLastModified(response.getTimestamp());
        Appointment test = null;
        int objectIdPos = response.getColumnPos(Appointment.OBJECT_ID);
        int lastModifiedOfNewestAttachmentPos = response.getColumnPos(Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT);
        for (Object[] objA : response) {
            if (appointment.getObjectID() == ((Integer) objA[objectIdPos]).intValue()) {
                test = new Appointment();
                test.setLastModifiedOfNewestAttachment(new Date(((Long) objA[lastModifiedOfNewestAttachmentPos]).longValue()));
                break;
            }
        }
        Assert.assertNotNull("Can not find the created appointment with an attachment.", test);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithList() throws Throwable {
        CommonListResponse response = getClient().execute(new ListRequest(ListIDs.l(new int[] { appointment.getParentFolderID(), appointment.getObjectID() }), new int[] { Appointment.OBJECT_ID, Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }));
        appointment.setLastModified(response.getTimestamp());
        Appointment test = null;
        int objectIdPos = response.getColumnPos(Appointment.OBJECT_ID);
        int lastModifiedOfNewestAttachmentPos = response.getColumnPos(Appointment.LAST_MODIFIED_OF_NEWEST_ATTACHMENT);
        for (Object[] objA : response) {
            if (appointment.getObjectID() == ((Integer) objA[objectIdPos]).intValue()) {
                test = new Appointment();
                test.setLastModifiedOfNewestAttachment(new Date(((Long) objA[lastModifiedOfNewestAttachmentPos]).longValue()));
                break;
            }
        }
        Assert.assertNotNull("Can not find the created appointment with an attachment.", test);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }
}
