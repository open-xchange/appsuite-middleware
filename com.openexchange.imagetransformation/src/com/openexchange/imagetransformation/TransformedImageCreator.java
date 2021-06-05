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

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * {@link TransformedImageCreator} - Creates a {@link BasicTransformedImage} from a {@link BufferedImage} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface TransformedImageCreator {

    /**
     * Writes out an image into a byte-array and wraps it into a transformed image.
     *
     * @param image The image to write
     * @param formatName The format to use, e.g. "jpeg" or "tiff"
     * @param transformationContext The transformation context
     * @param needsCompression Whether compression is needed
     * @return The image data
     * @throws IOException If an I/O error occurs
     */
    TransformedImage writeTransformedImage(BufferedImage image, String formatName, TransformationContext transformationContext, boolean needsCompression) throws IOException;
}
