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

package com.openexchange.imagetransformation.java.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.imagetransformation.ImageTransformationIdler;
import com.openexchange.imagetransformation.ImageTransformationProvider;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.imagetransformation.java.scheduler.Scheduler;
import com.openexchange.imagetransformation.java.transformations.ImageTransformationsTask;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;

/**
 * {@link JavaImageTransformationProvider}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JavaImageTransformationProvider implements ImageTransformationProvider, ImageTransformationIdler {

    /**
     * Initializes a new {@link JavaImageTransformationProvider}.
     */
    public JavaImageTransformationProvider() {
        super();
    }

    @Override
    public ImageTransformations transfom(final BufferedImage sourceImage) {
        return transfom(sourceImage, null);
    }

    @Override
    public ImageTransformations transfom(final BufferedImage sourceImage, final Object source) {
        return new ImageTransformationsTask(sourceImage, source);
    }

    @Override
    public ImageTransformations transfom(final InputStream imageStream) throws IOException {
        return transfom(imageStream, null);
    }

    @Override
    public ImageTransformations transfom(final InputStream imageStream, final Object source) throws IOException {
        return new ImageTransformationsTask(imageStream, source);
    }

    @Override
    public ImageTransformations transfom(IFileHolder imageFile, Object source) throws IOException {
        return new ImageTransformationsTask(imageFile, source);
    }

    @Override
    public ImageTransformations transfom(final byte[] imageData) throws IOException {
        return transfom(imageData, null);
    }

    @Override
    public ImageTransformations transfom(final byte[] imageData, final Object source) throws IOException {
        return transfom(new UnsynchronizedByteArrayInputStream(imageData), source);
    }

    @Override
    public void idle() {
        Scheduler.shutDown();
    }

}
