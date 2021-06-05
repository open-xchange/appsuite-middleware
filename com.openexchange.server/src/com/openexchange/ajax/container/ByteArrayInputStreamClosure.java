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

package com.openexchange.ajax.container;

import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder.InputStreamClosure;
import com.openexchange.java.Streams;

/**
 * {@link ByteArrayInputStreamClosure} - The byte array {@link InputStreamClosure}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ByteArrayInputStreamClosure implements InputStreamClosure {

    /** The byte array */
    final byte[] bytes;

    /**
     * Initializes a new {@link ByteArrayInputStreamClosure}.
     */
    public ByteArrayInputStreamClosure(final byte[] bytes) {
        super();
        this.bytes = bytes;
    }

    @Override
    public InputStream newStream() {
        return Streams.newByteArrayInputStream(bytes);
    }

}
