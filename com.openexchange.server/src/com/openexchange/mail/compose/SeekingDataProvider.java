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

package com.openexchange.mail.compose;

import java.io.InputStream;
import com.openexchange.exception.OXException;

/**
 * {@link SeekingDataProvider} - Provider for (a part of) an attachment's binary stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface SeekingDataProvider extends DataProvider {

    /**
     * Gets (a part of) the attachment's binary data
     *
     * @param offset The requested start offset of the file stream in bytes
     * @param length The requested length in bytes, starting from the offset
     * @return The binary data
     * @throws OXException If binary data cannot be returned
     */
    InputStream getData(long offset, long length) throws OXException;
}
