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

package com.openexchange.file.storage.rdb.internal;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.rdb.RdbFileStorageAccountManager;
import com.openexchange.file.storage.rdb.Services;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.session.Session;


/**
 * {@link RdbFileStorageAccountManagerProvider} - The default account manager provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class RdbFileStorageAccountManagerProvider implements FileStorageAccountManagerProvider {

    /**
     * Initializes a new {@link RdbFileStorageAccountManagerProvider}.
     */
    public RdbFileStorageAccountManagerProvider() {
        super();
    }

    @Override
    public boolean supports(final String serviceId) {
        return true;
    }

    @Override
    public FileStorageAccountManager getAccountManagerFor(final String serviceId) throws OXException {
        return new RdbFileStorageAccountManager(Services.getService(FileStorageServiceRegistry.class).getFileStorageService(serviceId));
    }

    @Override
    public int getRanking() {
        return DEFAULT_RANKING;
    }

    @Override
    public FileStorageAccountManager getAccountManager(String accountId, Session session) throws OXException {
        return RdbFileStorageAccountManager.getAccountById(accountId, session);
    }

}
