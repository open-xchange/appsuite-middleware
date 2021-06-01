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

package com.openexchange.ajax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import org.junit.Before;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.test.common.test.OXTestToolkit;

/**
 * {@link AppointmentTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - added parseUserParticipants
 */
public class AppointmentTest extends AbstractAJAXSession {

    public static final int[] APPOINTMENT_FIELDS = { DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY, FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, CalendarObject.START_DATE, CalendarObject.END_DATE, Appointment.LOCATION, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE, CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.SHOWN_AS, Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION, Appointment.FULL_TIME, Appointment.COLOR_LABEL, Appointment.DELETE_EXCEPTIONS, Appointment.CHANGE_EXCEPTIONS, Appointment.RECURRENCE_START, Appointment.ORGANIZER, Appointment.UID, Appointment.SEQUENCE };

    protected static final String APPOINTMENT_URL = "/ajax/calendar";

    protected int appointmentFolderId = -1;

    protected long startTime = 0;

    protected long endTime = 0;

    protected final long dayInMillis = 86400000;

    protected int userId = 0;

    protected TimeZone timeZone = null;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentTest.class);

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        try {
            appointmentFolderId = getClient().getValues().getPrivateAppointmentFolder();
            userId = getClient().getValues().getUserId();
            timeZone = getClient().getValues().getTimeZone();

            LOG.debug(new StringBuilder().append("use timezone: ").append(timeZone).toString());

            final Calendar c = Calendar.getInstance();
            c.setTimeZone(timeZone);
            c.set(Calendar.HOUR_OF_DAY, 8);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            startTime = c.getTimeInMillis();
            startTime += timeZone.getOffset(startTime);
            endTime = startTime + 3600000;
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    protected void compareObject(final Appointment appointmentObj1, final Appointment appointmentObj2) {
        compareObject(appointmentObj1, appointmentObj2, appointmentObj1.getStartDate().getTime(), appointmentObj1.getEndDate().getTime());
    }

    protected void compareObject(final Appointment appointmentObj1, final Appointment appointmentObj2, final long newStartTime, final long newEndTime) {
        assertEquals("id", appointmentObj1.getObjectID(), appointmentObj2.getObjectID());
        OXTestToolkit.assertEqualsAndNotNull("title", appointmentObj1.getTitle(), appointmentObj2.getTitle());
        assertEquals("start", newStartTime, appointmentObj2.getStartDate().getTime());
        assertEquals("end", newEndTime, appointmentObj2.getEndDate().getTime());
        OXTestToolkit.assertEqualsAndNotNull("location", appointmentObj1.getLocation(), appointmentObj2.getLocation());
        assertEquals("shown_as", appointmentObj1.getShownAs(), appointmentObj2.getShownAs());
        assertEquals("folder id", appointmentObj1.getParentFolderID(), appointmentObj2.getParentFolderID());
        assertTrue("private flag", appointmentObj1.getPrivateFlag() == appointmentObj2.getPrivateFlag());
        assertTrue("full time", appointmentObj1.getFullTime() == appointmentObj2.getFullTime());
        assertEquals("label", appointmentObj1.getLabel(), appointmentObj2.getLabel());
        assertEquals("recurrence_type", appointmentObj1.getRecurrenceType(), appointmentObj2.getRecurrenceType());
        assertEquals("interval", appointmentObj1.getInterval(), appointmentObj2.getInterval());
        assertEquals("days", appointmentObj1.getDays(), appointmentObj2.getDays());
        assertEquals("month", appointmentObj1.getMonth(), appointmentObj2.getMonth());
        assertEquals("day_in_month", appointmentObj1.getDayInMonth(), appointmentObj2.getDayInMonth());
        assertEquals("until", appointmentObj1.getUntil(), appointmentObj2.getUntil());
        if (appointmentObj1.getOrganizer() != null && appointmentObj2.getOrganizer() != null) {
            assertEquals("organizer", appointmentObj1.getOrganizer(), appointmentObj2.getOrganizer());
        }
        if (appointmentObj1.containsUid()) {
            assertEquals("uid", appointmentObj1.getUid(), appointmentObj2.getUid());
        }
        // assertEquals("sequence", appointmentObj1.getSequence(), appointmentObj2.getSequence());
        OXTestToolkit.assertEqualsAndNotNull("note", appointmentObj1.getNote(), appointmentObj2.getNote());
        OXTestToolkit.assertEqualsAndNotNull("categories", appointmentObj1.getCategories(), appointmentObj2.getCategories());
        OXTestToolkit.assertEqualsAndNotNull("delete_exceptions", appointmentObj1.getDeleteException(), appointmentObj2.getDeleteException());

        OXTestToolkit.assertEqualsAndNotNull("participants are not equals", participants2String(appointmentObj1.getParticipants()), participants2String(appointmentObj2.getParticipants()));
    }

    protected Appointment createAppointmentObject(final String title) {
        final Appointment appointmentobject = new Appointment();
        appointmentobject.setTitle(title);
        appointmentobject.setStartDate(new Date(startTime));
        appointmentobject.setEndDate(new Date(endTime));
        appointmentobject.setLocation("Location");
        appointmentobject.setShownAs(Appointment.ABSENT);
        appointmentobject.setParentFolderID(appointmentFolderId);
        appointmentobject.setIgnoreConflicts(true);
        return appointmentobject;
    }

    private HashSet<String> participants2String(final Participant[] participant) {
        if (participant == null) {
            return null;
        }

        final HashSet<String> hs = new HashSet<>();

        for (Participant element : participant) {
            hs.add(participant2String(element));
        }

        return hs;
    }

    private String participant2String(final Participant p) {
        final StringBuffer sb = new StringBuffer();
        sb.append("T" + p.getType());
        sb.append("ID" + p.getIdentifier());
        sb.append("E" + p.getEmailAddress());
        sb.append("D" + p.getDisplayName());

        return sb.toString();
    }

    protected Appointment link(final Appointment base, final Appointment update) {
        update.setLastModified(base.getLastModified());
        update.setParentFolderID(base.getParentFolderID());
        update.setObjectID(base.getObjectID());
        return update;
    }
}
