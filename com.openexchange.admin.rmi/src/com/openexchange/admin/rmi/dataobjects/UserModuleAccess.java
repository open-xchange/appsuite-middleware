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

package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Object for setting/getting access informations to the different ox modules
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class UserModuleAccess implements Serializable, Cloneable {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -5336341908204911967L;

    // ALL ACCESS MODULES;
    // MAKE SURE YOU REWRITE THE "equals" METHOD
    // IF YOU CHANGE SOMETHING HERE!!!
    private boolean calendar = true;

    private boolean contacts = true;

    private boolean delegateTask = true;

    private boolean editPublicFolders = true;

    private boolean ical = true;

    private boolean infostore = true;

    private boolean readCreateSharedFolders = true;

    private boolean Syncml = true;

    private boolean Tasks = true;

    private boolean Vcard = true;

    private boolean Webdav = true;

    private boolean WebdavXml = true;

    private boolean Webmail = true;

    private boolean EditGroup = true;

    private boolean EditResource = true;

    private boolean EditPassword = true;

    private boolean CollectEmailAddresses = true;

    private boolean MultipleMailAccounts = true;

    private boolean Subscription = true;

    private boolean Publication = true;

    private boolean ActiveSync = true;

    private boolean USM = true;

    private boolean OLOX20 = true;

    private boolean GlobalAddressBookDisabled = false;

    private boolean PublicFolderEditable = false;

    private boolean deniedPortal;

    /**
     * Creates a new instance of UserModuleAccess
     */
    public UserModuleAccess() {
        super();
    }

    @Override
    public UserModuleAccess clone() {
        try {
            return (UserModuleAccess) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalError("CloneNotSupportedException although Colenable is implemented");
        }
    }

    /**
     * Enable all modules
     */
    public void enableAll() {
        this.calendar = true;
        this.contacts = true;
        this.delegateTask = true;
        this.editPublicFolders = true;
        this.ical = true;
        this.infostore = true;
        this.readCreateSharedFolders = true;
        this.Syncml = true;
        this.Tasks = true;
        this.Vcard = true;
        this.Webdav = true;
        this.WebdavXml = true;
        this.Webmail = true;
        this.EditGroup = true;
        this.EditResource = true;
        this.EditPassword = true;
        this.CollectEmailAddresses = true;
        this.MultipleMailAccounts = true;
        this.Subscription = true;
        this.Publication = true;
        this.ActiveSync = true;
        this.USM = true;
        this.GlobalAddressBookDisabled = false;
        this.PublicFolderEditable = true;
        this.OLOX20 = true;
    }

    /**
     * Disable all modules
     */
    public void disableAll() {
        this.calendar = false;
        this.contacts = false;
        this.delegateTask = false;
        this.editPublicFolders = false;
        this.ical = false;
        this.infostore = false;
        this.readCreateSharedFolders = false;
        this.Syncml = false;
        this.Tasks = false;
        this.Vcard = false;
        this.Webdav = false;
        this.WebdavXml = false;
        this.Webmail = false;
        this.EditGroup = false;
        this.EditResource = false;
        this.EditPassword = false;
        this.CollectEmailAddresses = false;
        this.MultipleMailAccounts = false;
        this.Subscription = false;
        this.Publication = false;
        this.ActiveSync = false;
        this.USM = false;
        this.GlobalAddressBookDisabled = true;
        this.PublicFolderEditable = false;
        this.OLOX20 = false;
    }

    public boolean getEditGroup() {
        return EditGroup;
    }

    public void setEditGroup(final boolean editGroup) {
        EditGroup = editGroup;
    }

    public boolean getEditResource() {
        return EditResource;
    }

    public void setEditResource(final boolean editResource) {
        EditResource = editResource;
    }

    public boolean getEditPassword() {
        return EditPassword;
    }

    public void setEditPassword(final boolean editPassword) {
        EditPassword = editPassword;
    }

    /**
     * Gets the collect-email-addresses access.
     *
     * @return The collect-email-addresses access
     */
    public boolean isCollectEmailAddresses() {
        return CollectEmailAddresses;
    }

    /**
     * Sets the collect-email-addresses access.
     *
     * @param collectEmailAddresses The collect-email-addresses access to set
     */
    public void setCollectEmailAddresses(final boolean collectEmailAddresses) {
        CollectEmailAddresses = collectEmailAddresses;
    }

    /**
     * Gets the multiple-mail-accounts access.
     *
     * @return The multiple-mail-accounts access
     */
    public boolean isMultipleMailAccounts() {
        return MultipleMailAccounts;
    }

    /**
     * Sets the multiple-mail-accounts access.
     *
     * @param multipleMailAccounts The multiple-mail-accounts access to set
     */
    public void setMultipleMailAccounts(final boolean multipleMailAccounts) {
        MultipleMailAccounts = multipleMailAccounts;
    }

    /**
     * Gets the subscription access.
     *
     * @return The subscription access
     */
    public boolean isSubscription() {
        return Subscription;
    }

    /**
     * Sets the subscription access.
     *
     * @param subscription The subscription access to set
     */
    public void setSubscription(final boolean subscription) {
        Subscription = subscription;
    }

    /**
     * Gets the publication access.
     *
     * @return The publication
     */
    public boolean isPublication() {
        return Publication;
    }

    /**
     * Sets the publication access.
     *
     * @param publication The publication access to set
     */
    public void setPublication(final boolean publication) {
        Publication = publication;
    }

    /**
     * Shows if a user has access to the calendar module of ox.
     *
     * @return Returns <CODE>true</CODE> if user has access to calendar module
     *         or <CODE>false</CODE> if he has now access!
     */
    public boolean getCalendar() {
        return calendar;
    }

    /**
     * Defines if a user has access to the calendar module of ox.
     *
     * @param val
     *            Set to <CODE>true</CODE> if user should be able to access
     *            the calendar module!
     */
    public void setCalendar(final boolean val) {
        this.calendar = val;
    }

    /**
     * Shows if a user has access to the contact module of ox.
     *
     * @return Returns <CODE>true</CODE> if user has access to contact module
     *         or <CODE>false</CODE> if he has now access!
     */
    public boolean getContacts() {
        return contacts;
    }

    /**
     * Defines if a user has access to the contact module of ox.
     *
     * @param val
     *            Set to <CODE>true</CODE> if user should be able to access
     *            the contact module!
     */
    public void setContacts(final boolean val) {
        this.contacts = val;
    }

    /**
     * Shows if a user has the right to delegate tasks in the ox groupware.
     *
     * @return Returns <CODE>true</CODE> if user has the right to delegate
     *         tasks in the ox groupware. Or <CODE>false</CODE> if he has no
     *         right to delegate tasks!
     */
    public boolean getDelegateTask() {
        return delegateTask;
    }

    /**
     * Defines if a user has the right to delegate tasks in the ox groupware.
     *
     * @param val
     *            Set to <CODE>true</CODE> if user should be able to delegate
     *            tasks in the ox groupware.
     */
    public void setDelegateTask(final boolean val) {
        this.delegateTask = val;
    }

    public boolean getEditPublicFolders() {
        return editPublicFolders;
    }

    public void setEditPublicFolders(final boolean val) {
        this.editPublicFolders = val;
    }

    public boolean getIcal() {
        return ical;
    }

    public void setIcal(final boolean val) {
        this.ical = val;
    }

    public boolean getInfostore() {
        return infostore;
    }

    public void setInfostore(final boolean val) {
        this.infostore = val;
    }

    public boolean getReadCreateSharedFolders() {
        return readCreateSharedFolders;
    }

    public void setReadCreateSharedFolders(final boolean val) {
        this.readCreateSharedFolders = val;
    }

    public boolean getSyncml() {
        return Syncml;
    }

    public void setSyncml(final boolean val) {
        this.Syncml = val;
    }

    public boolean getTasks() {
        return Tasks;
    }

    public void setTasks(final boolean val) {
        this.Tasks = val;
    }

    public boolean getVcard() {
        return Vcard;
    }

    public void setVcard(final boolean val) {
        this.Vcard = val;
    }

    public boolean getWebdav() {
        return Webdav;
    }

    public void setWebdav(final boolean val) {
        this.Webdav = val;
    }

    public boolean getWebdavXml() {
        return WebdavXml;
    }

    public void setWebdavXml(final boolean val) {
        this.WebdavXml = val;
    }

    public boolean getWebmail() {
        return Webmail;
    }

    public void setWebmail(final boolean val) {
        this.Webmail = val;
    }

    public boolean isActiveSync() {
        return ActiveSync;
    }

    public void setActiveSync(final boolean activeSync) {
        this.ActiveSync = activeSync;
    }

    public boolean isUSM() {
        return USM;
    }

    public void setUSM(final boolean val) {
        this.USM = val;
    }

    public boolean isOLOX20() {
        return OLOX20;
    }

    public void setOLOX20(final boolean val) {
        this.OLOX20 = val;
    }

    public void setDeniedPortal(final boolean val) {
        this.deniedPortal = val;
    }

    public boolean isDeniedPortal() {
        return deniedPortal;
    }

    public boolean isGlobalAddressBookDisabled() {
        return GlobalAddressBookDisabled;
    }

    public void setGlobalAddressBookDisabled(final boolean val) {
        this.GlobalAddressBookDisabled = val;
    }

    public boolean isPublicFolderEditable() {
        return PublicFolderEditable;
    }

    public void setPublicFolderEditable(final boolean publicFolderEditable) {
        this.PublicFolderEditable = publicFolderEditable;
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                final Object ob = f.get(this);
                final String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (final IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (final IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (ActiveSync ? 1231 : 1237);
        result = prime * result + (CollectEmailAddresses ? 1231 : 1237);
        result = prime * result + (EditGroup ? 1231 : 1237);
        result = prime * result + (EditPassword ? 1231 : 1237);
        result = prime * result + (EditResource ? 1231 : 1237);
        result = prime * result + (GlobalAddressBookDisabled ? 1231 : 1237);
        result = prime * result + (MultipleMailAccounts ? 1231 : 1237);
        result = prime * result + (OLOX20 ? 1231 : 1237);
        result = prime * result + (PublicFolderEditable ? 1231 : 1237);
        result = prime * result + (Publication ? 1231 : 1237);
        result = prime * result + (Subscription ? 1231 : 1237);
        result = prime * result + (Syncml ? 1231 : 1237);
        result = prime * result + (Tasks ? 1231 : 1237);
        result = prime * result + (USM ? 1231 : 1237);
        result = prime * result + (Vcard ? 1231 : 1237);
        result = prime * result + (Webdav ? 1231 : 1237);
        result = prime * result + (WebdavXml ? 1231 : 1237);
        result = prime * result + (Webmail ? 1231 : 1237);
        result = prime * result + (calendar ? 1231 : 1237);
        result = prime * result + (contacts ? 1231 : 1237);
        result = prime * result + (delegateTask ? 1231 : 1237);
        result = prime * result + (deniedPortal ? 1231 : 1237);
        result = prime * result + (editPublicFolders ? 1231 : 1237);
        result = prime * result + (ical ? 1231 : 1237);
        result = prime * result + (infostore ? 1231 : 1237);
        result = prime * result + (readCreateSharedFolders ? 1231 : 1237);
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
        if (!(obj instanceof UserModuleAccess)) {
            return false;
        }
        UserModuleAccess other = (UserModuleAccess) obj;
        if (ActiveSync != other.ActiveSync) {
            return false;
        }
        if (CollectEmailAddresses != other.CollectEmailAddresses) {
            return false;
        }
        if (EditGroup != other.EditGroup) {
            return false;
        }
        if (EditPassword != other.EditPassword) {
            return false;
        }
        if (EditResource != other.EditResource) {
            return false;
        }
        if (GlobalAddressBookDisabled != other.GlobalAddressBookDisabled) {
            return false;
        }
        if (MultipleMailAccounts != other.MultipleMailAccounts) {
            return false;
        }
        if (OLOX20 != other.OLOX20) {
            return false;
        }
        if (PublicFolderEditable != other.PublicFolderEditable) {
            return false;
        }
        if (Publication != other.Publication) {
            return false;
        }
        if (Subscription != other.Subscription) {
            return false;
        }
        if (Syncml != other.Syncml) {
            return false;
        }
        if (Tasks != other.Tasks) {
            return false;
        }
        if (USM != other.USM) {
            return false;
        }
        if (Vcard != other.Vcard) {
            return false;
        }
        if (Webdav != other.Webdav) {
            return false;
        }
        if (WebdavXml != other.WebdavXml) {
            return false;
        }
        if (Webmail != other.Webmail) {
            return false;
        }
        if (calendar != other.calendar) {
            return false;
        }
        if (contacts != other.contacts) {
            return false;
        }
        if (delegateTask != other.delegateTask) {
            return false;
        }
        if (deniedPortal != other.deniedPortal) {
            return false;
        }
        if (editPublicFolders != other.editPublicFolders) {
            return false;
        }
        if (ical != other.ical) {
            return false;
        }
        if (infostore != other.infostore) {
            return false;
        }
        if (readCreateSharedFolders != other.readCreateSharedFolders) {
            return false;
        }
        return true;
    }

    /**
     * Transfers enabled permissions to <code>enabled</code> and disabled ones to <code>disabled</code>.
     *
     * @param enabled The {@code UserModuleAccess} carrying the enabled permissions
     * @param disabled The {@code UserModuleAccess} carrying the disbaled permissions
     */
    public void transferTo(UserModuleAccess enabled, UserModuleAccess disabled) {
        if (ActiveSync) {
            enabled.setActiveSync(true);
        } else {
            disabled.setActiveSync(true);
        }

        if (calendar) {
            enabled.setCalendar(true);
        } else {
            disabled.setCalendar(true);
        }

        if (CollectEmailAddresses) {
            enabled.setCollectEmailAddresses(true);
        } else {
            disabled.setCollectEmailAddresses(true);
        }

        if (contacts) {
            enabled.setContacts(true);
        } else {
            disabled.setContacts(true);
        }

        if (delegateTask) {
            enabled.setDelegateTask(true);
        } else {
            disabled.setDelegateTask(true);
        }

        if (deniedPortal) {
            enabled.setDeniedPortal(true);
        } else {
            disabled.setDeniedPortal(true);
        }

        if (EditGroup) {
            enabled.setEditGroup(true);
        } else {
            disabled.setEditGroup(true);
        }

        if (EditPassword) {
            enabled.setEditPassword(true);
        } else {
            disabled.setEditPassword(true);
        }

        if (editPublicFolders) {
            enabled.setEditPublicFolders(true);
        } else {
            disabled.setEditPublicFolders(true);
        }

        if (EditResource) {
            enabled.setEditResource(true);
        } else {
            disabled.setEditResource(true);
        }

        if (GlobalAddressBookDisabled) {
            enabled.setGlobalAddressBookDisabled(true);
        } else {
            disabled.setGlobalAddressBookDisabled(true);
        }

        if (ical) {
            enabled.setIcal(true);
        } else {
            disabled.setIcal(true);
        }

        if (infostore) {
            enabled.setInfostore(true);
        } else {
            disabled.setInfostore(true);
        }

        if (MultipleMailAccounts) {
            enabled.setMultipleMailAccounts(true);
        } else {
            disabled.setMultipleMailAccounts(true);
        }

        if (OLOX20) {
            enabled.setOLOX20(true);
        } else {
            disabled.setOLOX20(true);
        }

        if (Publication) {
            enabled.setPublication(true);
        } else {
            disabled.setPublication(true);
        }

        if (PublicFolderEditable) {
            enabled.setPublicFolderEditable(true);
        } else {
            disabled.setPublicFolderEditable(true);
        }

        if (readCreateSharedFolders) {
            enabled.setReadCreateSharedFolders(true);
        } else {
            disabled.setReadCreateSharedFolders(true);
        }

        if (Subscription) {
            enabled.setSubscription(true);
        } else {
            disabled.setSubscription(true);
        }

        if (Syncml) {
            enabled.setSyncml(true);
        } else {
            disabled.setSyncml(true);
        }

        if (Tasks) {
            enabled.setTasks(true);
        } else {
            disabled.setTasks(true);
        }

        if (USM) {
            enabled.setUSM(true);
        } else {
            disabled.setUSM(true);
        }

        if (Vcard) {
            enabled.setVcard(true);
        } else {
            disabled.setVcard(true);
        }

        if (Webdav) {
            enabled.setWebdav(true);
        } else {
            disabled.setWebdav(true);
        }

        if (WebdavXml) {
            enabled.setWebdavXml(true);
        } else {
            disabled.setWebdavXml(true);
        }

        if (Webmail) {
            enabled.setWebmail(true);
        } else {
            disabled.setWebmail(true);
        }

    }

}
