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

package com.openexchange.ajax.contact.action;

import java.util.TimeZone;
import java.util.UUID;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.FinalContactConstants;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class GetContactByUIDRequest extends AbstractContactRequest<GetResponse> {

    protected UUID uid;
    protected TimeZone tz;

    public GetContactByUIDRequest(UUID uid, TimeZone tz) {
        super();
        this.uid = uid;
        this.tz = tz;
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
        return new Params(AJAXServlet.PARAMETER_ACTION, FinalContactConstants.ACTION_GET_BY_UUID.getName(), FinalContactConstants.PARAMETER_UUID.getName(), String.valueOf(uid)).toArray();
    }

    @Override
    public AbstractAJAXParser<? extends GetResponse> getParser() {
        return new AbstractAJAXParser<GetResponse>(false) {

            @Override
            protected GetResponse createResponse(Response response) {
                return new GetResponse(response, tz);
            }

        };
    }

}
