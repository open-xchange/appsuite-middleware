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

package com.openexchange.file.storage.config.internal;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.config.ConfigFileStorageAccount;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link ConfigFileStorageAccountManagerProvider} - The config account manager provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class ConfigFileStorageAccountManagerProvider implements FileStorageAccountManagerProvider {

    private final ConfigFileStorageAccountParser parser;

    /**
     * Initializes a new {@link ConfigFileStorageAccountManagerProvider}.
     */
    public ConfigFileStorageAccountManagerProvider() {
        super();
        parser = ConfigFileStorageAccountParser.getInstance();
    }

    @Override
    public boolean supports(final String serviceId) {
        final Map<String, ConfigFileStorageAccountImpl> accounts = parser.getAccountsFor(serviceId);
        return (null != accounts && !accounts.isEmpty());
    }

    @Override
    public FileStorageAccountManager getAccountManagerFor(final String serviceId) throws OXException {
        return new ConfigFileStorageAccountManager(Services.getService(FileStorageServiceRegistry.class).getFileStorageService(serviceId));
    }

    @Override
    public int getRanking() {
        return 10;
    }

    @Override
    public FileStorageAccountManager getAccountManager(final String accountId, final Session session) throws OXException {
        final ConfigFileStorageAccount storageAccount = parser.get(accountId);
        if (null == storageAccount) {
            return null;
        }
        FileStorageService fileStorageService = storageAccount.getFileStorageService();
        if (null == fileStorageService) {
            try {
                fileStorageService = Services.getService(FileStorageServiceRegistry.class).getFileStorageService(storageAccount.getServiceId());
            } catch (OXException e) {
                if (FileStorageExceptionCodes.UNKNOWN_FILE_STORAGE_SERVICE.equals(e)) {
                    return null;
                }
                throw e;
            }
        }
        return new ConfigFileStorageAccountManager(fileStorageService);
    }

}
