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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;

/**
 *
 * {@link MockRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class MockRequest implements AJAXRequest<MockResponse> {

    private boolean failOnError = true;
    private final String uri;
    private final InputStream response;
    private final int statusCode;
    private final Map<String, String> responseHeaders;
    private final int delay;
    private final MockRequestMethod method;

    public MockRequest(String uri, InputStream response) {
        this(uri, response, HttpStatus.SC_OK);
    }

    public MockRequest(String uri, InputStream response, int statusCode) {
        this(uri, response, statusCode, java.util.Collections.emptyMap());
    }

    public MockRequest(String uri, InputStream response, int statusCode, Map<String, String> responseHeaders) {
        this(uri, response, statusCode, responseHeaders, 0);
    }

    public MockRequest(String uri, InputStream response, int statusCode, Map<String, String> responseHeaders, int delay) {
        this(MockRequestMethod.GET, uri, response, statusCode, responseHeaders, delay);
    }

    public MockRequest(MockRequestMethod method, String uri, InputStream response, int statusCode, Map<String, String> responseHeaders, int delay) {
        this.method = method;
        this.uri = uri;
        this.response = response;
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.delay = delay;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.UPLOAD;
    }

    @Override
    public String getServletPath() {
        return "/ajax/mock";
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "mock"));
        parameters.add(new Parameter(MockConstants.URI_KEY, uri));
        parameters.add(new Parameter(MockConstants.STATUS_CODE_KEY, Integer.toString(statusCode)));
        parameters.add(new Parameter(MockConstants.METHOD_KEY, method.name()));

        if (responseHeaders != null && !responseHeaders.isEmpty()) {
            parameters.add(new Parameter(MockConstants.HEADERS_KEY, addHeaders()));
        }
        if (delay > 0) {
            parameters.add(new Parameter(MockConstants.DELAY_KEY, delay));
        }

        parameters.add(new FileParameter("mockResponse", "response.ics", response, "text/calendar"));
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends MockResponse> getParser() {
        return new AbstractAJAXParser<MockResponse>(failOnError) {

            @Override
            protected MockResponse createResponse(Response response) throws JSONException {
                return new MockResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    private String addHeaders() throws JSONException {
        JSONObject jOptions = new JSONObject();
        for (Entry<String, String> entry : responseHeaders.entrySet()) {
            jOptions.put(entry.getKey(), entry.getValue());
        }
        return jOptions.toString();
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

}
