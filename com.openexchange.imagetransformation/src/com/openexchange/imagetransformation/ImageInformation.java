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

/**
 * {@link ImageInformation} - Provides basic image information (orientation, width, and height).
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class ImageInformation {

    /** The image orientation */
    public final int orientation;

    /** The image width */
    public final int width;

    /** The image height */
    public final int height;

    /**
     * Initializes a new {@link ImageInformation}.
     *
     * @param orientation The image orientation
     * @param width The image width
     * @param height The image height
     */
    public ImageInformation(int orientation, int width, int height) {
        super();
        this.orientation = orientation;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(width).append('x').append(height).append(',').append(orientation).toString();
    }
}
