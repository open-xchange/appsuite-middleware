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

package com.openexchange.file.storage;

import com.openexchange.tools.id.IDMangler;

/**
 * Utility methods related to {@link FileStorageAccount}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FileStorageAccounts {

    /**
     * Gets the full-qualified (i.e. globally across all file storage services) ID of a
     * file storage account.
     *
     * @param account The account to get the ID for
     * @return The full-qualified ID
     */
    public static String getQualifiedID(FileStorageAccount account) {
        return getQualifiedID(account.getFileStorageService().getId(), account.getId());
    }

    /**
     * Gets the full qualified (i.e. globally across all file storage services) ID of a
     * file storage account.
     *
     * @param serviceId ID of the accounts file storage service
     * @param accountId ID of the account
     * @return The full-qualified ID
     */
    public static String getQualifiedID(String serviceId, String accountId) {
        return IDMangler.mangle(serviceId, accountId);
    }

    /**
     * Checks whether the given account is the users default file storage account.
     *
     * @param account The account to get the ID for
     * @return <code>true</code> if it's the default account
     */
    public static boolean isDefaultAccount(FileStorageAccount account) {
        return "com.openexchange.infostore".equals(account.getFileStorageService().getId()) && "infostore".equals(account.getId());
    }

}
