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

package com.openexchange.file.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

/**
 * {@link FileHolder}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FileHolder {

    private final File file;

    private final byte[] content;

    public FileHolder(File file) {
        super();
        this.file = new DefaultFile(file);
        this.content = null;
    }

    public FileHolder(File file, byte[] content) {
        super();
        this.file = new DefaultFile(file);
        this.content = content;
    }

    @SuppressWarnings("deprecation")
    public FileHolder(File file, InputStream content) {
        super();
        this.file = new DefaultFile(file);
        byte[] tmp;
        try {
            tmp = IOUtils.toByteArray(content);
        } catch (@SuppressWarnings("unused") IOException e) {
            tmp = new byte[0];
        } finally {
            IOUtils.closeQuietly(content);
        }

        this.content = tmp;
    }

    public File getFile() {
        return new DefaultFile(file);
    }

    public File getInternalFile() {
        return file;
    }

    public InputStream getContent() {
        if (content == null) {
            return null;
        }

        return new ByteArrayInputStream(content);
    }

}
