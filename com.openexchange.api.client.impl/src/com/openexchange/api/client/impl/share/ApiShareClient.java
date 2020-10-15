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

package com.openexchange.api.client.impl.share;

import java.net.URL;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.Credentials;
import com.openexchange.api.client.LoginInformation;
import com.openexchange.api.client.common.calls.login.AccessShareCall;
import com.openexchange.api.client.common.calls.login.AnonymousLoginCall;
import com.openexchange.api.client.common.calls.login.GuestLoginCall;
import com.openexchange.api.client.common.calls.login.RedeemTokenCall;
import com.openexchange.api.client.common.calls.login.ShareLoginInformation;
import com.openexchange.api.client.impl.AbstractApiClient;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ApiShareClient} - Logins the client for a share.
 * <p>
 * For possible login types, see com.openexchange.share.servlet.utils.LoginType
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ApiShareClient extends AbstractApiClient {

    private LoginInformation information;
    private final Credentials credentials;

    /**
     * Initializes a new {@link ApiShareClient}.
     *
     * @param services The service lookup
     * @param contextId The context identifier of this local OX node
     * @param userId The user identifier of this local OX node
     * @param loginTarget The link to the target to log in into
     * @param credentials The credentials to access the targets resources
     */
    public ApiShareClient(ServiceLookup services, int contextId, int userId, URL loginTarget, Credentials credentials) {
        super(services, contextId, userId, loginTarget);
        this.credentials = null == credentials ? new Credentials("") : credentials;
    }

    @Override
    @Nullable
    public LoginInformation getLoginInformation() {
        return information;
    }

    @Override
    @Nullable
    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    protected synchronized void doLogin() throws OXException {
        AccessShareCall accessCall = new AccessShareCall(loginLink);
        ShareLoginInformation shareLoginInfos = execute(accessCall);

        String loginType = shareLoginInfos.getLoginType();
        if ("message".equals(loginType) || "message_continue".equals(loginType)) {
            throw ApiClientExceptions.ACCESS_REVOKED.create();
        }

        if ("anonymous_password".equals(loginType)) {
            /*
             * Perform anonymous login
             */
            shareLoginInfos = execute(new RedeemTokenCall(shareLoginInfos.getToken()));
            /*
             * Perform login with password
             */
            AnonymousLoginCall loginCall = new AnonymousLoginCall(services, credentials, shareLoginInfos.getShare(), shareLoginInfos.getTarget());
            this.information = execute(loginCall);

        } else if ("guest".equals(loginType) || "guest_password".equals(loginType)) {
            GuestLoginCall loginCall = new GuestLoginCall(credentials, shareLoginInfos.getLoginName(), shareLoginInfos.getShare(), shareLoginInfos.getTarget());
            this.information = execute(loginCall);
        } else {
            /*
             * Login should have been successful
             */
            this.information = shareLoginInfos;
        }

        if (Strings.isEmpty(information.getRemoteSessionId())) {
            /*
             * No session could be created
             */
            throw ApiClientExceptions.NO_ACCESS.create(loginLink);
        }
    }
}
