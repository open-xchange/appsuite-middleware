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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal;

import static com.openexchange.osgi.Tools.requireService;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.ClientData;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.OAuthGrant;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.OAuthScopeProvider;
import com.openexchange.oauth.provider.internal.authcode.AbstractAuthorizationCodeProvider;
import com.openexchange.oauth.provider.internal.authcode.AuthCodeInfo;
import com.openexchange.oauth.provider.internal.client.OAuthClientStorage;
import com.openexchange.oauth.provider.internal.grant.OAuthGrantStorage;
import com.openexchange.oauth.provider.internal.grant.OAuthGrantImpl;
import com.openexchange.oauth.provider.tools.UserizedToken;
import com.openexchange.server.ServiceLookup;

/**
 * {@link OAuthProviderServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthProviderServiceImpl implements OAuthProviderService {

    private final AbstractAuthorizationCodeProvider authCodeProvider;

    private final OAuthClientStorage clientStorage;

    private final OAuthGrantStorage grantStorage;

    private final ServiceLookup services;


    public OAuthProviderServiceImpl(AbstractAuthorizationCodeProvider authCodeProvider, OAuthClientStorage clientStorage, OAuthGrantStorage grantStorage, ServiceLookup services) {
        super();
        this.authCodeProvider = authCodeProvider;
        this.clientStorage = clientStorage;
        this.grantStorage = grantStorage;
        this.services = services;
    }

    @Override
    public Client getClientById(String clientId) throws OXException {
        return clientStorage.getClientById(clientId);
    }

    @Override
    public Client registerClient(ClientData clientData) throws OXException {
        return clientStorage.registerClient(clientData);
    }

    @Override
    public boolean unregisterClient(String clientId) throws OXException {
        grantStorage.deleteGrantsForClient(clientId);
        return clientStorage.unregisterClient(clientId);
    }

    @Override
    public Client revokeClientSecret(String clientId) throws OXException {
        grantStorage.deleteGrantsForClient(clientId);
        return clientStorage.revokeClientSecret(clientId);
    }

    @Override
    public Client updateClient(String clientId, ClientData clientData) throws OXException {
        return clientStorage.updateClient(clientId, clientData);
    }

    @Override
    public void enableClient(String clientId) throws OXException {
        clientStorage.enableClient(clientId);
    }

    @Override
    public void disableClient(String clientId) throws OXException {
        grantStorage.deleteGrantsForClient(clientId); // TODO: really?
        clientStorage.disableClient(clientId);
    }

    @Override
    public String generateAuthorizationCodeFor(String clientId, String redirectURI, String scopeString, int userId, int contextId) throws OXException {
        // Adjust scope based on users permissions
        CapabilityService capabilityService = requireService(CapabilityService.class, services);
        CapabilitySet capabilities = capabilityService.getCapabilities(userId, contextId);
        Set<String> finalScopes = new HashSet<>();

        DefaultScopes scopes = DefaultScopes.parseScope(scopeString);
        for (String scope : scopes.getScopes()) {
            OAuthScopeProvider provider = ScopeRegistry.getInstance().getProvider(scope);
            if (provider != null && provider.canBeGranted(capabilities, scopes.isReadOnly(scope))) {
                finalScopes.add(scopes.getQualifiedScope(scope));
            }
        }

        return authCodeProvider.generateAuthorizationCodeFor(clientId, redirectURI, new DefaultScopes(finalScopes.toArray(new String[0])), userId, contextId);
    }

    @Override
    public OAuthGrant redeemAuthCode(Client client, String redirectURI, String authCode) throws OXException {
        AuthCodeInfo authCodeInfo = authCodeProvider.redeemAuthCode(client, authCode);
        if (authCodeInfo == null || !isValidAuthCode(authCodeInfo, client.getId(), redirectURI)) {
            return null;
        }

        int contextId = authCodeInfo.getContextId();
        int userId = authCodeInfo.getUserId();
        String accessToken = new UserizedToken(userId, contextId).getToken();
        String refreshToken = new UserizedToken(userId, contextId).getToken();
        Date expirationDate = new Date(System.currentTimeMillis() + OAuthProviderConstants.DEFAULT_EXPIRATION);
        OAuthGrant grant = new OAuthGrantImpl(authCodeInfo, accessToken, refreshToken, expirationDate);
        grantStorage.persistGrant(grant);
        return grant;
    }

    /**
     * Checks validity of passed value in comparison to given time stamp (and session).
     *
     * @param value The value to check
     * @param clientId The client identifier
     * @param redirectURI The redirect URI
     * @return <code>true</code> if valid; otherwise <code>false</code>
     */
    private boolean isValidAuthCode(AuthCodeInfo authCodeInfo, String clientId, String redirectURI) {
        if (!clientId.equals(authCodeInfo.getClientId())) {
            return false;
        }

        if (!URIValidator.urisEqual(authCodeInfo.getRedirectURI(), redirectURI)) {
            return false;
        }

        long now = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(now - authCodeInfo.getNanos()) <= OAuthProviderService.AUTH_CODE_TIMEOUT_MILLIS;
    }



    @Override
    public OAuthGrant redeemRefreshToken(Client client, String refreshToken) throws OXException {
        OAuthGrant grant = grantStorage.getGrantByRefreshToken(refreshToken);
        if (grant == null) {
            return null;
        }

        if (!client.getId().equals(grant.getClientId())) {
            return null;
        }

        OAuthGrantImpl newGrant = new OAuthGrantImpl(grant);
        newGrant.setAccessToken(new UserizedToken(grant.getUserId(), grant.getContextId()).getToken());
        newGrant.setRefreshToken(new UserizedToken(grant.getUserId(), grant.getContextId()).getToken());
        newGrant.setExpirationDate(new Date(System.currentTimeMillis() + OAuthProviderConstants.DEFAULT_EXPIRATION));
        grantStorage.persistGrant(newGrant);
        return newGrant;
    }



    @Override
    public boolean isValidScopeString(String scopeString) {
        if (DefaultScopes.isValidScopeString(scopeString)) {
            DefaultScopes scopes = DefaultScopes.parseScope(scopeString);
            if (scopes.size() == 0) {
                return false;
            }

            for (String scope : scopes.getScopes()) {
                if (!ScopeRegistry.getInstance().hasScopeProvider(scope)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

}
