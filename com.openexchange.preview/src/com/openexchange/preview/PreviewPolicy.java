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

package com.openexchange.preview;


/**
 * A {@link PreviewPolicy} describes how a given mime type can be converted into
 * a {@link PreviewOutput}. A quality parameter of this conversion is also given.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class PreviewPolicy {

    private final String mimeType;

    private final PreviewOutput output;

    private final Quality quality;


    /**
     * Initializes a new {@link PreviewPolicy}.
     * @param mimeType
     * @param output
     * @param quality
     */
    public PreviewPolicy(final String mimeType, final PreviewOutput output, final Quality quality) {
        super();
        this.mimeType = mimeType;
        this.output = output;
        this.quality = quality;
    }

    /**
     * Gets the mimeType
     *
     * @return The mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Gets the output
     *
     * @return The output
     */
    public PreviewOutput getOutput() {
        return output;
    }

    /**
     * Gets the quality
     *
     * @return The quality
     */
    public Quality getQuality() {
        return quality;
    }
}
