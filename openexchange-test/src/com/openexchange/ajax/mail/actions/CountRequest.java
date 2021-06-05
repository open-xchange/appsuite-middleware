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

package com.openexchange.ajax.mail.actions;

import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 *
 * {@link CountRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class CountRequest extends AbstractMailRequest<CountResponse> {

    private final String folderID;

    public CountRequest(final String folderID) {
        super();
        this.folderID = folderID;
    }

    @Override
    public Object getBody() throws JSONException {
        return null;//does not have a RequestBody - return null, empty JSONObject or empty JSONArray?
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_COUNT), new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderID)
        };
    }

    @Override
    public AbstractAJAXParser<CountResponse> getParser() {
        return new AbstractAJAXParser<CountResponse>(false) {

            @Override
            protected CountResponse createResponse(final Response response) throws JSONException {
                return new CountResponse(response);
            }
        };
    }

}
