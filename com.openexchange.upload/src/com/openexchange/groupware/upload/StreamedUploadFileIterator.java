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

import com.openexchange.exception.OXException;

/**
 * {@link StreamedUploadFileIterator} - An iterator over streamed upload files.
 * <p>
 * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
 * In contrast to other iterators, the returned <code>StreamedUploadFile</code> instances are supposed to be handled directly
 * since advancing to the next instance obsoletes the previously obtained one.
 * <p>
 * Especially the returned {@link StreamedUploadFile#getStream() input stream} becomes unusable.
 * </div>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public interface StreamedUploadFileIterator {

    /**
     * Returns {@code true} if the iteration has more upload files.
     *
     * @return {@code true} if the iteration has more upload files
     * @throws OXException if the next upload file cannot be checked
     */
    boolean hasNext() throws OXException;

    /**
     * Returns the next upload file in the iteration.
     *
     * @return The next upload file in the iteration
     * @throws OXException if the next upload file cannot be returned
     */
    StreamedUploadFile next() throws OXException;

    /**
     * Returns the total size of all contained upload files. The value might
     * include encoding and envelope data overhead, if it could not be determined
     * exactly upfront.
     *
     * @return The size or <code>-1</code> if could not be determined
     */
    long getRawTotalBytes();

}
