/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.api.client.impl;

import java.net.URL;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.Credentials;
import com.openexchange.api.client.LoginInformation;
import com.openexchange.api.client.common.calls.login.AccessShareCall;
import com.openexchange.api.client.common.calls.login.AnonymousLoginCall;
import com.openexchange.api.client.common.calls.login.GuestLoginCall;
import com.openexchange.api.client.common.calls.login.RedeemTokenCall;
import com.openexchange.api.client.common.calls.login.ShareLoginInformation;
import com.openexchange.api.client.common.calls.user.GetUserCall;
import com.openexchange.api.client.common.calls.user.UserInformation;
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
    private final @NonNull Credentials credentials;

    /**
     * Initializes a new {@link ApiShareClient}.
     *
     * @param services The service lookup
     * @param contextId The context identifier of this local OX node
     * @param userId The user identifier of this local OX node
     * @param sessionId The session identifier of this local OX node
     * @param loginTarget The link to the target to log in into
     * @param credentials The credentials to access the targets resources
     */
    public ApiShareClient(ServiceLookup services, int contextId, int userId, String sessionId, URL loginTarget, Credentials credentials) {
        super(services, contextId, userId, sessionId, loginTarget);
        this.credentials = null == credentials ? Credentials.EMPTY : credentials;
    }

    @Override
    @Nullable
    public LoginInformation getLoginInformation() {
        return information;
    }

    @Override
    @NonNull
    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    protected synchronized void doLogin() throws OXException {
        /*
         * Clean up data from former logins
         */
        this.information = null;

        /*
         * Access the share
         */
        AccessShareCall accessCall = new AccessShareCall(loginLink);
        ShareLoginInformation infos = execute(accessCall);

        /*
         * Validate result and perform further calls if needed
         */
        checkResponse(infos);
        String loginType = null == infos.getLoginType() ? "" : infos.getLoginType();
        switch (loginType) {
            case "anonymous_password":
                this.information = doAnonymousLogin(infos.getToken());
                break;
            case "guest":
            case "guest_password":
                this.information = doGuestLogin(infos);
                break;
            case "message_continue":
                this.information = doGuestOnRemovedShareTarget(infos.getToken());
                break;
            default:
                /*
                 * Login should have been successful.
                 */
                this.information = infos;
        }

        if (Strings.isEmpty(information.getRemoteSessionId())) {
            /*
             * No session could be created
             */
            throw ApiClientExceptions.NO_ACCESS.create(loginLink);
        }
    }

    /*
     * ============================== HELPERS ==============================
     */

    /**
     * Performs a guest login
     *
     * @param shareLoginInfos The infos
     * @throws OXException In case login is not possible
     * @return The login information
     */
    private LoginInformation doGuestLogin(ShareLoginInformation shareLoginInfos) throws OXException {
        GuestLoginCall loginCall = new GuestLoginCall(credentials, shareLoginInfos.getLoginName(), shareLoginInfos.getShare(), shareLoginInfos.getTarget());
        return execute(loginCall);
    }

    /**
     * Performs a login even tough the specific share target has been removed.
     * However guest still exists. Redeem token and login as guest.
     *
     * @param shareLoginInfos The token to redeem
     * @throws OXException In case login is not possible
     * @return The login information
     * @see {@link com.openexchange.share.servlet.handler.WebUIShareHandler#redirectToLoginPage(AccessShareRequest, HttpServletRequest, HttpServletResponse)}
     */
    private LoginInformation doGuestOnRemovedShareTarget(String token) throws OXException {
        ShareLoginInformation infos = execute(new RedeemTokenCall(token));
        GuestLoginCall loginCall = new GuestLoginCall(credentials, infos.getLoginName(), infos.getShare(), infos.getTarget());
        LoginInformation loginInformation = execute(loginCall);
        this.information = loginInformation;

        /*
         * Remote mail address isn't resolved yet, see JavaDoc link.
         * Execute additional call to get missing information
         */
        UserInformation userInformation = execute(new GetUserCall());
        return new CompositingLoginInformation(loginInformation, userInformation);
    }

    /**
     * Performs a login for an anonymous guest user with a password for the share
     *
     * @param token The token to redeem
     * @throws OXException In case login is not possible
     * @return The login information
     */
    private LoginInformation doAnonymousLogin(String token) throws OXException {
        /*
         * Precondition check
         */
        if (null == credentials.getPassword()) {
            throw ApiClientExceptions.MISSING_CREDENTIALS.create();
        }
        /*
         * Redeem the token to get the information for a login
         */
        ShareLoginInformation infos = execute(new RedeemTokenCall(token));
        /*
         * Perform login with password
         */
        AnonymousLoginCall loginCall = new AnonymousLoginCall(services, credentials, infos.getShare(), infos.getTarget());
        return execute(loginCall);
    }

    /**
     * Checks the response for common error cases
     *
     * @param shareLoginInfos The login infos
     * @throws OXException In case the share can't be accessed
     * @see {@link com.openexchange.share.servlet.utils.LoginType#MESSAGE}
     * @see {@link com.openexchange.share.servlet.handler.WebUIShareHandler#redirectToLoginPage(AccessShareRequest, HttpServletRequest, HttpServletResponse)}
     * @see {@link com.openexchange.share.servlet.utils.LoginLocation#status(String)}
     */
    private void checkResponse(ShareLoginInformation shareLoginInfos) throws OXException {
        if (false == "message".equals(shareLoginInfos.getLoginType())) {
            /*
             * No errors, continue.
             */
            return;
        }
        /*
         * Resolve token and get more infos
         */
        ShareLoginInformation errorInfos = execute(new RedeemTokenCall(shareLoginInfos.getToken()));
        String status = null == errorInfos.getStatus() ? "" : errorInfos.getStatus();
        switch (status) {
            case "not_found":
                /*
                 * This indicates a removed resource
                 */
                throw ApiClientExceptions.ACCESS_REVOKED.create();
            case "client_blacklisted":
                /*
                 * Permanent error, can only be resolved by the remote server
                 */
                LoggerFactory.getLogger(ApiShareClient.class).info("Remote OX {} blacklisted API client. Can't resolve share.", loginLink.getHost());
                throw ApiClientExceptions.NO_ACCESS.create(loginLink);
            case "internal_error":
            default:
                /*
                 * "Try again later" error or unexpected response
                 */
                throw ApiClientExceptions.REMOTE_SERVER_ERROR.create(null == errorInfos.getMessage() ? "" : errorInfos.getMessage());
        }
    }
}
