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
import com.openexchange.conversion.Data;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link RemoteInternalPreviewService} - An interface for preview services that schedule a remote job.
 * <p>
 * If an <tt>InternalPreviewService</tt> implements this interface, the caller attempts to delegate retrieval of a preview document to a
 * separate thread to mitigate any unresponsive service behavior.
 * <p>
 * However, the implementing class can control that behavior through {@link #getTimeToWaitMillis()} return value; a value of less than or
 * equal to 0 (zero) implies that {@link Thread#currentThread() current thread} is supposed to be used for preview document retrieval.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface RemoteInternalPreviewService extends InternalPreviewService {

    /**
     * Gets the time to wait in milliseconds.
     * <p>
     * A value of less than or equal to 0 (zero) implies that current thread is supposed to be used for preview document retrieval.
     *
     * @return The time to wait in milliseconds or <code>-1L</code>
     */
    long getTimeToWaitMillis();

    /**
     * Asynchonously triggers the creation of a preview document for the specified arguments and output format.
     * The implementation has to trigger the creation for the given parameters and should immediately
     * return. Future calls to {@link PreviewService#getPreviewFor(String, PreviewOutput, Session, int)}
     * should then return an already cached result, if possible.
     *
     * @param arg The argument either denotes an URL or a file
     * @param output The output format
     * @param session The session
     * @param pages The number of pages to be generated, if possible. If not, this argument is ignored. -1 for "all pages"
     * @throws OXException If preview document cannot be generated
     */
    void triggerGetPreviewFor(String arg, PreviewOutput output, Session session, int pages) throws OXException;

    /**
     * Asynchonously triggers the creation of a preview document for the specified arguments and output format.
     * The implementation has to trigger the creation for the given parameters and should immediately
     * return. Future calls to {@link PreviewService#getPreviewFor(Data<InputStream> documentData, PreviewOutput output, Session session, int pages)}
     * should then return an already cached result, if possible.
     *
     * @param documentData The data
     * @param output The output format
     * @param session The session
     * @param pages The number of pages to be generated, if possible. If not, this argument is ignored. -1 for "all pages"
     * @throws OXException If preview document cannot be generated
     */
    void triggerGetPreviewFor(Data<InputStream> documentData, PreviewOutput output, Session session, int pages) throws OXException;
}
