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
        } catch (final JSONException e) {
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
