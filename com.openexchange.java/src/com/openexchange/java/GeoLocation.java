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

package com.openexchange.java;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link GeoLocation} - Represents an immutable latitude and longitude pair, giving a position on Earth in spherical coordinates.
 * <p>
 * Values of latitude and longitude are given in degrees.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class GeoLocation {

    private final double latitude;
    private final double longitude;
    private int hash;

    /**
     * Initializes a new {@link GeoLocation}.
     *
     * @param latitude The latitude of this geo location, in degrees
     * @param longitude The longitude of this geo location, in degrees
     */
    public GeoLocation(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        hash = 0;
    }

    /**
     * Gets the latitude of this geo location, in degrees
     *
     * @return The latitudinal angle of this location, in degrees
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude of this geo location, in degrees
     *
     * @return The longitudinal angle of this location, in degrees
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Checks if both latitude and longitude are equal to zero.
     *
     * @return <code>true</code> if both latitude and longitude are equal to zero; otherwise <code>false</code>
     */
    public boolean isZero() {
        return latitude == 0 && longitude == 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoLocation that = (GeoLocation) o;
        if (Double.compare(that.latitude, latitude) != 0) {
            return false;
        }
        if (Double.compare(that.longitude, longitude) != 0) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = this.hash;
        if (result == 0) {
            long temp;
            temp = latitude != +0.0d ? Double.doubleToLongBits(latitude) : 0L;
            result = (int) (temp ^ (temp >>> 32));
            temp = longitude != +0.0d ? Double.doubleToLongBits(longitude) : 0L;
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            this.hash = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(78).append('(').append(latitude).append(", ").append(longitude).append(')').toString();
    }

    /**
     * Gets a string representation of this location, of format:<br><code>-1&#x00b0; 23' 4.56", 54&#x00b0; 32' 1.92"</code>
     *
     * @return A string representation of this location, of format:<br><code>-1&#x00b0; 23' 4.56", 54&#x00b0; 32' 1.92"</code>
     */
    public String toDMSString() {
        return decimalToDegreesMinutesSecondsString(latitude) + ", " + decimalToDegreesMinutesSecondsString(longitude);
    }

    /**
     * Gets the SQL POINT notation for this location; e.g. <code>"POINT(28.093833333333333 -16.735833333333336)"</code>
     *
     * @return The SQL POINT notation
     */
    public String toSqlPoint() {
        return new StringBuilder(78).append("POINT(").append(latitude).append(' ').append(longitude).append(')').toString();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Parses an SQL POINT notation to a <code>GeoLocation</code> instance.
     *
     * @param point The SQL POINT notation to parse; e.g. <code>"POINT(28.093833333333333 -16.735833333333336)"</code>
     * @return The resulting <code>GeoLocation</code> instance or <code>null</code> if not parseable
     */
    public static GeoLocation parseSqlPoint(String point) {
        if (Strings.isEmpty(point)) {
            return null;
        }

        String toParse = point.trim();
        if (!toParse.startsWith("POINT(")) {
            return null;
        }

        toParse = toParse.substring(6);
        int pos = toParse.indexOf(' ');
        if (pos <= 0) {
            return null;
        }

        double latitude = Double.parseDouble(toParse.substring(0, pos));

        toParse = toParse.substring(pos + 1);
        pos = toParse.indexOf(')');
        if (pos <= 0) {
            return null;
        }

        double longitude = Double.parseDouble(toParse.substring(0, pos));
        return new GeoLocation(latitude, longitude);
    }

    /**
     * Converts a decimal degree angle into its corresponding DMS (degrees-minutes-seconds) representation as a string, of format:
     * {@code -1\u00b0 23' 4.56"}
     *
     * @param decimal The spherical coordinate in decimal degree
     * @return The DMS (degrees-minutes-seconds) representation
     */
    public static String decimalToDegreesMinutesSecondsString(double decimal) {
        double[] dms = decimalToDegreesMinutesSeconds(decimal);
        DecimalFormat format = new DecimalFormat("0.##");
        return String.format("%s\u00B0 %s' %s\"", format.format(dms[0]), format.format(dms[1]), format.format(dms[2]));
    }

    /**
     * Converts a decimal degree angle into its corresponding DMS (degrees-minutes-seconds) component values, as a double array.
     *
     * @param decimal The spherical coordinate in decimal degree
     * @return The DMS (degrees-minutes-seconds) representation as a double array:<br>
     * {@code [ <degrees>, <minutes>, <seconds> ]}
     */
    public static double[] decimalToDegreesMinutesSeconds(double decimal) {
        int d = (int) decimal;
        double m = Math.abs((decimal % 1) * 60);
        double s = (m % 1) * 60;
        return new double[] { d, (int) m, s };
    }

    /**
     * Converts DMS (degrees-minutes-seconds) rational values, into a single value in degrees, as a double.
     * <h3>
     * Decimal Degrees vs Degrees/Minutes/Seconds
     * </h3>
     * <p>
     * One way to write spherical coordinates (latitudes and longitudes) is using degrees-minutes-seconds (DMS). Minutes and seconds
     * range from 0 to 60. For example, the geographic coordinate expressed in degrees-minutes-seconds for New York City is:
     * <p>
     * <table>
     * <tr><td><strong>LATITUDE:</strong></td><td align=right>40 degrees, 42 minutes, 51 seconds N</td></tr>
     * <tr><td><strong>LONGITUDE:</strong></td><td align=right>74 degrees, 0 minutes, 21 seconds W</td></tr>
     * </table>
     * <p>
     * But you can also express geographic coordinates in decimal degrees. It's just another way to represent that same location in a
     * different format. For example, here is New York City in decimal degrees:
     * <p>
     * <table>
     * <tr><td><strong>LATITUDE:</strong></td><td align=right>40.714</td></tr>
     * <tr><td><strong>LONGITUDE:</strong></td><td align=right>-74.006</td></tr>
     * </table>
     *
     * @param degs The degrees
     * @param mins The minutes
     * @param secs The seconds
     * @param isNegative Whether DMS is negative
     * @return The decimal degree
     */
    public static Double degreesMinutesSecondsToDecimal(double degs, double mins, double secs, boolean isNegative) {
        double decimal = Math.abs(degs) + mins / 60.0d + secs / 3600.0d;
        if (Double.isNaN(decimal)) {
            return null;
        }

        if (isNegative) {
            decimal *= -1;
        }
        return Double.valueOf(decimal);
    }

    private static final Pattern geoPattern = Pattern.compile("[-]?(\\d*)\u00b0 (\\d*)' (\\d*(?:[,.]\\d*)?)\"");

    /**
     * Parses the DMS (degrees-minutes-seconds) representation of a spherical coordinate to a double.
     *
     * @param dmsString The spherical coordinate's DMS (degrees-minutes-seconds) representation
     * @return The decimal degree or <code>null</code>
     * @see #degreesMinutesSecondsToDecimal(double, double, double, boolean)
     */
    public static Double parseDMSStringToDouble(String dmsString) {
        if (Strings.isEmpty(dmsString)) {
            return null;
        }

        String dms = dmsString.trim();
        Matcher matcher = geoPattern.matcher(dms);
        if (!matcher.find()) {
            return null;
        }

        try {
            double degs = Double.parseDouble(matcher.group(1));
            double mins = Double.parseDouble(matcher.group(2));
            double secs = Double.parseDouble(matcher.group(3).replace(',', '.'));
            return degreesMinutesSecondsToDecimal(degs, mins, secs, dms.startsWith("-"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
