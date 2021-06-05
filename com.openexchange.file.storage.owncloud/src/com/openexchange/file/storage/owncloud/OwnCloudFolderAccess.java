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

package com.openexchange.file.storage.owncloud;

import java.net.MalformedURLException;
import java.net.URL;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.UserCreatedFileStorageFolderAccess;
import com.openexchange.file.storage.webdav.AbstractWebDAVFolderAccess;
import com.openexchange.file.storage.webdav.WebDAVFileStorageConstants;
import com.openexchange.file.storage.webdav.utils.WebDAVEndpointConfig;
import com.openexchange.webdav.client.WebDAVClient;

/**
 * {@link OwnCloudFolderAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class OwnCloudFolderAccess extends AbstractWebDAVFolderAccess implements UserCreatedFileStorageFolderAccess {

    private final String rootUrl;

    /**
     * Initializes a new {@link NextCloudFolderAccessa}.
     */
    protected OwnCloudFolderAccess(@NonNull WebDAVClient webdavClient, @NonNull OwnCloudAccountAccess accountAccess) throws OXException {
        super(webdavClient, accountAccess);
        if (account.getConfiguration().containsKey(WebDAVFileStorageConstants.WEBDAV_URL)) {
            rootUrl = new WebDAVEndpointConfig.Builder(this.session, accountAccess.getWebDAVFileStorageService(), (String) account.getConfiguration().get(WebDAVFileStorageConstants.WEBDAV_URL)).build().getUrl();
        } else {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(WebDAVFileStorageConstants.ID, WebDAVFileStorageConstants.WEBDAV_URL);
        }
    }

    /**
     * Gets the path to the root folder
     *
     * @return The path to the root folder
     * @throws OXException in case the root url is invalid
     */
    public String getRootFolderId() throws OXException {
        try {
            return new URL(rootUrl).getPath();
        } catch (MalformedURLException e) {
            throw FileStorageExceptionCodes.INVALID_URL.create(e, e.getMessage());
        }
    }

}
