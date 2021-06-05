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

package com.openexchange.file.storage.dropbox;

import com.openexchange.file.storage.FileStorageFolder;

/**
 * {@link Utils} - Utility class for Dropbox resources.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utils {

    /**
     * Initializes a new {@link Utils}.
     */
    private Utils() {
        super();
    }

    /**
     * Normalizes given folder identifier
     *
     * @param folderId The folder identifier
     * @return The normalizes folder identifier
     */
    public static String normalizeFolderId(String folderId) {
        if (null == folderId) {
            return folderId;
        }
        return folderId.endsWith("/") ? folderId.substring(0, folderId.length() - 1) : folderId;
    }

    /**
     * Checks if specified folder identifier ends with a <code>'/'</code> character.
     *
     * @param folderId The folder identifier to check
     * @param rootUri The root URI of the connected WebDAV server
     * @return The checked folder identifier
     */
    public static String checkFolderId(final String folderId, final String rootUri) {
        if (FileStorageFolder.ROOT_FULLNAME.equals(folderId)) {
            return rootUri;
        }
        return checkFolderId(folderId);
    }

    /**
     * Checks if specified folder identifier ends with a <code>'/'</code> character.
     *
     * @param folderId The folder identifier to check
     * @return The checked folder identifier
     */
    public static String checkFolderId(final String folderId) {
        if (null == folderId) {
            return null;
        }
        if (folderId.endsWith("/")) {
            return folderId;
        }
        return new StringBuilder(folderId).append('/').toString();
    }

    /**
     * Checks the href provided by a multi-status response.
     *
     * @param href The multi-status response's href
     * @param isDirectory <code>true</code> if href denotes a directory; otherwise <code>false</code>
     * @return The checked href
     */
    public static String checkHref(final String href, final boolean isDirectory) {
        return isDirectory ? checkFolderId(href) : checkFileId(href);
    }

    /**
     * Checks specified file identifier.
     *
     * @param fileId The file identifier
     * @return The checked file identifier
     */
    public static String checkFileId(final String fileId) {
        if (null == fileId) {
            return null;
        }
        if (fileId.endsWith("/")) {
            final int length = fileId.length();
            return length == 1 ? "" : fileId.substring(0, length - 1);
        }
        return fileId;
    }
}
