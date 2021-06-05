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

package com.openexchange.file.storage.composition.crypto;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.ComparedPermissions;
import com.openexchange.session.Session;

/**
 * {@link CryptoAwareSharingService}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.0
 */
public interface CryptoAwareSharingService {

    /**
     * Update sharing permissions for an encrypted file.
     * Adjust the file to allow sharing
     * @param file
     * @param permissions
     * @throws OXException
     */
    @SuppressWarnings("rawtypes")
    public void updateSharing (Session session, File file, FileStorageFileAccess fileAccess, ComparedPermissions permissions, List<FileStorageObjectPermission> updatedPermissions) throws OXException;

    /**
     * Check if the file is encrypted.
     * @param file
     * @return
     */
    boolean isEncrypted(File file);

}
