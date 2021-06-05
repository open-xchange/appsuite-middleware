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

package com.openexchange.ajax.zip;

import java.io.IOException;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.java.IOs;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ZipEntryAdder} - Responsible for adding appropriate ZIP entries to ZIP archive's output stream
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
@FunctionalInterface
public interface ZipEntryAdder {

    /**
     * Adds ZIP entries to ZIP archive's output stream
     *
     * @param zipOutputProvider The provider for the ZIP archive's output stream
     * @param buffer The buffer to use
     * @param fileNamesInArchive A map for managing already used file names
     * @throws OXException If adding ZIP entries fails
     */
    void addZipEntries(ZipArchiveOutputStreamProvider zipOutputProvider, Buffer buffer, Map<String, Integer> fileNamesInArchive) throws OXException;

    /**
     * Handles given {@code IOException} instance and returns an appropriate {@code OXException} for it.
     *
     * @param ioe The I/O exception to handle
     * @return The resulting {@code OXException} instance
     */
    default OXException handleIOException(IOException ioe) {
        OXException oxe = AjaxExceptionCodes.IO_ERROR.create(ioe, ioe.getMessage());
        if (IOs.isConnectionReset(ioe)) {
            oxe.markLightWeight();
        }
        return oxe;
    }

}
