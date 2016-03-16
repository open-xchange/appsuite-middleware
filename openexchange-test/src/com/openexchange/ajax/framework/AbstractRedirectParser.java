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
