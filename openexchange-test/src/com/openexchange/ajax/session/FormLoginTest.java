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

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.session.actions.FormLoginRequest;
import com.openexchange.ajax.session.actions.FormLoginResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;

/**
 * Tests the action formLogin of the login servlet.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FormLoginTest extends AbstractAJAXSession {

    private String login;

    private String password;

    public FormLoginTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        AJAXConfig.init();
        login = AJAXConfig.getProperty(Property.LOGIN) + "@" + AJAXConfig.getProperty(Property.CONTEXTNAME);
        password = AJAXConfig.getProperty(Property.PASSWORD);
    }

    @Override
    protected void tearDown() throws Exception {
        login = null;
        password = null;
        super.tearDown();
    }

    public void testFormLogin() throws Exception {
        final AJAXSession session = new AJAXSession();
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            FormLoginResponse response = myClient.execute(new FormLoginRequest(login, password));
            assertNotNull("Path of redirect response is not found.", response.getPath());
            assertNotNull("Session identifier not found as fragment.", response.getSessionId());
            assertNotNull("Login string was not found as fragment.", response.getLogin());
            assertNotSame("", I(-1), I(response.getUserId()));
            assertNotNull("Language string was not found as fragment.", response.getLanguage());
            session.setId(response.getSessionId());
        } finally {
            myClient.logout();
        }
    }

    /**
     * This test is disabled because it tests a hidden feature. Normally the formLogin request requires an authId. This test verifies that
     * the formLogin request works without authId. To disable the required authId put
     * <code>com.openexchange.login.formLoginWithoutAuthId=true</code> into the login.properties configuration file.
     */
    public void dontTestFormLoginWithoutAuthId() throws Exception {
        final AJAXSession session = new AJAXSession();
        final AJAXClient myClient = new AJAXClient(session, false);
        try {
            FormLoginResponse response = myClient.execute(new FormLoginRequest(login, password, null));
            assertNotNull("Path of redirect response is not found.", response.getPath());
            assertNotNull("Session identifier not found as fragment.", response.getSessionId());
            assertNotNull("Login string was not found as fragment.", response.getLogin());
            assertNotSame("", I(-1), I(response.getUserId()));
            assertNotNull("Language string was not found as fragment.", response.getLanguage());
            session.setId(response.getSessionId());
        } finally {
            myClient.logout();
        }
    }
}
