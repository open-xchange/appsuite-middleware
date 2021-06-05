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

package com.openexchange.geolocation;

import com.openexchange.java.Strings;

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
        private String postalCode = "N/A";

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
            if (Strings.isNotEmpty(continent)) {
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
            if (Strings.isNotEmpty(country)) {
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
            if (Strings.isNotEmpty(city)) {
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
        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            if (Strings.isNotEmpty(postalCode)) {
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
    private final String postalCode;
    private final short bitmask;

    DefaultGeoInformation(String continent, String country, String city, String postalCode, short bitmask) {
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
    public String getPostalCode() {
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
