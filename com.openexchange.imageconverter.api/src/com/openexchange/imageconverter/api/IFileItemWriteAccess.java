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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link IFileItemWriteAccess}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
public interface IFileItemWriteAccess extends IFileItemReadAccess {

    /**
     * Returns the output stream to write content to.</br>
     * The returned {@link OutputStream} instance is owned by the caller and
     * needs to be closed appropriately after final usage.</br>
     * To avoid output data corruption, either this method <b>or</b>
     * the {@link getOutputFile} method be used for an opened write
     * access instance. Both methods must not be used for one instance!
     *
     * @return The {@link OutputStream} to write content to.
     * @throws IOException
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Returns the output file to write content to.</br>
     * To avoid output data corruption, either this method  <b>or</b>
     * the {@link getOutputFile} method be used for an opened write
     * write access instance. Both methods must not be used for one instance!
     *
     * @return The {@link File} to write content to.
     * @throws IOException
     */
    File getOutputFile() throws IOException;

    /**
     * @param key
     * @param value
     */
    void setKeyValue(final String key, final String value) throws FileItemException;
}
