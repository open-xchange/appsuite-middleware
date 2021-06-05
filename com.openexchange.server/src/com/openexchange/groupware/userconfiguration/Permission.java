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

package com.openexchange.groupware.userconfiguration;

import static com.openexchange.java.Strings.asciiLowerCase;
import static com.openexchange.java.Strings.toUpperCase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.java.Strings;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

/**
 * Enumeration of known permissions.
 */
public enum Permission {

    WEBMAIL(UserConfiguration.WEBMAIL, "WebMail"),
    CALENDAR(UserConfiguration.CALENDAR, "Calendar"),
    CONTACTS(UserConfiguration.CONTACTS, "Contacts"),
    TASKS(UserConfiguration.TASKS, "Tasks"),
    INFOSTORE(UserConfiguration.INFOSTORE, "Infostore"),
    /** @deprecated */
    @Deprecated
    WEBDAV_XML(UserConfiguration.WEBDAV_XML, "WebDAVXML"),
    WEBDAV(UserConfiguration.WEBDAV, "WebDAV"),
    ICAL(UserConfiguration.ICAL, "ICal"),
    VCARD(UserConfiguration.VCARD, "VCard"),
    MOBILITY(UserConfiguration.MOBILITY, "SyncML"),
    EDIT_PUBLIC_FOLDERS(UserConfiguration.EDIT_PUBLIC_FOLDERS, "FullPublicFolderAccess"),
    READ_CREATE_SHARED_FOLDERS(UserConfiguration.READ_CREATE_SHARED_FOLDERS, "FullSharedFolderAccess"),
    DELEGATE_TASKS(UserConfiguration.DELEGATE_TASKS, "DelegateTasks"),
    EDIT_GROUP(UserConfiguration.EDIT_GROUP, "EditGroup"),
    EDIT_RESOURCE(UserConfiguration.EDIT_RESOURCE, "EditResource"),
    EDIT_PASSWORD(UserConfiguration.EDIT_PASSWORD, "EditPassword"),
    COLLECT_EMAIL_ADDRESSES(UserConfiguration.COLLECT_EMAIL_ADDRESSES, "CollectEMailAddresses"),
    MULTIPLE_MAIL_ACCOUNTS(UserConfiguration.MULTIPLE_MAIL_ACCOUNTS, "MultipleMailAccounts"),
    SUBSCRIPTION(UserConfiguration.SUBSCRIPTION, "Subscription"),
    ACTIVE_SYNC(UserConfiguration.ACTIVE_SYNC, "ActiveSync"),
    USM(UserConfiguration.USM, "USM"),
    /** @deprecated */
    @Deprecated
    OLOX20(UserConfiguration.OLOX20, "OLOX20"),
    DENIED_PORTAL(UserConfiguration.DENIED_PORTAL, "DeniedPortal"),
    CALDAV(UserConfiguration.CALDAV, "CalDAV"),
    CARDDAV(UserConfiguration.CARDDAV, "CardDAV");

    private static final class AdderProcedure implements TIntObjectProcedure<Permission> {

        private final Set<String> set;
        private final int bits;

        AdderProcedure(final int bits, final Set<String> set) {
            super();
            this.set = set;
            this.bits = bits;
        }

        @Override
        public boolean execute(final int bit, final Permission p) {
            if (bit == (bits & bit)) {
                set.add(Strings.toLowerCase(p.name()));
            }
            return true;
        }
    }

    private static final TIntObjectMap<Permission> byBit;
    static {
        final Permission[] permissions = values();
        final TIntObjectMap<Permission> m = new TIntObjectHashMap<Permission>(permissions.length);
        for (final Permission p : permissions) {
            m.put(p.bit, p);
        }
        byBit = m;
    }

    /** The associated bit constant */
    final int bit;

    /** The associated tag name */
    final String tagName;

    private Permission(final int bit, final String name) {
        this.bit = bit;
        this.tagName = name;
    }

    /**
     * Gets the associated bit constant.
     *
     * @return The bit
     */
    public int getBit() {
        return bit;
    }

    /**
     * Gets the tag name.
     *
     * @return The tag name
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Gets the capability name (the lower-case enum constant name).
     *
     * @return The capability name
     */
    public String getCapabilityName() {
        return asciiLowerCase(name());
    }

    /**
     * Gets the permission associated with given bit.
     *
     * @param bit The bit
     * @return The associated permission or <code>null</code>
     */
    public static Permission byBit(final int bit) {
        return byBit.get(bit);
    }

    /**
     * Gets the permissions associated with given bits.
     *
     * @param bits The bits
     * @return The associated permissions
     */
    public static List<Permission> byBits(final int bits) {
        if (0 == bits) {
            return Collections.emptyList();
        }
        final Permission[] pa = values();
        final List<Permission> permissions = new ArrayList<Permission>(pa.length);
        for (final Permission p : pa) {
            final int bit = p.bit;
            if ((bits & bit) == bit) {
                permissions.add(p);
            }
        }
        return permissions;
    }

    /**
     * Adds the permission names to specified set associated with given bits.
     *
     * @param bits The bits
     * @param set The set
     */
    public static void addByBits(final int bits, final Set<String> set) {
        byBit.forEachEntry(new AdderProcedure(bits, set));
    }

    /**
     * Gets the permission bits from specified permissions.
     *
     * @param col The permissions
     * @return The permission bits
     */
    public static int toBits(final Collection<Permission> col) {
        if (null == col) {
            return 0;
        }
        final Iterator<Permission> iterator = col.iterator();
        int bits = 0;
        for (int i = col.size(); i-- > 0;) {
            bits = bits | iterator.next().bit;
        }
        return bits;
    }

    /**
     * Gets the permission by specified identifier.
     *
     * @param name The identifier
     * @return The permission or <code>null</code>
     */
    public static Permission get(String name) {
        if (null == name) {
            return null;
        }
        final String upperCase = toUpperCase(name);
        for (Permission p : values()) {
            if (p.name().equals(upperCase)) {
                return p;
            }
        }
        return null;
    }

} // End of Permission class
