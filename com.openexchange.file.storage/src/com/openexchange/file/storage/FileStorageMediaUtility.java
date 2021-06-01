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

package com.openexchange.file.storage;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * {@link FileStorageMediaUtility} - Utility methods for media information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class FileStorageMediaUtility {

    /**
     * Initializes a new {@link FileStorageMediaUtility}.
     */
    private FileStorageMediaUtility() {
        super();
    }

    /**
     * Gets the description for a given camera focal length in millimeter; e.g. <code>"38mm"</code>.
     *
     * @param focalLength The focal length in millimeter
     * @param locale The locale to use
     * @return The focal length description
     */
    public static String getCameraFocalLengthDescription(double focalLength, Locale locale) {
        Locale loc = null == locale ? Locale.US : locale;

        DecimalFormat format = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(loc));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(focalLength) + "mm";
    }

    private static final double ROOT_TWO = Math.sqrt(2);

    /**
     * Gets the description for a given camera lens aperture; e.g. <code>"f/1,8"</code>.
     *
     * @param aperture The camera lens aperture
     * @param locale The locale to use
     * @return The lens aperture description
     */
    public static String getCameraApertureDescription(double aperture, Locale locale) {
        Locale loc = null == locale ? Locale.US : locale;

        double fStop = Math.pow(ROOT_TWO, aperture);

        DecimalFormat format = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(loc));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return "f/" + format.format(fStop);
    }

    /**
     * Gets the description for a given camera exposure time; e.g. <code>"1/4s"</code>.
     *
     * @param exposureTime The exposure time as APEX value
     * @param locale The locale to use
     * @return The exposure time description
     */
    public static String getCameraExposureTimeDescription(double exposureTime, Locale locale) {
        Locale loc = null == locale ? Locale.US : locale;

        if (exposureTime <= 1) {
            float apexPower = (float)(1 / (Math.exp(exposureTime * Math.log(2))));
            long apexPower10 = Math.round(apexPower * 10.0);
            float fApexPower = apexPower10 / 10.0f;
            DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(loc));
            format.setRoundingMode(RoundingMode.HALF_UP);
            return format.format(fApexPower) + "s";
        }

        int apexPower = (int)((Math.exp(exposureTime * Math.log(2))));
        return "1/" + apexPower + "s";
    }

}
