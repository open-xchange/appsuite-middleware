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

package com.openexchange.preview;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * {@link PreviewDocument}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface PreviewDocument {

    /**
     * Gets the document's meta data.
     * <p>
     * Typical meta data would be:
     * <ul>
     * <li><code>"title"</code></li>
     * <li><code>"subject"</code></li>
     * <li><code>"resourcename"</code></li>
     * <li><code>"content-type"</code></li>
     * <li><code>"author"</code></li>
     * <li>...</li>
     * </ul>
     *
     * @return The meta data as a {@link Map}
     */
    Map<String, String> getMetaData();

    /**
     * Checks if this preview document provides content via {@link #getContent()} method.
     *
     * @return <code>true</code> if content is provided; otherwise <code>false</code>
     */
    boolean hasContent();

    /**
     * Gets the document's content in its output format.
     *
     * @return The content (or <code>null</code> if output format does not imply a content; e.g. {@link PreviewOutput#METADATA})
     */
    List<String> getContent();

    /**
     * Gets the preview image (thumbnail).
     *
     * @return The input stream for the image or <code>null</code> if the image is not available.
     */
    InputStream getThumbnail();


    /**
     * Determines if the original document contains more content than this preview document provides.
     *
     * @return true, if more content is available, false if not and null if the document does not know anything about more content.
     */
    Boolean isMoreAvailable();

}
