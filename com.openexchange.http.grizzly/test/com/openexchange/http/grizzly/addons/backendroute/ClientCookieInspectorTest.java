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

package com.openexchange.http.grizzly.addons.backendroute;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.Header;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.http.grizzly.filter.backendroute.ClientCookieInspector;

/**
 * {@link ClientCookieInspectorTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ClientCookieInspectorTest {

    protected final static String OX_PUBLIC_SESSION_COOKIE = "open-xchange-public-session=a6e16d4e09a148bbbbebcc99fa7cfdf7";

    protected final static String OX_SECRET_COOKIE = "open-xchange-secret-RuJgKjzyPBBJptrKxlX7A=182873790d25496f98e40086a77a4cb5";

    protected final static String OX_SESSION_COOKIE = "";

    protected final static String JSESSIONID_VALUE = "5a5b7c0100910984944";

    protected final static String JSESSIONID = "JSESSIONID=" + JSESSIONID_VALUE;

    protected final static String BACKENDROUTE_WRONG = "0OX1";

    protected final static String BACKENDROUTE_CORRECT = "OX1";

    protected final static String BASIC_HEADER = OX_PUBLIC_SESSION_COOKIE + "; " + OX_SECRET_COOKIE;

    protected final static String HEADER_NO_JSESSIONID = BASIC_HEADER;

    protected final static String HEADER_JSESSIONID_NO_ROUTE = BASIC_HEADER + "; " + JSESSIONID;

    protected final static String HEADER_JSESSIONID_WRONG_ROUTE = HEADER_JSESSIONID_NO_ROUTE + "." + BACKENDROUTE_WRONG;

    protected final static String HEADER_JSESSIONID_CORRECT_ROUTE = HEADER_JSESSIONID_NO_ROUTE + "." + BACKENDROUTE_CORRECT;

    /** Header contains no JSessionId */
    private ClientCookieInspector noJSessionIdInspector;

    /** JSessionId has no route but backend defines one */
    private ClientCookieInspector noJSessionIdRouteBackendRouteInspector;

    /** JSessionId has wrong route but backend defines one */
    private ClientCookieInspector wrongJSessionIdRouteBackendRouteInspector;

    /** JSessionId has same route as backend */
    private ClientCookieInspector correctJSessionIdRouteBackendRouteInspector;

    /** JSessionId is wrong because backend defines none */
    private ClientCookieInspector wrongJSessionIdRouteNoBackendRouteInspector;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        noJSessionIdInspector = new ClientCookieInspector(httpRequestPacketFromHeaderLine(HEADER_NO_JSESSIONID), BACKENDROUTE_CORRECT);
        noJSessionIdRouteBackendRouteInspector = new ClientCookieInspector(httpRequestPacketFromHeaderLine(HEADER_JSESSIONID_NO_ROUTE), BACKENDROUTE_CORRECT);
        wrongJSessionIdRouteBackendRouteInspector = new ClientCookieInspector(httpRequestPacketFromHeaderLine(HEADER_JSESSIONID_WRONG_ROUTE), BACKENDROUTE_CORRECT);
        correctJSessionIdRouteBackendRouteInspector = new ClientCookieInspector(httpRequestPacketFromHeaderLine(HEADER_JSESSIONID_CORRECT_ROUTE), BACKENDROUTE_CORRECT);
        wrongJSessionIdRouteNoBackendRouteInspector = new ClientCookieInspector(httpRequestPacketFromHeaderLine(HEADER_JSESSIONID_WRONG_ROUTE), "");
    }

    /**
     * Test method for {@link com.openexchange.http.grizzly.filter.backendroute.AbstractCookieInspector#isJSessionIdExistant()}.
     */
    @Test
    public void testIsJSessionIdExistant() {
        assertFalse(noJSessionIdInspector.isJSessionIdExistant());
        assertTrue(noJSessionIdRouteBackendRouteInspector.isJSessionIdExistant());
        assertTrue(wrongJSessionIdRouteBackendRouteInspector.isJSessionIdExistant());
        assertTrue(correctJSessionIdRouteBackendRouteInspector.isJSessionIdExistant());
        assertTrue(wrongJSessionIdRouteNoBackendRouteInspector.isJSessionIdExistant());

    }

    /**
     * Test method for {@link com.openexchange.http.grizzly.filter.backendroute.AbstractCookieInspector#isJSessionIdValid()}.
     */
    @Test
    public void testIsJSessionIdValid() {
        assertFalse(noJSessionIdInspector.isJSessionIdValid());
        assertFalse(noJSessionIdRouteBackendRouteInspector.isJSessionIdValid());
        assertFalse(wrongJSessionIdRouteBackendRouteInspector.isJSessionIdValid());
        assertTrue(correctJSessionIdRouteBackendRouteInspector.isJSessionIdValid());
        assertFalse(wrongJSessionIdRouteNoBackendRouteInspector.isJSessionIdValid());
    }

    /**
     * Test method for {@link com.openexchange.http.grizzly.filter.backendroute.AbstractCookieInspector#getJSessionIdValue()}.
     */
    @Test
    public void testGetJSessionIdValue() {
        assertTrue(noJSessionIdInspector.getJSessionIdValue().isEmpty());
        assertEquals(JSESSIONID_VALUE, noJSessionIdRouteBackendRouteInspector.getJSessionIdValue());
        assertEquals(JSESSIONID_VALUE+"."+BACKENDROUTE_WRONG, wrongJSessionIdRouteBackendRouteInspector.getJSessionIdValue());
        assertEquals(JSESSIONID_VALUE+"."+BACKENDROUTE_CORRECT, correctJSessionIdRouteBackendRouteInspector.getJSessionIdValue());
        assertEquals(JSESSIONID_VALUE+"."+BACKENDROUTE_WRONG, wrongJSessionIdRouteNoBackendRouteInspector.getJSessionIdValue());
    }
    /**
     * Test method for {@link com.openexchange.http.grizzly.filter.backendroute.ClientCookieInspector#getCookieHeaderLine()}.
     */
    @Test
    public void testGetCookieHeaderLine() {
        assertEquals(headerLineToSet(HEADER_NO_JSESSIONID),
            headerLineToSet(noJSessionIdInspector.getCookieHeaderLine().toStringContent()));

        assertEquals(headerLineToSet(HEADER_JSESSIONID_NO_ROUTE),
            headerLineToSet(noJSessionIdRouteBackendRouteInspector.getCookieHeaderLine().toStringContent()));

        assertEquals(headerLineToSet(HEADER_JSESSIONID_WRONG_ROUTE),
            headerLineToSet(wrongJSessionIdRouteBackendRouteInspector.getCookieHeaderLine().toStringContent()));

        assertEquals(headerLineToSet(HEADER_JSESSIONID_CORRECT_ROUTE),
            headerLineToSet(correctJSessionIdRouteBackendRouteInspector.getCookieHeaderLine().toStringContent()));

        assertEquals(headerLineToSet(HEADER_JSESSIONID_WRONG_ROUTE),
            headerLineToSet(wrongJSessionIdRouteNoBackendRouteInspector.getCookieHeaderLine().toStringContent()));
    }

    /**
     * Test method for {@link com.openexchange.http.grizzly.filter.backendroute.AbstractCookieInspector#fixJSessionId()}.
     */
    @Test
    public void testFixJSessionId() {
        // no route in id, append it
        noJSessionIdRouteBackendRouteInspector.fixJSessionId();
        assertEquals(
            "JSessionIds not fixed",
            JSESSIONID_VALUE + "." + BACKENDROUTE_CORRECT,
            noJSessionIdRouteBackendRouteInspector.getJSessionIdValue());

        // wrong route in id, replace it
        wrongJSessionIdRouteBackendRouteInspector.fixJSessionId();
        assertEquals(
            "JSessionIds not fixed",
            JSESSIONID_VALUE + "." + BACKENDROUTE_CORRECT,
            wrongJSessionIdRouteBackendRouteInspector.getJSessionIdValue());

        // correct route,don't break it
        correctJSessionIdRouteBackendRouteInspector.fixJSessionId();
        assertEquals(
            "JSessionIds not fixed",
            JSESSIONID_VALUE + "." + BACKENDROUTE_CORRECT,
            correctJSessionIdRouteBackendRouteInspector.getJSessionIdValue());

        // wrong route in id, backend has none, remove it
        wrongJSessionIdRouteNoBackendRouteInspector.fixJSessionId();
        assertEquals("JSessionIds not fixed", JSESSIONID_VALUE, wrongJSessionIdRouteNoBackendRouteInspector.getJSessionIdValue());
    }

    private Set<String> headerLineToSet(String headerLine) {
        return new HashSet<String>(Arrays.asList(headerLine.toString().split("; ")));
    }

    private HttpRequestPacket httpRequestPacketFromHeaderLine(String headerLine) {
        HttpRequestPacket packet = HttpRequestPacket.builder().header(Header.Cookie, headerLine).build();
        return packet;
    }

}
