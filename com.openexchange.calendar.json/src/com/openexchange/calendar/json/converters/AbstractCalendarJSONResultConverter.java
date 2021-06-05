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

package com.openexchange.calendar.json.converters;

import java.util.TimeZone;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.AppointmentAJAXRequestFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractCalendarJSONResultConverter}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public abstract class AbstractCalendarJSONResultConverter implements ResultConverter {

    protected static final String OUTPUT_FORMAT = "json";

    @Override
    public String getOutputFormat() {
        return OUTPUT_FORMAT;
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        final TimeZone timeZone;
        final String sTimeZone = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
        if (null != sTimeZone) {
            timeZone = TimeZoneUtils.getTimeZone(sTimeZone);
        } else {
            timeZone = TimeZoneUtils.getTimeZone(session.getUser().getTimeZone());
        }
        convertCalendar(
            AppointmentAJAXRequestFactory.createAppointmentAJAXRequest(requestData, session),
            result,
            session,
            converter,
            timeZone);
    }

    protected abstract void convertCalendar(AppointmentAJAXRequest request, AJAXRequestResult result, ServerSession session, Converter converter, TimeZone userTimeZone) throws OXException;

    protected TimeZone getTimeZone(final String timeZoneId) {
        return TimeZoneUtils.getTimeZone(timeZoneId);
    }

}
