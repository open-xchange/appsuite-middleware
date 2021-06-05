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

package com.openexchange.java;

import java.io.File;
import java.io.InputStream;

/**
 * {@link FileKnowingInputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class FileKnowingInputStream extends SizeKnowingInputStream {

    private final File file;

    /**
     * Initializes a new {@link FileKnowingInputStream}.
     *
     * @param in The input stream
     * @param file The (optional) file
     */
    public FileKnowingInputStream(InputStream in, File file) {
        super(in, null == file ? -1L : file.length());
        this.file = file;
    }

    /**
     * Gets the (optional) file reference.
     *
     * @return The file or <code>null</code>
     */
    public File getFile() {
        return file;
    }

}
