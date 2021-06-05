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

package com.openexchange.file.storage.composition;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link IDBasedETagProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Deprecated
public interface IDBasedETagProvider {

    /**
     * Gets a value indicating whether the ETags delivered by this storage can be assumed to be recursive or not. When being "recursive",
     * a changed ETag of a subfolder will result in changed ETags of all parent folders recursively.
     * <p/>
     * <b>Note: </b>Only available if {@link IDBasedETagProvider#supportsETags} is <code>true</code>.
     *
     * @param folderId The folder to check
     * @return <code>true</code> if ETags delivered by this storage are recursive, <code>false</code>, otherwise.
     * @throws OXException
     */
    boolean isRecursive(String folderId) throws OXException;

    /**
     * Gets the ETags for the supplied folders to quickly determine which folders contain changes. An updated ETag in a folder indicates a
     * change, for example a new, modified or deleted file. If {@link IDBasedETagProvider#isRecursive()} is <code>true</code>, an
     * updated ETag may also indicate a change in one of the folder's subfolders.
     * <p/>
     * <b>Note: </b>Only available if {@link IDBasedETagProvider#supportsETags} is <code>true</code>.
     *
     * @param folderIds A list of folder IDs to get the ETags for
     * @return A map holding the resulting ETags to each requested folder ID
     * @throws OXException
     */
    Map<String, String> getETags(List<String> folderIds) throws OXException;

    /**
     * Gets a value indicating whether sequence numbers are supported by the given folder.
     *
     * @param folderId The folder to check
     * @return <code>true</code> if sequence numbers are supported, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean supportsETags(String folderId) throws OXException;

}
