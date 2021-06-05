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
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.tools.servlet.http.Tools;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractRedirectParser<T extends AbstractAJAXResponse> extends AbstractAJAXParser<T> {

    private final boolean cookiesNeeded, locationNeeded, failOnNonRedirect;
    private String location;
    private int statusCode;
    private String reasonPhrase;

    /**
     * Initializes a new {@link AbstractRedirectParser}.
     */
    protected AbstractRedirectParser() {
        this(true);
    }

    /**
     * Initializes a new {@link AbstractRedirectParser}.
     *
     * @param cookiesNeeded <code>true</code> if cookies should be parsed and checked from the response, <code>false</code>, otherwise
     */
    protected AbstractRedirectParser(boolean cookiesNeeded) {
        this(cookiesNeeded, true);
    }

    /**
     * Initializes a new {@link AbstractRedirectParser}.
     *
     * @param cookiesNeeded <code>true</code> if cookies should be parsed and checked from the response, <code>false</code>, otherwise
     * @param locationNeeded <code>true</code> to fail if the response contains no <code>Location</code> header, <code>false</code>, otherwise
     */
    protected AbstractRedirectParser(boolean cookiesNeeded, boolean locationNeeded) {
        this(cookiesNeeded, locationNeeded, true);
    }

    /**
     * Initializes a new {@link AbstractRedirectParser}.
     *
     * @param cookiesNeeded <code>true</code> if cookies should be parsed and checked from the response, <code>false</code>, otherwise
     * @param locationNeeded <code>true</code> to fail if the response contains no <code>Location</code> header, <code>false</code>, otherwise
     * @param failOnNonRedirect <code>true</code> to fail if the response status code is anything else than <code>HTTP 302</code>, <code>false</code>, otherwise
     */
    protected AbstractRedirectParser(boolean cookiesNeeded, boolean locationNeeded, boolean failOnNonRedirect) {
        super(true);
        this.cookiesNeeded = cookiesNeeded;
        this.locationNeeded = locationNeeded;
        this.failOnNonRedirect = failOnNonRedirect;
    }

    protected int getStatusCode() {
        return statusCode;
    }

    protected void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    protected String getReasonPhrase() {
        return reasonPhrase;
    }

    protected void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    protected void setLocation(String location) {
        this.location = location;
    }

    protected String getLocation() {
        return location;
    }

    @Override
    protected Response getResponse(String body) throws JSONException {
        throw new JSONException("Method not supported when parsing redirect responses.");
    }

    protected boolean isCookiesNeeded() {
        return cookiesNeeded;
    }

    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        statusCode = resp.getStatusLine().getStatusCode();
        reasonPhrase = resp.getStatusLine().getReasonPhrase();
        if (failOnNonRedirect) {
            assertEquals("Response code is not okay.", HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
        }
        parseLocationHeader(resp);
        if (cookiesNeeded) {
            parseCookies(resp);
        }
        return EntityUtils.toString(resp.getEntity());
    }

    protected static final void parseCookies(HttpResponse resp) {
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
                continue;
            }
        }
        assertTrue("Session cookie is missing.", oxCookieFound);
        assertTrue("JSESSIONID cookie is missing.", jsessionIdCookieFound);
    }

    protected final void parseLocationHeader(HttpResponse resp) {
        Header[] headers = resp.getHeaders("Location");
        if (headers.length > 0) {
            location = headers[0].getValue();
        }
        if (locationNeeded) {
            assertEquals("There should be exactly one Location header.", 1, headers.length);
            assertNotNull("Location for redirect is missing.", location);
        }
    }

    @Override
    public T parse(String body) throws JSONException {
        if (locationNeeded && null == location) {
            throw new JSONException("Location for redirect is missing. Ensure to call method parseLocationHeader(HttpResponse) when overwriting checkResponse(HttpResponse).");
        }
        return createResponse(location);
    }

    @Override
    protected final T createResponse(Response response) {
        return null;
    }

    protected abstract T createResponse(String myLocation) throws JSONException;
}
