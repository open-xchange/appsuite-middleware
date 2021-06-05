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
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RangeRequest extends AbstractReminderRequest<RangeResponse> {

    /**
     * Reminder until this date will be fetched from server.
     */
    private final Date end;
    private boolean failOnError;

    public RangeRequest(Date end, boolean failOnError) {
        super();
        this.failOnError = failOnError;
        this.end = new Date(end.getTime());
    }

    /**
     * Default constructor.
     * 
     * @param end reminder until this date will be fetched from server.
     */
    public RangeRequest(final Date end) {
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
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_RANGE), new Parameter(AJAXServlet.PARAMETER_END, String.valueOf(end.getTime()))
        };
    }

    @Override
    public RangeParser getParser() {
        return new RangeParser(failOnError);
    }
}
