/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
