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

import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.reminder.ReminderObject;

/**
 * {@link RemindAgainRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RemindAgainRequest extends AbstractReminderRequest<RemindAgainResponse> {

    private final ReminderObject reminder;

    /**
     * Default constructor.
     *
     * @param reminder The reminder with new alarm date set
     */
    public RemindAgainRequest(final ReminderObject reminder) {
        super();
        if (null == reminder) {
            throw new NullPointerException("reminder is null.");
        } else if (reminder.getObjectId() <= 0) {
            throw new IllegalArgumentException("Missing identifier in reminder.");
        } else if (null == reminder.getDate()) {
            throw new IllegalArgumentException("Missing alarm date in reminder.");
        }
        this.reminder = reminder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        return convert(reminder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, "remindAgain"), new Parameter(AJAXServlet.PARAMETER_ID, String.valueOf(reminder.getObjectId())),
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RemindAgainParser getParser() {
        return new RemindAgainParser();
    }

}
