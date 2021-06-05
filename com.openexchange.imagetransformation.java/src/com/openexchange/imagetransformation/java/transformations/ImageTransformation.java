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

package com.openexchange.imagetransformation.java.transformations;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import com.openexchange.imagetransformation.ImageInformation;
import com.openexchange.imagetransformation.TransformationContext;

/**
 * {@link ImageTransformation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface ImageTransformation {

    /**
     * Performs the image transformation.
     *
     * @param sourceImage The source image
     * @param transformationContext The transformation context
     * @param imageInformation The additional image information, or <code>null</code> if not needed
     * @return The resulting image
     */
    BufferedImage perform(BufferedImage sourceImage, TransformationContext transformationContext, ImageInformation imageInformation) throws IOException;

    /**
     * Gets a value indicating whether the supplied image format is supported by the transformation or not.
     *
     * @param formatName The image format name, e.g. <code>jpeg</code> or <code>tiff</code>
     * @return <code>true</code>, if the format is supported, <code>false</code>, otherwise
     */
    boolean supports(String formatName);

    /**
     * Gets a value indicating whether the transformation needs additional image information or not.
     *
     * @return <code>true</code>, if additional information is required, <code>false</code>, otherwise
     */
    boolean needsImageInformation();

    /**
     * Gets the resolution that is required for this transformation to operate on, based on the input image's dimensions.
     *
     * @param originalResolution The dimension of the source image
     * @return The required resolution, or <code>null</code> if not relevant
     */
    Dimension getRequiredResolution(Dimension originalResolution);

}