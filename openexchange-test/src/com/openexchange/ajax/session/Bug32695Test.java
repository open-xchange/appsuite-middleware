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

package com.openexchange.ajax.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.List;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.FormLoginRequest;
import com.openexchange.ajax.session.actions.FormLoginResponse;
import com.openexchange.ajax.session.actions.StoreRequest;
import com.openexchange.test.pool.TestUser;

/**
 * Session count steadily grows with usage of form login
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug32695Test extends AbstractAJAXSession {
    
    private AJAXClient client;
    private TestUser acquireUser;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        acquireUser = testContext.acquireUser();
        client = new AJAXClient(new AJAXSession(), true);
    }

    @Test
    public void testAutoFormLogin() throws Exception {
        /*
         * perform initial form login & store session cookie
         */
        String firstSessionID = firstFormLogin(true);
        /*
         * perform second form login
         */
        FormLoginRequest secondLoginRequest = new FormLoginRequest(acquireUser.getLogin(), acquireUser.getPassword());
        secondLoginRequest.setCookiesNeeded(false);
        FormLoginResponse secondLoginResponse = this.client.execute(secondLoginRequest);
        String secondSessionID = secondLoginResponse.getSessionId();
        assertNotNull("No session ID", secondSessionID);
        assertEquals("Different session IDs", firstSessionID, secondSessionID);
    }

    @Test
    public void testAutoFormLoginWithOtherUser() throws Exception {
        /*
         * perform initial form login & store session cookie
         */
        String firstSessionID = firstFormLogin(true);
        /*
         * perform second form login
         */
        TestUser acquireUser2 = testContext.acquireUser();
        FormLoginRequest secondLoginRequest = new FormLoginRequest(acquireUser2.getLogin(), acquireUser2.getPassword());
        secondLoginRequest.setCookiesNeeded(false);
        FormLoginResponse secondLoginResponse = this.client.execute(secondLoginRequest);
        String secondSessionID = secondLoginResponse.getSessionId();
        assertFalse("Same session ID", firstSessionID.equals(secondSessionID));
        this.client.getSession().setId(secondSessionID);
    }

    @Test
    public void testAutoFormLoginWithWrongCredentials() throws Exception {
        /*
         * perform initial form login & store session cookie
         */
        firstFormLogin(true);
        /*
         * perform second form login with wrong credentials
         */
        FormLoginRequest secondLoginRequest = new FormLoginRequest(acquireUser.getLogin(), "wrongpassword");
        secondLoginRequest.setCookiesNeeded(false);
        AssertionError expectedError = null;
        try {
            this.client.execute(secondLoginRequest);
        } catch (AssertionError e) {
            expectedError = e;
        }
        assertNotNull("No errors performing second login with wrong password", expectedError);
    }

    @Test
    public void testAutoFormLoginWithWrongSecretCookie() throws Exception {
        /*
         * perform initial form login & store session cookie
         */
        String firstSessionID = firstFormLogin(true);
        /*
         * perform second form login with wrong secret cookie
         */
        findCookie(LoginServlet.SECRET_PREFIX).setValue("wrongsecret");
        FormLoginRequest secondLoginRequest = new FormLoginRequest(acquireUser.getLogin(), acquireUser.getPassword());
        secondLoginRequest.setCookiesNeeded(false);
        FormLoginResponse secondLoginResponse = this.client.execute(secondLoginRequest);
        String secondSessionID = secondLoginResponse.getSessionId();
        assertFalse("Same session ID", firstSessionID.equals(secondSessionID));
        this.client.getSession().setId(secondSessionID);
    }

    @Test
    public void testAutoFormLoginWithWrongSessionCookie() throws Exception {
        /*
         * perform initial form login & store session cookie
         */
        String firstSessionID = firstFormLogin(true);
        /*
         * perform second form login with wrong secret cookie
         */
        findCookie(LoginServlet.SESSION_PREFIX).setValue("wrongsession");
        FormLoginRequest secondLoginRequest = new FormLoginRequest(acquireUser.getLogin(), acquireUser.getPassword());
        secondLoginRequest.setCookiesNeeded(false);
        FormLoginResponse secondLoginResponse = this.client.execute(secondLoginRequest);
        String secondSessionID = secondLoginResponse.getSessionId();
        assertFalse("Same session ID", firstSessionID.equals(secondSessionID));
        this.client.getSession().setId(secondSessionID);
    }

    @Test
    public void testAutoFormLoginWithoutStore() throws Exception {
        /*
         * perform initial form login, don't store session cookie
         */
        String firstSessionID = firstFormLogin(false);
        /*
         * perform second form login
         */
        FormLoginRequest secondLoginRequest = new FormLoginRequest(acquireUser.getLogin(), acquireUser.getPassword());
        secondLoginRequest.setCookiesNeeded(false);
        FormLoginResponse secondLoginResponse = this.client.execute(secondLoginRequest);
        String secondSessionID = secondLoginResponse.getSessionId();
        assertFalse("Same session ID", firstSessionID.equals(secondSessionID));
        this.client.getSession().setId(secondSessionID);
    }

    private String firstFormLogin(boolean store) throws Exception {
        FormLoginResponse loginResponse = this.client.execute(new FormLoginRequest(acquireUser.getLogin(), acquireUser.getPassword()));
        String sessionID = loginResponse.getSessionId();
        assertNotNull("No session ID", sessionID);
        this.client.getSession().setId(sessionID);
        if (store) {
            this.client.execute(new StoreRequest(sessionID));
        }
        return sessionID;
    }

    private BasicClientCookie findCookie(String prefix) {
        List<Cookie> cookies = this.client.getSession().getHttpClient().getCookieStore().getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            if (cookies.get(i).getName().startsWith(prefix)) {
                return (BasicClientCookie) cookies.get(i);
            }
        }
        fail("No cookie with prefix \"" + prefix + "\" found");
        return null;
    }

}
