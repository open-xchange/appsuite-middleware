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

package com.openexchange.ajax.framework;

import java.io.IOException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;

/**
 * {@link CustomizedParser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class CustomizedParser<T extends AbstractAJAXResponse> extends AbstractAJAXParser<T> {

    protected final AbstractAJAXParser<T> delegate;

    protected CustomizedParser(AbstractAJAXParser<T> delegate) {
        super(delegate.isFailOnError());
        this.delegate = delegate;
    }

    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        String checkCustom = checkCustom(resp);
        if (checkCustom == null) {
            return delegate.checkResponse(resp, request);
        }

        return checkCustom;
    }

    protected abstract String checkCustom(HttpResponse resp) throws ParseException, IOException;

    @Override
    public T parse(String body) throws JSONException {
        return delegate.parse(body);
    }

    @Override
    public T createResponse(Response response) throws JSONException {
        return delegate.createResponse(response);
    }

}
