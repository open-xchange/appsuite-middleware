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
import com.openexchange.groupware.container.Appointment;

/**
 * Implements creating the necessary values for a appointment update request. All necessary values are read from the appointment object. The
 * appointment must contain the folder and object identifier and the last modification timestamp.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class UpdateRequest extends AbstractAppointmentRequest<UpdateResponse> {

    private final Appointment appointmentObj;

    private final TimeZone timeZone;

    private final boolean failOnError;

    private final int originFolder;

    /**
     * Default constructor.
     *
     * @param appointment Appointment object with updated attributes. This appointment must contain the attributes parent folder
     *            identifier, object identifier and last modification timestamp.
     */
    public UpdateRequest(final Appointment appointment, final TimeZone timeZone) {
        this(appointment, timeZone, true);
    }

    public UpdateRequest(final Appointment appointment, final TimeZone timezone, final boolean failOnError) {
        this(appointment.getParentFolderID(), appointment, timezone, failOnError);

    }

    /**
     * Special constructor for moving appointment.
     * 
     * @param originFolder folder where the appointment is located currently.
     * @param appointment destination appointment object.
     * @param timezone time zone for correctly shifting times.
     * @param failOnError <code>false</code> if exception from backend should be ignored.
     */
    public UpdateRequest(final int originFolder, final Appointment appointment, final TimeZone timezone, final boolean failOnError) {
        super();
        this.appointmentObj = appointment;
        this.timeZone = timezone;
        this.failOnError = failOnError;
        this.originFolder = originFolder;
    }

    @Override
    public JSONObject getBody() throws JSONException {
        return convert(appointmentObj, timeZone);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE), new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(this.originFolder)), new Parameter(AJAXServlet.PARAMETER_ID, String.valueOf(appointmentObj.getObjectID())), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(appointmentObj.getLastModified().getTime())) };
    }

    @Override
    public UpdateParser getParser() {
        return new UpdateParser(failOnError);
    }

    protected Appointment getAppointment() {
        return appointmentObj;
    }
}
