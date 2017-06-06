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

package com.openexchange.webdav;

import static org.junit.Assert.assertTrue;
import org.jdom2.Namespace;
import org.junit.Before;
import com.meterware.httpunit.Base64;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.framework.ProvisioningSetup;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.framework.Constants;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public abstract class AbstractWebdavTest {

    protected static final String propertyHost = "hostname";

    protected static final Namespace webdav = Constants.NS_DAV;

    private String hostName = "localhost";

    protected String login = null;

    protected String password = null;

    protected String secondlogin = null;

    protected String context;

    protected int userId = -1;

    protected String authData = null;

    protected WebRequest req = null;

    protected WebResponse resp = null;

    protected WebConversation webCon = null;

    protected WebConversation secondWebCon = null;

    protected static final int dayInMillis = 86400000;

    public static final String AUTHORIZATION = "authorization";

    protected TestContext testContext;

    protected String userParticipant2;

    protected String userParticipant3;

    protected String groupParticipant;

    protected String resourceParticipant;

    /**
     * {@inheritDoc}
     */
    @Before
    public void setUp() throws Exception {
        ProvisioningSetup.init();

        webCon = new WebConversation();
        secondWebCon = new WebConversation();

        testContext = TestContextPool.getAllTimeAvailableContexts().get(0);
        TestUser testUser = testContext.acquireUser();
        login = testUser.getLogin();
        password = testUser.getPassword();
        context = testContext.getName();

        TestUser testUser2 = testContext.acquireUser();
        secondlogin = testUser2.getLogin();

        hostName = AJAXConfig.getProperty(Property.PROTOCOL) + "://" + AJAXConfig.getProperty(Property.HOSTNAME);

        userParticipant2 = testContext.getUserParticipants().get(1) + "@" + context;
        userParticipant3 = testContext.getUserParticipants().get(2) + "@" + context;
        groupParticipant = testContext.getGroupParticipants().get(0) + "@" + context;
        resourceParticipant = testContext.getResourceParticipants().get(0) + "@" + context;

        userId = GroupUserTest.getUserId(getWebConversation(), getHostURI(), getLogin(), getPassword());
        assertTrue("user not found", userId != -1);

        authData = getAuthData(login, password);
    }

    protected static String getAuthData(String login, String password) throws Exception {
        if (password == null) {
            password = "";
        }
        return new String(Base64.encode(login + ":" + password));
    }

    protected WebConversation getWebConversation() {
        return webCon;
    }

    protected WebConversation getNewWebConversation() {
        return new WebConversation();
    }

    protected WebConversation getSecondWebConversation() {
        return secondWebCon;
    }

    protected String getHostURI() {
        return hostName;
    }

    protected String getLogin() {
        return login;
    }

    protected String getPassword() {
        return password;
    }

    protected String getSecondLogin() {
        return secondlogin;
    }
}
