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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.geolocation;

/**
 * {@link DefaultGeoInformation} - The default (immutable) implementation for {@link GeoInformation}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.4
 */
public class DefaultGeoInformation implements GeoInformation {

    /**
     * Creates a new builder
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builds a new instance of {@link DefaultGeoInformation} */
    public static class Builder {

        private String continent = "N/A";
        private String country = "N/A";
        private String city = "N/A";
        private int postalCode;

        /**
         * 0: postal // 00001 1
         * 1: city // 00010 2
         * 2: country // 00100 4
         * 3: continent // 01000 8
         */
        private short bitmask;

        /**
         * Initializes a new {@link DefaultGeoInformation.Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the continent
         *
         * @param continent The continent to set
         * @return This builder
         */
        public Builder continent(String continent) {
            this.continent = continent;
            if (continent != null) {
                bitmask = (short) (bitmask | 8);
            }
            return this;
        }

        /**
         * Sets the country
         *
         * @param country The country to set
         * @return This builder
         */
        public Builder country(String country) {
            this.country = country;
            if (country != null) {
                bitmask = (short) (bitmask | 4);
            }
            return this;
        }

        /**
         * Sets the city
         *
         * @param city The city to set
         * @return This builder
         */
        public Builder city(String city) {
            this.city = city;
            if (city != null) {
                bitmask = (short) (bitmask | 2);
            }
            return this;
        }

        /**
         * Sets the postal code
         *
         * @param postalCode The postalCode to set
         * @return This builder
         */
        public Builder postalCode(int postalCode) {
            this.postalCode = postalCode;
            if (postalCode > 0) {
                bitmask = (short) (bitmask | 1);
            }
            return this;
        }

        /**
         * Creates the new (immutable) {@link DefaultGeoInformation} instance from this builder's attributes
         *
         * @return The new {@link DefaultGeoInformation} instance
         */
        public DefaultGeoInformation build() {
            return new DefaultGeoInformation(continent, country, city, postalCode, bitmask);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------

    private final String continent;
    private final String country;
    private final String city;
    private final int postalCode;
    private final short bitmask;

    DefaultGeoInformation(String continent, String country, String city, int postalCode, short bitmask) {
        super();
        this.continent = continent;
        this.country = country;
        this.city = city;
        this.postalCode = postalCode;
        this.bitmask = bitmask;
    }

    @Override
    public String getContinent() {
        return continent;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public int getPostalCode() {
        return postalCode;
    }

    @Override
    public boolean hasContinent() {
        return ((bitmask & 8) > 0);
    }

    @Override
    public boolean hasCountry() {
        return ((bitmask & 4) > 0);
    }

    @Override
    public boolean hasCity() {
        return ((bitmask & 2) > 0);
    }

    @Override
    public boolean hasPostalCode() {
        return ((bitmask & 1) > 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("{");
        if (continent != null) {
            sb.append("continent=").append(continent).append(", ");
        }
        if (country != null) {
            sb.append("country=").append(country).append(", ");
        }
        if (city != null) {
            sb.append("city=").append(city).append(", ");
        }
        sb.append("postalCode=").append(postalCode).append("}");
        return sb.toString();
    }

}
