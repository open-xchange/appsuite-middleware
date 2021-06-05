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

package com.openexchange.mime;

import java.io.File;
import java.util.List;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link MimeTypeMap} - Maps MIME types to file extensions and vice versa.
 * <p>
 * This class looks in various places for MIME types file entries. When requests are made to look up MIME types or file extensions, it
 * searches MIME types files in the following order:
 * <ol>
 * <li>The file <i>.mime.types</i> in the user's home directory.</li>
 * <li>The file <i>&lt;java.home&gt;/lib/mime.types</i>.</li>
 * <li>The file or resources named <i>META-INF/mime.types</i>.</li>
 * <li>The file or resource named <i>META-INF/mimetypes.default</i>.</li>
 * <li>The file or resource denoted by property <i>MimeTypeFileName</i>.</li>
 * </ol>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface MimeTypeMap {

    /**
     * Gets the MIME type associated with given file.
     *
     * @param file The file
     * @return The MIME type associated with given file or <code>application/octet-stream</code> if none found
     */
    String getContentType(final File file);

    /**
     * Gets the MIME type associated with given file name.
     *
     * @param fileName The file name; e.g. <code>"file.html"</code>
     * @return The MIME type associated with given file name or <code>application/octet-stream</code> if none found
     */
    String getContentType(String fileName);

    /**
     * Gets the MIME type associated with given file extension.
     *
     * @param extension The file extension; e.g. <code>"txt"</code>
     * @return The MIME type associated with given file extension or <code>application/octet-stream</code> if none found
     */
    String getContentTypeByExtension(String extension);

    /**
     * Gets the file extension for given MIME type.
     *
     * @param mimeType The MIME type
     * @return The file extension for given MIME type or <code>dat</code> if none found
     */
    List<String> getFileExtensions(String mimeType);
}
