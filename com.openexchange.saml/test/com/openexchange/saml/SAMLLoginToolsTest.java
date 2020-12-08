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
 *    trademarks of the OX Software GmbH. group of companies.
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
