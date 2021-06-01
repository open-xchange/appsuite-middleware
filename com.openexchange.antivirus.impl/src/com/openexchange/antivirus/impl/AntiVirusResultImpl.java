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

package com.openexchange.antivirus.impl;

import com.openexchange.antivirus.AntiVirusResult;
import com.openexchange.exception.OXException;

/**
 * {@link AntiVirusResultImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class AntiVirusResultImpl implements AntiVirusResult {

    private static final long serialVersionUID = -5834482033190623039L;

    private final String isTag;
    private final String antiVirusServiceId;
    private final Boolean infected;
    private final OXException error;
    private final String threatName;
    private final long scanTimestamp;
    private final boolean scanned;

    /**
     * Initializes a new {@link AntiVirusResultImpl}.
     */
    AntiVirusResultImpl(String isTag, String antiVirusServiceId, Boolean infected, OXException error, String threatName, long scanTimestamp, boolean scanned) {
        super();
        this.isTag = isTag;
        this.antiVirusServiceId = antiVirusServiceId;
        this.infected = infected;
        this.error = error;
        this.threatName = threatName;
        this.scanTimestamp = scanTimestamp;
        this.scanned = scanned;
    }

    @Override
    public String getAntiVirusServiceId() {
        return antiVirusServiceId;
    }

    @Override
    public String getISTag() {
        return isTag;
    }

    @Override
    public Boolean isInfected() {
        return infected;
    }

    @Override
    public OXException getError() {
        return error;
    }

    @Override
    public String getThreatName() {
        return threatName;
    }

    @Override
    public long getScanTimestamp() {
        return scanTimestamp;
    }

    @Override
    public boolean isStreamScanned() {
        return scanned;
    }


    ////////////////////////////////// BUILDER ///////////////////////////////////

    /**
     * Creates a new builder instance
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>AntiVirusResultImpl</code> */
    public static final class Builder {

        private String isTag;
        private String antiVirusServiceId;
        private Boolean infected;
        private OXException error;
        private String threatName;
        private long scanTimestamp;
        private boolean scanned;

        /**
         * Initialises a new {@link AntiVirusResultImpl.Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Sets the isTag
         *
         * @param isTag The isTag to set
         * @return this Builder instance for chained calls
         */
        public Builder withIsTag(String isTag) {
            this.isTag = isTag;
            return this;
        }

        /**
         * Sets the antiVirusServiceId
         *
         * @param antiVirusServiceId The antiVirusServiceId to set
         * @return this Builder instance for chained calls
         */
        public Builder withAntiVirusServiceId(String antiVirusServiceId) {
            this.antiVirusServiceId = antiVirusServiceId;
            return this;
        }

        /**
         * Sets the infected
         *
         * @param infected The infected to set
         * @return this Builder instance for chained calls
         */
        public Builder withInfected(Boolean infected) {
            this.infected = infected;
            return this;
        }

        /**
         * Sets the error
         *
         * @param error The error to set
         * @return this Builder instance for chained calls
         */
        public Builder withError(OXException error) {
            this.error = error;
            return this;
        }

        /**
         * Sets the threatName
         *
         * @param threatName The threatName to set
         * @return this Builder instance for chained calls
         */
        public Builder withThreatName(String threatName) {
            this.threatName = threatName;
            return this;
        }

        /**
         * Sets the scanTimestamp
         *
         * @param scanTimestamp The scanTimestamp to set
         * @return this Builder instance for chained calls
         */
        public Builder withScanTimestamp(long scanTimestamp) {
            this.scanTimestamp = scanTimestamp;
            return this;
        }

        /**
         * Sets the streamScanned
         *
         * @param scanned The screamScanned value
         * @return this Builder instance for chained calls;
         */
        public Builder withStreamScanned(boolean scanned) {
            this.scanned = scanned;
            return this;
        }

        /**
         * Builds the resulting instance of {@code AntiVirusResultImpl} from this builder's arguments.
         *
         * @return The {@code AntiVirusResultImpl} instance
         */
        public AntiVirusResultImpl build() {
            return new AntiVirusResultImpl(isTag, antiVirusServiceId, infected, error, threatName, scanTimestamp, scanned);
        }
    }
}
