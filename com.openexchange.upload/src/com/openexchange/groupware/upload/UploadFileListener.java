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

package com.openexchange.groupware.upload;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link UploadFileListener} - Receives various call-backs on upload of a file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface UploadFileListener {

    /**
     * Invoked before the upload's stream gets processed; providing basic information.
     *
     * @param uploadId The identifier that uniquely identifies the upload
     * @param fileName The name of the uploaded file
     * @param fieldName The name of the field in the multipart form (if any)
     * @param contentType The content type
     * @param session The session associated with the upload or <code>null</code>
     * @throws OXException If this listener signals that uploaded should be aborted
     */
    void onBeforeUploadProcessed(String uploadId, String fileName, String fieldName, String contentType, Session session) throws OXException;

    /**
     * Invoked after the upload's stream gets processed; providing basic information.
     *
     * @param uploadId The identifier that uniquely identifies the upload
     * @param uploadFile The fully parsed uploaded file
     * @param session The session associated with the upload or <code>null</code>
     * @throws OXException If this listener signals that uploaded should be aborted
     */
    void onAfterUploadProcessed(String uploadId, UploadFile uploadFile, Session session) throws OXException;

    /**
     * Invoked in case upload failed while processing individual uploaded files;<br>
     * e.g. upload quota is exceeded.
     * <p>
     * All previously processed upload files will be deleted.
     *
     * @param uploadId The identifier that uniquely identifies the upload
     * @param exception The exception rendering the upload as failure
     * @param session The session associated with the upload or <code>null</code>
     */
    void onUploadFailed(String uploadId, OXException exception, Session session);

    /**
     * Invoked in case upload succeeded.
     * <p>
     * All uploaded files were successfully processed.
     *
     * @param uploadId The identifier that uniquely identifies the upload
     * @param upload The successfully parsed upload
     * @param session The session associated with the upload or <code>null</code>
     */
    void onUploadSuceeded(String uploadId, Upload upload, Session session);
}
