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
 * {@link ImageMetadataOptions} - The options for retrieving an image's meta-data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ImageMetadataOptions {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>ImageMetadataOptions</code> */
    public static class Builder {

        private boolean dimension;
        private boolean formatName;

        Builder() {
            super();
        }

        /**
         * Marks this builder that dimension meta-data is demanded
         *
         * @return This builder
         */
        public ImageMetadataOptions.Builder withDimension() {
            this.dimension = true;
            return this;
        }

        /**
         * Marks this builder that format name meta-data is demanded
         *
         * @return This builder
         */
        public ImageMetadataOptions.Builder withFormatName() {
            this.formatName = true;
            return this;
        }

        /**
         * Builds a new options instance from this builder's arguments.
         *
         * @return The new options instance
         */
        public ImageMetadataOptions build() {
            return new ImageMetadataOptions(dimension, formatName);
        }
    }

    // ---------------------------------------------------------------------------------------

    private final boolean dimension;
    private final boolean formatName;

    /**
     * Initializes a new {@link ImageMetadataOptions}.
     */
    ImageMetadataOptions(boolean dimension, boolean formatName) {
        super();
        this.dimension = dimension;
        this.formatName = formatName;
    }

    /**
     * Checks whether dimension meta-data is demanded
     *
     * @return <code>true</code> if demanded; otherwise <code>false</code>
     */
    public boolean isDimension() {
        return dimension;
    }

    /**
     * Checks whether format name meta-data is demanded
     *
     * @return <code>true</code> if demanded; otherwise <code>false</code>
     */
    public boolean isFormatName() {
        return formatName;
    }
}
