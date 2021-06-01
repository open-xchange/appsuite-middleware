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

package com.openexchange.ajax.appointment.recurrence;

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link DeleteExceptionTimestampTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class DeleteExceptionTimestampTest extends AbstractAJAXSession {

    private Appointment appointment;

    public DeleteExceptionTimestampTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Create series
        appointment = new Appointment();
        appointment.setTitle(this.getClass().getCanonicalName());
        appointment.setStartDate(D("24/02/2007 10:00"));
        appointment.setEndDate(D("24/02/2007 12:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(5);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(appointment);
    }

    @Test
    public void testTimestampShouldBeDifferentAfterCreatingDeleteException() throws OXException, IOException, JSONException {
        Date oldTimestamp = appointment.getLastModified();

        DeleteRequest deleteRequest = new DeleteRequest(appointment.getObjectID(), appointment.getParentFolderID(), 3, oldTimestamp, true);
        CommonDeleteResponse response = getClient().execute(deleteRequest);

        assertTrue("Timestamp should be later", oldTimestamp.before(response.getTimestamp()));
    }

}
