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
 *     Copyright (C) 2018-2020 OX Software GmbH
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
