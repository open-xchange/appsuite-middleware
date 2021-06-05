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

package com.openexchange.ajax.session.actions;

import java.io.IOException;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeaderElementIterator;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.tools.servlet.http.Tools;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginResponseParser extends AbstractAJAXParser<LoginResponse> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginResponseParser.class);

    private String jvmRoute;

    LoginResponseParser(final boolean failOnError) {
        super(failOnError);
    }

    @Override
    public String checkResponse(final HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        String body = super.checkResponse(resp, request);
        // Check for error messages
        try {
            super.getResponse(body);
        } catch (JSONException e) {
            LOG.error("Invalid login body: \"" + body + "\"");
            fail(e.getMessage());
        }
        if (isFailOnError()) {
            boolean oxCookieFound = false;
            boolean jsessionIdCookieFound = false;
            HeaderElementIterator iter = new BasicHeaderElementIterator(resp.headerIterator("Set-Cookie"));
            while (iter.hasNext()) {
                HeaderElement element = iter.nextElement();
                if (element.getName().startsWith(LoginServlet.SECRET_PREFIX)) {
                    oxCookieFound = true;
                    continue;
                }
                if (Tools.JSESSIONID_COOKIE.equals(element.getName())) {
                    jsessionIdCookieFound = true;
                    final String jsessionId = element.getValue();
                    final int dotPos = jsessionId.lastIndexOf('.');
                    assertTrue("jvmRoute is missing.", dotPos > 0);
                    jvmRoute = jsessionId.substring(dotPos + 1);
                    continue;
                }
            }
            assertTrue("Secret cookie is missing.", oxCookieFound);
            assertTrue("JSESSIONID cookie is missing.", jsessionIdCookieFound);
        }
        return body;
    }

    @Override
    public LoginResponse parse(final String body) throws JSONException {
        final JSONObject json = new JSONObject(body);
        final Response response = getResponse(body);
        response.setData(json);
        return createResponse(response);
    }

    @Override
    protected LoginResponse createResponse(final Response response) throws JSONException {
        final LoginResponse retval = new LoginResponse(response);
        final JSONObject json = (JSONObject) response.getData();
        if (response.hasError()) {
            response.setData(null);
        } else {
            retval.setJvmRoute(jvmRoute);
            retval.setSessionId(json.getString(LoginServlet.PARAMETER_SESSION));
            retval.setRandom(json.optString(LoginFields.RANDOM_PARAM));
            if (json.has(LoginServlet.PARAMETER_PASSWORD)) {
                retval.setPassword(json.getString(LoginServlet.PARAMETER_PASSWORD));
            }
        }
        if (isFailOnError()) {
            assertFalse(response.getErrorMessage(), response.hasError());
            assertTrue("Session ID is missing.", json.has(LoginServlet.PARAMETER_SESSION));
            assertFalse("Random should be missing.", json.has(LoginFields.RANDOM_PARAM));
        }
        return retval;
    }
}
