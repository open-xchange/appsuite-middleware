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
    public static IMAPUpdateableData newInstance(final long uid, final int flags, final Collection<? extends CharSequence> userFlags) {
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
    private IMAPUpdateableData(final long uid, final int flags, final Collection<? extends CharSequence> userFlags) {
        super();
        this.uid = uid;
        this.flags = flags;
        if (null == userFlags) {
            this.userFlags = Collections.emptySet();
        } else {
            this.userFlags = new HashSet<HeaderName>(userFlags.size());
            for (final CharSequence uFlag : userFlags) {
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
    public boolean equals(final Object obj) {
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
    public boolean equalsByUID(final Object obj) {
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
