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

package com.openexchange.ajax.appointment.action;

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;

/**
 *
 * {@link CopyResponse}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class CopyResponse extends AbstractAJAXResponse {

    private Appointment appointmentObj;

    /**
     * @param response
     */
    CopyResponse(final Response response) {
        super(response);
    }

    /**
     * @return the appointment
     * @throws OXException parsing the appointment out of the response fails.
     * @throws JSONException if parsing of some extra values fails.
     */
    public Appointment getAppointment(final TimeZone timeZone) throws OXException {
        if (null == appointmentObj) {
            this.appointmentObj = new Appointment();
            Response resp = getResponse();
            assertFalse(resp.getErrorMessage(), resp.hasError());
            final JSONObject json = (JSONObject) resp.getData();
            new AppointmentParser(true, timeZone).parse(appointmentObj, json);
        }
        return appointmentObj;
    }

    /**
     * @param appointmentObj the appointment to set
     */
    public void setAppointment(final Appointment appointmentObj) {
        this.appointmentObj = appointmentObj;
    }
}
