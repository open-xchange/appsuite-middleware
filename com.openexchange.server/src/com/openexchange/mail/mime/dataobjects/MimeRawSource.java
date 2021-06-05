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

package com.openexchange.mail.mime.dataobjects;

import java.io.InputStream;
import javax.mail.Part;
import com.openexchange.exception.OXException;

/**
 * {@link MimeRawSource} - Provides access to raw data with any Content-Transfer-Encoding intact.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MimeRawSource {

    /**
     * Gets an {@link InputStream} to the raw data with any Content-Transfer-Encoding intact. This method is useful if the
     * "Content-Transfer-Encoding" header is incorrect or corrupt. In such a case the application may use this method and attempt to decode
     * the raw data itself.
     *
     * @return The raw input stream
     * @throws OXException If an error occurs
     */
    InputStream getRawInputStream() throws OXException;

    /**
     * Gets the {@link Part part}.
     *
     * @return The {@link Part part} or <code>null</code>
     */
    Part getPart();

}
