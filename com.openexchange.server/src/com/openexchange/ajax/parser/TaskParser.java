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

import static com.openexchange.ajax.fields.TaskFields.ACTUAL_DURATION;
import static com.openexchange.ajax.fields.TaskFields.TARGET_DURATION;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskExceptionCode;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * TaskParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class TaskParser extends CalendarParser {

    public TaskParser(final TimeZone timeZone) {
        super(timeZone);
    }

    public TaskParser(final boolean parseAll, final TimeZone timeZone) {
        super(parseAll, timeZone);
    }

    public void parse(Task taskobject, JSONObject jsonobject, Locale locale)
        throws OXException {
        try {
            parseElementTask(taskobject, jsonobject, locale);
        } catch (OXException e) {
            throw e;
        } catch (Exception e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
        }
    }

    protected void parseElementTask(final Task taskobject, final JSONObject json, Locale locale) throws JSONException, OXException {
        if (json.has(CalendarFields.START_DATE)) {
            taskobject.setStartDate(parseDate(json, CalendarFields.START_DATE));
            taskobject.setFullTime(true); // implicitly
        }
        if (json.has(CalendarFields.END_DATE)) {
            taskobject.setEndDate(parseDate(json, CalendarFields.END_DATE));
            taskobject.setFullTime(true); // implicitly
        }
        boolean isFullTime = json.has(CalendarFields.FULL_TIME) && parseBoolean(json, CalendarFields.FULL_TIME);
        if (json.has(TaskFields.START_TIME)) {
            if (isFullTime) {
                taskobject.setStartDate(parseDate(json, TaskFields.START_TIME));
            } else {
                taskobject.setStartDate(parseTime(json, TaskFields.START_TIME, timeZone));
            }
        }
        if (json.has(TaskFields.END_TIME)) {
            if (isFullTime) {
                taskobject.setEndDate(parseDate(json, TaskFields.END_TIME));
            } else {
                taskobject.setEndDate(parseTime(json, TaskFields.END_TIME, timeZone));
            }
        }
        if (json.has(TaskFields.STATUS)) {
            taskobject.setStatus(parseInt(json, TaskFields.STATUS));
        }
        if (json.has(TaskFields.ACTUAL_COSTS)) {
            try {
                taskobject.setActualCosts(parseBigDecimal(json, TaskFields.ACTUAL_COSTS));
            } catch (OXException e) {
                throw handleParseException(e, parseString(json, TaskFields.ACTUAL_COSTS), Task.ACTUAL_COSTS, locale);
            }
        }
        if (json.has(ACTUAL_DURATION)) {
            try {
                taskobject.setActualDuration(parseLong(json, ACTUAL_DURATION));
            } catch (OXException e) {
                throw handleParseException(e, parseString(json, TaskFields.ACTUAL_DURATION), Task.ACTUAL_DURATION, locale);
            }
        }
        if (json.has(TaskFields.PERCENT_COMPLETED)) {
            taskobject.setPercentComplete(parseInt(json, TaskFields.PERCENT_COMPLETED));
        }
        if (json.has(TaskFields.DATE_COMPLETED)) {
            taskobject.setDateCompleted(parseDate(json, TaskFields.DATE_COMPLETED));
        }
        if (json.has(TaskFields.BILLING_INFORMATION)) {
            taskobject.setBillingInformation(parseString(json, TaskFields.BILLING_INFORMATION));
        }
        if (json.has(TaskFields.TARGET_COSTS)) {
            try {
                taskobject.setTargetCosts(parseBigDecimal(json, TaskFields.TARGET_COSTS));
            } catch (OXException e) {
                throw handleParseException(e, parseString(json, TaskFields.TARGET_COSTS), Task.TARGET_COSTS, locale);
            }
        }
        if (json.has(TARGET_DURATION)) {
            try {
                taskobject.setTargetDuration(parseLong(json, TARGET_DURATION));
            } catch (OXException e) {
                throw handleParseException(e, parseString(json, TARGET_DURATION), Task.TARGET_DURATION, locale);
            }
        }
        if (json.has(TaskFields.PRIORITY)) {
            taskobject.setPriority(parseInteger(json, TaskFields.PRIORITY));
        }
        if (json.has(TaskFields.CURRENCY)) {
            taskobject.setCurrency(parseString(json, TaskFields.CURRENCY));
        }
        if (json.has(TaskFields.TRIP_METER)) {
            taskobject.setTripMeter(parseString(json, TaskFields.TRIP_METER));
        }
        if (json.has(TaskFields.COMPANIES)) {
            taskobject.setCompanies(parseString(json, TaskFields.COMPANIES));
        }
        if (json.has(CalendarFields.ALARM)) {
            taskobject.setAlarm(parseTime(json, CalendarFields.ALARM, timeZone));
        }
        parseElementCalendar(taskobject, json);
    }

    private static OXException handleParseException(OXException e, String jsonValue, int attributeId, Locale locale) throws OXException {
        if (OXJSONExceptionCodes.CONTAINS_NON_DIGITS.equals(e) || OXJSONExceptionCodes.INVALID_VALUE.equals(e)) {
            throw TaskExceptionCode.CONTAINS_NON_DIGITS.create(e, jsonValue, translate(locale, Mapping.getMapping(attributeId).getDisplayName()));
        }
        throw e;
    }

    private static String translate(Locale locale, String attributeName) {
        I18nServiceRegistry service = ServerServiceRegistry.getServize(I18nServiceRegistry.class);
        I18nService i18nService = service.getI18nService(locale);
        return i18nService.getLocalized(attributeName);
    }
}
