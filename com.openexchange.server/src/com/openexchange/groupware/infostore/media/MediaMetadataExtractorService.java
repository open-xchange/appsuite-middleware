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
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.session.Session;

/**
 * {@link MediaMetadataExtractorService} - Extracts possible metadata from media resources (images, videos, etc.).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface MediaMetadataExtractorService {

    /**
     * Checks the effort of extracting media metadata from given input stream.
     *
     * @param session The user session
     * @param provider The provider to obtain the input stream to examine
     * @param document The document associated with given stream
     * @return The effort estimation result
     * @throws OXException If effort estimation fails
     */
    EstimationResult estimateEffort(Session session, InputStreamProvider provider, DocumentMetadata document) throws OXException;

    /**
     * Extracts media metadata from given stream using specified extractor and applies them to specified document.
     *
     * @param extractor The extractor to use
     * @param optStream The optional initial stream
     * @param provider The input stream provider once a new stream needs to be obtained
     * @param document The document to apply to
     * @param optArguments Optional (immutable) additional arguments
     * @return The extractor result
     * @throws OXException If extraction fails
     */
    ExtractorResult extractAndApplyMediaMetadataUsing(MediaMetadataExtractor extractor, InputStream optStream, InputStreamProvider provider, DocumentMetadata document, Map<String, Object> optArguments) throws OXException;

    /**
     * Extracts media metadata from given stream and applies them to specified document.
     *
     * @param provider The provider to obtain the input stream to extract from
     * @param document The document to apply to
     * @param optArguments Optional (immutable) additional arguments
     * @return The extractor result
     * @throws OXException If extraction fails
     */
    ExtractorResult extractAndApplyMediaMetadata(InputStreamProvider provider, DocumentMetadata document, Map<String, Object> optArguments) throws OXException;

    // ------------------------------------------- More methods for job scheduling etc. ----------------------------------------------------

    /**
     * Schedules to extract media metadata from given stream and applies them to specified document.
     *
     * @param document The document to obtain necessary information from (folder identifier, document identifier, version, storage location, etc.)
     * @param fileStorage The file storage holding binary data
     * @param optArguments Optional (immutable) additional arguments
     * @param session The session providing user information
     * @return The job identifier
     * @throws OXException If scheduling fails
     */
    String scheduleMediaMetadataExtraction(DocumentMetadata document, FileStorage fileStorage, Map<String, Object> optArguments, Session session) throws OXException;

    /**
     * Checks specified document is currently scheduled for media metadata extraction.
     *
     * @param document The document
     * @param session The session providing user information
     * @return <code>true</code> if currently scheduled; otherwise <code>false</code> if there is no such scheduled document or extraction is already in execution
     * @throws OXException If check fails
     */
    boolean isScheduledForMediaMetadataExtraction(DocumentMetadata document, Session session) throws OXException;

}
