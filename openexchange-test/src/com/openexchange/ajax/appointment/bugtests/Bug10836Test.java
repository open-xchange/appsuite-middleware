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

import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDInt;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * Checks if the calendar has a vulnerability in the list request.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug10836Test extends AbstractAJAXSession {

    /**
     * Default constructor.
     * 
     * @param name Name of the test.
     */
    public Bug10836Test() {
        super();
    }

    /**
     * Creates a private appointment with user A and tries to read it with user
     * B through a list request.
     * 
     * @throws Throwable if some exception occurs.
     */
    @Test
    public void testVulnerability() throws Throwable {
        final AJAXClient clientA = getClient();
        final AJAXClient clientB = testUser2.getAjaxClient();
        final int folderA = clientA.getValues().getPrivateAppointmentFolder();
        final int folderB = clientB.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = clientA.getValues().getTimeZone();
        final Appointment app = new Appointment();
        app.setParentFolderID(folderA);
        app.setTitle("Bug10836Test");
        app.setStartDate(new Date(TimeTools.getHour(0, tz)));
        app.setEndDate(new Date(TimeTools.getHour(1, tz)));
        app.setIgnoreConflicts(true);
        final CommonInsertResponse insertR = clientA.execute(new InsertRequest(app, tz));
        try {
            final ListIDs list = new ListIDs();
            list.add(new ListIDInt(folderB, insertR.getId()));
            final CommonListResponse listR = clientB.execute(new ListRequest(list, new int[] { Appointment.TITLE }, false));

            assertTrue(listR.hasError());
            /*
             * for (Object[] obj1 : listR) {
             * for (Object obj2 : obj1) {
             * assertNull(obj2);
             * }
             * }
             */
        } finally {
            clientA.execute(new DeleteRequest(insertR.getId(), folderA, insertR.getTimestamp()));
        }
    }
}
