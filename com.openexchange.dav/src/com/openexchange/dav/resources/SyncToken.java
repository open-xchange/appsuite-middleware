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

package com.openexchange.dav.resources;

import com.openexchange.java.Strings;

/**
 * {@link SyncToken}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class SyncToken {

    public enum Flag {

        /**
         * Indicates that the sync token was generated for an initial synchronization response.
         */
        INITIAL(1 << 0),

        /**
         * Indicates that the sync token was generated along with a truncated response.
         */
        TRUNCATED(1 << 2),
        ;

        final int value;

        private Flag(int value) {
            this.value = value;
        }
    }

    /**
     * Parses an encoded sync-token.
     *
     * @param token The token to parse, as supplied by the client in the <code>DAV:sync-collection</code> REPORT
     * @return The parsed sync token
     * @throws IllegalArgumentException If the supplied token cannot be parsed
     */
    public static SyncToken parse(String token) throws IllegalArgumentException {
        if (Strings.isEmpty(token)) {
            return new SyncToken(0L, null, Flag.INITIAL);
        }
        String[] splitted = Strings.splitByDots(token);
        if (0 == splitted.length) {
            throw new IllegalArgumentException(token);
        }
        long timestamp = Long.parseLong(splitted[0]);
        int flags = 1 < splitted.length ? Integer.parseInt(splitted[1]) : 0;
        String additional = 2 < splitted.length ? splitted[2] : null;
        return new SyncToken(timestamp, additional, flags);
    }

    private final long timestamp;
    private final int flags;
    private final String additional;

    /**
     * Initializes a new {@link SyncToken}.
     *
     * @param timestamp The corresponding timestamp of the collection contents
     */
    public SyncToken(long timestamp) {
        this(timestamp, null, 0);
    }

    /**
     * Initializes a new {@link SyncToken}.
     *
     * @param timestamp The corresponding timestamp of the collection contents
     * @param additional An additional, arbitrary value to encode along with the token
     * @param flags Additional flags to encode along with the token
     */
    public SyncToken(long timestamp, String additional, Flag... flags) {
        super();
        this.timestamp = timestamp;
        this.additional = additional;
        int encodedFlags = 0;
        if (null != flags) {
            for (Flag flag : flags) {
                encodedFlags |= flag.value;
            }
        }
        this.flags = encodedFlags;
    }

    /**
     * Initializes a new {@link SyncToken}.
     *
     * @param timestamp The corresponding timestamp of the collection contents
     * @param additional An additional, arbitrary value to encode along with the token
     * @param flags Additional flags as bitmask to encode along with the token
     */
    public SyncToken(long timestamp, String additional, int flags) {
        super();
        this.timestamp = timestamp;
        this.additional = additional;
        this.flags = flags;
    }

    /**
     * Gets the timestamp represented by the sync-token.
     *
     * @return The timestamp, or <code>0L</code> when there is none.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets a value indicating whether the token was generated for an initial synchronization response.
     *
     * @return <code>true</code> if it was generated for an initial synchronization response, <code>false</code>, otherwise
     */
    public boolean isInitial() {
        return 0 < (flags & Flag.INITIAL.value);
    }

    /**
     * Gets a value indicating whether the token was generated along with a truncated response.
     *
     * @return <code>true</code> if the token was generated along with a truncated response, <code>false</code>, otherwise
     */
    public boolean isTruncated() {
        return 0 < (flags & Flag.TRUNCATED.value);
    }

    /**
     * Gets an additional arbitrary value encoded within the sync token.
     *
     * @return An additional string value, or <code>null</code> if not set
     */
    public String getAdditional() {
        return additional;
    }

    /**
     * Gets the additional flags encoded with the sync token.
     *
     * @return The flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Gets the string representation of the token.
     *
     * @return The encoded sync-token
     */
    @Override
    public String toString() {
        if (null == additional && 0 >= flags) {
            return String.valueOf(timestamp);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(timestamp).append('.').append(flags);
        if (null != additional) {
            stringBuilder.append('.').append(additional);
        }
        return stringBuilder.toString();
    }

}
