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

package com.openexchange.file.storage;

import java.util.EnumMap;
import com.openexchange.java.Strings;

/**
 * {@link MediaStatus} - Represents the status of parsing/analyzing media meta-data from a file.
 * <p>
 * A persisted media status is associated with a certain version number. Hence, once
 * {@link #getApplicationVersionNumber() application has a higher version number}, media data is supposed to be newly handled.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MediaStatus {

    /**
     * Gets the version number for the media status.
     * <p>
     * This allows to check if application's version number is possible higher than persisted one, in which case a new extraction of media
     * information is supposed to be scheduled
     *
     * @return The version number
     */
    public static int getApplicationVersionNumber() {
        return 1;
    }

    /** The status enumeration */
    public static enum Status {
        /**
         * Parsing/analyzing media meta-data is not applicable or was not considered.
         */
        NONE("none"),
        /**
         * Parsing/analyzing media meta-data is pending.
         */
        PENDING("pending"),
        /**
         * Parsing/analyzing media meta-data has been successfully done.
         */
        DONE_SUCCESS("success"),
        /**
         * Parsing/analyzing media meta-data failed.
         */
        DONE_FAILURE("failure"),
        /**
         * A severe error occurred while parsing/analyzing media meta-data.
         */
        ERROR("error"),
        ;

        private final String identifier;

        private Status(String identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Gets the status for given identifier.
         *
         * @param identifier The identifier to look-up by
         * @return The associated status or <code>null</code>
         */
        public static Status statusFor(String identifier) {
            if (Strings.isEmpty(identifier)) {
                return null;
            }

            String id = identifier.trim();
            for (Status s : Status.values()) {
                if (s.identifier.equalsIgnoreCase(id)) {
                    return s;
                }
            }
            return null;
        }
    }

    /** The map containing <code>MediaStatus</code> instances carrying application's version number */
    static EnumMap<Status, MediaStatus> INSTANCES = new EnumMap<MediaStatus.Status, MediaStatus>(Status.class);

    static {
        for (Status status : Status.values()) {
            INSTANCES.put(status, new MediaStatus(status, getApplicationVersionNumber()));
        }
    }

    /**
     * Gets the associated media status for given status constant carrying application's version number.
     *
     * @param status The status
     * @return The media status
     */
    public static MediaStatus valueFor(Status status) {
        return null == status ? null : INSTANCES.get(status);
    }

    /**
     * Gets the {@link Status#NONE} media status carrying application's version number.
     *
     * @return The media status
     * @see #valueFor(Status)
     */
    public static MediaStatus none() {
        return INSTANCES.get(Status.NONE);
    }

    /**
     * Gets the {@link Status#PENDING} media status carrying application's version number.
     *
     * @return The media status
     * @see #valueFor(Status)
     */
    public static MediaStatus pending() {
        return INSTANCES.get(Status.PENDING);
    }

    /**
     * Gets the {@link Status#DONE_SUCCESS} media status carrying application's version number.
     *
     * @return The media status
     * @see #valueFor(Status)
     */
    public static MediaStatus success() {
        return INSTANCES.get(Status.DONE_SUCCESS);
    }

    /**
     * Gets the {@link Status#DONE_FAILURE} media status carrying application's version number.
     *
     * @return The media status
     * @see #valueFor(Status)
     */
    public static MediaStatus failure() {
        return INSTANCES.get(Status.DONE_FAILURE);
    }

    /**
     * Gets the {@link Status#ERROR} media status carrying application's version number.
     *
     * @return The media status
     * @see #valueFor(Status)
     */
    public static MediaStatus error() {
        return INSTANCES.get(Status.ERROR);
    }

    /**
     * Parses the appropriate media status from given string.
     *
     * @param mediaStatus The string to parse from
     * @return The media status
     */
    public static MediaStatus valueFor(String mediaStatus) {
        if (Strings.isEmpty(mediaStatus)) {
            return null;
        }

        int pos = mediaStatus.indexOf('@');
        if (pos < 0) {
            // No version information contained
            return valueFor(Status.statusFor(mediaStatus));
        }

        // Expect something like "success@1"
        return new MediaStatus(Status.statusFor(mediaStatus.substring(0, pos)), Strings.getUnsignedInt(mediaStatus.substring(pos + 1)));
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final Status status;
    private final int version;
    private int h = 0;

    /**
     * Initializes a new {@link MediaStatus}.
     *
     * @param status The status
     * @param version The version number
     */
    public MediaStatus(Status status, int version) {
        super();
        if (null == status) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        if (version < 0) {
            throw new IllegalArgumentException("Version must not be negative.");
        }
        this.status = status;
        this.version = version;
    }

    /**
     * Gets the status
     *
     * @return The status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the version number
     *
     * @return The version number
     */
    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        int result = h;
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + (status.hashCode());
            result = prime * result + version;
            h = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MediaStatus other = (MediaStatus) obj;
        if (status != other.status) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(status.getIdentifier()).append('@').append(version).toString();
    }

}
