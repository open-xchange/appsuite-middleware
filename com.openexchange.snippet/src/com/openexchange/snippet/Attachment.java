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

package com.openexchange.snippet;

import java.io.IOException;
import java.io.InputStream;


/**
 * {@link Attachment} - Represents a file attachment for a snippet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Attachment {

    /**
     * Gets the attachment identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the content type according to RFC 822; e.g. <code>"text/plain; charset=UTF-8; name=mytext.txt"</code>
     *
     * @return The content type or <code>null</code>
     */
    String getContentType();

    /**
     * Gets the content disposition according to RFC 822; e.g. <code>"attachment; filename=mytext.txt"</code>
     *
     * @return The content disposition or <code>null</code>
     */
    String getContentDisposition();

    /**
     * Gets the <code>Content-Id</code> according to RFC 822.
     *
     * @return The <code>Content-Id</code> value or <code>null</code>
     */
    String getContentId();

    /**
     * Gets the attachment's size if known.
     *
     * @return The size or <code>-1</code> if unknown
     */
    long getSize();

    /**
     * Gets the input stream.
     *
     * @return The input stream
     * @throws IOException If an I/O error occurs
     */
    InputStream getInputStream() throws IOException;
}
