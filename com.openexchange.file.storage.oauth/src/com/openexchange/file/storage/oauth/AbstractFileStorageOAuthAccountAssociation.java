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
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.oauth.association.AbstractOAuthAccountAssociation;
import com.openexchange.oauth.association.Module;

/**
 * {@link AbstractFileStorageOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractFileStorageOAuthAccountAssociation extends AbstractOAuthAccountAssociation {

    private final FileStorageAccount fileStorageAccount;

    /**
     * Initializes a new {@link AbstractFileStorageOAuthAccountAssociation}.
     *
     * @param accountId The identifier of the OAuth account
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param fileStorageAccount The file storage account
     */
    protected AbstractFileStorageOAuthAccountAssociation(int accountId, int userId, int contextId, FileStorageAccount fileStorageAccount) {
        super(accountId, userId, contextId);
        this.fileStorageAccount = fileStorageAccount;
    }

    @Override
    public String getId() {
        return fileStorageAccount.getId();
    }

    @Override
    public String getDisplayName() {
        return fileStorageAccount.getDisplayName();
    }

    @Override
    public String getModule() {
        return Module.INFOSTORE.getModuleName();
    }

    @Override
    public String getFolder() {
        return new FolderID(getServiceId(), getId(), FileStorageFolder.ROOT_FULLNAME).toUniqueID();
    }

    /**
     * Returns the {@link FileStorageAccount}
     *
     * @return the {@link FileStorageAccount}
     */
    protected FileStorageAccount getFileStorageAccount() {
        return fileStorageAccount;
    }

}
