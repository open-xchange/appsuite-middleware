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

package com.openexchange.imageconverter.api;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * {@link IFileItemReadAccess}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
public interface IFileItemReadAccess extends Closeable {

    /**
     * Returns the {@link InputStream} to read content from.</br>
     * The returned {@link InputStream} instance is owned by the caller and
     * needs to be closed appropriately after final usage.
     *
     * @return The {@link InputStream} to read content from.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns the {@link File} to read content from.
     *
     * @return The {@link File} to read content from.
     * @throws IOException
     */
    File getInputFile() throws IOException;

    /**
     * @return The creation {@link Date} of the file as Gregorian calendar date
     */
    Date getCreateDate();

    /**
     * @return The last access {@link Date} of the file in as Gregorian calendar date
     */
    Date getModificationDate();

    /**
     * @return The length of the existing file item.
     */
    long getLength();

    /**
     * Returns the value of the FileItem's property with the
     * given key. The key aliases need to be registered once
     * via the {
     *
     * @param key The key to retrieve the value for
     * @return
     */
    String getKeyValue(final String key) throws FileItemException;
}
