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
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.SearchRequest;
import com.openexchange.ajax.appointment.action.SearchResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13625Test extends AbstractAJAXSession {

    private Appointment appointment;

    private int userId;

    private final int[] columns = new int[] { Appointment.OBJECT_ID };

    public Bug13625Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        userId = getClient().getValues().getUserId();

        appointment = new Appointment();
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setTitle("Test Bug 13625");
        appointment.setStartDate(new Date(TimeTools.getHour(0, getClient().getValues().getTimeZone())));
        appointment.setEndDate(new Date(TimeTools.getHour(1, getClient().getValues().getTimeZone())));
        appointment.setParticipants(ParticipantTools.createParticipants(userId));
        appointment.setCategories("eins, zwei");
        appointment.setIgnoreConflicts(true);
        InsertRequest request = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(request);
        insertResponse.fillObject(appointment);
    }

    @Test
    public void testBug13625() throws Exception {
        SearchRequest searchRequest = new SearchRequest("eins", getClient().getValues().getPrivateAppointmentFolder(), columns, true);
        SearchResponse searchResponse = getClient().execute(searchRequest);
        final JSONArray jsonArray = (JSONArray) searchResponse.getResponse().getData();
        assertTrue("No results", jsonArray.length() > 0);
        boolean found = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray object = jsonArray.optJSONArray(i);
            if (object != null) {
                if (object.getInt(0) == appointment.getObjectID()) {
                    found = true;
                }
            }
        }
        assertTrue("Appointment not found", found);
    }

}
