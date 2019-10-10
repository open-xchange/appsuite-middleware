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

package com.openexchange.session.oauth;

import java.util.concurrent.TimeUnit;

/**
 * {@link TokenRefreshConfig}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class TokenRefreshConfig {

    /**
     * Creates a new builder instance.
     * 
     * @return The new builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** The builder for an instance of <code>TokenRefreshConfig</code> */
    public static final class Builder {

        private long refreshThreshold = 0L;
        private TimeUnit refreshThresholdUnit = TimeUnit.SECONDS;
        private long lockTimeout = 5L;
        private TimeUnit lockTimeoutUnit = TimeUnit.SECONDS;
        private boolean tryRecoverStoredTokens = false;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Threshold within an access token is eagerly considered expired
         * 
         * @param threshold
         * @param unit
         * @return This builder instance
         */
        public Builder setRefreshThreshold(long threshold, TimeUnit unit) {
            this.refreshThreshold = threshold;
            this.refreshThresholdUnit = unit;
            return this;
        }

        /**
         * Max. time to wait for obtaining the token lock if another thread
         * is trying to refresh concurrently
         * 
         * @param timeout The timeout
         * @param unit The time unit for the timeout
         * @return This builder instance
         */
        public Builder setLockTimeout(long timeout, TimeUnit unit) {
            this.lockTimeout = timeout;
            this.lockTimeoutUnit = unit;
            return this;
        }

        /**
         * Enables to try to obtain potentially more recent oauth tokens from the
         * stored version of the session within session storage. This is performed
         * after the local refresh token was considered invalid during token exchange.
         * 
         * @return This builder instance
         */
        public Builder enableTryRecoverStoredTokens() {
            this.tryRecoverStoredTokens = true;
            return this;
        }

        /**
         * Sets whether to try to obtain potentially more recent oauth tokens from the
         * stored version of the session within session storage. This is performed
         * after the local refresh token was considered invalid during token exchange.
         * 
         * @param value <code>true</code> to obtain more recent tokens,
         *            <code>false</code> otherwise
         * @return This builder instance
         */
        public Builder setTryRecoverStoredTokens(boolean value) {
            this.tryRecoverStoredTokens = value;
            return this;
        }

        /**
         * Creates a new {@link TokenRefreshConfig} instance
         * 
         * @return The configuration
         * @throws IllegalArgumentException if refresh threshold or lock timeout have been set to values < 0
         */
        public TokenRefreshConfig build() throws IllegalArgumentException {
            if (refreshThreshold < 0) {
                throw new IllegalArgumentException("refreshThreshold must be >= 0");
            }
            if (lockTimeout < 0) {
                throw new IllegalArgumentException("lockTimeout must be >= 0");
            }
            return new TokenRefreshConfig(refreshThreshold, refreshThresholdUnit, lockTimeout, lockTimeoutUnit, tryRecoverStoredTokens);
        }
    }
    
    // -------------------------------------------------------------------------------------------------------------------------------------

    private final long refreshThreshold;
    private final TimeUnit refreshThresholdUnit;
    private final long lockTimeout;
    private final TimeUnit lockTimeoutUnit;
    private final boolean tryRecoverStoredTokens;

    /**
     * Initializes a new {@link TokenRefreshConfig}.
     * 
     * @param refreshThreshold The refresh threshold
     * @param refreshThresholdUnit The time unit for the refresh threshold
     * @param lockTimeout The lock timeout
     * @param lockTimeoutUnit The time unit for the lock timeout
     * @param tryRecoverStoredTokens Whether to try recovering stored tokens or not
     */
    TokenRefreshConfig(long refreshThreshold, TimeUnit refreshThresholdUnit, long lockTimeout, TimeUnit lockTimeoutUnit, boolean tryRecoverStoredTokens) {
        super();
        this.refreshThreshold = refreshThreshold;
        this.refreshThresholdUnit = refreshThresholdUnit;
        this.lockTimeout = lockTimeout;
        this.lockTimeoutUnit = lockTimeoutUnit;
        this.tryRecoverStoredTokens = tryRecoverStoredTokens;
    }

    /**
     * Gets the refreshThreshold
     *
     * @return The refreshThreshold
     */
    public long getRefreshThreshold() {
        return refreshThreshold;
    }

    /**
     * Gets the refreshThresholdUnit
     *
     * @return The refreshThresholdUnit
     */
    public TimeUnit getRefreshThresholdUnit() {
        return refreshThresholdUnit;
    }

    /**
     * Gets the lockTimeout
     *
     * @return The lockTimeout
     */
    public long getLockTimeout() {
        return lockTimeout;
    }

    /**
     * Gets the lockTimeoutUnit
     *
     * @return The lockTimeoutUnit
     */
    public TimeUnit getLockTimeoutUnit() {
        return lockTimeoutUnit;
    }

    /**
     * Gets the tryRecoverStoredTokens
     *
     * @return The tryRecoverStoredTokens
     */
    public boolean isTryRecoverStoredTokens() {
        return tryRecoverStoredTokens;
    }

    /**
     * Get the lockTimeout in milliseconds
     *
     * @return lockTimeout in milliseconds
     */
    public long getLockTimeoutMillis() {
        return lockTimeoutUnit.toMillis(lockTimeout);
    }

    /**
     * Get the refreshThreshold in milliseconds
     *
     * @return refreshThreshold in milliseconds
     */
    public long getRefreshThresholdMillis() {
        return refreshThresholdUnit.toMillis(refreshThreshold);
    }

}
