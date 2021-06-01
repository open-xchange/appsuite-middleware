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

package com.openexchange.textxtraction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.tika.io.TikaInputStream;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link AbstractTextXtractService} - The abstract {@link TextXtractService} class providing default implementation for
 * {@link #extractFrom(String, String)} and {@link #extractFromResource(String, String)}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractTextXtractService implements TextXtractService {

    /**
     * The ISO-8859-1 character set.
     */
    protected static final Charset CHARSET_ISO_8859_1 = Charsets.ISO_8859_1;

    /**
     * Initializes a new {@link AbstractTextXtractService}.
     */
    protected AbstractTextXtractService() {
        super();
    }

    @Override
    public String extractFrom(final String content, final String optMimeType) throws OXException {
        return extractFrom(Streams.newByteArrayInputStream(content.getBytes(CHARSET_ISO_8859_1)), optMimeType);
    }

    @Override
    public String extractFromResource(final String resource, final String optMimeType) throws OXException {
        final File file = new File(resource);
        InputStream input = null;
        try {
            /*
             * Generate an InputStream that supports mark()/reset()
             */
            if (file.isFile()) {
                input = Streams.bufferedInputStreamFor(new FileInputStream(file));
            } else {
                input = TikaInputStream.get(new URL(resource));
            }
            return extractFrom(input, optMimeType);
        } catch (IOException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(input);
        }
    }

}
