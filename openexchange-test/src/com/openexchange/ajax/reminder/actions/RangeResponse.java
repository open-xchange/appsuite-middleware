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

package com.openexchange.ajax.reminder.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.ReminderParser;
import com.openexchange.ajax.reminder.ReminderTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RangeResponse extends AbstractAJAXResponse {

    private List<ReminderObject> reminders;

    /**
     * @param response
     */
    RangeResponse(final Response response) {
        super(response);
    }

    public ReminderObject[] getReminder(final TimeZone timeZone) throws OXException {
        if (null == reminders) {
            final ReminderParser parser = new ReminderParser(timeZone);
            final JSONArray array = (JSONArray) getData();
            reminders = new ArrayList<ReminderObject>(array.length());
            for (int i = 0; i < array.length(); i++) {
                try {
                    final JSONObject jremind = array.getJSONObject(i);
                    final ReminderObject reminder = new ReminderObject();
                    parser.parse(reminder, jremind);
                    reminders.add(reminder);
                } catch (JSONException e) {
                    throw OXJSONExceptionCodes.JSON_READ_ERROR.create(array.toString());
                }
            }
        }
        return reminders.toArray(new ReminderObject[reminders.size()]);
    }

    public ReminderObject getReminderByTarget(final TimeZone timeZone, final int targetId) throws OXException {
        return ReminderTools.searchByTarget(getReminder(timeZone), targetId);
    }
}
