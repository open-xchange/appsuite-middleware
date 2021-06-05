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

package com.openexchange.ajax.appointment.action;

import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonUpdatesParser;

/**
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class AppointmentUpdatesParser extends CommonUpdatesParser<AppointmentUpdatesResponse> {

    protected AppointmentUpdatesParser(int[] columns) {
        super(true, columns);
    }

    @Override
    protected AppointmentUpdatesResponse createResponse(Response response) throws JSONException {
        /*
         * Calling super.createResponse initiates the modified and deleted ids for the update response
         */
        return super.createResponse(response);
    }

    @Override
    protected AppointmentUpdatesResponse instantiateResponse(Response response) {
        return new AppointmentUpdatesResponse(response);
    }
}
