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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.container.Response;
import com.openexchange.tools.servlet.http.Tools;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractRedirectParser<T extends AbstractAJAXResponse> extends AbstractAJAXParser<T> {

    private String location;
    private final boolean cookiesNeeded;
    private final boolean failOnNonRedirect;

    protected AbstractRedirectParser() {
        this(true);
    }

    protected AbstractRedirectParser(boolean cookiesNeeded) {
        this(cookiesNeeded, true);
    }

    protected AbstractRedirectParser(boolean cookiesNeeded, boolean failOnNonRedirect) {
        super(true);
        this.cookiesNeeded = cookiesNeeded;
        this.failOnNonRedirect = failOnNonRedirect;
    }

    @Override
    protected Response getResponse(String body) throws JSONException {
        throw new JSONException("Method not supported when parsing redirect responses.");
    }

    @Override
    public String checkResponse(HttpResponse resp) throws ParseException, IOException {
        if (failOnNonRedirect) {
            assertEquals("Response code is not okay.", HttpServletResponse.SC_MOVED_TEMPORARILY, resp.getStatusLine().getStatusCode());            
        } else {
            final int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode >= HttpServletResponse.SC_BAD_REQUEST) {
                final String reasonPhrase = resp.getStatusLine().getReasonPhrase();
                return Integer.toString(statusCode) + (null == reasonPhrase ? "" : reasonPhrase);
            }
        }
        Header[] headers = resp.getHeaders("Location");
        assertEquals("There should be exactly one Location header.", 1, headers.length);
        location = headers[0].getValue();
        assertNotNull("Location for redirect is missing.", location);
        if (cookiesNeeded) {
            boolean oxCookieFound = false;
            boolean jsessionIdCookieFound = false;
            HeaderElementIterator iter = new BasicHeaderElementIterator(resp.headerIterator("Set-Cookie"));
            while (iter.hasNext()) {
                HeaderElement element = iter.nextElement();
                if (element.getName().startsWith(Login.SECRET_PREFIX)) {
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
        return EntityUtils.toString(resp.getEntity());
    }

    @Override
    public final T parse(String body) throws JSONException {
        return createResponse(null == location ? body : location);
    }

    @Override
    protected final T createResponse(Response response) {
        return null;
    }

    protected abstract T createResponse(String myLocation) throws JSONException;
}
