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

package com.openexchange.groupware.upload;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link StreamedUploadFile} - An upload file backed by a stream.
 * <p>
 * This instance is supposed to be directly handled.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface StreamedUploadFile extends BasicUploadFile {

    /**
     * Gets the {@link InputStream} for the uploaded file.
     *
     * @return The <tt>InputStream</tt> instance
     * @throws IOException If stream cannot be returned
     */
    StreamedUploadFileInputStream getStream() throws IOException;

}
