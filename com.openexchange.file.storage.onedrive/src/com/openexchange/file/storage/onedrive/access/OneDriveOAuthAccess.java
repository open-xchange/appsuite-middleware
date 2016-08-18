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

package com.openexchange.file.storage.onedrive.access;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.MsLiveConnectApi;
import org.scribe.model.Token;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.onedrive.AbstractOneDriveResourceAccess;
import com.openexchange.file.storage.onedrive.OneDriveClosure;
import com.openexchange.file.storage.onedrive.OneDriveConstants;
import com.openexchange.file.storage.onedrive.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.oauth.AbstractOAuthAccess;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.session.Session;

/**
 * {@link OneDriveOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OneDriveOAuthAccess extends AbstractOAuthAccess {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OneDriveOAuthAccess.class);

    private FileStorageAccount fsAccount;
    private Session session;

    /**
     * Initialises a new {@link OneDriveOAuthAccess}.
     */
    public OneDriveOAuthAccess(FileStorageAccount fsAccount, Session session) throws OXException {
        super();
        this.fsAccount = fsAccount;
        this.session = session;

        int oauthAccountId = getAccountId();
        // Grab Live Connect OAuth account
        OAuthAccount liveconnectOAuthAccount;
        {
            OAuthService oAuthService = Services.getService(OAuthService.class);
            liveconnectOAuthAccount = oAuthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());
        }

        // Assign Live Connect account
        setOAuthAccount(liveconnectOAuthAccount);

        // Initialise client
        setOAuthClient(new OAuthClient<>(HttpClients.getHttpClient("Open-Xchange OneDrive Client"), getOAuthAccount().getToken()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#initialise()
     */
    @Override
    public void initialise() throws OXException {
        synchronized (this) {
            OAuthAccount newAccount = recreateTokenIfExpired(true, getOAuthAccount(), session);
            if (newAccount != null) {
                setOAuthAccount(newAccount);
                setOAuthClient(new OAuthClient<>(HttpClients.getHttpClient("Open-Xchange OneDrive Client"), getOAuthAccount().getToken()));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#revoke()
     */
    @Override
    public void revoke() throws OXException {
        // No revoke call
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ping()
     */
    @Override
    public boolean ping() throws OXException {
        OneDriveClosure<Boolean> closure = new OneDriveClosure<Boolean>() {

            @Override
            protected Boolean doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                HttpGet request = null;
                try {
                    List<NameValuePair> qparams = new LinkedList<NameValuePair>();
                    qparams.add(new BasicNameValuePair("access_token", getOAuthAccount().getToken()));
                    request = new HttpGet(AbstractOneDriveResourceAccess.buildUri("/me/skydrive", qparams));
                    HttpResponse httpResponse = httpClient.execute(request);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (401 == statusCode || 403 == statusCode) {
                        return Boolean.FALSE;
                    }

                    AbstractOneDriveResourceAccess.handleHttpResponse(httpResponse, JSONObject.class);
                    return Boolean.TRUE;
                } finally {
                    AbstractOneDriveResourceAccess.reset(request);
                }
            }
        };
        return closure.perform(null, (DefaultHttpClient) getClient().client, session).booleanValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#getAccountId()
     */
    @Override
    public int getAccountId() throws OXException {
        try {
            return getAccountId(fsAccount.getConfiguration());
        } catch (IllegalArgumentException e) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(OneDriveConstants.ID, fsAccount.getId());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ensureNotExpired()
     */
    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        if (isExpired()) {
            synchronized (this) {
                if (isExpired()) {
                    initialise();
                }
            }
        }
        return this;
    }

    /**
     * Re-creates the token if expired
     * 
     * @param considerExpired
     * @param liveconnectOAuthAccount
     * @param session
     * @return
     * @throws OXException
     */
    private OAuthAccount recreateTokenIfExpired(boolean considerExpired, OAuthAccount liveconnectOAuthAccount, Session session) throws OXException {
        // Create Scribe Microsoft OneDrive OAuth service
        final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(MsLiveConnectApi.class);
        serviceBuilder.apiKey(liveconnectOAuthAccount.getMetaData().getAPIKey(session)).apiSecret(liveconnectOAuthAccount.getMetaData().getAPISecret(session));
        MsLiveConnectApi.MsLiveConnectService scribeOAuthService = (MsLiveConnectApi.MsLiveConnectService) serviceBuilder.build();

        // Check expiration
        if (considerExpired || scribeOAuthService.isExpired(liveconnectOAuthAccount.getToken())) {
            // Expired...
            String refreshToken = liveconnectOAuthAccount.getSecret();
            Token accessToken;
            try {
                accessToken = scribeOAuthService.getAccessToken(new Token(liveconnectOAuthAccount.getToken(), liveconnectOAuthAccount.getSecret()), null);
            } catch (org.scribe.exceptions.OAuthException e) {
                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(e, liveconnectOAuthAccount.getDisplayName(), liveconnectOAuthAccount.getId());
            }
            if (Strings.isEmpty(accessToken.getSecret())) {
                LOGGER.warn("Received invalid request_token from Live Connect: {}. Response:{}{}", null == accessToken.getSecret() ? "null" : accessToken.getSecret(), Strings.getLineSeparator(), accessToken.getRawResponse());
            } else {
                refreshToken = accessToken.getSecret();
            }
            // Update account
            OAuthService oAuthService = Services.getService(OAuthService.class);
            int accountId = liveconnectOAuthAccount.getId();
            Map<String, Object> arguments = new HashMap<String, Object>(3);
            arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, new DefaultOAuthToken(accessToken.getToken(), refreshToken));
            arguments.put(OAuthConstants.ARGUMENT_SESSION, session);
            oAuthService.updateAccount(accountId, arguments, session.getUserId(), session.getContextId(), liveconnectOAuthAccount.getEnabledScopes());

            // Reload
            return oAuthService.getAccount(accountId, session, session.getUserId(), session.getContextId());
        }
        return null;
    }
}
