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

package com.openexchange.textxtraction;

import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link TextXtractService} - The service to extract plain text from various document formats.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface TextXtractService {

    /**
     * Extracts plain-text content from specified stream's content.
     * <p>
     * An auto-detection mechanism is performed to determine stream's document format if <code>optMimeType</code> is <code>null</code>.
     *
     * @param inputStream The input stream to extract text from
     * @param optMimeType The optional MIME type, pass <code>null</code> to auto-detect
     * @return The extracted plain-text
     * @throws OXException If text extraction fails for any reason
     */
    String extractFrom(InputStream inputStream, String optMimeType) throws OXException;

    /**
     * Extracts plain-text content from specified content.
     * <p>
     * An auto-detection mechanism is performed to determine stream's document format if <code>optMimeType</code> is <code>null</code>.
     *
     * @param content The content to extract text from (and hopefully no plain-text content)
     * @param optMimeType The optional MIME type, pass <code>null</code> to auto-detect
     * @return The extracted plain-text
     * @throws OXException If text extraction fails for any reason
     */
    String extractFrom(String content, String optMimeType) throws OXException;

    /**
     * Extracts plain-text content from specified resource's content.
     * <p>
     * An auto-detection mechanism is performed to determine file's/URL's document format if <code>optMimeType</code> is <code>null</code>.
     *
     * @param resource The (resource) argument either denotes an URL or a file
     * @param optMimeType The optional MIME type, pass <code>null</code> to auto-detect
     * @return The extracted plain-text
     * @throws OXException If text extraction fails for any reason
     */
    String extractFromResource(String resource, String optMimeType) throws OXException;

}
