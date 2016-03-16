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
 *    trademarks of the OX Software GmbH group of companies.
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
