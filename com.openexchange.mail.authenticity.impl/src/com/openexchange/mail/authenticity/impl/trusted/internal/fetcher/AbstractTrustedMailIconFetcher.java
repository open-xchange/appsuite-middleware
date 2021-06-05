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

package com.openexchange.mail.authenticity.impl.trusted.internal.fetcher;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * {@link AbstractTrustedMailIconFetcher}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
class AbstractTrustedMailIconFetcher {

    private static final String FORMAT = "png";

    /**
     * Initialises a new {@link AbstractTrustedMailIconFetcher}.
     */
    public AbstractTrustedMailIconFetcher() {
        super();
    }

    /**
     * Processes the specified {@link BufferedImage} as a PNG image and
     * returns the byte array
     * 
     * @param image The {@link BufferedImage} to process
     * @return The byte array
     * @throws IOException if the {@link BufferedImage} is <code>null</code>
     */
    byte[] process(BufferedImage image) throws IOException {
        if (image == null) {
            throw new IOException("No image found");
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, FORMAT, stream);
        return stream.toByteArray();
    }
}
