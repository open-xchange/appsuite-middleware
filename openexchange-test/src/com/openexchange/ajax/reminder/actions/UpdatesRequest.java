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

import java.util.Date;
import com.openexchange.ajax.AJAXServlet;

/**
 * 
 * {@link UpdatesRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class UpdatesRequest extends AbstractReminderRequest<UpdatesResponse> {

    /**
     * Reminder until this date will be fetched from server.
     */
    private final Date timestamp;
    private boolean failOnError;

    public UpdatesRequest(Date end, boolean failOnError) {
        super();
        this.failOnError = failOnError;
        this.timestamp = end;
    }

    /**
     * Default constructor.
     * 
     * @param end reminder until this date will be fetched from server.
     */
    public UpdatesRequest(final Date end) {
        this(end, true);
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES), new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(timestamp.getTime()))
        };
    }

    @Override
    public UpdatesParser getParser() {
        return new UpdatesParser(failOnError);
    }
}
