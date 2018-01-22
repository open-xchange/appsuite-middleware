/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    private String uri;
    private InputStream response;
    private int statusCode;
    private Map<String, String> responseHeaders;
    private int delay;
    private MockRequestMethod method;

    public MockRequest(String uri, InputStream response) {
        this(uri, response, HttpStatus.SC_OK);
    }

    public MockRequest(String uri, InputStream response, int statusCode) {
        this(uri, response, statusCode, java.util.Collections.emptyMap());
    }

    public MockRequest(String uri, InputStream response, int statusCode, Map<String, String> responseHeaders) {
        this(uri, response, statusCode, java.util.Collections.emptyMap(), 0);
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
