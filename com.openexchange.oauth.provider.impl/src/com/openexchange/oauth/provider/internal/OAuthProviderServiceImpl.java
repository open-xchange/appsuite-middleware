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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.ClientData;
import com.openexchange.oauth.provider.DefaultClient;
import com.openexchange.oauth.provider.OAuthGrant;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.Scope;
import com.openexchange.oauth.provider.internal.authcode.AbstractAuthorizationCodeProvider;
import com.openexchange.oauth.provider.internal.authcode.AuthCodeInfo;
import com.openexchange.oauth.provider.tools.UserizedToken;
import com.openexchange.server.ServiceLookup;

/**
 * {@link OAuthProviderServiceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthProviderServiceImpl implements OAuthProviderService {

    private final ServiceLookup services;

    private final AbstractAuthorizationCodeProvider authCodeProvider;

    private final Map<String, DefaultClient> clients;

    private final List<OAuthGrantImpl> grants;


    public OAuthProviderServiceImpl(ServiceLookup services, AbstractAuthorizationCodeProvider authCodeProvider) {
        super();
        this.services = services;
        this.authCodeProvider = authCodeProvider;
        clients = new HashMap<>();
        grants = new LinkedList<>();

        DefaultClient client = new DefaultClient();
        client.setId("983e78b3e76d423988ed09c345364f05");
        client.setSecret("a1dd1c62735f4e61b80b6aa2b29df37d");
        client.setName("Example App");
        client.setDescription("An app that provides funny example stuff");
        client.addRedirectURI("http://localhost:8080");
        clients.put(client.getId(), client);
    }

    @Override
    public Client getClientById(String clientId) throws OXException {
        return clients.get(clientId);
    }

    @Override
    public Client registerClient(ClientData clientData) throws OXException {
        DefaultClient client = new DefaultClient();
        client.setId(UUIDs.getUnformattedStringFromRandom());
        client.setSecret(UUIDs.getUnformattedStringFromRandom());
        client.setName(clientData.getName());
        client.setDescription(clientData.getDescription());
        for (String uri : clientData.getRedirectURIs()) {
            client.addRedirectURI(uri);
        }

        clients.put(client.getId(), client);
        return client;
    }

    @Override
    public boolean unregisterClient(String clientId) throws OXException {
        DefaultClient client = clients.remove(clientId);
        if (client == null) {
            return false;
        }

        deleteGrantsForClient(clientId);
        return true;
    }

    @Override
    public Client revokeClientSecret(String clientId) throws OXException {
        DefaultClient client = clients.get(clientId);
        if (client == null) {
            return null;
        }

        client.setSecret(UUIDs.getUnformattedStringFromRandom());
        deleteGrantsForClient(clientId);
        return client;
    }

    @Override
    public String generateAuthorizationCodeFor(String clientId, Scope scope, int userId, int contextId) throws OXException {
        return authCodeProvider.generateAuthorizationCodeFor(clientId, scope, userId, contextId);
    }

    @Override
    public OAuthGrant redeemAuthCode(Client client, String authCode) throws OXException {
        AuthCodeInfo authCodeInfo = authCodeProvider.redeemAuthCode(client, authCode);
        if (authCodeInfo == null) {
            return null;
        }

        int contextId = authCodeInfo.getContextId();
        int userId = authCodeInfo.getUserId();
        String accessToken = new UserizedToken(userId, contextId).getToken();
        String refreshToken = new UserizedToken(userId, contextId).getToken();
        Date expirationDate = new Date(System.currentTimeMillis() + OAuthProviderConstants.DEFAULT_EXPIRATION);
        return persistGrant(authCodeInfo, accessToken, refreshToken, expirationDate);
    }

    private OAuthGrant persistGrant(AuthCodeInfo authCodeInfo, String accessToken, String refreshToken, Date expirationDate) throws OXException {
        OAuthGrantImpl grant = new OAuthGrantImpl(authCodeInfo, accessToken, refreshToken, expirationDate);
        grants.add(grant);
        return grant;
    }

    private void deleteGrantsForClient(String clientId) {
        Iterator<OAuthGrantImpl> it = grants.iterator();
        while (it.hasNext()) {
            OAuthGrantImpl grant = it.next();
            if (clientId.equals(grant.getAuthCodeInfo().getClientId())) {
                it.remove();
            }
        }
    }

    @Override
    public OAuthGrant redeemRefreshToken(Client client, String refreshToken) {
        Iterator<OAuthGrantImpl> it = grants.iterator();
        while (it.hasNext()) {
            OAuthGrantImpl grant = it.next();
            if (client.getId().equals(grant.getAuthCodeInfo().getClientId()) && grant.getRefreshToken().equals(refreshToken)) {
                grant.setAccessToken(new UserizedToken(grant.getUserId(), grant.getContextId()).getToken());
                grant.setRefreshToken(new UserizedToken(grant.getUserId(), grant.getContextId()).getToken());
                grant.setExpirationDate(new Date(System.currentTimeMillis() + OAuthProviderConstants.DEFAULT_EXPIRATION));
                return grant;
            }
        }

        return null;
    }

    public OAuthGrantImpl getGrantByAccessToken(String accessToken) {
        Iterator<OAuthGrantImpl> it = grants.iterator();
        while (it.hasNext()) {
            OAuthGrantImpl grant = it.next();
            if (grant.getAccessToken().equals(accessToken)) {
                return grant;
            }
        }

        return null;
    }

    @Override
    public Client updateClient(String clientId, ClientData clientData) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isValidScope(Scope scope) {
        // FIXME
        return true;
    }

}
