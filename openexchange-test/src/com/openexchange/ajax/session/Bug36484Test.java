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

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.session.actions.FormLoginRequest;
import com.openexchange.ajax.session.actions.FormLoginResponse;
import com.openexchange.ajax.session.actions.StoreRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.java.Strings;

/**
 * Login using "formlogin" doesn't work after changing IP address of client
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug36484Test extends AbstractAJAXSession {

    private AJAXClient client;
    private String login;
    private String password;

    /**
     * Initializes a new {@link Bug36484Test}.
     *
     * @param name The test name
     */
    public Bug36484Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        AJAXConfig.init();
        login = AJAXConfig.getProperty(Property.LOGIN) + "@" + AJAXConfig.getProperty(Property.CONTEXTNAME);
        password = AJAXConfig.getProperty(Property.PASSWORD);
        client = new AJAXClient(new AJAXSession(), true);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != client && false == Strings.isEmpty(client.getSession().getId())) {
            client.logout();
        }
        super.tearDown();
    }

    public void testAutoFormLoginWithChangedIP() throws Exception {
        /*
         * perform initial form login & store session cookie
         */
        FormLoginResponse loginResponse = client.execute(new FormLoginRequest(login, password));
        String firstSessionID = loginResponse.getSessionId();
        assertNotNull("No session ID", firstSessionID);
        client.getSession().setId(firstSessionID);
        client.execute(new StoreRequest(firstSessionID));
        /*
         * perform second form login from other IP (faked via X-Forwarded-For header)
         */
        FormLoginRequest secondLoginRequest = new ChangedIPFormLoginRequest(login, password, "53.246.23.4");
        secondLoginRequest.setCookiesNeeded(false);
        FormLoginResponse secondLoginResponse = client.execute(secondLoginRequest);
        String secondSessionID = secondLoginResponse.getSessionId();
        assertFalse("Same session ID", firstSessionID.equals(secondSessionID));
        client.getSession().setId(secondSessionID);
    }

    private static final class ChangedIPFormLoginRequest extends FormLoginRequest {

        private final String fakedRemoteIP;

        /**
         * Initializes a new {@link ChangedIPFormLoginRequest}.
         *
         * @param login The login name
         * @param password The password
         * @param fakedRemoteIP The remote address to use
         */
        public ChangedIPFormLoginRequest(String login, String password, String fakedRemoteIP) {
            super(login, password);
            this.fakedRemoteIP = fakedRemoteIP;
        }

        @Override
        public Header[] getHeaders() {
            return new Header[] { new com.openexchange.ajax.framework.Header.SimpleHeader("X-OX-Test-Fake-Remote-IP", fakedRemoteIP) };
        }

    }

}
