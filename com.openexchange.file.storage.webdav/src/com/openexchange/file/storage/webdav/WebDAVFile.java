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

package com.openexchange.file.storage.webdav;

import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;

/**
 * {@link WebDAVFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class WebDAVFile extends DefaultFile {

    private String etag;

    /**
     * Initializes a new {@link WebDAVFile} from given file.
     *
     * @param file The file
     */
    public WebDAVFile(File file) {
        super(file);
    }

    public WebDAVFile() {
        super();
    }

    @Override
    public String toString() {
        final String url = getURL();
        return url == null ? super.toString() : url;
    }

    /**
     * Gets the file's ETag
     *
     * @return The ETag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Sets the ETag
     *
     * @param etag THe ETag to set
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }
}