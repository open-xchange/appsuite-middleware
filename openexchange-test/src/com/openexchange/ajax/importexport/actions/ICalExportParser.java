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

package com.openexchange.ajax.importexport.actions;

import java.io.IOException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ICalExportParser extends AbstractAJAXParser<ICalExportResponse> {

    private HttpResponse httpResponse;
    
    /**
     * @param failOnError
     */
    ICalExportParser(final boolean failOnError) {
        super(failOnError);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response getResponse(final String body) throws JSONException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICalExportResponse parse(final String body) throws JSONException {
        final ICalExportResponse retval = new ICalExportResponse(httpResponse);
        retval.setICal(body);
        return retval;
    }

    @Override
    protected ICalExportResponse createResponse(final Response response) throws JSONException {
        return new ICalExportResponse(httpResponse);
    }
    
    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        this.httpResponse = resp;
        return super.checkResponse(resp, request);
    }
}
