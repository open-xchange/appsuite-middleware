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

import static com.openexchange.ajax.framework.AJAXClient.User.User1;
import static com.openexchange.ajax.framework.AJAXClient.User.User2;
import junit.framework.TestCase;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.sessiond.SessionExceptionCodes;

/**
 * Checks if the server detects correctly a duplicate used authId.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DuplicateAuthIdTest extends TestCase {

    private String sameAuthId;

    private AJAXClient client1;

    private String login1;

    private String password1;

    private AJAXSession session2;

    private AJAXClient client2;

    private String login2;

    private String password2;

    public DuplicateAuthIdTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AJAXConfig.init();
        sameAuthId = LoginTools.generateAuthId();
        final AJAXSession session1 = new AJAXSession();
        client1 = new AJAXClient(session1, false); // explicit logout in tearDown
        login1 = AJAXConfig.getProperty(User1.getLogin()) + "@" + AJAXConfig.getProperty(Property.CONTEXTNAME);
        password1 = AJAXConfig.getProperty(User1.getPassword());
        LoginResponse response = client1.execute(new LoginRequest(login1, password1, sameAuthId, LoginTest.class.getName(), "6.15.0"));

        session1.setId(response.getSessionId());
        session2 = new AJAXSession();
        session2.getConversation().putCookie("JSESSIONID", session1.getConversation().getCookieValue("JSESSIONID"));
        client2 = new AJAXClient(session2, false); // explicit logout in test method
        login2 = AJAXConfig.getProperty(User2.getLogin()) + "@" + AJAXConfig.getProperty(Property.CONTEXTNAME);
        ;
        password2 = AJAXConfig.getProperty(User2.getPassword());
    }

    @Override
    protected void tearDown() throws Exception {
        client1.logout();
        super.tearDown();
    }

    public void testDuplicateAuthId() throws Throwable {
        LoginResponse response = client2.execute(new LoginRequest(login2, password2, sameAuthId, LoginTest.class.getName(), "6.15.0", false));
        if (!response.hasError()) {
            session2.setId(response.getSessionId());
            client2.logout();
            fail("Duplicate authId not detected.");
        } else {
            OXException e = response.getException();
            OXException se = SessionExceptionCodes.DUPLICATE_AUTHID.create(login1, login2);
            assertTrue("Found wrong exception.", se.similarTo(e));
        }
    }
}
