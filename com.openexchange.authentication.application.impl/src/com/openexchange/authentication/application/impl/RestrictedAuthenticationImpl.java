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

package com.openexchange.authentication.application.impl;

import static com.openexchange.authentication.application.impl.AppPasswordSessionStorageParameterNamesProvider.PARAM_APP_PASSWORD_ID;
import static com.openexchange.authentication.application.impl.AppPasswordSessionStorageParameterNamesProvider.PARAM_RESTRICTED;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.authentication.application.AppPasswordMailOauthService;
import com.openexchange.authentication.application.RestrictedAuthentication;
import com.openexchange.authentication.application.storage.AuthenticatedApplicationPassword;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link RestrictedAuthenticationImpl}
 *
 * Authentication from application specific authentication.
 * Contains session enhancement as well as the users main password
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class RestrictedAuthenticationImpl implements RestrictedAuthentication {

    private final SessionEnhancement sessionEnhancement;
    private final AuthenticatedApplicationPassword appPassword;

    /**
     * Initializes a new {@link RestrictedAuthenticationImpl}.
     *
     * @param services A service lookup reference
     * @param appPassword The authenticated application password
     * @param scopes The available scopes for the session
     * @param login The user name as used during the login request, which may get forwarded to obtain an OAuth token for mail access
     * @param loginPassword The password as used during the login request, which may get forwarded to obtain an OAuth token for mail access
     */
    public RestrictedAuthenticationImpl(ServiceLookup services, AuthenticatedApplicationPassword appPassword, String[] scopes, String login, String loginPassword) {
        super();
        this.appPassword = appPassword;
        this.sessionEnhancement = new SessionEnhancement() {

            @Override
            public void enhanceSession(Session session) {
                session.setParameter(PARAM_APP_PASSWORD_ID, appPassword.getApplicationPassword().getGUID());
                session.setParameter(PARAM_RESTRICTED, Strings.toCommaSeparatedList(scopes));
                try {
                    AuthType mailAuthType = MailConfig.getConfiguredAuthType(true, session);
                    if (AuthType.isOAuthType(mailAuthType)) {
                        AppPasswordMailOauthService oauthService = services.getOptionalService(AppPasswordMailOauthService.class);
                        if (oauthService != null) {
                            oauthService.getAndApplyToken(session, login, loginPassword);
                        }
                    }
                } catch (OXException e) {
                    org.slf4j.LoggerFactory.getLogger(RestrictedAuthenticationImpl.class).error("Error getting oauth token for mail access", e);
                }
            }
        };
    }

    @Override
    public String getContextInfo() {
        return String.valueOf(getContextID());
    }

    @Override
    public String getUserInfo() {
        String userInfo = appPassword.getApplicationPassword().getUserInfo();
        return userInfo.contains("@") ? userInfo.substring(userInfo.indexOf("@")) : userInfo;
    }

    @Override
    public SessionEnhancement getSessionEnhancement() {
        return sessionEnhancement;
    }

    @Override
    public String getPassword() {
        return appPassword.getApplicationPassword().getFullPassword();
    }

    @Override
    public int getContextID() {
        return appPassword.getContextId();
    }

    @Override
    public int getUserID() {
        return appPassword.getUserId();
    }

}
