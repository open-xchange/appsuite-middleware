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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;

/**
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class NewAppointmentSearchRequest extends AbstractAppointmentRequest<NewAppointmentSearchResponse> {

    /**
     * The start range
     */
    private final Date start;

    /**
     * The end range
     */
    private final Date end;

    /**
     * The max count of returned appointments
     */
    private final int limit;

    private final TimeZone timeZone;

    private int[] columns = { DataObject.OBJECT_ID, FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION, CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE, Appointment.SHOWN_AS, Appointment.FULL_TIME, Appointment.COLOR_LABEL
    };

    /**
     * Default constructor.
     */
    public NewAppointmentSearchRequest(final Date start, final Date end, final int limit, final TimeZone timeZone) {
        super();
        this.start = start;
        this.end = end;
        this.limit = limit;
        this.timeZone = timeZone;
    }

    public NewAppointmentSearchRequest(final Date start, final Date end, final int limit, final TimeZone timeZone, final int[] columns) {
        super();
        this.start = start;
        this.end = end;
        this.limit = limit;
        this.timeZone = timeZone;
        this.columns = columns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return Method.GET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW_APPOINTMENTS));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_START, String.valueOf(start.getTime())));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_END, String.valueOf(end.getTime())));
        parameterList.add(new Parameter("limit", String.valueOf(limit)));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));

        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NewAppointmentSearchParser getParser() {
        return new NewAppointmentSearchParser(columns, timeZone);
    }
}
