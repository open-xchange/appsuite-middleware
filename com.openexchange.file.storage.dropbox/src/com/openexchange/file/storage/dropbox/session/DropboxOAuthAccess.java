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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.dropbox.session;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.dropbox.DropboxConfiguration;
import com.openexchange.file.storage.dropbox.DropboxExceptionCodes;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.session.Session;

/**
 * {@link DropboxOAuthAccess} - Initializes and provides Dropbox OAuth access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxOAuthAccess {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DropboxOAuthAccess.class);

    /**
     * Gets the Dropbox OAuth access for given Dropbox account.
     *
     * @param fsAccount The Dropbox account providing credentials and settings
     * @param session The user session
     * @return The Dropbox OAuth access; either newly created or fetched from underlying registry
     * @throws OXException If a Dropbox session could not be created
     */
    public static DropboxOAuthAccess accessFor(final FileStorageAccount fsAccount, final Session session) throws OXException {
        final DropboxOAuthAccessRegistry registry = DropboxOAuthAccessRegistry.getInstance();
        final String accountId = fsAccount.getId();
        DropboxOAuthAccess dropboxOAuthAccess = registry.getSession(session.getContextId(), session.getUserId(), accountId);
        if (null == dropboxOAuthAccess) {
            final DropboxOAuthAccess newInstance = new DropboxOAuthAccess(fsAccount, session, session.getUserId(), session.getContextId());
            dropboxOAuthAccess = registry.addSession(session.getContextId(), session.getUserId(), accountId, newInstance);
            if (null == dropboxOAuthAccess) {
                dropboxOAuthAccess = newInstance;
            }
        }
        return dropboxOAuthAccess;
    }

    /**
     * The Dropbox OAuth service.
     */
    private final org.scribe.oauth.OAuthService dropboxOAuthService;

    /**
     * The OAuth account.
     */
    private final OAuthAccount oauthAccount;

    /**
     * The Dropbox user identifier.
     */
    private final long dropboxUserId;

    /**
     * The Dropbox user's full name
     */
    private final String dropboxUserName;

    /**
     * The OAuth access token for Dropbox.
     */
    private final Token dropboxAccessToken;

    /**
     * The Web-authenticating session.
     */
    private WebAuthSession webAuthSession;

    /**
     * The Dropbox API reference.
     */
    private DropboxAPI<WebAuthSession> mDBApi;

    /**
     * Initializes a new {@link FacebookMessagingResource}.
     *
     * @param fsAccount The Dropbox account providing credentials and settings
     * @throws OXException
     */
    private DropboxOAuthAccess(final FileStorageAccount fsAccount, final Session session, final int user, final int contextId) throws OXException {
        super();
        /*
         * Get OAuth account identifier from messaging account's configuration
         */
        final int oauthAccountId;
        {
            final Map<String, Object> configuration = fsAccount.getConfiguration();
            if (null == configuration) {
                throw DropboxExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            final Object accountId = configuration.get("account");
            if (null == accountId) {
                throw DropboxExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            oauthAccountId = ((Integer) accountId).intValue();
        }
        final OAuthService oAuthService = DropboxServices.getService(OAuthService.class);
        try {
            oauthAccount = oAuthService.getAccount(oauthAccountId, session, user, contextId);
            dropboxAccessToken = new Token(checkToken(oauthAccount.getToken()), oauthAccount.getSecret());
            /*
             * Generate FB service
             */
            {
                final String apiKey = DropboxConfiguration.getInstance().getApiKey();
                final String secretKey = DropboxConfiguration.getInstance().getSecretKey();
                dropboxOAuthService = new ServiceBuilder().provider(FacebookApi.class).apiKey(apiKey).apiSecret(secretKey).build();
            }
            /*-
             * Retrieve information about the user's Dropbox account.
             * 
             * See: https://www.dropbox.com/developers/reference/api#account-info
             */
            final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.dropbox.com/1/account/info");
            dropboxOAuthService.signRequest(dropboxAccessToken, request);
            final Response response = request.send();
            final JSONObject object = (JSONObject) new JSONParser().parse(response.getBody());
            checkForErrors(object);
            dropboxUserId = ((Long) object.get("uid")).longValue();
            dropboxUserName = (String) object.get("display_name");
            // Initialize Dropbox access
            final AppKeyPair appKeys = new AppKeyPair(DropboxConfiguration.getInstance().getApiKey(), DropboxConfiguration.getInstance().getSecretKey());
            webAuthSession = new WebAuthSession(appKeys, AccessType.APP_FOLDER);
            mDBApi = new DropboxAPI<WebAuthSession>(webAuthSession);
            // re-auth specific stuff
            final AccessTokenPair reAuthTokens = new AccessTokenPair(dropboxAccessToken.getToken(), dropboxAccessToken.getSecret());
            mDBApi.getSession().setAccessTokenPair(reAuthTokens);
            // http://aaka.sh/patel/2011/12/20/authenticating-dropbox-java-api/
        } catch (final OXException e) {
            throw new OXException(e);
        } catch (final org.scribe.exceptions.OAuthException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ParseException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final Pattern P_EXPIRES = Pattern.compile("&expires(=[0-9]+)?$");

    private static String checkToken(final String accessToken) {
        if (accessToken.indexOf("&expires") < 0) {
            return accessToken;
        }
        final Matcher m = P_EXPIRES.matcher(accessToken);
        final StringBuffer sb = new StringBuffer(accessToken.length());
        if (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private void checkForErrors(final JSONObject object) throws OXException {
        if (object.containsKey("error")) {
            final JSONObject error = (JSONObject) object.get("error");
            if ("OAuthException".equals(error.get("type"))) {
                final OXException e = new OXException(OAuthExceptionCodes.TOKEN_EXPIRED.create(oauthAccount.getDisplayName()));
                LOG.error(e.getErrorCode() + " exceptionId=" + e.getExceptionId() + " JSON error object:\n" + error.toString());
                throw e;
            } else {
                throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(object.get("message"));
            }
        }
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("{ oauthAccount=").append(oauthAccount.getDisplayName()).append(", dropboxUserId=").append(
            dropboxUserId).append(", dropboxAccessToken=").append(dropboxAccessToken).append('}').toString();
    }
    
    /**
     * Gets the DropboxAPI reference
     *
     * @return The DropboxAPI reference
     */
    public DropboxAPI<WebAuthSession> getDropboxAPI() {
        return mDBApi;
    }

    /**
     * Disposes this Dropbox OAuth access.
     */
    public void dispose() {
        // So far nothing known to me that needs to be disposed
    }

    /**
     * Gets associated OAuth account.
     *
     * @return The OAuth account
     */
    public OAuthAccount getOauthAccount() {
        return oauthAccount;
    }

    /**
     * Gets the Dropbox user identifier.
     *
     * @return The Dropbox user identifier
     */
    public long getDropboxUserId() {
        return dropboxUserId;
    }

    /**
     * Gets the Dropbox user's display name.
     *
     * @return The Dropbox user's display name.
     */
    public String getDropboxUserName() {
        return dropboxUserName;
    }

    /**
     * Gets the Dropbox OAuth service needed to sign requests.
     *
     * @return The Dropbox OAuth service
     * @see org.scribe.oauth.OAuthService#signRequest(Token, OAuthRequest)
     */
    public org.scribe.oauth.OAuthService getDropboxOAuthService() {
        return dropboxOAuthService;
    }

    /**
     * Gets the Dropbox access token needed to sign requests.
     *
     * @return The Dropbox access token
     * @see org.scribe.oauth.OAuthService#signRequest(Token, OAuthRequest)
     */
    public Token getDropboxAccessToken() {
        return dropboxAccessToken;
    }

    /**
     * Executes GET request for specified URL.
     *
     * @param url The URL
     * @return The response
     * @throws OXException If request fails
     */
    public String executeGETRequest(final String url) throws OXException {
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, url);
            dropboxOAuthService.signRequest(dropboxAccessToken, request);
            return request.send().getBody();
        } catch (final org.scribe.exceptions.OAuthException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
