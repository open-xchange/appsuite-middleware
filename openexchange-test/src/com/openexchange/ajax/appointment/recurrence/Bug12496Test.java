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

package com.openexchange.ajax.appointment.recurrence;

import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * Verifies the until date of a full time daily series appointment.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12496Test extends AbstractAJAXSession {

    private TimeZone tz;

    private int folderId;

    private Appointment appointment;

    /**
     * Default constructor.
     * @param name test name.
     */
    public Bug12496Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final AJAXClient client = getClient();
        tz = client.getValues().getTimeZone();
        folderId = client.getValues().getPrivateAppointmentFolder();
        appointment = createAppointment();
        final InsertRequest request = new InsertRequest(appointment, tz);
        final AppointmentInsertResponse response = client.execute(request);
        response.fillAppointment(appointment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        final DeleteRequest request = new DeleteRequest(appointment);
        getClient().execute(request);
        super.tearDown();
    }

    public void testDailyFullTimeUntil() throws Throwable {
        final AJAXClient client = getClient();
        final Appointment changed = changeAppointment();
        final UpdateRequest request = new SpecialUpdateRequest(changed, tz);
        final UpdateResponse response = client.execute(request);
        appointment.setLastModified(response.getTimestamp());
    }

    private Appointment createAppointment() {
        final Calendar calendar = TimeTools.createCalendar(TimeZone
            .getTimeZone("UTC"));
        final Appointment appointment = new Appointment();
        appointment.setTitle("test for bug 12496");
        appointment.setParentFolderID(folderId);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.DATE, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setFullTime(true);
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(2);
        return appointment;
    }

    private Appointment changeAppointment() {
        final Calendar calendar = TimeTools.createCalendar(tz);
        final Appointment changed = new Appointment();
        changed.setTitle("test for bug 12496 changed");
        changed.setParentFolderID(folderId);
        changed.setObjectID(appointment.getObjectID());
        changed.setLastModified(appointment.getLastModified());
        changed.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        changed.setFullTime(false);
        changed.setEndDate(calendar.getTime());
        changed.setUntil(null);
        return changed;
    }

    private static final class SpecialUpdateRequest extends UpdateRequest {
        public SpecialUpdateRequest(final Appointment appointment,
            final TimeZone tz) {
            super(appointment, tz);
        }
        @Override
        public JSONObject getBody() throws JSONException {
            final JSONObject retval = super.getBody();
            retval.put(AppointmentFields.UNTIL, JSONObject.NULL);
            return retval;
        }
    }
}
