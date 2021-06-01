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

package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 *
 */
public final class Bug12444Test extends AbstractAJAXSession {

    /**
     * Default constructor.
     * 
     * @param name test name.
     */
    public Bug12444Test() {
        super();
    }

    @Test
    public void testExternalWithoutEmail() throws Throwable {
        final int folderId = getClient().getValues().getPrivateAppointmentFolder();
        final TimeZone tz = getClient().getValues().getTimeZone();
        final Appointment appointment = new Appointment();
        appointment.setTitle("Test for bug 12444");
        final Calendar calendar = TimeTools.createCalendar(tz);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setParentFolderID(folderId);
        appointment.setParticipants(createParticipants());
        final InsertRequest request = new InsertRequest(appointment, tz, false);
        final CommonInsertResponse response = getClient().execute(request);
        assertTrue("Server responded not with expected exception.", response.hasError());
        final OXException e = response.getException();
        assertEquals("Wrong exception code.", 8, e.getCode());
        assertEquals("Wrong exception category.", Category.CATEGORY_USER_INPUT, e.getCategory());
    }

    private Participant[] createParticipants() {
        final ExternalUserParticipant p1 = new ExternalUserParticipant("");
        p1.setDisplayName("User 1");
        final ExternalUserParticipant p2 = new ExternalUserParticipant("user@example.com");
        p2.setDisplayName("User 2");
        return new Participant[] { p1, p2 };
    }
}
