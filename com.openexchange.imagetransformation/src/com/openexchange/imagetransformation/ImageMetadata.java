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

/**
 * {@link ImageMetadata} - Provides queried image meta-data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ImageMetadata {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>ImageMetadata</code> */
    public static class Builder {

        private Dimension dimension;
        private String formatName;

        Builder() {
            super();
        }

        /**
         * Sets the dimension meta-data
         *
         * @param dimension The dimension to set
         * @return This builder
         */
        public Builder withDimension(Dimension dimension) {
            this.dimension = dimension;
            return this;
        }

        /**
         * Sets the format name meta-data
         *
         * @return This builder
         */
        public Builder withFormatName(String formatName) {
            this.formatName = formatName;
            return this;
        }

        /**
         * Builds a new meta-data instance from this builder's arguments.
         *
         * @return The new meta-data instance
         */
        public ImageMetadata build() {
            return new ImageMetadata(dimension, formatName);
        }
    }

    // ---------------------------------------------------------------------------------------

    private final Dimension dimension;
    private final String formatName;

    /**
     * Initializes a new {@link ImageMetadataOptions}.
     */
    ImageMetadata(Dimension dimension, String formatName) {
        super();
        this.dimension = dimension;
        this.formatName = formatName;
    }

    /**
     * Gets the dimension meta-data
     *
     * @return The dimension meta-data or <code>null</code>
     */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * Gets the format name meta-data
     *
     * @return The format name meta-data or <code>null</code>
     */
    public String getFormatName() {
        return formatName;
    }

}
