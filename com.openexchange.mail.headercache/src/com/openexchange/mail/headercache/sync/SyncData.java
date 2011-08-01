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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.headercache.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.mail.mime.HeaderName;

/**
 * {@link SyncData}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SyncData {

    /**
     * Initializes a new {@link SyncData}.
     *
     * @param uid The mail ID
     * @param flags The system flags
     * @param userFlags The user flags or <code>null</code>
     * @return The newly created {@link SyncData}
     */
    public static SyncData newInstance(final String id, final int flags, final Collection<? extends CharSequence> userFlags) {
        return new SyncData(id, flags, userFlags);
    }

    /**
     * Gets the IDs from specified sync data collection.
     *
     * @param collection The sync data collection
     * @return The IDs from specified sync data collection
     */
    public static Set<String> getIDs(final Collection<SyncData> collection) {
        if (collection.isEmpty()) {
            return Collections.emptySet();
        }
        final int size = collection.size();
        final Set<String> set = new HashSet<String>(size);
        final Iterator<SyncData> iterator = collection.iterator();
        for (int i = 0; i < size; i++) {
            set.add(iterator.next().id);
        }
        return set;
    }

    /**
     * Filters specified sync data collection by given IDs.
     *
     * @param ids The IDs
     * @param syncData The sync data collection
     * @return The filtered sync data collection
     */
    public static Collection<SyncData> filterByUIDs(final Set<String> ids, final Collection<SyncData> syncData) {
        final List<SyncData> tmp = new ArrayList<SyncData>(ids.size());
        for (final SyncData sd : syncData) {
            if (ids.contains(sd.getId())) {
                tmp.add(sd);
            }
        }
        return tmp;
    }

    /*-
     * Member section
     */

    private final String id;

    private final int flags;

    private final Set<HeaderName> userFlags;

    private final int hashcode;

    /**
     * Initializes a new {@link SyncData}.
     *
     * @param uid The mail ID
     * @param flags The system flags
     * @param userFlags The user flags
     */
    private SyncData(final String id, final int flags, final Collection<? extends CharSequence> userFlags) {
        super();
        if (null == id) {
            throw new IllegalArgumentException("id is null.");
        }
        if (flags < 0) {
            throw new IllegalArgumentException("flags is less than zero.");
        }
        this.id = id;
        this.flags = flags;
        if (null == userFlags) {
            this.userFlags = Collections.emptySet();
        } else {
            if (userFlags.isEmpty()) {
                this.userFlags = Collections.emptySet();
            } else {
                this.userFlags = new HashSet<HeaderName>(userFlags.size());
                for (final CharSequence uFlag : userFlags) {
                    this.userFlags.add(HeaderName.valueOf(uFlag));
                }
            }
        }
        /*
         * Hash code
         */
        final int prime = 31;
        int result = 1;
        result = prime * result + this.flags;
        result = prime * result + this.id.hashCode();
        result = prime * result + this.userFlags.hashCode();
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
        if (!(obj instanceof SyncData)) {
            return false;
        }
        final SyncData other = (SyncData) obj;
        if (flags != other.flags) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
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
     * Indicates whether some other object is "equal to" this one considering mail ID only.
     *
     * @param obj The reference object with which to compare.
     * @return <code>true</code> if this object is the same as the object argument; <code>false</code> otherwise.
     */
    public boolean equalsByID(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SyncData)) {
            return false;
        }
        final SyncData other = (SyncData) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the ID.
     *
     * @return The ID
     */
    public String getId() {
        return id;
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
        return new StringBuilder(128).append(super.toString()).append(" ID=").append(id).append(", flags=").append(flags).append(
            ", user-flags=").append(userFlags).toString();
    }
}
