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

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link FileStorageBackwardLinkAccess}.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public interface FileStorageBackwardLinkAccess extends FileStorageFileAccess {

    /**
     * Generates a <i>backward</i> link into the guest account of a subscribed share, pointing to a specific target, which can be used
     * to open the regular, browser-based guest mode on the remote host.
     *
     * @param folderId The targeted folder in the guest account
     * @param id The targeted item in the guest account, or <code>null</code> when pointing to a folder
     * @param additionals Additional data to include in the resulting backward link's share target, or <code>null</code> if not set
     * @return The generated backward link
     */
    String getBackwardLink(String folderId, String id, Map<String, String> additionals) throws OXException;

}
