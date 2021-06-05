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

package com.openexchange.file.storage.oauth;

import com.openexchange.file.storage.FileStorageAccount;

/**
 * {@link FileStorageAccountInfo}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class FileStorageAccountInfo {

    private final FileStorageAccount account;
    private final int ranking;

    /**
     * Initialises a new {@link FileStorageAccountInfo}.
     * 
     * @param account
     * @param ranking
     */
    FileStorageAccountInfo(FileStorageAccount account, int ranking) {
        super();
        this.account = account;
        this.ranking = ranking;
    }

    /**
     * Gets the account
     *
     * @return The account
     */
    public FileStorageAccount getAccount() {
        return account;
    }

    /**
     * Gets the ranking
     *
     * @return The ranking
     */
    public int getRanking() {
        return ranking;
    }
}
