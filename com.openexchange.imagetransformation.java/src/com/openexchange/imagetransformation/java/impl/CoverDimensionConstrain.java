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

import java.awt.Dimension;

/**
 * {@link CoverDimensionConstrain}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class CoverDimensionConstrain implements DimensionConstrain {

    private final int minWidth;

    private final int minHeight;

    public CoverDimensionConstrain(int minWidth, int minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
    }

    @Override
    public Dimension getDimension(Dimension dimension) {
        if (minWidth <= 0 && minHeight <= 0) {
            return dimension;
        }

        double scaleWidth = minWidth <= 0 ? 0.0 : minWidth / (double) dimension.width; // Calculate Width factor
        double scaleHeight = minHeight <= 0 ? 0.0 : minHeight / (double) dimension.height; // Calculate Height factor

        double scale = Math.max(scaleWidth, scaleHeight); // Choose largest boundary

        int dstWidth = (int) Math.round(dimension.width * scale);
        int dstHeight = (int) Math.round(dimension.height * scale);

        if (dstWidth < 3) {
            dstWidth = 3;
        }
        if (dstHeight < 3) {
            dstHeight = 3;
        }

        return new Dimension(dstWidth, dstHeight);
    }

}
