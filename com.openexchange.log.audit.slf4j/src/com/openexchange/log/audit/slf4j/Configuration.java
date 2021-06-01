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

package com.openexchange.log.audit.slf4j;

/**
 * {@link Configuration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Configuration {

    /** The configuration builder */
    public static class Builder {

        private boolean enabled;
        private boolean includeAttributeNames;
        private Slf4jLogLevel level;
        private DateFormatter dateFormatter;
        private String delimiter;
        private String fileLocation;
        private int fileLimit;
        private int fileCount;
        private String fileLayoutPattern;

        /**
         * Initializes a new {@link Builder}.
         */
        public Builder() {
            super();
            enabled = false;
            level = Slf4jLogLevel.INFO;
            fileLimit = 2097152;
            fileCount = 99;
            fileLayoutPattern = "%sanitisedMessage%n";
            dateFormatter = ISO8601DateFormatter.getInstance();
            includeAttributeNames = true;
        }

        /**
         * Sets the enabled flag
         *
         * @param enabled <code>true</code> to enable; otherwise <code>false</code>
         * @return This builder instance
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the whether to include attribute names
         *
         * @param includeAttributeNames <code>true</code> to include; otherwise <code>false</code>
         * @return This builder instance
         */
        public Builder includeAttributeNames(boolean includeAttributeNames) {
            this.includeAttributeNames = includeAttributeNames;
            return this;
        }

        /**
         * Sets the level
         *
         * @param level The level to set
         * @return This builder instance
         */
        public Builder level(Slf4jLogLevel level) {
            this.level = level;
            return this;
        }

        /**
         * Sets the date formatter
         *
         * @param dateFormatter The date formatter to set
         * @return This builder instance
         */
        public Builder dateFormatter(DateFormatter dateFormatter) {
            this.dateFormatter = dateFormatter;
            return this;
        }

        /**
         * Sets the file location. If set to non-<code>null</code>, then audit logs are written to files rather than to regular App Suite logs.
         *
         * @param fileLocation The file location to set
         * @return This builder instance
         */
        public Builder fileLocation(String fileLocation) {
            this.fileLocation = fileLocation;
            return this;
        }

        /**
         * Sets the file limit. Default is <code>2097152</code>.
         *
         * @param fileLimit The file limit to set
         * @return This builder instance
         */
        public Builder fileLimit(int fileLimit) {
            this.fileLimit = fileLimit;
            return this;
        }

        /**
         * Sets the file count. Default is <code>99</code>.
         *
         * @param fileCount The file count to set
         * @return This builder instance
         */
        public Builder fileCount(int fileCount) {
            this.fileCount = fileCount;
            return this;
        }

        /**
         * Sets the delimiter
         *
         * @param delimiter The delimiter to set
         * @return This builder instance
         */
        public Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * Sets the file layout pattern
         *
         * @param fileLayoutPattern The file layout pattern
         * @return This builder instance
         */
        public Builder fileLayoutPattern(String fileLayoutPattern) {
            this.fileLayoutPattern = fileLayoutPattern;
            return this;
        }

        /**
         * Builds the configuration carrying this builder's attributes.
         *
         * @return The configuration
         */
        public Configuration build() {
            return new Configuration(enabled, includeAttributeNames, level, dateFormatter, delimiter, fileLocation, fileLimit, fileCount, fileLayoutPattern);
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private final boolean enabled;
    private final boolean includeAttributeNames;
    private final Slf4jLogLevel level;
    private final DateFormatter dateFormatter;
    private final String fileLocation;
    private final int fileLimit;
    private final int fileCount;
    private final String delimiter;
    private final String fileLayoutPattern;

    /**
     * Initializes a new {@link Configuration}.
     * @param fileLayoutPattern
     */
    Configuration(boolean enabled, boolean includeAttributeNames, Slf4jLogLevel level, DateFormatter dateFormatter, String delimiter, String fileLocation, int fileLimit, int fileCount, String fileLayoutPattern) {
        super();
        this.enabled = enabled;
        this.includeAttributeNames = includeAttributeNames;
        this.level = level;
        this.dateFormatter = dateFormatter;
        this.delimiter = delimiter;
        this.fileLocation = fileLocation;
        this.fileLimit = fileLimit;
        this.fileCount = fileCount;
        this.fileLayoutPattern = fileLayoutPattern;
    }

    /**
     * Gets the enabled flag
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks whether to include attribute names
     *
     * @return <code>true</code> to include; otherwise <code>false</code>
     */
    public boolean isIncludeAttributeNames() {
        return includeAttributeNames;
    }

    /**
     * Gets the level
     *
     * @return The level
     */
    public Slf4jLogLevel getLevel() {
        return level;
    }

    /**
     * Gets the date formatter
     *
     * @return The date formatter
     */
    public DateFormatter getDateFormatter() {
        return dateFormatter;
    }

    /**
     * Gets the file location
     *
     * @return The file location
     */
    public String getFileLocation() {
        return fileLocation;
    }

    /**
     * Gets the file limit
     *
     * @return The file limit
     */
    public int getFileLimit() {
        return fileLimit;
    }

    /**
     * Gets the file count
     *
     * @return The file count
     */
    public int getFileCount() {
        return fileCount;
    }

    /**
     * Gets the file layout pattern
     *
     * @return The file layout pattern
     */
    public String getFileLayoutPattern() {
        return fileLayoutPattern;
    }

    /**
     * Gets the attribute delimiter.
     *
     * @return The attribute delimiter
     */
    public String getDelimiter() {
        return delimiter;
    }

}
