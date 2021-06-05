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

package com.openexchange.i18n.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import com.openexchange.java.Streams;

/**
 * Template that reads the template content from a file.
 */
public class FileTemplate extends CompiledLineParserTemplate {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CompiledLineParserTemplate.class);

    private final File file;

    private final Charset charset;

    /**
     * Default constructor.
     *
     * @param f from this file the template is read.
     * @param charset this charset is used to have proper special characters.
     */
    public FileTemplate(final File f, final Charset charset) {
        super();
        this.file = f;
        this.charset = charset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized String getContent() {
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), charset);
            final StringBuilder collect = new StringBuilder();
            char[] buf = new char[512];
            int length = -1;
            while ((length = reader.read(buf)) > 0) {
                collect.append(buf, 0, length);
            }
            return collect.toString();
        } catch (IOException e) {
            LOG.error("", e);
            return e.toString();
        } finally {
            Streams.close(reader);
        }
    }
}
