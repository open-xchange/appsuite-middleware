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

package com.openexchange.file.storage.owncloud.internal;

import com.openexchange.file.storage.File;
import com.openexchange.file.storage.owncloud.OwnCloudFileAccess;
import com.openexchange.file.storage.webdav.WebDAVFile;
import com.openexchange.webdav.client.WebDAVResource;

/**
 * {@link OwnCloudFile}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class OwnCloudFile extends WebDAVFile {

    private String fileId;
    private boolean favorite = false;

    /**
     * Initializes a new {@link OwnCloudFile}.
     */
    public OwnCloudFile(File file, WebDAVResource res) {
        super(file);
        setFileId(res.getProperty(OwnCloudFileAccess.OC_FILEID, String.class));
        setEtag(res.getEtag());
        Integer fav = res.getProperty(OwnCloudFileAccess.OC_FAVORITE, Integer.class);
        favorite = (fav != null && fav.intValue() == 1);
        setUniqueId(res.getProperty(OwnCloudFileAccess.OC_ID, String.class));
    }

    /**
     * Initializes a new {@link OwnCloudFile}.
     */
    public OwnCloudFile(File file, String fileId, String etag, String id) {
        super(file);
        setFileId(fileId);
        setEtag(etag);
        setUniqueId(id);
    }

    /**
     * Gets the fileId
     *
     * @return The fileid
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Sets the fileid
     *
     * @param fileId The fileid to set
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public int getColorLabel() {
        return favorite ? 1 : super.getColorLabel();
    }

}
