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

package com.openexchange.saml.oauth.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.lock.LockService;
import com.openexchange.mail.api.AuthenticationFailedHandler;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.saml.oauth.HttpClientOAuthAccessTokenService;
import com.openexchange.saml.oauth.OAuthFailedAuthenticationHandler;
import com.openexchange.saml.oauth.SAMLOAuthConfig;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class Activator extends HousekeepingActivator {

    private static final int SERVICE_RANKING = 100;

    private HttpClientOAuthAccessTokenService tokenService;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { SessiondService.class, ConfigViewFactory.class, SSLSocketFactoryProvider.class, SSLConfigurationService.class, LockService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        HttpClientOAuthAccessTokenService tokenService = new HttpClientOAuthAccessTokenService(getService(ConfigViewFactory.class), getService(SSLSocketFactoryProvider.class), getService(SSLConfigurationService.class));
        this.tokenService = tokenService;

        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                SAMLOAuthConfig.invalidateCache();
            }

            @Override
            public Interests getInterests() {
                return null;
            }
        });

        registerService(OAuthAccessTokenService.class, tokenService);
        registerService(AuthenticationFailedHandler.class, new OAuthFailedAuthenticationHandler(tokenService, this), SERVICE_RANKING);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();

        HttpClientOAuthAccessTokenService tokenService = this.tokenService;
        if (tokenService != null) {
            this.tokenService = null;
            tokenService.closeHttpClient();
        }
    }

}
