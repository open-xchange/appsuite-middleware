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

import com.openexchange.java.Strings;

/**
 * {@link ScaleType}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public enum ScaleType {

    /**
     * The "cover" scale type, specifying the minimum target dimensions. The source image will be resized in a way that the resulting
     * image covers the target resolution entirely, with the original aspect ratio being preserved.
     * <p/>
     * For example, scaling an image with an original resolution of 640x480 pixels to 200x200 pixels and type "cover", will result in the
     * picture being resized to 267x200 pixels.
     */
    COVER("cover"),

    /**
     * The "contain" scale type, specifying the maximum target dimensions. The source image will be resized in a way that the resulting
     * image fits into the target resolution entirely, with the original aspect ratio being preserved.
     * <p/>
     * For example, scaling an image with an original resolution of 640x480 pixels to 200x200 pixels and type "contain", will result in the
     * picture being resized to 200x150 pixels.
     */
    CONTAIN("contain"),

    /**
     * The "containForceDimension" scale type, specifying the maximum target dimensions. The source image will be resized in a way that the resulting
     * image fits into the target resolution entirely, with the original aspect ratio being preserved while smaller sides get padded to fit exact dimension.
     * <p/>
     * For example, scaling an image with an original resolution of 640x480 pixels to 200x200 pixels and type "contain", will result in the
     * picture being first resized to 200x150 pixels, then height gets padded by 25 pixels per side resulting in exactly 200x200 pixels.
     */
    CONTAIN_FORCE_DIMENSION("containforcedimension"),


    /**
     * The "cover" scale type, specifying the target dimensions. If the source image is bigger than the target dimension, in a first step the image will be resized in a way that the resulting
     * image covers the target resolution entirely, with the original aspect ratio being preserved. In a second step the image will be cropped to fit the target dimension.
     *
     * <p/>
     * For example, scaling an image with an original resolution of 640x480 pixels to 200x200 pixels and type "coverandcrop", will result in the
     * picture being resized to 267x200 pixels and then cropped to fit 200x200.
     *
     * <p/>
     * In case the image is smaller than then target dimension the image will not be resized and instead it gets padded to fit exact dimension.
     *
     * <p/>
     * For example, with an original resolution of 100x100 pixels and a target dimension of 200x200 pixels and type "coverandcrop", will result in the
     * picture being padded on all sides with 50 pixels.
     */
    COVER_AND_CROP("coverandcrop"),

    /**
     * The "auto" scale type
     */
    AUTO("auto");

    private final String keyword;

    private ScaleType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Gets the keyword
     *
     * @return The keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Gets the scale type for given keyword.
     *
     * @param keyword The keyword
     * @return The associated scale type or {@link ScaleType#AUTO AUTO}
     */
    public static ScaleType getType(String keyword) {
        if (keyword == null) {
            return AUTO;
        }

        String toLookUp = Strings.asciiLowerCase(keyword.trim());
        for (ScaleType scaleType : ScaleType.values()) {
            if (toLookUp.equals(scaleType.keyword)) {
                return scaleType;
            }
        }
        return AUTO;
    }
}
