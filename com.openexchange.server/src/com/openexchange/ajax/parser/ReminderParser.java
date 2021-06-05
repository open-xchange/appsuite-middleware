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

package com.openexchange.ajax.parser;

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.ReminderFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * JSON Parser for reminder objects.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ReminderParser extends DataParser {

    /**
     * Default constructor.
     * @param timeZone TimeZone for converting timestamps.
     */
    public ReminderParser(final TimeZone timeZone) {
        super(timeZone);
    }

    /**
     * Parses the attributes from the JSON and writes them into the reminder
     * object.
     * @param reminder attributes will be stored in this reminder object.
     * @param json a JSON object containing a reminder.
     * @throws OXException if parsing fails.
     */
    public void parse(final ReminderObject reminder, final JSONObject json)
        throws OXException {
        try {
            parseElementReminder(reminder, json);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e,
                json.toString());
        }
    }

    protected void parseElementReminder(final ReminderObject reminder,
        final JSONObject json) throws JSONException, OXException {
        if (json.has(DataFields.LAST_MODIFIED)) {
            reminder.setLastModified(parseDate(json, DataFields.LAST_MODIFIED));
        }
        if (json.has(ReminderFields.TARGET_ID)) {
            reminder.setTargetId(parseInt(json, ReminderFields.TARGET_ID));
        }
        if (json.has(ReminderFields.FOLDER)) {
            reminder.setFolder(parseInt(json, ReminderFields.FOLDER));
        }
        if (json.has(ReminderFields.ALARM)) {
            reminder.setDate(parseTime(json, ReminderFields.ALARM, getTimeZone()));
        }
        if (json.has(ReminderFields.MODULE)) {
            reminder.setModule(parseInt(json, ReminderFields.MODULE));
        }
        if (json.has(ReminderFields.USER_ID)) {
            reminder.setUser(parseInt(json, ReminderFields.USER_ID));
        }
        if (json.has(ReminderFields.RECURRENCE_APPOINTMENT)) {
            reminder.setRecurrenceAppointment(parseBoolean(json, ReminderFields.RECURRENCE_APPOINTMENT));
        }
        /* SERVER_TIME isn't parsed
         * writeParameter(ReminderFields.SERVER_TIME, new Date(), timeZone); */

        /* parseElementDataObject(reminder, json); doesn't work because
         * ReminderObject is not a subclass of DataObject */
        if (json.has(DataFields.ID)) {
            Long id = parseLong(json, DataFields.ID);
            if (null != id) {
                if (id.longValue() > Integer.MAX_VALUE) {
                    // Special handling for appointment reminder
                    int alarmId = (int) (id.longValue() >> 32);
                    reminder.setObjectId(alarmId);
                } else {
                    reminder.setObjectId(id.intValue());
                }
            }
        }
    }
}
