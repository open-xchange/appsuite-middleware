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

package com.openexchange.http.testservlet;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

/**
 * Unit tests for {@link TestServlet}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class TestServletTest {

    /**
     * The class to test
     */
    private TestServlet testServlet = null;

    /**
     * Mock of {@HttpServletRequest}
     */
    private HttpServletRequest httpServletRequest = null;

    /**
     * Mock of {@HttpServletRequest}
     */
    private HttpServletResponse httpServletResponse = null;

    /**
     * Header and attribute name parameters
     */
    private Enumeration<String> parameters;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // MEMBERS
        this.httpServletRequest = PowerMockito.mock(HttpServletRequest.class);
        this.httpServletResponse = PowerMockito.mock(HttpServletResponse.class);
        this.parameters = PowerMockito.mock(Enumeration.class);

        // MEMBER BEHAVIOUR
        PowerMockito.when(this.parameters.hasMoreElements()).thenReturn(false);
        PowerMockito.when(this.httpServletRequest.getHeaderNames()).thenReturn(this.parameters);
        try {
            ServletOutputStream servletOutputStream = PowerMockito.mock(ServletOutputStream.class);
            PowerMockito.when(this.httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);
        } catch (IOException ioException) {
            // will not happen
        }

    }

    @Test(timeout = 500)
    public void testDoGet_ThreadSleepNotExecuted_ReturnedWithin2000ms() throws ServletException, IOException {
        this.testServlet = new TestServlet() {

            @Override
            public String getBody(HttpServletRequest req) {
                return "theBody";
            }
        };

        this.testServlet.doGet(this.httpServletRequest, this.httpServletResponse);
    }

    @Test(timeout = 500)
    public void testDoPut_ThreadSleepNotExecuted_ReturnedWithin2000ms() throws ServletException, IOException {
        this.testServlet = new TestServlet() {

            @Override
            public String getBody(HttpServletRequest req) {
                return "theBody";
            }
        };

        this.testServlet.doPut(this.httpServletRequest, this.httpServletResponse);
    }
}
