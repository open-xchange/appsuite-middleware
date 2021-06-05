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

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.writer.ReminderWriter;
import com.openexchange.groupware.reminder.ReminderObject;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractReminderRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    /**
     * URL of the reminder AJAX interface.
     */
    private static final String REMINDER_URL = "/ajax/reminder";

    /**
     * Default constructor.
     */
    protected AbstractReminderRequest() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletPath() {
        return REMINDER_URL;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    protected JSONObject convert(final ReminderObject reminderObj) throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        final ReminderWriter writer = new ReminderWriter(TimeZone.getTimeZone("UTC"));
        writer.writeObject(reminderObj, jsonObj);
        return jsonObj;
    }

}
