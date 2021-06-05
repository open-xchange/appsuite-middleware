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
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.groupware.container.Appointment;

/**
 * Stores the parameters for inserting the appointment.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class InsertRequest extends AbstractAppointmentRequest<AppointmentInsertResponse> {

    /**
     * Appointment to insert.
     */
    final Appointment appointmentObj;

    /**
     * Time zone of the user.
     */
    final TimeZone timeZone;

    /**
     * Should the parser fail on error in server response.
     */
    final boolean failOnError;

    /**
     * More detailed constructor.
     * 
     * @param appointmentObj appointment to insert.
     * @param timeZone time zone of the user.
     * @param failOnError <code>true</code> to check the response for error
     *            messages.
     */
    public InsertRequest(final Appointment appointmentObj, final TimeZone timeZone, final boolean failOnError) {
        super();
        this.appointmentObj = appointmentObj;
        this.timeZone = timeZone;
        this.failOnError = failOnError;
    }

    /**
     * Default constructor.
     * 
     * @param appointment appointment to insert.
     * @param timeZone time zone of the user.
     */
    public InsertRequest(final Appointment appointment, final TimeZone timeZone) {
        this(appointment, timeZone, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject getBody() throws JSONException {
        return convert(appointmentObj, timeZone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW), new Parameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID()), new Parameter(AJAXServlet.PARAMETER_FOLDERID, String.valueOf(appointmentObj.getParentFolderID()))
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractAJAXParser<AppointmentInsertResponse> getParser() {
        return new InsertParser(failOnError);
    }
}
