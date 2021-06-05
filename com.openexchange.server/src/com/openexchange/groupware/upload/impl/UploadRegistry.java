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

package com.openexchange.groupware.upload.impl;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * An interface that defines a method to register instances of <code>com.openexchange.groupware.upload.UploadListener</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface UploadRegistry {

    /**
     * Fires the upload event by delegating this event to all registered listeners. Finally the <code>UploadEvent.cleanUp()</code> method is
     * invoked to delete temporary files from disk.
     *
     * @param uploadEvent The upload event
     * @param uploadListeners The upload listeners for current upload event
     * @throws OXException If an error like over quota occurs
     */
    void fireUploadEvent(UploadEvent uploadEvent, Collection<UploadListener> uploadListeners) throws OXException;

    /**
     * Create an <code>UpdateEvent</code> object from incoming multipart form data
     *
     * @param req The corresponding instance of <code>HttpServletRequest</code>
     * @return An <code>UpdateEvent</code> object from incoming multipart form data
     * @throws OXException If an error like over quota occurs
     * @deprecated Use {@link #processUpload(HttpServletRequest, long, long, Session)}
     */
    @Deprecated
    UploadEvent processUpload(HttpServletRequest req) throws OXException;

    /**
     * Create an <code>UpdateEvent</code> object from incoming multipart form data
     *
     * @param req The corresponding instance of <code>HttpServletRequest</code>
     * @param maxFileSize The maximum allowed size of a single uploaded file or <code>-1</code>
     * @param maxOverallSize The maximum allowed size of a complete request or <code>-1</code>
     * @param session The associated session or <code>null</code>
     * @return An <code>UpdateEvent</code> object from incoming multipart form data
     * @throws OXException If an error like over quota occurs
     */
    UploadEvent processUpload(HttpServletRequest req, long maxFileSize, long maxOverallSize, Session session) throws OXException;
}
