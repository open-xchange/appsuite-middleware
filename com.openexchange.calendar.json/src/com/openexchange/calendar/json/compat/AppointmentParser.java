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

package com.openexchange.calendar.json.compat;

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.parser.CalendarParser;
import com.openexchange.exception.OXException;

/**
 * moved from com.openexchange.ajax.parser.AppointmentParser
 *
 * AppointmentParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class AppointmentParser extends CalendarParser {

    protected AppointmentParser() {
        super();
    }

    public AppointmentParser(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public AppointmentParser(final boolean parseAll, final TimeZone timeZone) {
        this.parseAll = parseAll;
        this.timeZone = timeZone;
    }

    public void parse(final Appointment appointmentobject, final JSONObject jsonobject) throws OXException {
        try {
            parseElementAppointment(appointmentobject, jsonobject);
        } catch (OXException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new OXException(exc);
        }
    }

    protected void parseElementAppointment(final Appointment appointmentobject, final JSONObject jsonobject) throws JSONException, OXException {
        boolean isFullTime = jsonobject.has(CalendarFields.FULL_TIME) && parseBoolean(jsonobject, CalendarFields.FULL_TIME);

        if (jsonobject.has(CalendarFields.START_DATE)) {
            if (isFullTime) {
                appointmentobject.setStartDate(parseDate(jsonobject, CalendarFields.START_DATE));
            } else {
                appointmentobject.setStartDate(parseTime(jsonobject, CalendarFields.START_DATE, timeZone));
            }
        }

        if (jsonobject.has(CalendarFields.END_DATE)) {
            if (isFullTime) {
                appointmentobject.setEndDate(parseDate(jsonobject, CalendarFields.END_DATE));
            } else {
                appointmentobject.setEndDate(parseTime(jsonobject, CalendarFields.END_DATE, timeZone));
            }
        }

        if (jsonobject.has(AppointmentFields.SHOW_AS)) {
            appointmentobject.setShownAs(parseInt(jsonobject, AppointmentFields.SHOW_AS));
        }

        if (jsonobject.has(AppointmentFields.LOCATION)) {
            appointmentobject.setLocation(parseString(jsonobject, AppointmentFields.LOCATION));
        }

        if (jsonobject.has(CommonFields.COLORLABEL)) {
            appointmentobject.setLabel(parseInt(jsonobject, CommonFields.COLORLABEL));
        }

        if (jsonobject.has(CalendarFields.ALARM)) {
            appointmentobject.setAlarm(parseInt(jsonobject, CalendarFields.ALARM));
        }

        if (jsonobject.has(AppointmentFields.IGNORE_CONFLICTS)) {
            appointmentobject.setIgnoreConflicts(parseBoolean(jsonobject, AppointmentFields.IGNORE_CONFLICTS));
        }

        if (jsonobject.has(AppointmentFields.IGNORE_OUTDATED_SEQUENCE)) {
            appointmentobject.setIgnoreOutdatedSequence(parseBoolean(jsonobject, AppointmentFields.IGNORE_OUTDATED_SEQUENCE));
        }

        if (jsonobject.has(AppointmentFields.TIMEZONE)) {
            appointmentobject.setTimezone(parseString(jsonobject, AppointmentFields.TIMEZONE));
        }

        if (jsonobject.has(CalendarFields.RECURRENCE_START)) {
            appointmentobject.setRecurringStart(parseDate(jsonobject, CalendarFields.RECURRENCE_START).getTime());
        }

        if (parseAll) {
            if (jsonobject.has(CalendarFields.CHANGE_EXCEPTIONS)) {
                appointmentobject.setChangeExceptions(parseJSONDateArray(jsonobject, CalendarFields.CHANGE_EXCEPTIONS));
            }
            if (jsonobject.has(CalendarFields.DELETE_EXCEPTIONS)) {
                appointmentobject.setDeleteExceptions(parseJSONDateArray(jsonobject, CalendarFields.DELETE_EXCEPTIONS));
            }
        }

        parseElementCalendar(appointmentobject, jsonobject);
    }
}
