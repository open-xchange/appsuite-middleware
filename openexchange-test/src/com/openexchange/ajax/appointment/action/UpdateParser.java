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

import static com.openexchange.ajax.appointment.action.AppointmentParserTools.parseConflicts;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class UpdateParser extends AbstractAJAXParser<UpdateResponse> {

    UpdateParser(final boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected UpdateResponse createResponse(final Response response) throws JSONException {
        final UpdateResponse retval = new UpdateResponse(response);
        final JSONObject data = (JSONObject) response.getData();
        if (data != null) {
            if (data.has(DataFields.ID)) {
                final int objectId = data.getInt(DataFields.ID);
                retval.setId(objectId);
            }
            parseConflicts(data, retval);
        }
        return retval;
    }
}
