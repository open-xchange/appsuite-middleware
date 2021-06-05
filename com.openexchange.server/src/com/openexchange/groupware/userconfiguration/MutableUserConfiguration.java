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

import java.util.List;
import java.util.Set;
import com.openexchange.groupware.contexts.Context;


/**
 * {@link MutableUserConfiguration}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MutableUserConfiguration extends UserConfiguration {

    public MutableUserConfiguration(Set<String> capabilities, int userId, int[] groups, Context ctx) {
        super(capabilities, userId, groups, ctx);
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
     * Enables/Disables calendar access in user configuration.
     *
     * @param enableCalender
     */
    public void setCalendar(final boolean enableCalender) {
        setPermission(enableCalender, CALENDAR);
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
     * Enables/Disables task access in user configuration.
     *
     * @param enableTask
     */
    public void setTask(final boolean enableTask) {
        setPermission(enableTask, TASKS);
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
     * Enables/Disables WebDAV XML access in user configuration.
     *
     * @param enableWebDAVXML
     * @deprecated
     */
    @Deprecated
    public void setWebDAVXML(final boolean enableWebDAVXML) {
        setPermission(enableWebDAVXML, WEBDAV_XML);
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
     * Enables/Disables ICalendar access in user configuration.
     *
     * @param enableICal
     */
    public void setICal(final boolean enableICal) {
        setPermission(enableICal, ICAL);
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
     * Enables/Disables mobility access in user configuration
     *
     * @param enableSyncML
     */
    public void setSyncML(final boolean enableSyncML) {
        setPermission(enableSyncML, MOBILITY);
    }

    /**
     * Sets if this user is denied to access portal.
     */
    public void setDeniedPortal(final boolean deniedPortal) {
        setPermission(deniedPortal, DENIED_PORTAL);
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
     * Set enableFullSharedFolderAccess.
     *
     * @param enableFullSharedFolderAccess
     */
    public void setFullSharedFolderAccess(final boolean enableFullSharedFolderAccess) {
        setPermission(enableFullSharedFolderAccess, READ_CREATE_SHARED_FOLDERS);
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
     * Sets if this user configuration indicates to collect email addresses.
     *
     * @param collectEmailAddresses <code>true</code> if this user configuration indicates to collect email addresses; otherwise
     *            <code>false</code>
     */
    public void setCollectEmailAddresses(final boolean collectEmailAddresses) {
        setPermission(collectEmailAddresses, COLLECT_EMAIL_ADDRESSES);
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
     * Sets if this user configuration indicates to enable subscription.
     *
     * @param subscription <code>true</code> if this user configuration indicates to enable subscription; otherwise <code>false</code>
     */
    public void setSubscription(final boolean subscription) {
        setPermission(subscription, SUBSCRIPTION);

    }

    /**
     * Sets if this user is able to use Exchange Active Sync
     */
    public void setActiveSync(final boolean eas) {
        setPermission(eas, ACTIVE_SYNC);
    }

    /**
     * Sets if this user is able to use USM.
     */
    public void setUSM(final boolean usm) {
        setPermission(usm, USM);
    }

    /**
     * Sets if this user is able to user OLOX2.0.
     */
    @Deprecated
    public void setOLOX20(final boolean olox20) {
        setPermission(olox20, OLOX20);
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
     * Sets if this user configuration indicates that resources are allowed to be edited.
     *
     * @param editResource <code>true</code> if this user configuration indicates that resources are allowed to be edited; otherwise
     *            <code>false</code>
     */
    public void setEditResource(final boolean editResource) {
        setPermission(editResource, EDIT_RESOURCE);
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

    private void setPermission(final boolean enable, final int permission) {
        List<Permission> byBits = Permission.byBits(permission);
        if (enable) {
            for (Permission p : byBits) {
                capabilities.add(p.name().toLowerCase());
            }
        } else {
            for (Permission p : byBits) {
                capabilities.remove(p.name().toLowerCase());
            }
        }
    }

    private static final long serialVersionUID = 1L;

}
