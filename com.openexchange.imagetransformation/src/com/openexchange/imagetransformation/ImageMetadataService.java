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

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ImageMetadataService} - A service for providing several meta-data information for an image.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
@SingletonService
public interface ImageMetadataService {

    /**
     * Gets the {@link Dimension dimension} for specified image data.
     *
     * @param imageStream The image data
     * @param mimeType The image MIME type
     * @param name The image name
     * @return The dimension
     * @throws IOException If dimension cannot be returned
     */
    Dimension getDimensionFor(InputStream imageStream, String mimeType, String name) throws IOException;

    /**
     * Gets the meta-data for specified image data.
     *
     * @param imageStream The image data
     * @param mimeType The image MIME type
     * @param name The image name
     * @param options The options to consider when retrieving image's meta-data
     * @return The meta-data
     * @throws IOException If meta-data cannot be returned
     */
    ImageMetadata getMetadataFor(InputStream imageStream, String mimeType, String name, ImageMetadataOptions options) throws IOException;

}
