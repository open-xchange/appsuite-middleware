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

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.dropbox.DropboxConfiguration;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.file.storage.dropbox.Utils;
import com.openexchange.file.storage.dropbox.auth.TrustAllWebAuthSession;
import com.openexchange.oauth.AbstractOAuthAccess;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.session.Session;

/**
 * {@link DropboxOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxOAuthAccess extends AbstractOAuthAccess {

    private final FileStorageAccount fsAccount;
    private final Session session;

    /**
     * Initializes a new {@link DropboxOAuthAccess}.
     */
    public DropboxOAuthAccess(FileStorageAccount fsAccount, Session session) {
        super();
        this.fsAccount = fsAccount;
        this.session = session;
    }

    @Override
    public boolean ping() throws OXException {
        try {
            DropboxAPI<WebAuthSession> client = this.<DropboxAPI<WebAuthSession>> getClient().client;
            client.accountInfo();
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

    @Override
    public void initialize() throws OXException {
        synchronized (this) {
            try {
                int oauthAccountId = getAccountId();
                OAuthService oAuthService = DropboxServices.getService(OAuthService.class);
                OAuthAccount dropboxOAuthAccount = oAuthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());
                setOAuthAccount(dropboxOAuthAccount);

                /*-
                 * Retrieve information about the user's Dropbox account.
                 *
                 * See: https://www.dropbox.com/developers/reference/api#account-info
                 */
                final AppKeyPair appKeys = new AppKeyPair(DropboxConfiguration.getInstance().getApiKey(), DropboxConfiguration.getInstance().getSecretKey());
                WebAuthSession webAuthSession = new TrustAllWebAuthSession(appKeys, AccessType.DROPBOX);
                DropboxAPI<WebAuthSession> dropboxApi = new DropboxAPI<WebAuthSession>(webAuthSession);
                // Re-auth specific stuff
                final AccessTokenPair reAuthTokens = new AccessTokenPair(dropboxOAuthAccount.getToken(), dropboxOAuthAccount.getSecret());
                dropboxApi.getSession().setAccessTokenPair(reAuthTokens);

                setOAuthClient(new OAuthClient<DropboxAPI<WebAuthSession>>(dropboxApi, dropboxOAuthAccount.getToken()));
            } catch (RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    @Override
    public int getAccountId() throws OXException {
        try {
            return getAccountId(fsAccount.getConfiguration());
        } catch (IllegalArgumentException e) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(DropboxConstants.ID, fsAccount.getId());
        }
    }

    @Override
    public void revoke() throws OXException {
        // Nothing to do
    }

    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        return this;
    }

}
