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

package com.openexchange.groupware.infostore.media;

import java.io.InputStream;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.session.Session;

/**
 * {@link MediaMetadataExtractor} - Extracts possible metadata from a certain media resources (images, videos, etc.).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface MediaMetadataExtractor {

    /**
     * Checks if this extractor accepts specified document for being processed.
     *
     * @param document The document to check
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isApplicable(DocumentMetadata document) throws OXException;

    /**
     * Checks the effort of extracting media metadata from given input stream.
     *
     * @param session The user session
     * @param in The input stream to examine
     * @param document The document associated with given stream
     * @param optArguments Optional (mutable) additional arguments
     * @return The effort
     * @throws OXException If effort estimation fails
     */
    Effort estimateEffort(Session session, InputStream in, DocumentMetadata document, Map<String, Object> optArguments) throws OXException;

    /**
     * Extracts media metadata from given stream and applies them to specified document.
     *
     * @param optStream The optional initial stream
     * @param provider The input stream provider to extract from
     * @param document The document to apply to
     * @param optArguments Optional (immutable) additional arguments
     * @return The extractor result
     * @throws OXException If extraction fails
     */
    ExtractorResult extractAndApplyMediaMetadata(InputStream optStream, InputStreamProvider provider, DocumentMetadata document, Map<String, Object> optArguments) throws OXException;
}
