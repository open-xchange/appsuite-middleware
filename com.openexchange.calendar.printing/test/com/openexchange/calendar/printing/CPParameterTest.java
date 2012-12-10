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

package com.openexchange.calendar.printing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import junit.framework.TestCase;
import com.openexchange.calendar.printing.CPParameters;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPParameterTest extends TestCase {

    private class MockRequest implements HttpServletRequest {

        private final Map<String, Object> attributes = new HashMap<String, Object>();
        private final Map<String, String[]> parameters = new HashMap<String, String[]>();

        @Override
        public String getAuthType() {
            return null;
        }

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public Cookie[] getCookies() {
            return null;
        }

        @Override
        public String getParameter(String name) {
            return parameters.get(name)[0];
        }

        @Override
        public Map<String,String[]> getParameterMap() {
            return parameters;
        }


        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        @Override
        public void setAttribute(String name, Object o) {
            attributes.put(name, o);
        }


        public void setParameter(String key, String value) {
            parameters.put(key,new String[]{value});
        }

		@Override
		public Object getAttribute(String name) {
			// Nothing to do
			return null;
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			// Nothing to do
			return null;
		}

		@Override
		public String getCharacterEncoding() {
			// Nothing to do
			return null;
		}

		@Override
		public void setCharacterEncoding(String env)
				throws UnsupportedEncodingException {
			// Nothing to do
			
		}

		@Override
		public int getContentLength() {
			// Nothing to do
			return 0;
		}

		@Override
		public String getContentType() {
			// Nothing to do
			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			// Nothing to do
			return null;
		}

		@Override
		public Enumeration<String> getParameterNames() {
			// Nothing to do
			return null;
		}

		@Override
		public String[] getParameterValues(String name) {
			// Nothing to do
			return null;
		}

		@Override
		public String getProtocol() {
			// Nothing to do
			return null;
		}

		@Override
		public String getScheme() {
			// Nothing to do
			return null;
		}

		@Override
		public String getServerName() {
			// Nothing to do
			return null;
		}

		@Override
		public int getServerPort() {
			// Nothing to do
			return 0;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			// Nothing to do
			return null;
		}

		@Override
		public String getRemoteAddr() {
			// Nothing to do
			return null;
		}

		@Override
		public String getRemoteHost() {
			// Nothing to do
			return null;
		}

		@Override
		public Locale getLocale() {
			// Nothing to do
			return null;
		}

		@Override
		public Enumeration<Locale> getLocales() {
			// Nothing to do
			return null;
		}

		@Override
		public boolean isSecure() {
			// Nothing to do
			return false;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String path) {
			// Nothing to do
			return null;
		}

		@Override
		public String getRealPath(String path) {
			// Nothing to do
			return null;
		}

		@Override
		public int getRemotePort() {
			// Nothing to do
			return 0;
		}

		@Override
		public String getLocalName() {
			// Nothing to do
			return null;
		}

		@Override
		public String getLocalAddr() {
			// Nothing to do
			return null;
		}

		@Override
		public int getLocalPort() {
			// Nothing to do
			return 0;
		}

		@Override
		public long getDateHeader(String name) {
			// Nothing to do
			return 0;
		}

		@Override
		public String getHeader(String name) {
			// Nothing to do
			return null;
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			// Nothing to do
			return null;
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			// Nothing to do
			return null;
		}

		@Override
		public int getIntHeader(String name) {
			// Nothing to do
			return 0;
		}

		@Override
		public String getMethod() {
			// Nothing to do
			return null;
		}

		@Override
		public String getPathInfo() {
			// Nothing to do
			return null;
		}

		@Override
		public String getPathTranslated() {
			// Nothing to do
			return null;
		}

		@Override
		public String getQueryString() {
			// Nothing to do
			return null;
		}

		@Override
		public String getRemoteUser() {
			// Nothing to do
			return null;
		}

		@Override
		public boolean isUserInRole(String role) {
			// Nothing to do
			return false;
		}

		@Override
		public Principal getUserPrincipal() {
			// Nothing to do
			return null;
		}

		@Override
		public String getRequestedSessionId() {
			// Nothing to do
			return null;
		}

		@Override
		public String getRequestURI() {
			// Nothing to do
			return null;
		}

		@Override
		public StringBuffer getRequestURL() {
			// Nothing to do
			return null;
		}

		@Override
		public String getServletPath() {
			// Nothing to do
			return null;
		}

		@Override
		public HttpSession getSession(boolean create) {
			// Nothing to do
			return null;
		}

		@Override
		public HttpSession getSession() {
			// Nothing to do
			return null;
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			// Nothing to do
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			// Nothing to do
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			// Nothing to do
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			// Nothing to do
			return false;
		}
    }

    public void testShouldCryIfMissingFields() {
        MockRequest mockRequest = new MockRequest();
        CPParameters params = new CPParameters(mockRequest, TimeZone.getDefault());
        assertTrue("No parameters given, should miss fields", params.isMissingMandatoryFields());
    }

    public void testShouldCryIfCannotParseValue() {
        MockRequest mockRequest = new MockRequest();
        mockRequest.setParameter(CPParameters.PARAMETER_WEEK_START_DAY, "Elvis");
        CPParameters params = new CPParameters(mockRequest, TimeZone.getDefault());
        assertTrue("Parameter with bullshit value given, should fail", params.hasUnparseableFields());
        assertTrue("Parameter with bullshit value given, should be listed as missing field", params.getUnparseableFields().contains(
            CPParameters.PARAMETER_WEEK_START_DAY));
    }

    public void testShouldNotLeaveMissingParamFieldEmptyWhenEncounteringNumberFormatExceptionWhileParsingFieldValue() {
        MockRequest mockRequest = new MockRequest();
        mockRequest.setAttribute(CPParameters.PARAMETER_WEEK_START_DAY, "Elvis");
        CPParameters params = new CPParameters(mockRequest, TimeZone.getDefault());
        assertTrue("Should still miss fields", params.isMissingOptionalFields());
        assertTrue("Should at least miss the field it was trying to parse", params.getMissingOptionalFields().contains(CPParameters.PARAMETER_WEEK_START_DAY));

    }
}
