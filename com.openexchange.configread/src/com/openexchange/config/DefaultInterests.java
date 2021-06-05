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

package com.openexchange.config;

/**
 * {@link DefaultInterests} - The default implementation of {@link Interests}.
 * <p>
 * Example
 * <pre>
 *   Interests interests = DefaultInterests.builder()
 *      .propertiesOfInterest("c.o.modOne.*", "c.o.modTwo.attrFive")
 *      .configFileNames("myconf.yml")
 *      .build();
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultInterests implements Interests {

    /**
     * Creates a new builder instance
     *
     * @return The new builder instance
     */
    public static DefaultInterests.Builder builder() {
        return new DefaultInterests.Builder();
    }

    /** The builder for an instance of <code>DefaultInterests</code> */
    public static class Builder {

        private String[] propertiesOfInterest;
        private String[] configFileNames;

        /**
         * Initializes a new {@link DefaultInterests.Builder}.
         */
        Builder() {
            super();
            propertiesOfInterest = null;
            configFileNames = null;
        }

        /**
         * Sets the properties of interest
         *
         * @param propertiesOfInterest The properties of interest
         * @return This builder instance
         */
        public Builder propertiesOfInterest(String... propertiesOfInterest) {
            this.propertiesOfInterest = propertiesOfInterest;
            return this;
        }

        /**
         * Sets the names for files of interest.
         *
         * @param configFileNames The names for files of interest
         * @return THis builder instance
         */
        public Builder configFileNames(String... configFileNames) {
            this.configFileNames = configFileNames;
            return this;
        }

        /**
         * Builds a new instance of <code>DefaultInterests</code>.
         *
         * @return The <code>DefaultInterests</code> instance
         */
        public DefaultInterests build() {
            return new DefaultInterests(propertiesOfInterest, configFileNames);
        }
    }

    // ---------------------------------------------------------------------------------------

    private final String[] propertiesOfInterest;
    private final String[] configFileNames;

    DefaultInterests(String[] propertiesOfInterest, String[] configFileNames) {
        super();
        this.propertiesOfInterest = propertiesOfInterest;
        this.configFileNames = configFileNames;
    }

    @Override
    public String[] getPropertiesOfInterest() {
        return propertiesOfInterest;
    }

    @Override
    public String[] getConfigFileNames() {
        return configFileNames;
    }

}
