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

package com.openexchange.mail.compose.mailstorage.cache;

import java.io.IOException;
import javax.mail.internet.SharedInputStream;
import com.openexchange.mail.mime.MimeCleanUp;

/**
 * {@link CacheReference} - A reference to a cached resource containing MIME content.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public interface CacheReference extends MimeCleanUp {

    /**
     * Checks whether this resource is still valid and refers to an existing locally
     * cached resource.
     *
     * @return <code>true</code> if the reference is valid; otherwise <code>false</code>
     */
    boolean isValid();

    /**
     * Gets a shared input stream to a cached MIME message. Callers are
     * supposed to close the stream after reading.
     *
     * @return The input stream
     * @throws IOException If resource is currently unavailable or opening a stream fails
     */
    SharedInputStream getMimeStream() throws IOException;

    /**
     * Gets the raw size of the cached message.
     *
     * @return The size in bytes or <code>-1</code> if unknown or non-existent
     */
    long getSize();

}
