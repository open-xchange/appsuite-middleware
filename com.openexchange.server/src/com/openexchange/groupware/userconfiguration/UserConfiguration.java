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

package com.openexchange.groupware.userconfiguration;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link UserConfiguration} - Represents a user configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UserConfiguration implements Serializable, Cloneable {

    private static final long serialVersionUID = -8277899698366715803L;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserConfiguration.class);

    /**
     * The permission bit for mail access.
     */
    public static final int WEBMAIL = UserPermissionBits.WEBMAIL;

    /**
     * The permission bit for calendar access.
     */
    public static final int CALENDAR = UserPermissionBits.CALENDAR;

    /**
     * The permission bit for contacts access.
     */
    public static final int CONTACTS = UserPermissionBits.CONTACTS;

    /**
     * The permission bit for tasks access.
     */
    public static final int TASKS = UserPermissionBits.TASKS;

    /**
     * The permission bit for infostore access.
     */
    public static final int INFOSTORE = UserPermissionBits.INFOSTORE;

    /**
     * @Obsolete Unused permission was removed. Leaving it here to prevent confusion.
     */
    private static final int PROJECTS = UserPermissionBits.PROJECTS;

    /**
     * @Obsolete Unused permission was removed. Leaving it here to prevent confusion.
     */
    private static final int FORUM = UserPermissionBits.FORUM;

    /**
     * @Obsolete Unused permission was removed. Leaving it here to prevent confusion.
     */
    private static final int PINBOARD_WRITE_ACCESS = UserPermissionBits.PINBOARD_WRITE_ACCESS;

    /**
     * The permission bit for WebDAV/XML access.
     */
    public static final int WEBDAV_XML = UserPermissionBits.WEBDAV_XML;

    /**
     * The permission bit for WebDAV access.
     */
    public static final int WEBDAV = UserPermissionBits.WEBDAV;

    /**
     * The permission bit for iCal access.
     */
    public static final int ICAL = UserPermissionBits.ICAL;

    /**
     * The permission bit for vCard access.
     */
    public static final int VCARD = UserPermissionBits.VCARD;

    /**
     * @Obsolete Unused permission was removed. Leaving it here to prevent confusion.
     */
    private static final int RSS_BOOKMARKS = UserPermissionBits.RSS_BOOKMARKS;

    /**
     * @Obsolete Unused permission was removed. Leaving it here to prevent confusion.
     */
    private static final int RSS_PORTAL = UserPermissionBits.RSS_PORTAL;

    /**
     * The permission bit for mobility access.
     */
    public static final int MOBILITY = UserPermissionBits.MOBILITY;

    /**
     * The permission bit whether write access to public folders is granted.
     */
    public static final int EDIT_PUBLIC_FOLDERS = UserPermissionBits.EDIT_PUBLIC_FOLDERS;

    /**
     * The permission bit whether shared folders are accessible.
     */
    public static final int READ_CREATE_SHARED_FOLDERS = UserPermissionBits.READ_CREATE_SHARED_FOLDERS;

    /**
     * The permission bit if tasks may be delegated.
     */
    public static final int DELEGATE_TASKS = UserPermissionBits.DELEGATE_TASKS;

    /**
     * The permission bit whether groups may be modified.
     */
    public static final int EDIT_GROUP = UserPermissionBits.EDIT_GROUP;

    /**
     * The permission bit for whether resources may be modified.
     */
    public static final int EDIT_RESOURCE = UserPermissionBits.EDIT_RESOURCE;

    /**
     * The permission bit for whether password may be changed.
     */
    public static final int EDIT_PASSWORD = UserPermissionBits.EDIT_PASSWORD;

    /**
     * The permission bit whether email addresses shall be collected.
     */
    public static final int COLLECT_EMAIL_ADDRESSES = UserPermissionBits.COLLECT_EMAIL_ADDRESSES;

    /**
     * The permission bit for multiple mail account access.
     */
    public static final int MULTIPLE_MAIL_ACCOUNTS = UserPermissionBits.MULTIPLE_MAIL_ACCOUNTS;

    /**
     * The permission bit for subscription access.
     */
    public static final int SUBSCRIPTION = UserPermissionBits.SUBSCRIPTION;

    /**
     * The permission bit for publication access.
     */
    public static final int PUBLICATION = UserPermissionBits.PUBLICATION;

    /**
     * The permission bit for active sync access.
     */
    public static final int ACTIVE_SYNC = UserPermissionBits.ACTIVE_SYNC;

    /**
     * The permission bit for USM access.
     */
    public static final int USM = UserPermissionBits.USM;

    /**
     * The permission bit for OLOX v2.0 access.
     */
    public static final int OLOX20 = UserPermissionBits.OLOX20;

    /**
     * The permission bit for denied portal access.
     */
    public static final int DENIED_PORTAL = UserPermissionBits.DENIED_PORTAL;

    /**
     * The permission bit for caldav access. ATTENTION: This is actually handled by the config cascade!
     */
    public static final int CALDAV = UserPermissionBits.CALDAV;

    /**
     * The permission bit for carddav access. ATTENTION: This is actually handled by the config cascade!
     */
    public static final int CARDDAV = UserPermissionBits.CARDDAV;

    /*-
     * Field members
     */

    /**
     * The user capabilities.
     */
    protected Set<String> capabilities;

    /**
     * The user identifier.
     */
    protected final int userId;

    /**
     * The identifiers of user's groups
     */
    protected int[] groups;

    /**
     * The context.
     */
    protected final Context ctx;

    /**
     * Initializes a new {@link UserConfiguration}.
     *
     * @param permissionBits The permissions' bit mask
     * @param userId The user ID
     * @param groups The user's group IDs
     * @param ctx The context
     */
    public UserConfiguration(final Set<String> capabilities, final int userId, final int[] groups, final Context ctx) {
        super();
        this.capabilities = capabilities;
        this.userId = userId;
        if (null == groups) {
            this.groups = null;
        } else {
            this.groups = new int[groups.length];
            System.arraycopy(groups, 0, this.groups, 0, groups.length);
        }
        this.ctx = ctx;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if ((other == null) || !(other instanceof UserConfiguration)) {
            return false;
        }
        final UserConfiguration uc = (UserConfiguration) other;
        if ((userId != uc.userId) || (!getExtendedPermissions().equals(uc.getExtendedPermissions()))) {
            return false;
        }
        if (null != groups) {
            if (null == uc.groups) {
                return false;
            }
            Arrays.sort(groups);
            Arrays.sort(uc.groups);
            if (!Arrays.equals(groups, uc.groups)) {
                return false;
            }
        }
        if (null != uc.groups) {
            return false;
        }
        if (null != ctx) {
            if (null == uc.ctx) {
                return false;
            }
            return (ctx.getContextId() == uc.ctx.getContextId());
        }
        return (null == uc.ctx);
    }

    /**
     * Gets the mutable user configuration
     *
     * @return The mutable user configuration
     */
    public MutableUserConfiguration getMutable() {
        final int[] groupCopy = new int[groups.length];
        System.arraycopy(groups, 0, groupCopy, 0, groups.length);
        return new MutableUserConfiguration(new HashSet<String>(capabilities), userId, groupCopy, ctx);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + userId;
        for (String p : capabilities) {
            hash = 31 * hash + p.hashCode();
        }
        if (null != groups) {
            Arrays.sort(groups);
            for (int group : groups) {
                hash = 31 * hash + group;
            }
        }
        if (null != ctx) {
            hash = 31 * hash + ctx.getContextId();
        }
        return hash;
    }

    @Override
    public UserConfiguration clone() {
        try {
            final UserConfiguration clone = (UserConfiguration) super.clone();
            if (groups != null) {
                clone.groups = new int[groups.length];
                System.arraycopy(groups, 0, clone.groups, 0, groups.length);
            }
            clone.capabilities = capabilities;
            /*
             * if (userSettingMail != null) { clone.userSettingMail = (UserSettingMail) userSettingMail.clone(); }
             */
            return clone;
        } catch (final CloneNotSupportedException e) {
            LOG.error("", e);
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * Gets this user configuration's bit pattern.
     *
     * @return the bit pattern as an <code>int</code>.
     */
    public int getPermissionBits() {
        return UserPermissionBits.getPermissionBits(capabilities);
    }


    /**
     * Detects if user configuration allows web mail access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>.
     */
    public boolean hasWebMail() {
        return capabilities.contains(Permission.WEBMAIL.getCapabilityName());
    }

    /**
     * Detects if user configuration allows calendar access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasCalendar() {
        return capabilities.contains(Permission.CALENDAR.getCapabilityName());
    }

    /**
     * Detects if user configuration allows contact access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasContact() {
        return capabilities.contains(Permission.CONTACTS.getCapabilityName());
    }

    /**
     * Detects if user configuration allows task access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasTask() {
        return capabilities.contains(Permission.TASKS.getCapabilityName());
    }

    /**
     * Detects if user configuration allows infostore access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasInfostore() {
        return capabilities.contains(Permission.INFOSTORE.getCapabilityName());
    }

    /**
     * Detects if user configuration allows WebDAV XML.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasWebDAVXML() {
        return capabilities.contains(Permission.WEBDAV_XML.getCapabilityName());
    }

    /**
     * Detects if user configuration allows WebDAV.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasWebDAV() {
        return capabilities.contains(Permission.WEBDAV.getCapabilityName());
    }

    /**
     * Detects if user configuration allows ICalendar.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasICal() {
        return capabilities.contains(Permission.ICAL.getCapabilityName());
    }

    /**
     * Detects if user configuration allows VCard.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasVCard() {
        return capabilities.contains(Permission.VCARD.getCapabilityName());
    }

    /**
     * Detects if user configuration allows mobility functionality.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasSyncML() {
        return capabilities.contains(Permission.MOBILITY.getCapabilityName());
    }

    /**
     * Detects if user configuration allows PIM functionality (Calendar, Contact, and Task).
     *
     * @return <code>true</code> if PIM functionality (Calendar, Contact, and Task) is allowed; otherwise <code>false</code>
     */
    public boolean hasPIM() {
        return hasCalendar() && hasContact() && hasTask();
    }

    /**
     * Detects if user configuration allows team view.
     *
     * @return <code>true</code> if team view is allowed; otherwise <code>false</code>
     */
    public boolean hasTeamView() {
        return hasCalendar() && hasFullSharedFolderAccess() && hasFullPublicFolderAccess();
    }

    /**
     * Detects if user configuration allows free busy.
     *
     * @return <code>true</code> if free busy is allowed; otherwise <code>false</code>
     */
    public boolean hasFreeBusy() {
        return hasCalendar() && hasFullSharedFolderAccess() && hasFullPublicFolderAccess();
    }

    /**
     * Detects if user configuration allows conflict handling.
     *
     * @return <code>true</code> if conflict handling is allowed; otherwise <code>false</code>
     */
    public boolean hasConflictHandling() {
        return hasCalendar() && hasFullSharedFolderAccess() && hasFullPublicFolderAccess();
    }

    /**
     * Calculates if the user configuration allows the participant dialog.
     *
     * @return <code>true</code> if the user configuration allows the participant dialog.
     */
    public boolean hasParticipantsDialog() {
        return hasConflictHandling();
    }

    /**
     * Calculates if the user configuration allows the groupware functionality.
     *
     * @return <code>true</code> if the user configuration allows the groupware functionality.
     */
    public boolean hasGroupware() {
        return hasFullSharedFolderAccess() || hasFullPublicFolderAccess();
    }

    /**
     * Detects if user configuration allows portal page in GUI.
     *
     * @return <code>true</code> if portal page is allowed; otherwise <code>false</code>
     */
    public boolean hasPortal() {
        return capabilities.contains("portal");
    }

    /**
     * Determines all accessible modules as defined in user configuration. The returned array of <code>int</code> is sorted according to
     * <code>{@link Arrays#sort(int[])}</code>, thus <code>{@link Arrays#binarySearch(int[], int)}</code> can be used to detect if a user
     * holds module access to a certain module (or invoke <code>{@link UserConfiguration#hasModuleAccess(int)}</code>).
     * <p>
     * The <code>int</code> values matches the constants <code>{@link FolderObject#TASK}</code>, <code>{@link FolderObject#CALENDAR}</code>,
     * <code>{@link FolderObject#CONTACT}</code>, <code>{@link FolderObject#UNBOUND}</code>, <code>{@link FolderObject#SYSTEM_MODULE}</code>, <code>{@link FolderObject#MAIL}</code>, <code>{@link FolderObject#INFOSTORE}</code>
     *
     * @return A sorted array of <code>int</code> carrying accessible module integer constants
     */
    public int[] getAccessibleModules() {
        final TIntList array = new TIntArrayList(10);
        if (hasTask()) {
            array.add(FolderObject.TASK); // 1
        }
        if (hasCalendar()) {
            array.add(FolderObject.CALENDAR); // 2
        }
        if (hasContact()) {
            array.add(FolderObject.CONTACT); // 3
        }
        array.add(FolderObject.UNBOUND); // 4
        array.add(FolderObject.SYSTEM_MODULE); // 5
        if (hasWebMail()) {
            array.add(FolderObject.MAIL); // 7
        }
        if (hasInfostore()) {
            // if (InfostoreFacades.isInfoStoreAvailable()) {
            array.add(FolderObject.INFOSTORE); // 8
            // }
        }
        // TODO: Switcher for messaging module
        array.add(FolderObject.MESSAGING); // 13
        // TODO: Switcher for file storage module
        array.add(FolderObject.FILE); // 14
        return array.toArray();
    }

    /**
     * Checks if user has access to given module.
     *
     * @param module The module carrying a value defined in constants <code>
     *            {@link FolderObject#TASK}</code> , <code>
     *            {@link FolderObject#CALENDAR}</code>
     *            , <code>
     *            {@link FolderObject#CONTACT}</code> , <code>
     *            {@link FolderObject#UNBOUND}</code>, <code>
     *            {@link FolderObject#SYSTEM_MODULE}</code>
     *            , <code>
     *            {@link FolderObject#MAIL}</code> , <code>
     *            {@link FolderObject#INFOSTORE}</code>
     * @return <code>true</code> if user configuration permits access to given module; otherwise <code>false</code>
     */
    public boolean hasModuleAccess(final int module) {
        return Arrays.binarySearch(getAccessibleModules(), module) >= 0;
    }

    /**
     * If this permission is not granted, it is prohibited for the user to create or edit public folders. Existing public folders are
     * visible in any case.
     *
     * @return <code>true</code> full public folder access is granted; otherwise <code>false</code>
     */
    public boolean hasFullPublicFolderAccess() {
        return capabilities.contains(Permission.EDIT_PUBLIC_FOLDERS.getCapabilityName());
    }

    /**
     * If this permission is not granted, neither folders are allowed to be shared nor shared folders are allowed to be seen by user.
     * Existing permissions are not removed if user loses this right, but the display of shared folders is suppressed.
     *
     * @return <code>true</code> full shared folder access is granted; otherwise <code>false</code>
     */
    public boolean hasFullSharedFolderAccess() {
        return capabilities.contains(Permission.READ_CREATE_SHARED_FOLDERS.getCapabilityName());
    }

    /**
     * Checks if this user configuration allows to delegate tasks.
     *
     * @return <code>true</code> if user can delegate tasks; otherwise <code>false</code>
     */
    public boolean canDelegateTasks() {
        return capabilities.contains(Permission.DELEGATE_TASKS.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates to collect email addresses.
     *
     * @return <code>true</code> if this user configuration indicates to collect email addresses; otherwise <code>false</code>
     */
    public boolean isCollectEmailAddresses() {
        return capabilities.contains(Permission.COLLECT_EMAIL_ADDRESSES.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates to enable multiple mail accounts.
     *
     * @return <code>true</code> if this user configuration indicates to enable multiple mail accounts; otherwise <code>false</code>
     */
    public boolean isMultipleMailAccounts() {
        return capabilities.contains(Permission.MULTIPLE_MAIL_ACCOUNTS.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates to enable subscription.
     *
     * @return <code>true</code> if this user configuration indicates to enable subscription; otherwise <code>false</code>
     */
    public boolean isSubscription() {
        return capabilities.contains(Permission.SUBSCRIPTION.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates to enable publication.
     *
     * @return <code>true</code> if this user configuration indicates to enable publication; otherwise <code>false</code>
     */
    public boolean isPublication() {
        return capabilities.contains(Permission.PUBLICATION.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates that the user may use Exchange Active Sync
     */
    public boolean hasActiveSync() {
        return capabilities.contains(Permission.ACTIVE_SYNC.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates that the user may use USM.
     */
    public boolean hasUSM() {
        return capabilities.contains(Permission.USM.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates that the user may use OLOX2.0.
     */
    public boolean hasOLOX20() {
        return capabilities.contains(Permission.OLOX20.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates that groups are allowed to be edited.
     *
     * @return <code>true</code> if this user configuration indicates that groups are allowed to be edited; otherwise <code>false</code>
     */
    public boolean isEditGroup() {
        return capabilities.contains(Permission.EDIT_GROUP.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates that resources are allowed to be edited.
     *
     * @return <code>true</code> if this user configuration indicates that resources are allowed to be edited; otherwise <code>false</code>
     */
    public boolean isEditResource() {
        return capabilities.contains(Permission.EDIT_RESOURCE.getCapabilityName());
    }

    /**
     * Checks if this user configuration indicates that user password is allowed to be edited.
     *
     * @return <code>true</code> if this user configuration indicates that user password is allowed to be edited; otherwise
     *         <code>false</code>
     */
    public boolean isEditPassword() {
        return capabilities.contains(Permission.EDIT_PASSWORD.getCapabilityName());
    }

    /**
     * Checks if this user configuration enables specified permission bit.
     *
     * @param permissionBit The permission bit(s) to check
     * @return <code>true</code> if this user configuration enabled specified permission bit(s); otherwise <code>false</code>
     */
    public boolean hasPermission(final int permissionBit) {
        if (0 == permissionBit) {
            // According to previous implementation:
            //  (permissionBits & permission) == permission
            return true;
        }

        for (Permission p : Permission.byBits(permissionBit)) {
            if (!capabilities.contains(com.openexchange.java.Strings.toLowerCase(p.name()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this user configuration enables specified permission.
     *
     * @param permission The permission
     * @return <code>true</code> if this user configuration enabled specified permission; otherwise <code>false</code>
     */
    public boolean hasPermission(final Permission permission) {
        if (null == permission) {
            return false;
        }
        return capabilities.contains(com.openexchange.java.Strings.toLowerCase(permission.name()));
    }

    /**
     * Checks if this user configuration enables named permission.
     *
     * @param name The permission name
     * @return <code>true</code> if this user configuration enabled named permission; otherwise <code>false</code>
     */
    public boolean hasPermission(final String name) {
        return getExtendedPermissions().contains(com.openexchange.java.Strings.toLowerCase(name));
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the group IDs.
     *
     * @return The group IDs
     */
    public int[] getGroups() {
        if (null == groups) {
            return null;
        }
        final int[] clone = new int[groups.length];
        System.arraycopy(groups, 0, clone, 0, clone.length);
        return clone;
    }

    /**
     * Gets the context.
     *
     * @return The context
     */
    public Context getContext() {
        return ctx;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("UserConfiguration_").append(userId).append('@').append(capabilities.toString()).toString();
    }

    /**
     * Gets the extended permissions.
     *
     * @return The extended permissions
     * @deprecated Might return incomplete capabilities as capabilities are loaded by user/context identifier pair not providing further
     * session attributes
     */
    @Deprecated
    public Set<String> getExtendedPermissions() {
        return capabilities;
    }

    /**
     * Gets the appropriate user permission bits for this user configuration.
     *
     * @return The user permission bits
     */
    public UserPermissionBits getUserPermissionBits() {
        return new UserPermissionBits(getPermissionBits(), userId, groups, ctx);
    }
}
