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
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.ReminderParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderObject;

/**
 * {@link RemindAgainResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RemindAgainResponse extends AbstractAJAXResponse {

    private ReminderObject reminder;

    /**
     * @param response
     */
    RemindAgainResponse(final Response response) {
        super(response);
    }

    public ReminderObject getReminder(final TimeZone timeZone) throws OXException {
        if (null == reminder) {
            final JSONObject jremind = (JSONObject) getData();
            reminder = new ReminderObject();
            new ReminderParser(timeZone).parse(reminder, jremind);
        }
        return reminder;
    }

}
