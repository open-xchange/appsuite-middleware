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

package com.openexchange.test.mock.objects.external.javax.servlet.http;

import java.util.Date;
import java.util.Enumeration;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.test.mock.main.MockFactory;
import com.openexchange.test.mock.main.util.MockDefaultValues;
import com.openexchange.test.mock.objects.AbstractMock;


/**
 * Mock for {@link HttpServletRequest}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class HttpServletRequestMock<T extends HttpServletRequest> extends AbstractMock {

    /**
     * The mock for {@link HttpServletRequest}
     */
    private T httpServletRequest;

    /**
     * Header and attribute name parameters
     */
    private Enumeration<?> parameters;

    /**
     * Mock for the {@link HttpSession}
     */
    private HttpSession httpSession;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get() {
        return (T) this.httpServletRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createMocks() throws Exception {
        this.httpServletRequest = (T) PowerMockito.mock(HttpServletRequest.class);

        this.parameters = PowerMockito.mock(Enumeration.class);
        this.httpSession = MockFactory.getMock(HttpSession.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeMembers() {
        // nothing to do yet
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void defineMockSpecificBehaviour() {
        PowerMockito.when(this.parameters.hasMoreElements()).thenReturn(false);

        PowerMockito.when(this.httpServletRequest.getHeaderNames()).thenReturn(this.parameters);
        PowerMockito.when(this.httpServletRequest.getParameter(anyString())).thenReturn(MockDefaultValues.DEFAULT_ATTRIBUTE_VALUE);
        PowerMockito.when(this.httpServletRequest.getParameter("split")).thenReturn(MockDefaultValues.DEFAULT_FALSE_BOOLEAN_AS_STRING);
        PowerMockito.when(this.httpServletRequest.getCharacterEncoding()).thenReturn(MockDefaultValues.DEFAULT_CHARACTER_ENCODING);
        PowerMockito.when(this.httpServletRequest.getRequestURI()).thenReturn(MockDefaultValues.DEFAULT_ENCODED_URL);
        PowerMockito.when(this.httpServletRequest.getPathInfo()).thenReturn(MockDefaultValues.DEFAULT_CONTEXT_AND_SERVLET_PATH);
        PowerMockito.when(this.httpServletRequest.getServletPath()).thenReturn(MockDefaultValues.DEFAULT_CONTEXT_AND_SERVLET_PATH);
        PowerMockito.when(this.httpServletRequest.getHeader(anyString())).thenReturn(MockDefaultValues.DEFAULT_ATTRIBUTE_VALUE);
        PowerMockito.when(this.httpServletRequest.getDateHeader(anyString())).thenReturn(new Date().getTime());
        PowerMockito.when(this.httpServletRequest.getAttribute(anyString())).thenReturn(MockDefaultValues.DEFAULT_ATTRIBUTE_VALUE);
        PowerMockito.when(this.httpServletRequest.getAttributeNames()).thenReturn(this.parameters);
        PowerMockito.when(this.httpServletRequest.getAuthType()).thenReturn(MockDefaultValues.DEFAULT_AUTH_TYPE);
        PowerMockito.when(this.httpServletRequest.getContentLength()).thenReturn(0);
        PowerMockito.when(this.httpServletRequest.getContentType()).thenReturn(MockDefaultValues.DEFAULT_CONTENT_TYPE);
        PowerMockito.when(this.httpServletRequest.getContextPath()).thenReturn(MockDefaultValues.DEFAULT_CONTEXT_AND_SERVLET_PATH);
        PowerMockito.when(this.httpServletRequest.getCookies()).thenReturn(new Cookie[] { Mockito.mock(Cookie.class) });
        PowerMockito.when(this.httpServletRequest.getHeaders(anyString())).thenReturn(this.parameters);
        PowerMockito.when(this.httpServletRequest.getIntHeader(anyString())).thenReturn(MockDefaultValues.DEFAULT_INTEGER_VALUE);
        PowerMockito.when(this.httpServletRequest.getLocalAddr()).thenReturn(MockDefaultValues.DEFAULT_HOST);
        PowerMockito.when(this.httpServletRequest.getLocale()).thenReturn(MockDefaultValues.DEFAULT_LOCALE);
        PowerMockito.when(this.httpServletRequest.getLocalName()).thenReturn(MockDefaultValues.DEFAULT_HOST);
        PowerMockito.when(this.httpServletRequest.getLocalPort()).thenReturn(MockDefaultValues.DEFAULT_PORT);
        PowerMockito.when(this.httpServletRequest.getMethod()).thenReturn(MockDefaultValues.DEFAULT_METHOD_TYPE_POST);
        PowerMockito.when(this.httpServletRequest.getProtocol()).thenReturn(MockDefaultValues.DEFAULT_PROTOCOL);
        PowerMockito.when(this.httpServletRequest.getQueryString()).thenReturn(MockDefaultValues.DEFAULT_HOST);
        PowerMockito.when(this.httpServletRequest.getRemoteAddr()).thenReturn(MockDefaultValues.DEFAULT_HOST);
        PowerMockito.when(this.httpServletRequest.getRemoteHost()).thenReturn(MockDefaultValues.DEFAULT_HOST);
        PowerMockito.when(this.httpServletRequest.getRemotePort()).thenReturn(MockDefaultValues.DEFAULT_PORT);
        PowerMockito.when(this.httpServletRequest.getRemoteUser()).thenReturn(MockDefaultValues.DEFAULT_USER_LOGIN_NAME);
        PowerMockito.when(this.httpServletRequest.getRequestedSessionId()).thenReturn(MockDefaultValues.DEFAULT_SESSION_ID);
        PowerMockito.when(this.httpServletRequest.getScheme()).thenReturn(MockDefaultValues.DEFAULT_PROTOCOL);
        PowerMockito.when(this.httpServletRequest.getServerName()).thenReturn(MockDefaultValues.DEFAULT_HOST);
        PowerMockito.when(this.httpServletRequest.getServletPath()).thenReturn(MockDefaultValues.DEFAULT_CONTEXT_AND_SERVLET_PATH);
        PowerMockito.when(this.httpServletRequest.getServerPort()).thenReturn(MockDefaultValues.DEFAULT_PORT);
        PowerMockito.when(this.httpServletRequest.getSession()).thenReturn(this.httpSession);
        PowerMockito.when(this.httpServletRequest.getSession(anyBoolean())).thenReturn(this.httpSession);
        PowerMockito.when(this.httpServletRequest.isRequestedSessionIdFromCookie()).thenReturn(true);
        PowerMockito.when(this.httpServletRequest.isRequestedSessionIdFromURL()).thenReturn(true);
        PowerMockito.when(this.httpServletRequest.isRequestedSessionIdValid()).thenReturn(true);
        PowerMockito.when(this.httpServletRequest.isSecure()).thenReturn(false);
        PowerMockito.when(this.httpServletRequest.isUserInRole(anyString())).thenReturn(false);
    }
}
