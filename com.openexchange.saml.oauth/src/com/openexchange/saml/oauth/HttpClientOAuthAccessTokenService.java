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

package com.openexchange.saml.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.saml.oauth.service.OAuthAccessToken;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link HttpClientOAuthAccessTokenService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class HttpClientOAuthAccessTokenService implements OAuthAccessTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthAccessTokenService.class);

    // -----------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    
    private final OAuthAccessTokenRequest accessTokenRequest;
    private final OAuthRefreshTokenRequest refreshTokenRequest;

    /**
     * Initializes a new {@link HttpClientOAuthAccessTokenService}.
     * 
     * @param services The service lookup to get the {@link ConfigViewFactory} and the {@link HttpClientService} from
     */
    public HttpClientOAuthAccessTokenService(ServiceLookup services) {
        super();
        this.services = services;

        // Initialize request instances
        accessTokenRequest = new OAuthAccessTokenRequest(services, "saml-oauth");
        refreshTokenRequest = new OAuthRefreshTokenRequest(services, "saml-oauth");
    }

    @Override
    public OAuthAccessToken getAccessToken(OAuthGrantType type, String data, int userId, int contextId, String scope) throws OXException {
        if (null == type) {
            throw new OXException(new IllegalArgumentException("Missing grant type"));
        }

        switch (type) {
            case SAML:
                OAuthAccessToken result = accessTokenRequest.requestAccessToken(data, userId, contextId, scope);
                LOG.debug("Successfully handled a SAML assertion for an access token.");
                return result;
            case REFRESH_TOKEN:
                return refreshTokenRequest.requestAccessToken(data, userId, contextId, scope);
        }

        // Should never occur
        throw OXException.general("Unknown grant type: " + type);
    }

    @Override
    public boolean isConfigured(int userId, int contextId) throws OXException {
        return SAMLOAuthConfig.isConfigured(userId, contextId, services.getServiceSafe(ConfigViewFactory.class));
    }

}
