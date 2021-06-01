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
import java.io.InputStream;


/**
 * {@link ImageTransformations}
 *
 * Allows chaining of multiple transformations to an image. Every transformation has an expense.
 * Expenses are summed up during processing and can be retrieved from a resulting {@link BasicTransformedImage}.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface ImageTransformations {

    /**
     * Applies to cheap transformations.
     */
    static final int LOW_EXPENSE = 1;

    /**
     * Applies to expensive transformations.
     */
    static final int HIGH_EXPENSE = 3;

    /**
     * Adds a 'rotate' transformation, leading to the image being rotated according to the contained EXIF information.
     *
     * @return A self reference
     */
    ImageTransformations rotate();

    /**
     * Adds a 'scale' transformation, leading to the image being scaled according to the supplied parameters.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Note: In case this transformation is supposed to be applied to properly auto-rotated image (according to possibly contained EXIF information),
     * the {@link #rotate()} transformation is required to be added to transformation chain as well
     * </div>
     * <p>
     *
     * @param maxWidth The maximum width of the target image
     * @param maxHeight The maximum height of the target image
     * @param scaleType The scale type to use
     * @return A self reference
     * @throws IllegalArgumentException If given maxWidth and/or maxHeight are not supported
     * @see Constants#getMaxWidth
     * @see Constants#getMaxHeight
     */
    ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType);

    /**
     * Adds a 'scale' transformation, leading to the image being scaled according to the supplied parameters.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Note: In case this transformation is supposed to be applied to properly auto-rotated image (according to possibly contained EXIF information),
     * the {@link #rotate()} transformation is required to be added to transformation chain as well
     * </div>
     * <p>
     *
     * @param maxWidth The maximum width of the target image
     * @param maxHeight The maximum height of the target image
     * @param scaleType The scale type to use
     * @param shrinkOnly <code>true</code> to only scale images 'greater than' target size, <code>false</code>, otherwise
     * @return A self reference
     * @throws IllegalArgumentException If given maxWidth and/or maxHeight are not supported
     * @see Constants#getMaxWidth
     * @see Constants#getMaxHeight
     */
    ImageTransformations scale(int maxWidth, int maxHeight, ScaleType scaleType, boolean shrinkOnly);

    /**
     * Adds a 'crop' transformation, leading to the image being cropped according to the supplied parameters.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Note: In case this transformation is supposed to be applied to properly auto-rotated image (according to possibly contained EXIF information),
     * the {@link #rotate()} transformation is required to be added to transformation chain as well
     * </div>
     * <p>
     *
     * @param x The X coordinate of the upper-left corner of the specified rectangular region
     * @param y The Y coordinate of the upper-left corner of the specified rectangular region
     * @param width The width of the specified rectangular region
     * @param height The height of the specified rectangular region
     * @return A self reference
     */
    ImageTransformations crop(int x, int y, int width, int height);

    /**
     * Adds a compression transformation, leading to the image being written out compressed depending on the chosen output format.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Note: In case this transformation is supposed to be applied to properly auto-rotated image (according to possibly contained EXIF information),
     * the {@link #rotate()} transformation is required to be added to transformation chain as well
     * </div>
     * <p>
     *
     * @return A self reference
     */
    ImageTransformations compress();

    /**
     * Applies all transformations and returns the result as image.
     *
     * @return The resulting image
     */
    BufferedImage getImage() throws IOException;

    /**
     * Applies all transformations and writes the result as raw image data in the given format.
     *
     * @return The resulting image data
     */
    byte[] getBytes(String formatName) throws IOException;

    /**
     * Applies all transformations and provides an input stream on the resulting raw image data in the given format.
     *
     * @param formatName The image format to use, e.g. <code>jpeg</code> or <code>png</code>
     * @return A new input stream carrying the resulting image data
     */
    InputStream getInputStream(String formatName) throws IOException;

    /**
     * Applies all transformations and writes the result as raw image data in the given format, including some meta information wrapped
     * into a transformed image reference.
     *
     * @return The resulting transformed image
     */
    BasicTransformedImage getTransformedImage(String formatName) throws IOException;

    /**
     * Applies all transformations and writes the result as raw image data in the given format, including some meta information wrapped
     * into a transformed image reference.
     *
     * @return The resulting transformed image
     */
    TransformedImage getFullTransformedImage(String formatName) throws IOException;

}
