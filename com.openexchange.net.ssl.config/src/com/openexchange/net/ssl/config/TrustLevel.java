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

package com.openexchange.net.ssl.config;

/**
 * {@link TrustLevel}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public enum TrustLevel {

    /**
     * The "trust-all" trust level (default).
     */
    TRUST_ALL("all"),
    /**
     * The restricted trust level taking configuration (custom trust-store, enabled protocols/cipher suites, etc.) into consideration.
     */
    TRUST_RESTRICTED("restricted");

    private final String level;

    private TrustLevel(String level) {
        this.level = level;
    }

    /**
     * Gets the identifier for this trust level
     *
     * @return The level identifier
     */
    public String level() {
        return level;
    }

    /**
     * Looks up the appropriate trust level for given identifier.
     *
     * @param abbr The identifier to look-up
     * @return The matching trust level or {@link #TRUST_ALL}
     */
    public static TrustLevel find(String abbr) {
        if (null == abbr) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TrustLevel.class);
            logger.error("The defined log level '{}' is invalid. Please use '{}' or '{}'. Will fall back to default: trust '{}'.", abbr, TRUST_ALL.level, TRUST_RESTRICTED.level, TRUST_ALL.level);
            return TrustLevel.TRUST_ALL;
        }

        for (TrustLevel v : values()) {
            if (v.level().equals(abbr)) {
                return v;
            }
        }
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TrustLevel.class);
        logger.error("The defined log level '{}' is invalid. Please use '{}' or '{}'. Will fall back to default: trust '{}'.", abbr, TRUST_ALL.level, TRUST_RESTRICTED.level, TRUST_ALL.level);
        return TrustLevel.TRUST_ALL;
    }
}
