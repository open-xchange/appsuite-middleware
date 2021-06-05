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

package com.openexchange.imap.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.mail.mime.HeaderName;

/**
 * {@link IMAPUpdateableData}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPUpdateableData {

    /**
     * Initializes a new {@link IMAPUpdateableData}.
     *
     * @param uid The message's UID
     * @param flags The message's flags
     * @param userFlags The message's user flags
     * @return The newly created {@link IMAPUpdateableData}
     */
    public static IMAPUpdateableData newInstance(long uid, int flags, Collection<? extends CharSequence> userFlags) {
        return new IMAPUpdateableData(uid, flags, userFlags);
    }

    /*-
     * Member section
     */

    private final long uid;

    private final int flags;

    private final Set<HeaderName> userFlags;

    private final int hashcode;

    /**
     * Initializes a new {@link IMAPUpdateableData}.
     *
     * @param uid The message's UID
     * @param flags The message's flags
     * @param userFlags The message's user flags
     */
    private IMAPUpdateableData(long uid, int flags, Collection<? extends CharSequence> userFlags) {
        super();
        this.uid = uid;
        this.flags = flags;
        if (null == userFlags) {
            this.userFlags = Collections.emptySet();
        } else {
            this.userFlags = new HashSet<HeaderName>(userFlags.size());
            for (CharSequence uFlag : userFlags) {
                this.userFlags.add(HeaderName.valueOf(uFlag));
            }
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + flags;
        result = prime * result + (int) (uid ^ (uid >>> 32));
        result = prime * result + ((userFlags == null) ? 0 : userFlags.hashCode());
        hashcode = result;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IMAPUpdateableData)) {
            return false;
        }
        final IMAPUpdateableData other = (IMAPUpdateableData) obj;
        if (flags != other.flags) {
            return false;
        }
        if (uid != other.uid) {
            return false;
        }
        if (userFlags == null) {
            if (other.userFlags != null) {
                return false;
            }
        } else if (!userFlags.equals(other.userFlags)) {
            return false;
        }
        return true;
    }

    /**
     * Indicates whether some other object is "equal to" this one considering UID only.
     *
     * @param obj The reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
     */
    public boolean equalsByUID(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IMAPUpdateableData)) {
            return false;
        }
        final IMAPUpdateableData other = (IMAPUpdateableData) obj;
        if (uid != other.uid) {
            return false;
        }
        return true;
    }

    /**
     * Gets the UID.
     *
     * @return The UID
     */
    public long getUid() {
        return uid;
    }

    /**
     * Gets the flags.
     *
     * @return The flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Gets the user flags.
     *
     * @return The user flags
     */
    public Set<HeaderName> getUserFlags() {
        return userFlags;
    }

    @Override
    public String toString() {
        return new StringBuilder(128).append(super.toString()).append(" UID=").append(uid).append(", flags=").append(flags).append(
            ", user-flags=").append(userFlags).toString();
    }
}
