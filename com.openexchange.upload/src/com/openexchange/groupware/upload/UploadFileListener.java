/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
