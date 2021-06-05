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

package com.openexchange.log;

import java.util.Optional;

/**
 * {@link DedicatedLogConfiguration}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class DedicatedLogConfiguration implements LogConfiguration {

    private final boolean enableDedicatedLogging;
    private final String loggingFileLocation;
    private final int loggingFileLimit;
    private final int loggingFileCount;
    private final String loggingFileLayoutPattern;
    private final String logLevel;
    private final String loggerName;

    /**
     * Initialises a new {@link DedicatedLogConfiguration}.
     */
    public DedicatedLogConfiguration(boolean enableDedicatedLogging, String logLevel, String loggerName, String loggingFileLocation, int loggingFileLimit, int loggingFileCount, String loggingFileLayoutPattern) {
        super();
        this.enableDedicatedLogging = enableDedicatedLogging;
        this.logLevel = logLevel;
        this.loggerName = loggerName;
        this.loggingFileLocation = loggingFileLocation;
        this.loggingFileLimit = loggingFileLimit;
        this.loggingFileCount = loggingFileCount;
        this.loggingFileLayoutPattern = loggingFileLayoutPattern;
    }

    @Override
    public boolean isEnabledDedicatedLogging() {
        return enableDedicatedLogging;
    }

    @Override
    public Optional<String> getLogLevel() {
        return Optional.ofNullable(logLevel);
    }

    @Override
    public int getLoggingFileCount() {
        return loggingFileCount;
    }

    @Override
    public String getLoggerName() {
        return loggerName;
    }

    @Override
    public int getLoggingFileLimit() {
        return loggingFileLimit;
    }

    @Override
    public String getLoggingFileLocation() {
        return loggingFileLocation;
    }

    @Override
    public Optional<String> getLoggingPattern() {
        return Optional.ofNullable(loggingFileLayoutPattern);
    }

    /////////////////////////////////////// BUILDER /////////////////////////////////

    /**
     * The builder for an instance of <code>DedicatedLogConfiguration</code>
     */
    public static class Builder {

        private boolean enableDedicatedLogging;
        private String logLevel;
        private String loggingFileLocation;
        private int loggingFileLimit;
        private int loggingFileCount;
        private String loggingFileLayoutPattern;
        private String loggerName;

        public Builder() {
            super();
        }

        public Builder setEnableDedicatedLogging(boolean enableDedicatedLogging) {
            this.enableDedicatedLogging = enableDedicatedLogging;
            return this;
        }

        public Builder setLogLevel(String logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder setLoggingFileCount(int loggingFileCount) {
            this.loggingFileCount = loggingFileCount;
            return this;
        }

        public Builder setLoggingFileLayoutPattern(String loggingFileLayoutPattern) {
            this.loggingFileLayoutPattern = loggingFileLayoutPattern;
            return this;
        }

        public Builder setLoggingFileLimit(int loggingFileLimit) {
            this.loggingFileLimit = loggingFileLimit;
            return this;
        }

        public Builder setLoggingFileLocation(String loggingFileLocation) {
            this.loggingFileLocation = loggingFileLocation;
            return this;
        }

        public Builder setLoggerName(String loggerName) {
            this.loggerName = loggerName;
            return this;
        }

        public DedicatedLogConfiguration build() {
            return new DedicatedLogConfiguration(enableDedicatedLogging, logLevel, loggerName, loggingFileLocation, loggingFileLimit, loggingFileCount, loggingFileLayoutPattern);
        }
    }
}
