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

package com.openexchange.file.storage.dropbox.access;

import java.util.Map;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.dropbox.DropboxConfiguration;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.file.storage.dropbox.Utils;
import com.openexchange.file.storage.dropbox.auth.TrustAllWebAuthSession;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.session.Session;

/**
 * {@link DropboxOAuthAccess} - Initializes and provides Dropbox OAuth access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxOAuthAccess {

    /**
     * Drops the Dropbox OAuth access for given Dropbox account.
     *
     * @param fsAccount The Dropbox account providing credentials and settings
     * @param session The user session
     */
    public static void dropFor(final FileStorageAccount fsAccount, final Session session) {
        DropboxOAuthAccessRegistry registry = DropboxOAuthAccessRegistry.getInstance();
        String accountId = fsAccount.getId();
        registry.purgeUserAccess(session.getContextId(), session.getUserId(), accountId);
    }

    /**
     * Gets the Dropbox OAuth access for given Dropbox account.
     *
     * @param fsAccount The Dropbox account providing credentials and settings
     * @param session The user session
     * @return The Dropbox OAuth access; either newly created or fetched from underlying registry
     * @throws OXException If a Dropbox session could not be created
     */
    public static DropboxOAuthAccess accessFor(final FileStorageAccount fsAccount, final Session session) throws OXException {
        DropboxOAuthAccessRegistry registry = DropboxOAuthAccessRegistry.getInstance();
        String accountId = fsAccount.getId();
        DropboxOAuthAccess dropboxOAuthAccess = registry.getAccess(session.getContextId(), session.getUserId(), accountId);
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
     * Pings the Dropbox account.
     *
     * @param fsAccount The Dropbox account providing credentials and settings
     * @param session The user session
     * @return <code>true</code> for successful ping attempt; otherwise <code>false</code>
     * @throws OXException If a Dropbox account could not be pinged
     */
    public static boolean pingFor(final FileStorageAccount fsAccount, final Session session) throws OXException {
        DropboxOAuthAccess access = accessFor(fsAccount, session);
        try {
            access.dropboxApi.accountInfo();
            return true;
        } catch (DropboxException e) {
            if (DropboxServerException.class.isInstance(e)) {
                DropboxServerException serverException = (DropboxServerException) e;
                int error = serverException.error;
                if (DropboxServerException._401_UNAUTHORIZED == error || DropboxServerException._403_FORBIDDEN == error) {
                    return false;
                }
            }
            throw Utils.handle(e, null);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /**
     * The Web-authenticating session.
     */
    private WebAuthSession webAuthSession;

    /**
     * The Dropbox API reference.
     */
    private DropboxAPI<WebAuthSession> dropboxApi;

    /**
     * The Dropbox v2 API reference.
     */
    private DbxClientV2 client;

    /**
     * Initialises a new {@link DropboxOAuthAccess}.
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
                throw FileStorageExceptionCodes.MISSING_CONFIG.create(DropboxConstants.ID, fsAccount.getId());
            }
            final Object accountId = configuration.get("account");
            if (null == accountId) {
                throw FileStorageExceptionCodes.MISSING_CONFIG.create(DropboxConstants.ID, fsAccount.getId());
            }
            if (accountId instanceof Integer) {
                oauthAccountId = ((Integer) accountId).intValue();
            } else {
                try {
                    oauthAccountId = Integer.parseInt(accountId.toString());
                } catch (final NumberFormatException e) {
                    throw FileStorageExceptionCodes.MISSING_CONFIG.create(e, DropboxConstants.ID, fsAccount.getId());
                }
            }
        }
        final OAuthService oAuthService = DropboxServices.getService(OAuthService.class);
        try {
            final OAuthAccount oauthAccount = oAuthService.getAccount(oauthAccountId, session, user, contextId);
            /*-
             * Retrieve information about the user's Dropbox account.
             *
             * See: https://www.dropbox.com/developers/documentation/http/documentation#users-get_current_account
             */
            //////////////// OAuth 1.0 /////////////////
            final AppKeyPair appKeys = new AppKeyPair(DropboxConfiguration.getInstance().getApiKey(), DropboxConfiguration.getInstance().getSecretKey());
            webAuthSession = new TrustAllWebAuthSession(appKeys, AccessType.DROPBOX);
            dropboxApi = new DropboxAPI<WebAuthSession>(webAuthSession);
            // Re-auth specific stuff
            final AccessTokenPair reAuthTokens = new AccessTokenPair(oauthAccount.getToken(), oauthAccount.getSecret());
            dropboxApi.getSession().setAccessTokenPair(reAuthTokens);

            /////////////// OAuth 2.0 /////////////////
            DbxRequestConfig config = new DbxRequestConfig(DropboxConfiguration.getInstance().getProductName());
            client = new DbxClientV2(config, oauthAccount.getToken());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the DropboxAPI reference
     *
     * @return The DropboxAPI reference
     * @deprecated Use {@link #getDropboxClient()} instead
     */
    public DropboxAPI<WebAuthSession> getDropboxAPI() {
        return dropboxApi;
    }

    /**
     * Returns the Dropbox v2 API client
     * 
     * @return the Dropbox v2 API client
     */
    public DbxClientV2 getDropboxClient() {
        return client;
    }

    /**
     * Disposes this Dropbox OAuth access.
     */
    public void dispose() {
        // So far nothing known to me that needs to be disposed
    }

}
