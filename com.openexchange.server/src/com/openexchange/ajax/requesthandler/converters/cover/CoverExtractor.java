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

package com.openexchange.ajax.requesthandler.converters.cover;

import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;

/**
 * {@link CoverExtractor} - Extracts cover image from a file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CoverExtractor {

    /**
     * Checks if this cover extractor handles specified file.
     *
     * @param file The file
     * @return <code>true</code> if this cover extractor handles specified file; otherwise <code>false</code>
     */
    boolean handlesFile(IFileHolder file);

    /**
     * Extracts the cover image from specified file.
     *
     * @param file The file
     * @return The extracted cover image as file
     * @throws OXException If extract attempt fails
     */
    IFileHolder extractCover(IFileHolder file) throws OXException;

}
