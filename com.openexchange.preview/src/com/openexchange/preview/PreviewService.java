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
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link PreviewService} - The preview service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface PreviewService {

    /**
     * Detects the MIME type of passed input stream's data.
     *
     * @param inputStream The input stream providing the data
     * @return The detected MIME type
     * @throws OXException If an error occurs
     */
    String detectDocumentType(InputStream inputStream) throws OXException;

    /**
     * Gets the preview document for specified argument and output format.
     *
     * @param arg The argument either denotes an URL or a file
     * @param output The output format
     * @param session The session
     * @param pages The number of pages to be generated, if possible. If not set, this argument is ignored. -1 for "all pages"
     * @return The preview document with its content set according to given output format
     * @throws OXException If preview document cannot be generated
     */
    PreviewDocument getPreviewFor(String arg, PreviewOutput output, Session session, int pages) throws OXException;

    /**
     * Gets the preview document for specified s data and output format.
     *
     * @param documentData The data
     * @param output The output format
     * @param session The session
     * @param pages The number of pages to be generated, if possible. If not set, this argument is ignored. -1 for "all pages"
     * @return The preview document with its content set according to given output format
     * @throws OXException If preview document cannot be generated
     */
    PreviewDocument getPreviewFor(Data<InputStream> documentData, PreviewOutput output, Session session, int pages) throws OXException;

}
