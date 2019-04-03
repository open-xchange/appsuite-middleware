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
