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
     * Gets the raw refresh threshold value. It makes only sense in conjunction
     * with the according time unit, which can be gotten with {@link #getRefreshThresholdUnit()}.
     *
     * @return The raw refresh threshold value
     */
    public long getRefreshThreshold() {
        return refreshThreshold;
    }

    /**
     * Gets the refresh threshold time unit
     *
     * @return The unit
     */
    public TimeUnit getRefreshThresholdUnit() {
        return refreshThresholdUnit;
    }

    /**
     * Gets the raw refresh lock timeout value. It makes only sense in conjunction
     * with the according time unit, which can be gotten with {@link #getLockTimeoutUnit()}.
     *
     * @return The raw lock timeout value
     */
    public long getLockTimeout() {
        return lockTimeout;
    }

    /**
     * Gets the lock timeout time unit
     *
     * @return The unit
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
