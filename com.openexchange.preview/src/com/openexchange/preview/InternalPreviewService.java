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
import com.openexchange.conversion.Data;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link InternalPreviewService} - Extends the {@link PreviewService} to specify what MIME types can be handled in what quality.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface InternalPreviewService extends PreviewService {

    /**
     * Gets the cached preview document for specified data and output format, if available at all.
     *
     * @param documentData The data
     * @param output The output format
     * @param session The session
     * @param pages The number of pages to be generated, if possible. If not set, this argument is ignored. -1 for "all pages"
     * @return The preview document with its content set according to given output format or null, if no cached preview is available
     * @throws OXException If preview document cannot be generated
     */
    PreviewDocument getCachedPreviewFor(Data<InputStream> documentData, PreviewOutput output, Session session, int pages) throws OXException;

    /**
     * Gets the preview policies of this <tt>PreviewService</tt>.
     *
     * @return The preview policies
     */
    List<PreviewPolicy> getPreviewPolicies();

    /**
     * Checks if this service is able to detect the content type of an input stream.
     *
     * @return <code>true</code> this service is able to detect the content type of an input stream; otherwise <code>false</code>
     */
    boolean canDetectContentType();

    /**
     * Checks if this <tt>PreviewService</tt> is supported for session-associated user
     *
     * @param session The session to check by
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isSupportedFor(Session session) throws OXException;
}
