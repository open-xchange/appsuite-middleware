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

package com.openexchange.imagetransformation;

import java.io.Closeable;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;

/**
 * {@link BasicTransformedImage} - A transformed image representation providing basic image information.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface BasicTransformedImage extends Closeable {

    /**
     * Gets the size of the image in bytes.
     *
     * @return The size
     */
    long getSize();

    /**
     * Gets the image format name.
     *
     * @return The width
     */
    String getFormatName();

    /**
     * Gets the image data.
     *
     * @return The image data
     * @throws OXException If image data cannot be returned
     */
    byte[] getImageData() throws OXException;

    /**
     * Gets the image data as a stream.
     *
     * @return The image data as a stream
     * @throws OXException If image stream cannot be returned
     */
    InputStream getImageStream() throws OXException;

    /**
     * Gets the image file.
     *
     * @return The image file or <code>null</code>
     */
    IFileHolder getImageFile();

    /**
     * Gets the sum of transformation expenses.
     * @see {@link ImageTransformations#LOW_EXPENSE} and {@link ImageTransformations#HIGH_EXPENSE}.
     * @return The expenses.
     */
    int getTransformationExpenses();

    /**
     * Closes this {@link BasicTransformedImage} instance and releases any system resources associated with it.
     */
    @Override
    void close();

}
