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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.userconfiguration;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.osgi.TrackerAvailabilityChecker;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.server.Initialization;

/**
 * Enumeration of known permissions.
 */
public enum Permission implements Initialization {

    WEBMAIL(UserConfiguration.WEBMAIL, "WebMail"),
    CALENDAR(UserConfiguration.CALENDAR, "Calendar"),
    CONTACTS(UserConfiguration.CONTACTS, "Contacts"),
    TASKS(UserConfiguration.TASKS, "Tasks"),
    INFOSTORE(UserConfiguration.INFOSTORE, "Infostore"),
    PROJECTS(UserConfiguration.PROJECTS, "Projects"),
    FORUM(UserConfiguration.FORUM, "Forum"),
    PINBOARD_WRITE_ACCESS(UserConfiguration.PINBOARD_WRITE_ACCESS, "PinboardWriteAccess"),
    WEBDAV_XML(UserConfiguration.WEBDAV_XML, "WebDAVXML"),
    WEBDAV(UserConfiguration.WEBDAV, "WebDAV"),
    ICAL(UserConfiguration.ICAL, "ICal"),
    VCARD(UserConfiguration.VCARD, "VCard"),
    RSS_BOOKMARKS(UserConfiguration.RSS_BOOKMARKS, "RSSBookmarks"),
    RSS_PORTAL(UserConfiguration.RSS_PORTAL, "RSSPortal"),
    MOBILITY(UserConfiguration.MOBILITY, "SyncML"),
    EDIT_PUBLIC_FOLDERS(UserConfiguration.EDIT_PUBLIC_FOLDERS, "FullPublicFolderAccess"),
    READ_CREATE_SHARED_FOLDERS(UserConfiguration.READ_CREATE_SHARED_FOLDERS, "FullSharedFolderAccess"),
    DELEGATE_TASKS(UserConfiguration.DELEGATE_TASKS, "DelegateTasks"),
    EDIT_GROUP(UserConfiguration.EDIT_GROUP, "EditGroup"),
    EDIT_RESOURCE(UserConfiguration.EDIT_RESOURCE, "EditResource"),
    EDIT_PASSWORD(UserConfiguration.EDIT_PASSWORD, "EditPassword", TrackerAvailabilityChecker.getAvailabilityCheckerFor(PasswordChangeService.class, true)),
    COLLECT_EMAIL_ADDRESSES(UserConfiguration.COLLECT_EMAIL_ADDRESSES, "CollectEMailAddresses"),
    MULTIPLE_MAIL_ACCOUNTS(UserConfiguration.MULTIPLE_MAIL_ACCOUNTS, "MultipleMailAccounts"),
    SUBSCRIPTION(UserConfiguration.SUBSCRIPTION, "Subscription"),
    PUBLICATION(UserConfiguration.PUBLICATION, "Publication"),
    ACTIVE_SYNC(UserConfiguration.ACTIVE_SYNC, "ActiveSync"),
    USM(UserConfiguration.USM, "USM"),
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
            if (bit == (bits & bit) && p.isAvailable()) {
                set.add(UserConfiguration.toLowerCase(p.name()));
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
    /** The availability checker */
    private final AvailabilityChecker checker;

    private Permission(final int bit, final String name) {
        this(bit, name, AvailabilityChecker.TRUE_AVAILABILITY_CHECKER);
    }

    private Permission(final int bit, final String name, final AvailabilityChecker checker) {
        this.bit = bit;
        this.tagName = name;
        this.checker = checker;
    }

    @Override
    public void start() throws OXException {
        checker.start();
    }

    @Override
    public void stop() throws OXException {
        checker.stop();
    }

    /**
     * Indicates if associated {@link Permission permission}'s service is available.
     *
     * @return <code>true</code> if available; otherwise <code>false</code>
     */
    public boolean isAvailable() {
        return checker.isAvailable();
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
        final Permission[] pa = values();
        final List<Permission> permissions = new ArrayList<Permission>(pa.length);
        for (final Permission p : pa) {
            final int bit = p.bit;
            if (((bits & bit) == bit) && p.checker.isAvailable()) {
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

} // End of Permission class