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

package com.openexchange.ajax.onboarding.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.rmi.server.UID;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.testing.httpclient.models.UserAttribute;
import com.openexchange.testing.httpclient.models.UserAttributionResponse;
import com.openexchange.testing.httpclient.modules.ClientonboardingApi;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link AbstractPlistSMSTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class AbstractPlistSMSTest extends AbstractConfigAwareAPIClientSession {

    protected static final String UID = new UID((short) 1).toString();

    protected static final String[] SCENARIOS = new String[] { "apple.iphone/mailsync", "apple.iphone/eassync", "apple.iphone/davsync" };

    protected ClientonboardingApi onboardingApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpConfiguration();
        UserApi userApi = new UserApi(getApiClient());
        UserAttribute attribute = new UserAttribute();
        attribute.setName("user_sms_link_secret");
        attribute.setValue(UID);

        UserAttributionResponse response = userApi.setUserAttribute(getApiClient().getSession(), String.valueOf(getApiClient().getUserId()), attribute, false);
        assertNull(response.getErrorDesc(), response.getError());

        onboardingApi = new ClientonboardingApi(getApiClient());
    }

    private void checkException(String code, String prefix, int number, String message) {
        assertNotNull("Unexpected response from the server! Response does not contain an exception.", code);
        assertTrue("The error code should start with " + prefix + " but it is " + code + " instead (error: " + message + ")", code.startsWith(prefix));
        int actualCodeNumber = Integer.parseInt(code.substring(code.indexOf("-")+1));
        assertEquals("Wrong exception number (error: " + message + ")!", number, actualCodeNumber);
    }

    protected void checkException(String code, DisplayableOXExceptionCode exception) {
        checkException(code, exception.getPrefix(), exception.getNumber(), exception.getDisplayMessage());
    }

}
