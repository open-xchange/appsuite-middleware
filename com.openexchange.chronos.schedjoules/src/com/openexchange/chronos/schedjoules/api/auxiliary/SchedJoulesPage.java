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

package com.openexchange.chronos.schedjoules.api.auxiliary;

import org.json.JSONValue;

/**
 * {@link SchedJoulesPage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesPage {

    private final JSONValue itemData;
    private final long lastModified;
    private final String etag;

    /**
     * Initialises a new {@link SchedJoulesPage}.
     */
    public SchedJoulesPage(JSONValue itemData, String etag, long lastModified) {
        super();
        this.itemData = itemData;
        this.etag = etag;
        this.lastModified = lastModified;
    }

    /**
     * Gets the itemData
     *
     * @return The itemData
     */
    public JSONValue getItemData() {
        return itemData;
    }

    /**
     * Gets the lastModified
     *
     * @return The lastModified
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Gets the eTag
     *
     * @return The eTag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * {@link SchedJoulesPageBuilder}
     */
    public static class SchedJoulesPageBuilder {

        private JSONValue itemData;
        private long lastModified;
        private String etag;

        /**
         * Initialises a new {@link SchedJoulesPage.SchedJoulesPageBuilder}.
         */
        public SchedJoulesPageBuilder() {
            super();
        }

        /**
         * Sets the itemData
         *
         * @param itemData The itemData to set
         */
        public SchedJoulesPageBuilder itemData(JSONValue itemData) {
            this.itemData = itemData;
            return this;
        }

        /**
         * Sets the lastModified
         *
         * @param lastModified The lastModified to set
         */
        public SchedJoulesPageBuilder lastModified(long lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        /**
         * Sets the etag
         *
         * @param etag The etag to set
         */
        public SchedJoulesPageBuilder etag(String etag) {
            this.etag = etag;
            return this;
        }

        public SchedJoulesPage build() {
            return new SchedJoulesPage(itemData, etag, lastModified);
        }

    }
}
