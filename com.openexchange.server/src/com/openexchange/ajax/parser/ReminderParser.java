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
        } catch (final JSONException e) {
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
            reminder.setObjectId(parseInt(json, DataFields.ID));
        }
    }
}
