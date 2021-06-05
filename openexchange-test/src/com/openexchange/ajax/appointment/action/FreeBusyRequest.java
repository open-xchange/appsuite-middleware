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

import java.util.Date;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.container.Appointment;

/**
 * 
 * {@link FreeBusyRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class FreeBusyRequest extends AbstractAppointmentRequest<FreeBusyResponse> {

    private final JSONObject body = new JSONObject();

    private final FreeBusyParser freeBusyParser;

    private int userId;

    private int type;

    private Date start;

    private Date end;

    public FreeBusyRequest(int userId, int type, final Date start, final Date end) {
        super();
        this.userId = userId;
        this.type = type;
        this.start = start;
        this.end = end;
        this.freeBusyParser = new FreeBusyParser(false, Appointment.ALL_COLUMNS);
        
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { 
            new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_FREEBUSY),
            new URLParameter(AJAXServlet.PARAMETER_ID, userId),
            new URLParameter(AJAXServlet.PARAMETER_TYPE, type),
            new URLParameter(AJAXServlet.PARAMETER_START, String.valueOf(start.getTime())),
            new URLParameter(AJAXServlet.PARAMETER_END, String.valueOf(end.getTime())),
        };
    }

    @Override
    public FreeBusyParser getParser() {
        return freeBusyParser;
    }

    @Override
    public Object getBody() {
        return body;
    }
}
