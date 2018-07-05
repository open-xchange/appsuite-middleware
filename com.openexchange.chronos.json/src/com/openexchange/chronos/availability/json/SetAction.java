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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.ajax.requesthandler.AJAXActionService#perform(com.openexchange.ajax.requesthandler.AJAXRequestData, com.openexchange.tools.session.ServerSession)
     */
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
