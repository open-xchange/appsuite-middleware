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

package com.openexchange.tools.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.imagetransformation.ImageTransformationProvider;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.imagetransformation.ImageTransformations;

/**
 * {@link WrappingImageTransformationService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class WrappingImageTransformationService implements ImageTransformationService {

    private final ImageTransformationProvider provider;

    /**
     * Initializes a new {@link WrappingImageTransformationService}.
     */
    public WrappingImageTransformationService(ImageTransformationProvider provider) {
        super();
        this.provider = provider;
    }

    @Override
    public ImageTransformations transfom(BufferedImage sourceImage) throws IOException {
        return provider.transfom(sourceImage);
    }

    @Override
    public ImageTransformations transfom(BufferedImage sourceImage, Object source) throws IOException {
        return provider.transfom(sourceImage, source);
    }

    @Override
    public ImageTransformations transfom(InputStream imageStream) throws IOException {
        return provider.transfom(imageStream);
    }

    @Override
    public ImageTransformations transfom(InputStream imageStream, Object source) throws IOException {
        return provider.transfom(imageStream, source);
    }

    @Override
    public ImageTransformations transfom(IFileHolder imageFile, Object source) throws IOException {
        return provider.transfom(imageFile, source);
    }

    @Override
    public ImageTransformations transfom(byte[] imageData) throws IOException {
        return provider.transfom(imageData);
    }

    @Override
    public ImageTransformations transfom(byte[] imageData, Object source) throws IOException {
        return provider.transfom(imageData, source);
    }

}
