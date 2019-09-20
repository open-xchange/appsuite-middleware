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
            return new SyncToken(0L, Flag.INITIAL);
        }
        String[] splitted = Strings.splitByDots(token);
        if (0 == splitted.length) {
            throw new IllegalArgumentException(token);
        }
        long timestamp = Long.parseLong(splitted[0]);
        int flags = 0;
        if (1 < splitted.length) {
            flags = Integer.parseInt(splitted[1]);
        }
        return new SyncToken(timestamp, flags);
    }

    private final long timestamp;
    private final int flags;

    /**
     * Initializes a new {@link SyncToken}.
     * 
     * @param timestamp The corresponding timestamp of the collection contents
     * @param flags Additional flags to encode along with the token
     */
    public SyncToken(long timestamp, Flag... flags) {
        super();
        this.timestamp = timestamp;
        int encodedFlags = 0;
        if (null != flags) {
            for (Flag flag : flags) {
                encodedFlags |= flag.value;
            }
        }
        this.flags = encodedFlags;
    }

    private SyncToken(long timestamp, int flags) {
        super();
        this.timestamp = timestamp;
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
     * Gets the string representation of the token.
     * 
     * @return The encoded sync-token
     */
    @Override
    public String toString() {
        return 0 < flags ? timestamp + "." + flags : String.valueOf(timestamp);
    }

}
