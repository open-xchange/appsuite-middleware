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

package com.openexchange.contact.vcard;

import java.io.Closeable;
import java.io.InputStream;
import java.util.List;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;

/**
 * {@link VCardExport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public interface VCardExport extends Closeable {

    /**
     * Gets a list of conversion warnings.
     *
     * @return The warnings
     */
    List<OXException> getWarnings();

    /**
     * Gets a file holder storing the exported vCard
     *
     * @return The exported vCard, or <code>null</code> if not available
     */
    IFileHolder getVCard();

    /**
     * Gets the input stream carrying the vCard contents.
     * <p>
     * Closing the stream will also {@link #close() close} this {@link VCardExport} instance.
     *
     * @return The input stream
     */
    InputStream getClosingStream() throws OXException;

    /**
     * Gets the exported vCard as byte array.
     *
     * @return The vCard bytes
     */
    byte[] toByteArray() throws OXException;

}
