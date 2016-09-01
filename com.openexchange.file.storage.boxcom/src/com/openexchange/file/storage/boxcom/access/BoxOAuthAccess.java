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

package com.openexchange.file.storage.boxcom.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.BoxApi;
import org.scribe.model.Token;
import org.slf4j.Logger;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxUser;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.boxcom.BoxClosure;
import com.openexchange.file.storage.boxcom.BoxConstants;
import com.openexchange.file.storage.boxcom.Services;
import com.openexchange.java.Strings;
import com.openexchange.oauth.AbstractOAuthAccess;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.session.Session;

/**
 * {@link BoxOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BoxOAuthAccess extends AbstractOAuthAccess {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BoxOAuthAccess.class);

    private final FileStorageAccount fsAccount;

    /**
     * Initializes a new {@link BoxOAuthAccess}.
     */
    public BoxOAuthAccess(FileStorageAccount fsAccount, Session session) {
        super(session);
        this.fsAccount = fsAccount;
    }

    @Override
    public void initialize() throws OXException {
        synchronized (this) {
            // Grab Box.com OAuth account
            int oauthAccountId = getAccountId();
            OAuthService oAuthService = Services.getService(OAuthService.class);
            OAuthAccount boxOAuthAccount = oAuthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());
            setOAuthAccount(boxOAuthAccount);

            OAuthAccount newAccount = recreateTokenIfExpired(true);
            if (newAccount != null) {
                setOAuthAccount(newAccount);
            }
            createOAuthClient(newAccount);
        }
    }

    @Override
    public void revoke() throws OXException {
        synchronized (this) {
            // No Java API call
            // More information here: https://docs.box.com/reference#revoke
            try {
                URL url = new URL("https://api.box.com/oauth2/revoke?client_id=" + getOAuthAccount().getMetaData().getId() + "&client_secret" + getOAuthAccount().getMetaData().getAPISecret(session) + "&token=" + getOAuthAccount().getToken());
                BoxAPIRequest request = new BoxAPIRequest((BoxAPIConnection) getOAuthClient().client, url, "GET");
                BoxAPIResponse apiResponse = request.send();

                // The Box SDK already checks for status code 200, so no need to check again
                apiResponse.getResponseCode();

                return;
            } catch (BoxAPIException e) {
                throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
            } catch (IOException e) {
                throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
    }

    @Override
    public boolean ping() throws OXException {
        BoxClosure<Boolean> closure = new BoxClosure<Boolean>() {

            @Override
            protected Boolean doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                try {
                    BoxAPIConnection api = (BoxAPIConnection) boxAccess.getClient().client;
                    BoxUser user = BoxUser.getCurrentUser(api);
                    user.getInfo();
                    //client.getUsersManager().getCurrentUser(new BoxDefaultRequestObject());
                    return Boolean.TRUE;
                } catch (BoxAPIException e) {
                    if (e.getResponseCode() == 401 || e.getResponseCode() == 403) {
                        return Boolean.FALSE;
                    }
                    throw e;
                }
            }
        };
        return closure.perform(null, this, session).booleanValue();
    }

    /**
     * Gets the extended box client
     *
     * @return The extended box client
     * @throws OXException
     * @deprecated No need for extended client anymore.
     */
    public OAuthClient<?> getExtendedClient() throws OXException {
        // TODO: Don't create a new client on every get; cache it
        //OAuthAccount account = getOAuthAccount();
        //ExtendedNonRefreshingBoxClient boxClient = new ExtendedNonRefreshingBoxClient(account.getMetaData().getAPIKey(session), account.getMetaData().getAPISecret(session), new BoxResourceHub(), new BoxJSONParser(new BoxResourceHub()), (new BoxConfigBuilder()).build());
        //applyOAuthToken(boxClient);
        //return new OAuthClient<>(boxClient, getOAuthAccount().getToken());
        throw new UnsupportedOperationException("This method is deprecated. Use the regular API call for the extended methods this client used to provide.");
    }

    @Override
    public int getAccountId() throws OXException {
        try {
            return getAccountId(fsAccount.getConfiguration());
        } catch (IllegalArgumentException e) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(e, BoxConstants.ID, fsAccount.getId());
        }
    }

    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        if (isExpired()) {
            synchronized (this) {
                if (isExpired()) {
                    OAuthAccount newAccount = recreateTokenIfExpired(false);
                    if (newAccount != null) {
                        setOAuthAccount(newAccount);
                        createOAuthClient(newAccount);
                    }
                }
            }
        }
        return this;
    }

    //////////////////////////////////////////// HELPERS ///////////////////////////////////////////////////

    /**
     * Re-creates the OAuth token if it is expired
     *
     * @param considerExpired flag to consider the token as expired
     * @return the {@link OAuthAccount} with the updated token
     * @throws OXException if the token cannot be recreated
     */
    private OAuthAccount recreateTokenIfExpired(boolean considerExpired) throws OXException {
        // Create Scribe Box.com OAuth service
        OAuthAccount boxOAuthAccount = getOAuthAccount();
        final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(BoxApi.class);
        serviceBuilder.apiKey(boxOAuthAccount.getMetaData().getAPIKey(session)).apiSecret(boxOAuthAccount.getMetaData().getAPISecret(session));
        BoxApi.BoxApiService scribeOAuthService = (BoxApi.BoxApiService) serviceBuilder.build();

        // Check expiration
        if (considerExpired || scribeOAuthService.isExpired(boxOAuthAccount.getToken())) {
            // Expired...
            String refreshToken = boxOAuthAccount.getSecret();
            Token accessToken;
            try {
                accessToken = scribeOAuthService.getAccessToken(new Token(boxOAuthAccount.getToken(), boxOAuthAccount.getSecret()), null);
            } catch (org.scribe.exceptions.OAuthException e) {
                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(e, boxOAuthAccount.getDisplayName(), boxOAuthAccount.getId());
            }
            if (Strings.isEmpty(accessToken.getSecret())) {
                LOG.warn("Received invalid request_token from Box.com: {}. Response:{}{}", null == accessToken.getSecret() ? "null" : accessToken.getSecret(), Strings.getLineSeparator(), accessToken.getRawResponse());
            } else {
                refreshToken = accessToken.getSecret();
            }
            // Update account
            OAuthService oAuthService = Services.getService(OAuthService.class);
            int accountId = boxOAuthAccount.getId();
            Map<String, Object> arguments = new HashMap<>(3);
            arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, new DefaultOAuthToken(accessToken.getToken(), refreshToken));
            arguments.put(OAuthConstants.ARGUMENT_SESSION, session);
            oAuthService.updateAccount(accountId, arguments, session.getUserId(), session.getContextId());

            // Reload
            return oAuthService.getAccount(accountId, session, session.getUserId(), session.getContextId());
        }
        return null;
    }

    /**
     * Creates the {@link OAuthClient}
     * 
     * @param account
     * @throws OXException
     */
    private void createOAuthClient(OAuthAccount account) throws OXException {
        OAuthServiceMetaData boxMetaData = account.getMetaData();
        BoxAPIConnection boxAPI = new BoxAPIConnection(boxMetaData.getAPIKey(session), boxMetaData.getAPISecret(session), getOAuthAccount().getToken(), getOAuthAccount().getSecret());
        OAuthClient<BoxAPIConnection> oAuthClient = new OAuthClient<>(boxAPI, getOAuthAccount().getToken());
        setOAuthClient(oAuthClient);
    }
}
