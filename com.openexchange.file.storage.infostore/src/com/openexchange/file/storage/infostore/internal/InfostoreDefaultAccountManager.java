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

package com.openexchange.file.storage.infostore.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.ServiceAware;
import com.openexchange.session.Session;


/**
 * {@link InfostoreDefaultAccountManager}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreDefaultAccountManager implements FileStorageAccountManager {

    /**
     * The default account for infostore.
     */
    private static final class FileStorageAccountImpl implements FileStorageAccount, ServiceAware {

        private static final long serialVersionUID = -4701429514008282005L;

        private final InfostoreFileStorageService storageService;

        /**
         * Initializes a new {@link InfostoreDefaultAccountManager.FileStorageAccountImpl}.
         */
        protected FileStorageAccountImpl(InfostoreFileStorageService storageService) {
            super();
            this.storageService = storageService;
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return Collections.emptyMap();
        }

        @Override
        public String getDisplayName() {
            return "Standard Infostore";
        }

        @Override
        public FileStorageService getFileStorageService() {
            return storageService;
        }

        @Override
        public String getId() {
            return InfostoreDefaultAccountManager.DEFAULT_ID;
        }

        @Override
        public String getServiceId() {
            return "com.openexchange.infostore";
        }

        @Override
        public JSONObject getMetadata() {
            return new JSONObject();
        }
    }

    public static final String DEFAULT_ID = "infostore";

    private final FileStorageAccount defaultAccount;


    public InfostoreDefaultAccountManager(InfostoreFileStorageService storageService) {
        super();
        defaultAccount = new FileStorageAccountImpl(storageService);
    }

    @Override
    public String addAccount(final FileStorageAccount account, final Session session) throws OXException {
        return "";
    }

    @Override
    public void deleteAccount(final FileStorageAccount account, final Session session) throws OXException {
        // Nope
    }

    @Override
    public FileStorageAccount getAccount(final String id, final Session session) throws OXException {
        if (/*InfostoreFacades.isInfoStoreAvailable() && */InfostoreDefaultAccountManager.DEFAULT_ID.equals(id)) {
            return defaultAccount;
        }
        throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(id, "com.openexchange.infostore");
    }

    @Override
    public List<FileStorageAccount> getAccounts(final Session session) throws OXException {
        return Arrays.asList(defaultAccount);
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public void updateAccount(final FileStorageAccount account, final Session session) throws OXException {
        // Nothing to do
    }

    @Override
    public boolean hasEncryptedItems(final Session session) throws OXException {
        return false;
    }

}
