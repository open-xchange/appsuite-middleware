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

package com.openexchange.chronos.availability.json;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.availability.json.mapper.AvailableMapper;
import com.openexchange.chronos.service.CalendarAvailabilityService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SetAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SetAction extends AbstractAction {

    /**
     * Initialises a new {@link SetAction}.
     */
    public SetAction(ServiceLookup services) {
        super(services);

    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        JSONObject requestBody = getRequestBody(requestData, JSONObject.class);
        try {
            CalendarAvailabilityService service = services.getService(CalendarAvailabilityService.class);
            CalendarSession calendarSession = getSession(session);

            // Delete the user's availability
            if (requestBody.isEmpty() || !requestBody.hasAndNotNull("availableTimes")) {
                service.deleteAvailability(calendarSession);
                return new AJAXRequestResult();
            }

            JSONArray availableTimesArray = requestBody.getJSONArray("availableTimes");

            // Parse the availability and the available blocks
            List<Available> availables = new ArrayList<>(requestBody.length());
            for (int index = 0; index < availableTimesArray.length(); index++) {
                availables.add(AvailableMapper.getInstance().deserialize(availableTimesArray.getJSONObject(index), AvailableMapper.getInstance().getMappedFields()));
            }

            Availability availability = new Availability();
            availability.setAvailable(availables);

            checkAndSetUids(availability);
            // Set the availability for the user and overwrite any existing one
            service.setAvailability(calendarSession, availability);

            return new AJAXRequestResult();
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

    /**
     * Checks that the specified {@link Availability} and its {@link Available} blocks have
     * their uids set. If not, they are set accordingly.
     * 
     * @param availability the {@link Availability} to check
     */
    private void checkAndSetUids(Availability availability) {
        if (availability.getUid() == null) {
            availability.setUid(UUID.randomUUID().toString());
        }
        for (Available available : availability.getAvailable()) {
            if (available.getUid() == null) {
                available.setUid(UUID.randomUUID().toString());
            }
        }
    }
}
