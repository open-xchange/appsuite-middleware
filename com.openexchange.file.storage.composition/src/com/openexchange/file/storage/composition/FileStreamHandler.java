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

package com.openexchange.file.storage.composition;

import java.io.InputStream;
import com.openexchange.exception.OXException;

/**
 * {@link FileStreamHandler} - Handles a requested document's {@link InputStream} and returns a possibly modified one.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FileStreamHandler {

    /**
     * Handles specified document's {@link InputStream}.
     * 
     * @param documentStream The document's {@link InputStream}
     * @param fileID The file identifier
     * @param version The version
     * @return The (possibly modified) stream
     * @throws OXException If handling stream fails
     */
    InputStream handleDocumentStream(InputStream documentStream, FileID fileID, String version, int context) throws OXException;

    /**
     * Gets this handler's ranking.
     * <p>
     * The default ranking is zero (<tt>0</tt>). A handler with a ranking of {@code Integer.MAX_VALUE} is very likely to be returned as the
     * default handler, whereas a handler with a ranking of {@code Integer.MIN_VALUE} is very unlikely to be returned.
     * 
     * @return The ranking
     */
    int getRanking();

}
