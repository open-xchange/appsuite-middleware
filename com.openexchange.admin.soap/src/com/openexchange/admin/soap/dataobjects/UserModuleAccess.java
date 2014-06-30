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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.soap.dataobjects;

import com.openexchange.admin.soap.SOAPUtils;

/**
 * Object for setting/getting access informations to the different ox modules
 */
public class UserModuleAccess {

    private Boolean calendar = true;

    private Boolean contacts = true;

    private Boolean delegateTask = true;

    private Boolean editPublicFolders = true;

    private Boolean ical = true;

    private Boolean infostore = true;

    private Boolean readCreateSharedFolders = true;

    private Boolean Syncml = true;

    private Boolean Tasks = true;

    private Boolean Vcard = true;

    private Boolean Webdav = true;

    private Boolean WebdavXml = true;

    private Boolean Webmail = true;

    private Boolean EditGroup = true;

    private Boolean EditResource = true;

    private Boolean EditPassword = true;

    private Boolean CollectEmailAddresses = true;

    private Boolean MultipleMailAccounts = true;

    private Boolean Subscription = true;

    private Boolean Publication = true;

    private Boolean ActiveSync = true;

    private Boolean USM = true;

    private Boolean OLOX20 = true;

    private Boolean GlobalAddressBookDisabled = false;

    private Boolean PublicFolderEditable = false;

    private Boolean deniedPortal;

    /**
     * Creates a new instance of UserModuleAccess
     */
    public UserModuleAccess() {
        super();
    }

    public UserModuleAccess(com.openexchange.admin.rmi.dataobjects.UserModuleAccess access) {
        super();
        SOAPUtils.moduleAccess2SoapModuleAccess(access, this);
    }

    public Boolean getEditGroup() {
        return EditGroup;
    }

    public void setEditGroup(final Boolean editGroup) {
        EditGroup = editGroup;
    }

    public Boolean getEditResource() {
        return EditResource;
    }

    public void setEditResource(final Boolean editResource) {
        EditResource = editResource;
    }

    public Boolean getEditPassword() {
        return EditPassword;
    }

    public void setEditPassword(final Boolean editPassword) {
        EditPassword = editPassword;
    }

    /**
     * Gets the collect-email-addresses access.
     *
     * @return The collect-email-addresses access
     */
    public Boolean getCollectEmailAddresses() {
        return CollectEmailAddresses;
    }

    /**
     * Sets the collect-email-addresses access.
     *
     * @param collectEmailAddresses The collect-email-addresses access to set
     */
    public void setCollectEmailAddresses(final Boolean collectEmailAddresses) {
        CollectEmailAddresses = collectEmailAddresses;
    }

    /**
     * Gets the multiple-mail-accounts access.
     *
     * @return The multiple-mail-accounts access
     */
    public Boolean getMultipleMailAccounts() {
        return MultipleMailAccounts;
    }

    /**
     * Sets the multiple-mail-accounts access.
     *
     * @param multipleMailAccounts The multiple-mail-accounts access to set
     */
    public void setMultipleMailAccounts(final Boolean multipleMailAccounts) {
        MultipleMailAccounts = multipleMailAccounts;
    }

    /**
     * Gets the subscription access.
     *
     * @return The subscription access
     */
    public Boolean getSubscription() {
        return Subscription;
    }

    /**
     * Sets the subscription access.
     *
     * @param subscription The subscription access to set
     */
    public void setSubscription(final Boolean subscription) {
        Subscription = subscription;
    }

    /**
     * Gets the publication access.
     *
     * @return The publication
     */
    public Boolean getPublication() {
        return Publication;
    }

    /**
     * Sets the publication access.
     *
     * @param publication The publication access to set
     */
    public void setPublication(final Boolean publication) {
        Publication = publication;
    }

    /**
     * Shows if a user has access to the calendar module of ox.
     *
     * @return Returns <CODE>true</CODE> if user has access to calendar module
     *         or <CODE>false</CODE> if he has now access!
     */
    public Boolean getCalendar() {
        return calendar;
    }

