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

package com.openexchange.chronos.availability.json;

import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AbstractAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractAction implements AJAXActionService {

    protected final ServiceLookup services;

    /**
     * Initialises a new {@link AbstractAction}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    AbstractAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Initialises a new {@link CalendarSession} from the specified groupware {@link Session}
     * 
     * @param session The groupware {@link Session}
     * @return The {@link CalendarSession}
     * @throws OXException if an error is occurred
     */
    CalendarSession getSession(Session session) throws OXException {
        return services.getService(CalendarService.class).init(session);
    }

    /**
     * Retrieves the request body from the specified {@link AJAXRequestData}
     * 
     * @param requestData The request payload
     * @return The request body
     * @throws OXException if the request body is missing or is invalid
     */
    <T extends JSONValue> T getRequestBody(AJAXRequestData requestData, Class<T> type) throws OXException {
        Object data = requestData.getData();
        if (data == null) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        if (data.getClass().equals(type)) {
            return type.cast(data);
        }
        throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
    }
}
