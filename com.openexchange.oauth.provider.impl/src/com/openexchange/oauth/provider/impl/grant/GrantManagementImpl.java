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

package com.openexchange.oauth.provider.impl.grant;

import static com.openexchange.oauth.provider.authorizationserver.client.ClientManagement.MAX_CLIENTS_PER_USER;
import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException;
import com.openexchange.oauth.provider.authorizationserver.grant.DefaultGrantView;
import com.openexchange.oauth.provider.authorizationserver.grant.Grant;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantView;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.impl.ScopeProviderRegistry;
import com.openexchange.oauth.provider.impl.authcode.AbstractAuthorizationCodeProvider;
import com.openexchange.oauth.provider.impl.authcode.AuthCodeInfo;
import com.openexchange.oauth.provider.impl.tools.UserizedToken;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.oauth.provider.tools.URIValidator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link GrantManagementImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GrantManagementImpl implements GrantManagement {

    private final AbstractAuthorizationCodeProvider authCodeProvider;

    private final ClientManagement clientManagement;

    private final OAuthGrantStorage grantStorage;

    private final ServiceLookup services;

    public GrantManagementImpl(AbstractAuthorizationCodeProvider authCodeProvider, ClientManagement clientManagement, OAuthGrantStorage grantStorage, ServiceLookup services) {
        super();
        this.authCodeProvider = authCodeProvider;
        this.clientManagement = clientManagement;
        this.grantStorage = grantStorage;
        this.services = services;
    }

    @Override
    public String generateAuthorizationCodeFor(String clientId, String redirectURI, Scope scope, Session session) throws OXException {
        int contextId = session.getContextId();
        int userId = session.getUserId();
        // Check if user is allowed to create more grants
        int distinctGrants = grantStorage.countDistinctGrants(contextId, userId);
        if (distinctGrants >= MAX_CLIENTS_PER_USER) {
            throw OAuthProviderExceptionCodes.GRANTS_EXCEEDED.create(userId, contextId, MAX_CLIENTS_PER_USER);
        }

        // Adjust scope based on users permissions
        CapabilityService capabilityService = requireService(CapabilityService.class, services);
        CapabilitySet capabilities = capabilityService.getCapabilities(session);

        List<String> grantedTokens = new LinkedList<>();
        for (String token : scope.get()) {
            OAuthScopeProvider provider = ScopeProviderRegistry.getInstance().getProvider(token);
            if (provider != null && provider.canBeGranted(capabilities)) {
                grantedTokens.add(token);
            }
        }

        return authCodeProvider.generateAuthorizationCodeFor(clientId, redirectURI, Scope.newInstance(grantedTokens), userId, contextId);
    }

    @Override
    public Grant redeemAuthCode(Client client, String redirectURI, String authCode) throws OXException {
        AuthCodeInfo authCodeInfo = authCodeProvider.remove(authCode);
        if (authCodeInfo == null || !isValidAuthCode(authCodeInfo, client.getId(), redirectURI)) {
            return null;
        }

        int contextId = authCodeInfo.getContextId();
        int userId = authCodeInfo.getUserId();
        UserizedToken accessToken = UserizedToken.generate(userId, contextId);
        UserizedToken refreshToken = UserizedToken.generate(userId, contextId);
        Date expirationDate = new Date(System.currentTimeMillis() + DEFAULT_EXPIRATION);

        StoredGrant storedGrant = new StoredGrant();
        storedGrant.setContextId(contextId);
        storedGrant.setUserId(userId);
        storedGrant.setClientId(client.getId());
        storedGrant.setAccessToken(accessToken);
        storedGrant.setRefreshToken(refreshToken);
        storedGrant.setExpirationDate(expirationDate);
        storedGrant.setScope(authCodeInfo.getScope());
        grantStorage.saveGrant(storedGrant);
        return new OAuthGrantImpl(storedGrant);
    }

    @Override
    public Grant getGrantByAccessToken(String accessTokenString) throws OXException {
        if (!UserizedToken.isValid(accessTokenString)) {
            return null;
        }

        UserizedToken accessToken = UserizedToken.parse(accessTokenString);
        StoredGrant storedGrant = grantStorage.getGrantByAccessToken(accessToken);
        return null == storedGrant ? null : new OAuthGrantImpl(storedGrant);
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

        long now = System.currentTimeMillis();
        return (now - authCodeInfo.getTimestamp()) <= AUTH_CODE_TIMEOUT_MILLIS;
    }

    @Override
    public Grant redeemRefreshToken(Client client, String refreshTokenString) throws OXException {
        if (!UserizedToken.isValid(refreshTokenString)) {
            return null;
        }

        UserizedToken refreshToken = UserizedToken.parse(refreshTokenString);
        StoredGrant storedGrant = grantStorage.getGrantByRefreshToken(refreshToken);
        if (storedGrant == null) {
            return null;
        }

        if (!client.getId().equals(storedGrant.getClientId())) {
            return null;
        }

        int contextId = storedGrant.getContextId();
        int userId = storedGrant.getUserId();
        storedGrant.setAccessToken(UserizedToken.generate(userId, contextId));
        storedGrant.setRefreshToken(UserizedToken.generate(userId, contextId));
        storedGrant.setExpirationDate(new Date(System.currentTimeMillis() + DEFAULT_EXPIRATION));
        grantStorage.updateGrant(refreshToken, storedGrant);

        return new OAuthGrantImpl(storedGrant);
    }

    @Override
    public boolean revokeByRefreshToken(String refreshTokenString) throws OXException {
        if (!UserizedToken.isValid(refreshTokenString)) {
            return false;
        }

        UserizedToken refreshToken = UserizedToken.parse(refreshTokenString);
        return grantStorage.deleteGrantByRefreshToken(refreshToken);
    }

    @Override
    public boolean revokeByAccessToken(String accessTokenString) throws OXException {
        if (!UserizedToken.isValid(accessTokenString)) {
            return false;
        }

        UserizedToken accessToken = UserizedToken.parse(accessTokenString);
        return grantStorage.deleteGrantByAccessToken(accessToken);
    }

    @Override
    public Iterator<GrantView> getGrants(int contextId, int userId) throws OXException {
        List<StoredGrant> grants = grantStorage.getGrantsForUser(contextId, userId);
        Map<String, List<StoredGrant>> grantsByClient = new HashMap<>();
        for (StoredGrant grant : grants) {
            List<StoredGrant> clientGrants = grantsByClient.get(grant.getClientId());
            if (clientGrants == null) {
                clientGrants = new LinkedList<>();
                grantsByClient.put(grant.getClientId(), clientGrants);
            }
            clientGrants.add(grant);
        }

        List<GrantView> grantViews = new ArrayList<GrantView>(grantsByClient.size());
        for (Entry<String, List<StoredGrant>> entry : grantsByClient.entrySet()) {
            String clientId = entry.getKey();
            Client client;
            try {
                client = clientManagement.getClientById(clientId);
                if (client == null) {
                    // The according client has been removed in the meantime. We'll ignore this grant...
                    continue;
                }
            } catch (ClientManagementException e) {
                if (e.getReason() == com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException.Reason.INVALID_CLIENT_ID) {
                    // The according client has been removed in the meantime. We'll ignore this grant...
                    continue;
                }

                throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }

            List<String> scopeTokens = new LinkedList<>();
            Date latestGrantDate = new Date(0);
            for (StoredGrant grant : entry.getValue()) {
                scopeTokens.addAll(grant.getScope().get());
                Date creationDate = grant.getCreationDate();
                if (creationDate.after(latestGrantDate)) {
                    latestGrantDate = creationDate;
                }
            }

            DefaultGrantView view = new DefaultGrantView();
            view.setClient(client);
            view.setScope(Scope.newInstance(scopeTokens));
            view.setLatestGrantDate(latestGrantDate);
            grantViews.add(view);
        }

        return Collections.unmodifiableList(grantViews).iterator();
    }

    @Override
    public void revokeGrants(String clientId, int contextId, int userId) throws OXException {
        grantStorage.deleteGrantsByClientAndUser(clientId, contextId, userId);
    }

    @Override
    public OAuthScopeProvider getScopeProvider(String token) {
        return ScopeProviderRegistry.getInstance().getProvider(token);
    }
}
