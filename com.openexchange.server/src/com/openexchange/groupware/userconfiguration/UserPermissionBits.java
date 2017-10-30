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
import java.util.Set;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UserPermissionBits}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserPermissionBits implements Serializable, Cloneable {

    private static final long serialVersionUID = -4210686154175384469L;

    /**
     * The permission bit for mail access.
     */
    public static final int WEBMAIL = 1;

    /**
     * The permission bit for calendar access.
     */
    public static final int CALENDAR = 1 << 1;

    /**
     * The permission bit for contacts access.
     */
    public static final int CONTACTS = 1 << 2;

    /**
     * The permission bit for tasks access.
     */
    public static final int TASKS = 1 << 3;

    /**
     * The permission bit for infostore access.
     */
    public static final int INFOSTORE = 1 << 4;

    /**
     * @Obsolete This permission is not used. Permission bit should stay here to prevent confusion.
     */
    public static final int PROJECTS = 1 << 5;

    /**
     * @Obsolete This permission is not used. Permission bit should stay here to prevent confusion.
     */
    public static final int FORUM = 1 << 6;

    /**
     * @Obsolete This permission is not used. Permission bit should stay here to prevent confusion.
     */
    public static final int PINBOARD_WRITE_ACCESS = 1 << 7;

    /**
     * The permission bit for WebDAV/XML access.
     */
    public static final int WEBDAV_XML = 1 << 8;

    /**
     * The permission bit for WebDAV access.
     */
    public static final int WEBDAV = 1 << 9;

    /**
     * The permission bit for iCal access.
     */
    public static final int ICAL = 1 << 10;

    /**
     * The permission bit for vCard access.
     */
    public static final int VCARD = 1 << 11;

    /**
     * @Obsolete This permission is not used. Permission bit should stay here to prevent confusion.
     */
    public static final int RSS_BOOKMARKS = 1 << 12;

    /**
     * @Obsolete This permission is not used. Permission bit should stay here to prevent confusion.
     */
    public static final int RSS_PORTAL = 1 << 13;

    /**
     * The permission bit for mobility access.
     */
    public static final int MOBILITY = 1 << 14;

    /**
     * The permission bit whether write access to public folders is granted.
     */
    public static final int EDIT_PUBLIC_FOLDERS = 1 << 15;

    /**
     * The permission bit whether shared folders are accessible.
     */
    public static final int READ_CREATE_SHARED_FOLDERS = 1 << 16;

    /**
     * The permission bit if tasks may be delegated.
     */
    public static final int DELEGATE_TASKS = 1 << 17;

    /**
     * The permission bit whether groups may be modified.
     */
    public static final int EDIT_GROUP = 1 << 18;

    /**
     * The permission bit for whether resources may be modified.
     */
    public static final int EDIT_RESOURCE = 1 << 19;

    /**
     * The permission bit for whether password may be changed.
     */
    public static final int EDIT_PASSWORD = 1 << 20;

    /**
     * The permission bit whether email addresses shall be collected.
     */
    public static final int COLLECT_EMAIL_ADDRESSES = 1 << 21;

    /**
     * The permission bit for multiple mail account access.
     */
    public static final int MULTIPLE_MAIL_ACCOUNTS = 1 << 22;

    /**
     * The permission bit for subscription access.
     */
    public static final int SUBSCRIPTION = 1 << 23;

    /**
     * The permission bit for publication access.
     */
    public static final int PUBLICATION = 1 << 24;

    /**
     * The permission bit for active sync access.
     */
    public static final int ACTIVE_SYNC = 1 << 25;

    /**
     * The permission bit for USM access.
     */
    public static final int USM = 1 << 26;

    /**
     * The permission bit for OLOX v2.0 access.
     */
    public static final int OLOX20 = 1 << 27;

    /**
     * The permission bit for denied portal access.
     */
    public static final int DENIED_PORTAL = 1 << 28;

    /**
     * The permission bit for caldav access.
     * <p>
     * <b>ATTENTION</b>: This is actually handled by the config cascade!
     */
    public static final int CALDAV = 1 << 29;

    /**
     * The permission bit for carddav access.
     * <p>
     * <b>ATTENTION</b>: This is actually handled by the config cascade!
     */
    public static final int CARDDAV = 1 << 30;

    // ---------------------------------------------------------------------------------------------------------- //

    private final int userId;
    private final Context context;
    private int permissionBits;
    private int[] groups;

    /**
     * Initializes a new {@link UserPermissionBits}.
     *
     * @param permissions The permissions
     * @param userId The user identifier
     * @param contextId The context ID
     * @deprecated Use {@link #UserPermissionBits(int, int, Context)}
     */
    @Deprecated
    public UserPermissionBits(final int permissions, final int userId, final int contextId) {
        this(permissions, userId, null, loadContext(contextId));
    }

    /**
     * Initializes a new {@link UserPermissionBits}.
     *
     * @param permissions The permissions
     * @param userId The user identifier
     * @param groups The user's groups
     * @param contextId The context ID
     * @deprecated Use {@link #UserPermissionBits(int, int, int[], Context)}
     */
    @Deprecated
    public UserPermissionBits(final int permissions, final int userId, final int[] groups, final int contextId) {
        this(permissions, userId, groups, loadContext(contextId));
    }

    /**
     * Initializes a new {@link UserPermissionBits}.
     *
     * @param permissions The permissions
     * @param userId The user identifier
     * @param context The context
     */
    public UserPermissionBits(final int permissions, final int userId, final Context context) {
        this(permissions, userId, null, context);
    }

    /**
     * Initializes a new {@link UserPermissionBits}.
     *
     * @param permissions The permissions
     * @param userId The user identifier
     * @param groups The user's groups
     * @param context The context
     */
    public UserPermissionBits(final int permissions, final int userId, final int[] groups, final Context context) {
        super();
        this.userId = userId;
        this.groups = groups;
        this.context = context;
        this.permissionBits = permissions;
    }

    @Override
    public UserPermissionBits clone() {
        try {
            final UserPermissionBits clone = (UserPermissionBits) super.clone();
            if (groups != null) {
                clone.groups = new int[groups.length];
                System.arraycopy(groups, 0, clone.groups, 0, groups.length);
            }
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * Gets this user configuration's bit pattern.
     *
     * @return the bit pattern as an <code>int</code>.
     */
    public int getPermissionBits() {
        return permissionBits;
    }

    /**
     * Sets this user configuration's bit pattern.
     *
     * @param permissionBits - the bit pattern.
     */
    public void setPermissionBits(final int permissionBits) {
        this.permissionBits = permissionBits;
    }

    /**
     * Detects if user configuration allows web mail access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>.
     */
    public boolean hasWebMail() {
        return hasPermission(WEBMAIL);
    }

    /**
     * Enables/Disables web mail access in user configuration.
     *
     * @param enableWebMail
     */
    public void setWebMail(final boolean enableWebMail) {
        setPermission(enableWebMail, WEBMAIL);
    }

    /**
     * Detects if user configuration allows calendar access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasCalendar() {
        return hasPermission(CALENDAR);
    }

    /**
     * Enables/Disables calendar access in user configuration.
     *
     * @param enableCalender
     */
    public void setCalendar(final boolean enableCalender) {
        setPermission(enableCalender, CALENDAR);
    }

    /**
     * Detects if user configuration allows contact access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasContact() {
        return hasPermission(CONTACTS);
    }

    /**
     * Enables/Disables contact access in user configuration.
     *
     * @param enableContact
     */
    public void setContact(final boolean enableContact) {
        setPermission(enableContact, CONTACTS);
    }

    /**
     * Detects if user configuration allows task access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasTask() {
        return hasPermission(TASKS);
    }

    /**
     * Enables/Disables task access in user configuration.
     *
     * @param enableTask
     */
    public void setTask(final boolean enableTask) {
        setPermission(enableTask, TASKS);
    }

    /**
     * Detects if user configuration allows infostore access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasInfostore() {
        return hasPermission(INFOSTORE);
    }

    /**
     * Enables/Disables infostore access in user configuration.
     *
     * @param enableInfostore
     */
    public void setInfostore(final boolean enableInfostore) {
        setPermission(enableInfostore, INFOSTORE);
    }

    /**
     * Detects if user configuration allows project access.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasProject() {
        return false;
    }

    /**
     * Detects if user configuration allows WebDAV XML.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasWebDAVXML() {
        return hasPermission(WEBDAV_XML);
    }

    /**
     * Enables/Disables WebDAV XML access in user configuration.
     *
     * @param enableWebDAVXML
     */
    public void setWebDAVXML(final boolean enableWebDAVXML) {
        setPermission(enableWebDAVXML, WEBDAV_XML);
    }

    /**
     * Detects if user configuration allows WebDAV.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasWebDAV() {
        return hasPermission(WEBDAV);
    }

    /**
     * Enables/Disables WebDAV access in user configuration.
     *
     * @param enableWebDAV
     */
    public void setWebDAV(final boolean enableWebDAV) {
        setPermission(enableWebDAV, WEBDAV);
    }

    /**
     * Detects if user configuration allows ICalendar.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasICal() {
        return hasPermission(ICAL);
    }

    /**
     * Enables/Disables ICalendar access in user configuration.
     *
     * @param enableICal
     */
    public void setICal(final boolean enableICal) {
        setPermission(enableICal, ICAL);
    }

    /**
     * Detects if user configuration allows VCard.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasVCard() {
        return hasPermission(VCARD);
    }

    /**
     * Enables/Disables VCard access in user configuration.
     *
     * @param enableVCard
     */
    public void setVCard(final boolean enableVCard) {
        setPermission(enableVCard, VCARD);
    }

    /**
     * Detects if user configuration allows mobility functionality.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean hasSyncML() {
        return hasPermission(MOBILITY);
    }

    /**
     * Enables/Disables mobility access in user configuration
     *
     * @param enableSyncML
     */
    public void setSyncML(final boolean enableSyncML) {
        setPermission(enableSyncML, MOBILITY);
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
        return !hasPermission(DENIED_PORTAL);
    }

    /**
     * Sets if this user is denied to access portal.
     */
    public void setDeniedPortal(final boolean deniedPortal) {
        setPermission(deniedPortal, DENIED_PORTAL);
    }

    /**
     * Determines all accessible modules as defined in user configuration. The returned array of <code>int</code> is sorted according to
     * <code>{@link Arrays#sort(int[])}</code>, thus <code>{@link Arrays#binarySearch(int[], int)}</code> can be used to detect if a user
     * holds module access to a certain module (or invoke <code>{@link UserConfiguration#hasModuleAccess(int)}</code>).
     * <p>
     * The <code>int</code> values matches the constants <code>{@link FolderObject#TASK}</code>, <code>{@link FolderObject#CALENDAR}</code>,
     * <code>{@link FolderObject#CONTACT}</code>, <code>{@link FolderObject#UNBOUND}</code>, <code>{@link FolderObject#SYSTEM_MODULE}</code>, <code>{@link FolderObject#PROJECT}</code>, <code>{@link FolderObject#MAIL}</code>, <code>{@link FolderObject#INFOSTORE}</code>
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
     *            {@link FolderObject#PROJECT}</code>, <code>
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
        return hasPermission(EDIT_PUBLIC_FOLDERS);
    }

    /**
     * Set enableFullPublicFolderAccess.
     *
     * @param enableFullPublicFolderAccess
     */
    public void setFullPublicFolderAccess(final boolean enableFullPublicFolderAccess) {
        setPermission(enableFullPublicFolderAccess, EDIT_PUBLIC_FOLDERS);
    }

    /**
     * If this permission is not granted, neither folders are allowed to be shared nor shared folders are allowed to be seen by user.
     * Existing permissions are not removed if user loses this right, but the display of shared folders is suppressed.
     *
     * @return <code>true</code> full shared folder access is granted; otherwise <code>false</code>
     */
    public boolean hasFullSharedFolderAccess() {
        return hasPermission(READ_CREATE_SHARED_FOLDERS);
    }

    /**
     * Set enableFullSharedFolderAccess.
     *
     * @param enableFullSharedFolderAccess
     */
    public void setFullSharedFolderAccess(final boolean enableFullSharedFolderAccess) {
        setPermission(enableFullSharedFolderAccess, READ_CREATE_SHARED_FOLDERS);
    }

    /**
     * Checks if this user configuration allows to delegate tasks.
     *
     * @return <code>true</code> if user can delegate tasks; otherwise <code>false</code>
     */
    public boolean canDelegateTasks() {
        return hasPermission(DELEGATE_TASKS);
    }

    /**
     * Sets enableDelegateTasks.
     *
     * @param enableDelegateTasks
     */
    public void setDelegateTasks(final boolean enableDelegateTasks) {
        setPermission(enableDelegateTasks, DELEGATE_TASKS);
    }

    /**
     * Checks if this user configuration indicates to collect email addresses.
     *
     * @return <code>true</code> if this user configuration indicates to collect email addresses; otherwise <code>false</code>
     */
    public boolean isCollectEmailAddresses() {
        return hasPermission(COLLECT_EMAIL_ADDRESSES);
    }

    /**
     * Sets if this user configuration indicates to collect email addresses.
     *
     * @param collectEmailAddresses <code>true</code> if this user configuration indicates to collect email addresses; otherwise
     *            <code>false</code>
     */
    public void setCollectEmailAddresses(final boolean collectEmailAddresses) {
        setPermission(collectEmailAddresses, COLLECT_EMAIL_ADDRESSES);
    }

    /**
     * Checks if this user configuration indicates to enable multiple mail accounts.
     *
     * @return <code>true</code> if this user configuration indicates to enable multiple mail accounts; otherwise <code>false</code>
     */
    public boolean isMultipleMailAccounts() {
        return hasPermission(MULTIPLE_MAIL_ACCOUNTS);
    }

    /**
     * Sets if this user configuration indicates to enable multiple mail accounts.
     *
     * @param multipleMailAccounts <code>true</code> if this user configuration indicates to enable multiple mail accounts; otherwise
     *            <code>false</code>
     */
    public void setMultipleMailAccounts(final boolean multipleMailAccounts) {
        setPermission(multipleMailAccounts, MULTIPLE_MAIL_ACCOUNTS);
    }

    /**
     * Checks if this user configuration indicates to enable subscription.
     *
     * @return <code>true</code> if this user configuration indicates to enable subscription; otherwise <code>false</code>
     */
    public boolean isSubscription() {
        return hasPermission(SUBSCRIPTION);
    }

    /**
     * Sets if this user configuration indicates to enable subscription.
     *
     * @param subscription <code>true</code> if this user configuration indicates to enable subscription; otherwise <code>false</code>
     */
    public void setSubscription(final boolean subscription) {
        setPermission(subscription, SUBSCRIPTION);

    }

    /**
     * Checks if this user configuration indicates to enable publication.
     *
     * @return <code>true</code> if this user configuration indicates to enable publication; otherwise <code>false</code>
     */
    public boolean isPublication() {
        return hasPermission(PUBLICATION);
    }

    /**
     * Sets if this user configuration indicates to enable publication.
     *
     * @param publication <code>true</code> if this user configuration indicates to enable publication; otherwise <code>false</code>
     */
    public void setPublication(final boolean publication) {
        setPermission(publication, PUBLICATION);
    }

    /**
     * Checks if this user configuration indicates that the user may use Exchange Active Sync
     */
    public boolean hasActiveSync() {
        return hasPermission(ACTIVE_SYNC);
    }

    /**
     * Sets if this user is able to use Exchange Active Sync
     */
    public void setActiveSync(final boolean eas) {
        setPermission(eas, ACTIVE_SYNC);
    }

    /**
     * Checks if this user configuration indicates that the user may use USM.
     */
    public boolean hasUSM() {
        return hasPermission(USM);
    }

    /**
     * Sets if this user is able to use USM.
     */
    public void setUSM(final boolean usm) {
        setPermission(usm, USM);
    }

    /**
     * Checks if this user configuration indicates that the user may use OLOX2.0.
     */
    public boolean hasOLOX20() {
        return hasPermission(OLOX20);
    }

    /**
     * Sets if this user is able to user OLOX2.0.
     */
    public void setOLOX20(final boolean olox20) {
        setPermission(olox20, OLOX20);
    }

    /**
     * Checks if this user configuration indicates that groups are allowed to be edited.
     *
     * @return <code>true</code> if this user configuration indicates that groups are allowed to be edited; otherwise <code>false</code>
     */
    public boolean isEditGroup() {
        return hasPermission(EDIT_GROUP);
    }

    /**
     * Sets if this user configuration indicates that groups are allowed to be edited.
     *
     * @param editGroup <code>true</code> if this user configuration indicates that groups are allowed to be edited; otherwise
     *            <code>false</code>
     */
    public void setEditGroup(final boolean editGroup) {
        setPermission(editGroup, EDIT_GROUP);
    }

    /**
     * Checks if this user configuration indicates that resources are allowed to be edited.
     *
     * @return <code>true</code> if this user configuration indicates that resources are allowed to be edited; otherwise <code>false</code>
     */
    public boolean isEditResource() {
        return hasPermission(EDIT_RESOURCE);
    }

    /**
     * Sets if this user configuration indicates that resources are allowed to be edited.
     *
     * @param editResource <code>true</code> if this user configuration indicates that resources are allowed to be edited; otherwise
     *            <code>false</code>
     */
    public void setEditResource(final boolean editResource) {
        setPermission(editResource, EDIT_RESOURCE);
    }

    /**
     * Checks if this user configuration indicates that user password is allowed to be edited.
     *
     * @return <code>true</code> if this user configuration indicates that user password is allowed to be edited; otherwise
     *         <code>false</code>
     */
    public boolean isEditPassword() {
        return hasPermission(EDIT_PASSWORD);
    }

    /**
     * Sets if this user configuration indicates that user password is allowed to be edited.
     *
     * @param editPassword <code>true</code> if this user configuration indicates that user password is allowed to be edited; otherwise
     *            <code>false</code>
     */
    public void setEditPassword(final boolean editPassword) {
        setPermission(editPassword, EDIT_PASSWORD);
    }

    /**
     * Checks if this user configuration enables specified permission bit.
     *
     * @param permissionBit The permission bit(s) to check
     * @return <code>true</code> if this user configuration enabled specified permission bit(s); otherwise <code>false</code>
     */
    public boolean hasPermission(final int permissionBit) {
        return (permissionBits & permissionBit) == permissionBit;
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
        return hasPermissionInternal(permission);
    }

    private boolean hasPermissionInternal(final Permission permission) {
        final int bit = permission.bit;
        return (permissionBits & bit) == bit;
    }

    private void setPermission(final boolean enable, final int permission) {
        /*
         * Set or unset specified permission
         */
        permissionBits = enable ? (permissionBits | permission) : (permissionBits & ~permission);
    }

    /**
     * Checks if global address book is enabled.
     *
     * @param serverSession The session
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @deprecated Use {@link #isGlobalAddressBookEnabled()} instead
     */
    @Deprecated
    public boolean isGlobalAddressBookEnabled(final ServerSession serverSession) {
        try {
            return new OXFolderAccess(context).isReadFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID, userId, groups, this);
        } catch (final OXException e) {
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserPermissionBits.class);
            logger.warn("Cannot check availability of Global Address Book.", e);
            return false;
        }
    }

    /**
     * Checks if global address book is enabled.
     *
     * @param context The session
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public boolean isGlobalAddressBookEnabled() {
        try {
            return new OXFolderAccess(context).isReadFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID, userId, groups, this);
        } catch (final OXException e) {
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserPermissionBits.class);
            logger.warn("Cannot check availability of Global Address Book.", e);
            return false;
        }
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the groups
     *
     * @return The groups
     */
    public int[] getGroups() {
        int[] thisGroups = groups;
        if (null == thisGroups) {
            try {
                thisGroups = groups = UserStorage.getInstance().getUser(userId, context.getContextId()).getGroups();
            } catch (OXException e) {
                thisGroups = groups = new int[0];
            }
        }
        return thisGroups;
    }

    /**
     * Sets the groups
     *
     * @param groups The groups to set
     * @return This user permission bits with groups applied
     */
    public UserPermissionBits setGroups(final int[] groups) {
        this.groups = groups;
        return this;
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContextId() {
        return context.getContextId();
    }

    /**
     * Gets the appropriate permission bits for specified capability identifiers.
     *
     * @param capabilities The capability identifiers
     * @return The appropriate permission bits
     */
    public static int getPermissionBits(final Set<String> capabilities) {
        int bits = 0;
        if (null != capabilities) {
            for (final String string : capabilities) {
                final Permission permission = Permission.get(string);
                if (permission != null) {
                    bits = bits | permission.bit;
                }
            }
        }
        return bits;
    }

    /**
     * Gets the context associated with this user permission bits.
     *
     * @return The context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @deprecated remove when deprecated constructors are removed
     */
    @Deprecated
    private static Context loadContext(final int contextId) {
        try {
            ContextService service = ServerServiceRegistry.getInstance().getService(ContextService.class);
            if (null == service) {
                throw ServiceExceptionCode.absentService(ContextService.class);
            }

            return service.getContext(contextId);
        } catch (OXException e) {
            throw new RuntimeException(e);
        }
    }

}
