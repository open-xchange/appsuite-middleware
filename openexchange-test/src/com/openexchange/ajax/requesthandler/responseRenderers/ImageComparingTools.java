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

package com.openexchange.ajax.requesthandler.responseRenderers;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * {@link ImageComparingTools}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.0
 */
public class ImageComparingTools {

    /**
     * Returns the mean value of the RGB histogram of an image
     * 
     * @param image the image
     * @return rounded average to second decimal place of RGB histogram
     */
    public static float meanHistogramRGBValue(BufferedImage image) {
        int[] hValue = new int[256];
        // fill zero matrix
        for (int i = 0; i < hValue.length; i++) {
            hValue[i] = 0;
        }
        // get colors from image
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int red = new Color(image.getRGB(i, j)).getRed();
                int green = new Color(image.getRGB(i, j)).getGreen();
                int blue = new Color(image.getRGB(i, j)).getBlue();
                hValue[red]++;
                hValue[green]++;
                hValue[blue]++;
            }
        }
        // aggregate frequency of values per color and calculates the average value
        float average = 0;
        for (int colorPos = 0; colorPos < hValue.length; colorPos++) {
            average += (hValue[colorPos] * colorPos) / 3.0f;
        }
        average = average / (image.getWidth() * image.getHeight());
        return Math.round(average * 100.0f) / 100.0f;
    }
}
