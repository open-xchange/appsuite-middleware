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

package com.openexchange.filestore;

import java.net.URI;
import com.openexchange.exception.OXException;

/**
 * {@link FileStorageProvider} - A provider for a file storage.
 * <p>
 * If the file storage provider needs to be reinitialized in case certain configuration properties are changed, the {@link InterestsAware}
 * interface is supposed to be implemented.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FileStorageProvider {

    /** The default ranking for a file storage provider */
    public static final int DEFAULT_RANKING = 0;

    /**
     * Gets the file storage
     *
     * @param uri The URI to create the file storage from
     * @return The file storage
     * @throws OXException If storage cannot be returned
     */
    FileStorage getFileStorage(URI uri) throws OXException;

    /**
     * Signals whether specified URI is supported or not.
     *
     * @param uri The URI to check
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean supports(URI uri) throws OXException;

    /**
     * Gets the ranking.
     *
     * @return The ranking
     * @see #DEFAULT_RANKING
     */
    int getRanking();

}
