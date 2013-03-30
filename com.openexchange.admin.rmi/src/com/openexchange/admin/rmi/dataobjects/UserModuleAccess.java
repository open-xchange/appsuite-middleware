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
public class UserModuleAccess implements Serializable {

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

    private boolean forum = true;

    private boolean ical = true;

    private boolean infostore = true;

    private boolean PinboardWrite = true;

    private boolean Projects = true;

    private boolean readCreateSharedFolders = true;

    private boolean RssBookmarks = true;

    private boolean RssPortal = true;

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

    /**
     * Enable all modules
     */
    public void enableAll() {
        this.calendar = true;
        this.contacts = true;
        this.delegateTask = true;
        this.editPublicFolders = true;
        this.forum = true;
        this.ical = true;
        this.infostore = true;
        this.PinboardWrite = true;
        this.Projects = true;
        this.readCreateSharedFolders = true;
        this.RssBookmarks = true;
        this.RssPortal = true;
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
        this.forum = false;
        this.ical = false;
        this.infostore = false;
        this.PinboardWrite = false;
        this.Projects = false;
        this.readCreateSharedFolders = false;
        this.RssBookmarks = false;
        this.RssPortal = false;
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
    /**
     * Currently NOT in use!
     */
    public boolean getForum() {
        return forum;
    }
    /**
     * Currently NOT in use!
     */
    public void setForum(final boolean val) {
        this.forum = val;
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
    /**
     * Currently NOT in use!
     */
    public boolean getPinboardWrite() {
        return PinboardWrite;
    }
    /**
     * Currently NOT in use!
     */
    public void setPinboardWrite(final boolean val) {
        this.PinboardWrite = val;
    }

    /**
     * Currently NOT in use!
     */
    public boolean getProjects() {
        return Projects;
    }

    /**
     * Currently NOT in use!
     */
    public void setProjects(final boolean val) {
        this.Projects = val;
    }

    public boolean getReadCreateSharedFolders() {
        return readCreateSharedFolders;
    }

    public void setReadCreateSharedFolders(final boolean val) {
        this.readCreateSharedFolders = val;
    }
    /**
     * Currently NOT in use!
     */
    public boolean getRssBookmarks() {
        return RssBookmarks;
    }
    /**
     * Currently NOT in use!
     */
    public void setRssBookmarks(final boolean val) {
        this.RssBookmarks = val;
    }
    /**
     * Currently NOT in use!
     */
    public boolean getRssPortal() {
        return RssPortal;
    }
    /**
     * Currently NOT in use!
     */
    public void setRssPortal(final boolean val) {
        this.RssPortal = val;
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
        result = prime * result + (PinboardWrite ? 1231 : 1237);
        result = prime * result + (Projects ? 1231 : 1237);
        result = prime * result + (PublicFolderEditable ? 1231 : 1237);
        result = prime * result + (Publication ? 1231 : 1237);
        result = prime * result + (RssBookmarks ? 1231 : 1237);
        result = prime * result + (RssPortal ? 1231 : 1237);
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
        result = prime * result + (forum ? 1231 : 1237);
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
        if (PinboardWrite != other.PinboardWrite) {
            return false;
        }
        if (Projects != other.Projects) {
            return false;
        }
        if (PublicFolderEditable != other.PublicFolderEditable) {
            return false;
        }
        if (Publication != other.Publication) {
            return false;
        }
        if (RssBookmarks != other.RssBookmarks) {
            return false;
        }
        if (RssPortal != other.RssPortal) {
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
        if (forum != other.forum) {
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

}
