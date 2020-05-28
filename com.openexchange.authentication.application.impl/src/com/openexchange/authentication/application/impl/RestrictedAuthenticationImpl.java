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

package com.openexchange.authentication.application.impl;

import static com.openexchange.authentication.application.impl.AppPasswordSessionStorageParameterNamesProvider.PARAM_APP_PASSWORD_ID;
import static com.openexchange.authentication.application.impl.AppPasswordSessionStorageParameterNamesProvider.PARAM_RESTRICTED;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.authentication.application.AppPasswordMailOauthService;
import com.openexchange.authentication.application.RestrictedAuthentication;
import com.openexchange.authentication.application.storage.AuthenticatedApplicationPassword;
import com.openexchange.exception.OXException;
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
                session.setParameter(PARAM_RESTRICTED, scopes);
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
