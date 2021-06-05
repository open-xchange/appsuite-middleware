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

package com.openexchange.saml;

import static org.junit.Assert.assertEquals;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.java.util.UUIDs;
import com.openexchange.saml.tools.SAMLLoginTools;
import com.openexchange.session.Session;

/**
 * {@link SAMLLoginToolsTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class SAMLLoginToolsTest {

    private Session session;

    @Before
    public void setUp() {
        session = Mockito.mock(Session.class);
        Mockito.when(session.getSessionID()).thenReturn(UUIDs.getUnformattedStringFromRandom());
    }

    @Test
    public void noResponseHeaderSplittingOnMaliciousPathRelative() {
        String location = SAMLLoginTools.buildFrontendRedirectLocation(session, "/appsuite/\r\nInvalid-Header: true", null);
        assertEquals("/appsuite/Invalid-Header:%20true#session=" + AJAXUtility.encodeUrl(session.getSessionID()), location);
    }

    @Test
    public void noResponseHeaderSplittingOnMaliciousPathAbsolute() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getServerName()).thenReturn("example.com");
        String location = SAMLLoginTools.buildAbsoluteFrontendRedirectLocation(request, session, "/appsuite/\r\nInvalid-Header: true", null);
        assertEquals("http://example.com/appsuite/Invalid-Header:%20true#session=" + AJAXUtility.encodeUrl(session.getSessionID()), location);
    }

    @Test
    public void properUriFragmentEncodingRelative() {
        String location = SAMLLoginTools.buildFrontendRedirectLocation(session, "/appsuite/", "!!&app=io.ox/mail&folder=default1//qewk4^Jqeit_5Wyfwsig");
        assertEquals("/appsuite/#session=" + AJAXUtility.encodeUrl(session.getSessionID()) + "&!!&app=io.ox/mail&folder=default1//qewk4%5EJqeit_5Wyfwsig", location);
    }

    @Test
    public void properUriFragmentEncodingAbsolute() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getServerName()).thenReturn("example.com");
        Mockito.when(request.getParameter(SAMLLoginTools.PARAM_URI_FRAGMENT)).thenReturn("!!&app=io.ox/mail&folder=default1//qewk4^Jqeit_5Wyfwsig");
        String location = SAMLLoginTools.buildAbsoluteFrontendRedirectLocation(request, session, "/appsuite/", null);
        assertEquals("http://example.com/appsuite/#session=" + AJAXUtility.encodeUrl(session.getSessionID()) + "&!!&app=io.ox/mail&folder=default1//qewk4%5EJqeit_5Wyfwsig", location);
    }


}
