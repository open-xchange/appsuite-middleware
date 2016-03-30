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

package com.openexchange.subscribe.google;

import com.openexchange.ajax.oauth.client.actions.OAuthService;
import com.openexchange.ajax.oauth.client.actions.InitOAuthAccountRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.AbstractSubscribeTestEnvironment;

/**
 * {@link GoogleSubscribeTestEnvironment}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GoogleSubscribeTestEnvironment extends AbstractSubscribeTestEnvironment {

    protected static final String CONTACT_SOURCE_ID = "com.openexchange.subscribe.google.contact";

    protected static final String CALENDAR_SOURCE_ID = "com.openexchange.subscribe.google.calendar";

    private static final GoogleSubscribeTestEnvironment INSTANCE = new GoogleSubscribeTestEnvironment();

    /**
     * Get the instance of the environment
     *
     * @return the instance
     */
    public static final GoogleSubscribeTestEnvironment getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------- //


    /**
     * Initializes a new {@link GoogleSubscribeTestEnvironment}.
     * @param serviceId
     */
    protected GoogleSubscribeTestEnvironment() {
        super("com.openexchange.oauth.google");
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.AbstractSubscribeTestEnvironment#initEnvironment()
     */
    @Override
    protected void initEnvironment() throws Exception {
        InitOAuthAccountRequest req = new InitOAuthAccountRequest(OAuthService.GOOGLE);
        ajaxClient.execute(req);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.subscribe.AbstractSubscribeTestEnvironment#createSubscriptions()
     */
    @Override
    protected void createSubscriptions() throws Exception {
        int userId = ajaxClient.getValues().getUserId();
        final int privateAppointmentFolder = ajaxClient.getValues().getPrivateAppointmentFolder();
        final int privateContactFolder = ajaxClient.getValues().getPrivateContactFolder();
        createSubscription(getAccountId(), CALENDAR_SOURCE_ID, FolderObject.CALENDAR, privateAppointmentFolder, userId);
        createSubscription(getAccountId(), CONTACT_SOURCE_ID, FolderObject.CONTACT, privateContactFolder, userId);

        // Give the asynchronous tasks a few seconds to finish
        System.out.print("Give the asynchronous tasks a few seconds to finish... ");
        Thread.sleep(15000);
        System.out.println("OK, proceeding with tests.");
    }
}
