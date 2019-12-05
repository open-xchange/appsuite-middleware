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
