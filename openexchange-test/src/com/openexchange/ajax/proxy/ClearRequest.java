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

package com.openexchange.ajax.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;

/**
 * 
 * {@link ClearRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ClearRequest implements AJAXRequest<AbstractAJAXResponse> {

    private boolean failOnError = true;
    private String uri;

    public ClearRequest(String uri) {
        this.uri = uri;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/mock";
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "clear"));
        parameters.add(new Parameter(MockConstants.URI_KEY, uri));
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends AbstractAJAXResponse> getParser() {
        return new AbstractAJAXParser<AbstractAJAXResponse>(failOnError) {

            @Override
            protected AbstractAJAXResponse createResponse(Response response) throws JSONException {
                return new AbstractAJAXResponse(response) {

                };
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

}
