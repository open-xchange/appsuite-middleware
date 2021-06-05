/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.file.storage.xox;

import static com.openexchange.file.storage.xox.XOXStorageConstants.PASSWORD;
import static com.openexchange.file.storage.xox.XOXStorageConstants.SHARE_URL;
import java.util.List;
import java.util.Map;
import com.openexchange.api.client.ApiClient;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.core.tools.ShareTool;

/**
 * {@link XOXFileStorageAccountManager} - Wrapper that ensures that the {@link ApiClient} used by the account is
 * removed and cleaned, too.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class XOXFileStorageAccountManager implements FileStorageAccountManager {

    private final ServiceLookup services;
    private final FileStorageAccountManager delegatee;

    /**
     * Initializes a new {@link XOXFileStorageAccountManager}.
     *
     * @param services The {@link ServiceLookup}
     * @param delegatee The actual manager to delegate to
     */
    public XOXFileStorageAccountManager(ServiceLookup services, FileStorageAccountManager delegatee) {
        super();
        this.services = services;
        this.delegatee = delegatee;
    }

    @Override
    public String addAccount(FileStorageAccount account, Session session) throws OXException {
        return delegatee.addAccount(account, session);
    }

    @Override
    public void updateAccount(FileStorageAccount account, Session session) throws OXException {
        FileStorageAccount storedAccount = getAccount(account.getId(), session);
        String storedShareUrl = (String) storedAccount.getConfiguration().get(SHARE_URL);
        if (null != account.getConfiguration()) {
            /*
             * Check if URL or password changed
             */
            Map<String, Object> configuration = account.getConfiguration();
            String shareUrl = (String) configuration.get(SHARE_URL);
            if (Strings.isNotEmpty(shareUrl) && false == ShareTool.equals(storedShareUrl, shareUrl)) {
                throw FileStorageExceptionCodes.UNALLOWED_ACCOUNT_UPDATE.create("Share URL must not be changed.");
            }

            String password = (String) configuration.get(PASSWORD);
            if (Strings.isEmpty(password) || password.equals(storedAccount.getConfiguration().get(PASSWORD))) {
                // Do not update API client
                storedShareUrl = null;
            }
        }

        delegatee.updateAccount(account, session);
        if (Strings.isNotEmpty(storedShareUrl)) {
            clearRemoteSessions(session, storedShareUrl);
        }
    }

    @Override
    public void deleteAccount(FileStorageAccount account, Session session) throws OXException {
        String shareLink = (String) account.getConfiguration().get(SHARE_URL);
        delegatee.deleteAccount(account, session);
        clearRemoteSessions(session, shareLink);
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return delegatee.getAccounts(session);
    }

    @Override
    public FileStorageAccount getAccount(String id, Session session) throws OXException {
        return delegatee.getAccount(id, session);
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        delegatee.cleanUp(secret, session);

    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        delegatee.removeUnrecoverableItems(secret, session);
    }

    @Override
    public void migrateToNewSecret(String oldSecret, String newSecret, Session session) throws OXException {
        delegatee.migrateToNewSecret(oldSecret, newSecret, session);
    }

    @Override
    public boolean hasEncryptedItems(Session session) throws OXException {
        return delegatee.hasEncryptedItems(session);
    }

    /**
     * Clear any remote session so follow up calls will work as expected
     *
     * @param session The user session
     * @param shareLink The share link or <code>null</code> to remove all API client belonging to the user
     * @param storageAccount The account to close
     * @throws OXException In case service is missing
     */
    private void clearRemoteSessions(Session session, String shareLink) throws OXException {
        services.getServiceSafe(ApiClientService.class).close(session, shareLink);
    }

}
