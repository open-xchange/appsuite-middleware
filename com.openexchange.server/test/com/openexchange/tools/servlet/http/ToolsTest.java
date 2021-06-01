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

package com.openexchange.tools.servlet.http;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertTrue;
import javax.servlet.http.sim.SimHttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.html.SimHtmlService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link ToolsTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class ToolsTest {

    private static final String QUERY_STRING_WITHOUT_PASSWORD = "action=autologin&client=open-xchange-appsuite&rampup=true&rampupFor=open-xchange-appsuite&version=7.8.0-0";

    private static final String QUERY_STRING_WITH_PASSWORD = "action=autologin&client=open-xchange-appsuite&rampup=true&rampupFor=open-xchange-appsuite&version=7.8.0-0&password=schalke04IsDerGeilsteVereinDerWelt";

    private SimHttpServletRequest request;

    @Mock
    private ConfigurationService configurationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(B(configurationService.getBoolProperty(Tools.COM_OPENEXCHANGE_CHECK_URL_PARAMS, true))).thenReturn(Boolean.TRUE);
        Tools.setConfigurationService(configurationService);

        request = new SimHttpServletRequest();
        request.setQueryString(QUERY_STRING_WITHOUT_PASSWORD);
    }

    @Test
    public void testCheckNonExistence_requestNull_doNothing() throws OXException {
        Tools.checkNonExistence(null, AJAXServlet.PARAMETER_PASSWORD);
    }

    @Test
    public void testCheckNonExistence_paramNull_doNothing() throws OXException {
        Tools.checkNonExistence(request, (String) null);
    }

    @Test
    public void testCheckNonExistence_nonNotAllowedFiled_doNothing() throws OXException {
        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_PASSWORD);
    }

    @Test(expected = OXException.class)
    public void testCheckNonExistence_containsNotAllowedField_throwException() throws OXException {
        request.setQueryString(QUERY_STRING_WITH_PASSWORD);

        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_PASSWORD);
    }

    @Test
    public void testCheckNonExistence_passwordInParamList_ignore() throws OXException {
        request.setParameter(AJAXServlet.PARAMETER_PASSWORD, "meinPassword");

        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_PASSWORD);
    }

    @Test
    public void testCheckNonExistence_queryStringNull_ignore() throws OXException {
        request.setQueryString(null);

        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_PASSWORD);
    }

    @Test(expected = OXException.class)
    public void testCheckNonExistence_multipleParamsWithNotAllowedField_throwException() throws OXException {
        request.setQueryString(QUERY_STRING_WITH_PASSWORD);

        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_IGNORE, AJAXServlet.PARAMETER_MAIL, AJAXServlet.PARAMETER_VERSION, AJAXServlet.PARAMETER_PASSWORD, AJAXServlet.PARAMETER_SESSION);
    }

    @Test(expected = OXException.class)
    public void testCheckNonExistence_multipleParamsAndNotAllowedFieldIncluded_throwException() throws OXException {
        request.setQueryString(QUERY_STRING_WITH_PASSWORD);

        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_IGNORE, AJAXServlet.PARAMETER_MAIL, AJAXServlet.PARAMETER_VERSION, AJAXServlet.PARAMETER_PASSWORD, AJAXServlet.PARAMETER_SESSION);
    }

    @Test(expected = OXException.class)
    public void testCheckNonExistence_multipleParamsAndVersionNotAllowed_throwException() throws OXException {
        request.setQueryString(QUERY_STRING_WITHOUT_PASSWORD);

        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_IGNORE, AJAXServlet.PARAMETER_MAIL, AJAXServlet.PARAMETER_VERSION, AJAXServlet.PARAMETER_PASSWORD, AJAXServlet.PARAMETER_SESSION);
    }

    @Test
    public void testCheckNonExistence_multipleParamsAndVersionNotAllowed_containsAllWrongParams() {
        request.setQueryString(QUERY_STRING_WITH_PASSWORD);

        try {
            Tools.checkNonExistence(request, AJAXServlet.PARAMETER_IGNORE, AJAXServlet.PARAMETER_MAIL, AJAXServlet.PARAMETER_VERSION, AJAXServlet.PARAMETER_PASSWORD, AJAXServlet.PARAMETER_SESSION);
        } catch (Exception e) {
            Assert.assertTrue("", e.getMessage().contains(AJAXServlet.PARAMETER_VERSION));
            Assert.assertTrue("", e.getMessage().contains(AJAXServlet.PARAMETER_PASSWORD));
        }
    }

    @Test
    public void testCheckNonExistence_multipleParamsAndNotAllowedFieldNotIncluded_ignore() throws OXException {
        request.setQueryString(QUERY_STRING_WITHOUT_PASSWORD);

        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_IGNORE, AJAXServlet.PARAMETER_MAIL, AJAXServlet.PARAMETER_PASSWORD, AJAXServlet.PARAMETER_SESSION);
    }

    @Test
    public void testCheckNonExistence_configurationServiceNull_ignore() throws OXException {
        Tools.setConfigurationService(null);

        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_IGNORE, AJAXServlet.PARAMETER_MAIL, AJAXServlet.PARAMETER_PASSWORD, AJAXServlet.PARAMETER_SESSION);
    }

    @Test
    public void testCheckNonExistence_configuredToNotCheck_ignore() throws OXException {
        Mockito.when(B(configurationService.getBoolProperty(Tools.COM_OPENEXCHANGE_CHECK_URL_PARAMS, true))).thenReturn(Boolean.FALSE);

        request.setQueryString(QUERY_STRING_WITHOUT_PASSWORD);

        Tools.checkNonExistence(request, AJAXServlet.PARAMETER_IGNORE, AJAXServlet.PARAMETER_MAIL, AJAXServlet.PARAMETER_PASSWORD, AJAXServlet.PARAMETER_SESSION);
    }

    @Test
    public void testForBug44409() {
        ServerServiceRegistry.getInstance().addService(HtmlService.class, new SimHtmlService());
        String errorPage = Tools.getErrorPage(403, null, "SES-0203 Categories=TRY_AGAIN Message='Your session <script>alert('XSS')</script>' expired. Please start a new browser session.' exceptionID=1-3");
        assertTrue("Not properly encoded for HTML:\n" + errorPage, errorPage.indexOf("<script>") < 0);
    }

}
