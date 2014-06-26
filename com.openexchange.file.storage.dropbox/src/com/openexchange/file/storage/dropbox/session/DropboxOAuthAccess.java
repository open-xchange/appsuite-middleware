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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.dropbox.DropboxConfiguration;
import com.openexchange.file.storage.dropbox.DropboxExceptionCodes;
import com.openexchange.file.storage.dropbox.DropboxServices;
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
     * The Dropbox user identifier.
     */
    private final long dropboxUserId;

    /**
     * The Dropbox user's full name
     */
    private final String dropboxUserName;

    /**
     * The Web-authenticating session.
     */
    private WebAuthSession webAuthSession;

    /**
     * The Dropbox API reference.
     */
    private DropboxAPI<WebAuthSession> dropboxApi;

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
            if (accountId instanceof Integer) {
                oauthAccountId = ((Integer) accountId).intValue();
            } else {
                try {
                    oauthAccountId = Integer.parseInt(accountId.toString());
                } catch (final NumberFormatException e) {
                    throw DropboxExceptionCodes.MISSING_CONFIG.create(e, fsAccount.getId());
                }
            }
        }
        final OAuthService oAuthService = DropboxServices.getService(OAuthService.class);
        try {
            final OAuthAccount oauthAccount = oAuthService.getAccount(oauthAccountId, session, user, contextId);
            /*-
             * Retrieve information about the user's Dropbox account.
             *
             * See: https://www.dropbox.com/developers/reference/api#account-info
             */
            final AppKeyPair appKeys = new AppKeyPair(DropboxConfiguration.getInstance().getApiKey(), DropboxConfiguration.getInstance().getSecretKey());
            webAuthSession = new TrustAllWebAuthSession(appKeys, AccessType.DROPBOX);
            dropboxApi = new DropboxAPI<WebAuthSession>(webAuthSession);
            // Re-auth specific stuff
            final AccessTokenPair reAuthTokens = new AccessTokenPair(oauthAccount.getToken(), oauthAccount.getSecret());
            dropboxApi.getSession().setAccessTokenPair(reAuthTokens);
            // Get account information
            final Account accountInfo = dropboxApi.accountInfo();
            dropboxUserId = accountInfo.uid;
            dropboxUserName = accountInfo.displayName;
        } catch (final OXException e) {
            throw e;
        } catch (final org.scribe.exceptions.OAuthException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final DropboxUnlinkedException e) {
            throw DropboxExceptionCodes.UNLINKED_ERROR.create();
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the DropboxAPI reference
     *
     * @return The DropboxAPI reference
     */
    public DropboxAPI<WebAuthSession> getDropboxAPI() {
        return dropboxApi;
    }

    /**
     * Disposes this Dropbox OAuth access.
     */
    public void dispose() {
        // So far nothing known to me that needs to be disposed
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

}