    /**
     * Defines if a user has access to the calendar module of ox.
     *
     * @param val
     *            Set to <CODE>true</CODE> if user should be able to access
     *            the calendar module!
     */
    public void setCalendar(final Boolean val) {
        this.calendar = val;
    }

    /**
     * Shows if a user has access to the contact module of ox.
     *
     * @return Returns <CODE>true</CODE> if user has access to contact module
     *         or <CODE>false</CODE> if he has now access!
     */
    public Boolean getContacts() {
        return contacts;
    }

    /**
     * Defines if a user has access to the contact module of ox.
     *
     * @param val
     *            Set to <CODE>true</CODE> if user should be able to access
     *            the contact module!
     */
    public void setContacts(final Boolean val) {
        this.contacts = val;
    }

    /**
     * Shows if a user has the right to delegate tasks in the ox groupware.
     *
     * @return Returns <CODE>true</CODE> if user has the right to delegate
     *         tasks in the ox groupware. Or <CODE>false</CODE> if he has no
     *         right to delegate tasks!
     */
    public Boolean getDelegateTask() {
        return delegateTask;
    }

    /**
     * Defines if a user has the right to delegate tasks in the ox groupware.
     *
     * @param val
     *            Set to <CODE>true</CODE> if user should be able to delegate
     *            tasks in the ox groupware.
     */
    public void setDelegateTask(final Boolean val) {
        this.delegateTask = val;
    }

    public Boolean getEditPublicFolders() {
        return editPublicFolders;
    }

    public void setEditPublicFolders(final Boolean val) {
        this.editPublicFolders = val;
    }

    public Boolean getIcal() {
        return ical;
    }

    public void setIcal(final Boolean val) {
        this.ical = val;
    }

    public Boolean getInfostore() {
        return infostore;
    }

    public void setInfostore(final Boolean val) {
        this.infostore = val;
    }

    public Boolean getReadCreateSharedFolders() {
        return readCreateSharedFolders;
    }

    public void setReadCreateSharedFolders(final Boolean val) {
        this.readCreateSharedFolders = val;
    }

    public Boolean getSyncml() {
        return Syncml;
    }

    public void setSyncml(final Boolean val) {
        this.Syncml = val;
    }

    public Boolean getTasks() {
        return Tasks;
    }

    public void setTasks(final Boolean val) {
        this.Tasks = val;
    }

    public Boolean getVcard() {
        return Vcard;
    }

    public void setVcard(final Boolean val) {
        this.Vcard = val;
    }

    public Boolean getWebdav() {
        return Webdav;
    }

    public void setWebdav(final Boolean val) {
        this.Webdav = val;
    }

    public Boolean getWebdavXml() {
        return WebdavXml;
    }

    public void setWebdavXml(final Boolean val) {
        this.WebdavXml = val;
    }

    public Boolean getWebmail() {
        return Webmail;
    }

    public void setWebmail(final Boolean val) {
        this.Webmail = val;
    }

    public Boolean getActiveSync() {
        return ActiveSync;
    }

    public void setActiveSync(final Boolean activeSync) {
        this.ActiveSync = activeSync;
    }

    public Boolean getUSM() {
        return USM;
    }

    public void setUSM(final Boolean val) {
        this.USM = val;
    }

    public Boolean getOLOX20() {
        return OLOX20;
    }

    public void setOLOX20(final Boolean val) {
        this.OLOX20 = val;
    }

    public void setDeniedPortal(final Boolean val) {
        this.deniedPortal = val;
    }

    public Boolean getDeniedPortal() {
        return deniedPortal;
    }

    public Boolean getGlobalAddressBookDisabled() {
        return GlobalAddressBookDisabled;
    }

    public void setGlobalAddressBookDisabled(final Boolean val) {
        this.GlobalAddressBookDisabled = val;
    }

    public Boolean getPublicFolderEditable() {
        return PublicFolderEditable;
    }

    public void setPublicFolderEditable(final Boolean publicFolderEditable) {
        this.PublicFolderEditable = publicFolderEditable;
    }

}
