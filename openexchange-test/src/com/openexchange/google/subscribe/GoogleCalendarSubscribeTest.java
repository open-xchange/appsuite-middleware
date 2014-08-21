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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.google.subscribe;

import java.util.Collections;
import junit.framework.TestCase;

/**
 * {@link GoogleCalendarSubscribeTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GoogleCalendarSubscribeTest extends TestCase {

    private static final String REDIRECT_URI = "https://oxappsuite.qa.open-xchange.com/ajax/defer";

    private static final String CLIENT_SECRET = "g_3bO5B795OF3naL4PlvntG7";

    private static final String CLIENT_ID = "953985963799-l5osk884lhi476u4nd5bulpgblupm1em.apps.googleusercontent.com";

    /**
     * Initializes a new {@link GoogleCalendarSubscribeTest}.
     */
    public GoogleCalendarSubscribeTest(final String name) {
        super(name);
    }

    /**
     * Simple example on how to fetch the access token
     * 
     * @throws Exception
     */
    public void testInit() throws Exception {
        GoogleOAuthClient googleOAuthClient = new GoogleOAuthClient();
        googleOAuthClient.login("ewaldbartkowiak@googlemail.com", "BewIbgeawn4");
        final String authCode = googleOAuthClient.getAuthorizationCode(
            CLIENT_ID,
            CLIENT_SECRET,
            REDIRECT_URI,
            Collections.singletonList("https://www.googleapis.com/auth/calendar.readonly"));
        String accessToken = googleOAuthClient.getAccessToken(CLIENT_ID, CLIENT_SECRET, authCode, REDIRECT_URI).getAccessToken();
        assertNotNull(accessToken);
    }
}
