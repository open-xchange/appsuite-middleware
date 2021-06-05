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
import com.openexchange.ajax.fileholder.IFileHolder;

/**
 * {@link ImageTransformationProvider} - An image transformation provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ImageTransformationProvider {

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image considering calling {@link Thread} as source.
     * <p>
     * This is the same as calling <code>transfom(BufferedImage, Object)</code> with latter parameter set to <code>null</code>, thus calling
     * {@link Thread} is referenced as source.
     *
     * @param sourceImage The source image to use
     * @return A new {@link ImageTransformations} instance for the image
     * @throws IOException If an I/O error occurs
     * @see #transfom(BufferedImage, Object)
     */
    ImageTransformations transfom(BufferedImage sourceImage) throws IOException;

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image.
     *
     * @param sourceImage The source image to use
     * @param source The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     * @return A new {@link ImageTransformations} instance for the image
     * @throws IOException If an I/O error occurs
     */
    ImageTransformations transfom(BufferedImage sourceImage, Object source) throws IOException;

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image stream considering calling {@link Thread} as
     * source.
     * <p>
     * This is the same as calling <code>transfom(InputStream, Object)</code> with latter parameter set to <code>null</code>, thus calling
     * {@link Thread} is referenced as source.
     *
     * @param imageStream The source image stream to use
     * @return A new {@link ImageTransformations} instance for the stream
     * @throws IOException If an I/O error occurs
     * @see #transfom(InputStream, Object)
     */
    ImageTransformations transfom(InputStream imageStream) throws IOException;

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image stream.
     *
     * @param imageStream The source image stream to use
     * @param source The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     * @return A new {@link ImageTransformations} instance for the stream
     * @throws IOException If an I/O error occurs
     */
    ImageTransformations transfom(InputStream imageStream, Object source) throws IOException;

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image stream.
     *
     * @param imageFile The source image to use
     * @param source The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     * @return A new {@link ImageTransformations} instance for the stream
     * @throws IOException If an I/O error occurs
     */
    ImageTransformations transfom(IFileHolder imageFile, Object source) throws IOException;

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image data considering calling {@link Thread} as
     * source.
     * <p>
     * This is the same as calling <code>transfom(byte[], Object)</code> with latter parameter set to <code>null</code>, thus calling
     * {@link Thread} is referenced as source.
     *
     * @param sourceImage The source image data to use
     * @return A new {@link ImageTransformations} instance for the image
     * @throws IOException If an I/O error occurs
     * @see #transfom(byte[], Object)
     */
    ImageTransformations transfom(byte[] imageData) throws IOException;

    /**
     * Initializes a new {@link ImageTransformations} working on the supplied source image data.
     *
     * @param sourceImage The source image data to use
     * @param source The source for this invocation; if <code>null</code> calling {@link Thread} is referenced as source
     * @return A new {@link ImageTransformations} instance for the image
     * @throws IOException If an I/O error occurs
     */
    ImageTransformations transfom(byte[] imageData, Object source) throws IOException;

    // ----------------------------------------------------------------------------------------------------------- //

}
