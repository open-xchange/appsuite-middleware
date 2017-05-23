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

package com.openexchange.groupware.ldap;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MockUser implements User {

    private static final int GROUP_ALL = 0;

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 2265710814522924009L;

    /**
     * Fore name.
     */
    private String givenName;

    /**
     * Sure name.
     */
    private String surname;

    /**
     * Unique identifier. This identifier must be only unique in a context.
     */
    private final int id;

    /**
     * Unique identifier of the contact belonging to this user.
     */
    private int contactId;

    /**
     * E-Mail address.
     */
    private String mail;

    /**
     * E-Mail domain.
     */
    private String mailDomain;

    /**
     * IMAP server.
     */
    private String imapServer;

    /**
     * Login for the IMAP server.
     */
    private String imapLogin;

    /**
     * SMTP server.
     */
    private String smtpServer;

    /**
     * Timezone for this user.
     */
    private String timeZone;

    /**
     * Portal shows appointments for this number of days.
     */
    private int appointmentDays;

    /**
     * Portal shows tasks for this number of days.
     */
    private int taskDays;

    /**
     * The preferred language of this user. According to RFC 2798 and 2068 it should be something like de-de, en-gb or en.
     */
    private String preferredLanguage;

    /**
     * Display name of the user.
     */
    private String displayName;

    /**
     * The hashed and base64 encoded password. The default value is <code>"x"</code> to cause matches fail.
     */
    private String userPassword = "x";

    /**
     * Determines if the user is enabled or disabled.
     */
    private boolean mailEnabled = false;

    /**
     * Days since Jan 1, 1970 that password was last changed.
     */
    private int shadowLastChange = -1;

    /**
     * Groups this user is member of.
     */
    private int[] groups;

    /**
     * Password encryption mechanism
     */

    private String passwordMech;

    private String loginInfo;

    private Locale locale;

    private int filestoreId = -1;
    private String filestoreName;
    private String[] filestorageAuth;
    private long fileStorageQuota;
    private int fileStorageOwner;

    private final Map<String, String> attributes = new HashMap<String, String>();

    public MockUser(int id) {
        super();
        this.id = id;
    }

    public MockUser() {
        super();
        this.id = 0;
    }

    /**
     * Getter for userPassword.
     *
     * @return Password.
     */
    @Override
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Getter for uid.
     *
     * @return User identifier.
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Setter for userPassword.
     *
     * @param userPassword Password.
     */
    public void setUserPassword(final String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Getter for mailEnabled.
     *
     * @return <code>true</code> if user is enabled.
     */
    @Override
    public boolean isMailEnabled() {
        return mailEnabled;
    }

    /**
     * Setter for mailEnabled.
     *
     * @param mailEnabled <code>true</code> to enable user.
     */
    public void setMailEnabled(final boolean mailEnabled) {
        this.mailEnabled = mailEnabled;
    }

    /**
     * Getter for shadowLastChange.
     *
     * @return Days since Jan 1, 1970 that password was last changed.
     */
    @Override
    public int getShadowLastChange() {
        return shadowLastChange;
    }

    /**
     * Setter for shadowLastChange.
     *
     * @param shadowLastChange Days since Jan 1, 1970 that password was last changed.
     */
    public void setShadowLastChange(final int shadowLastChange) {
        this.shadowLastChange = shadowLastChange;
    }

    /**
     * Setter for imapServer.
     *
     * @param imapServer IMAP server.
     */
    public void setImapServer(final String imapServer) {
        this.imapServer = imapServer;
    }

    /**
     * Getter for imapServer.
     *
     * @return IMAP server.
     */
    @Override
    public String getImapServer() {
        return imapServer;
    }

    /**
     * Setter for smtpServer.
     *
     * @param smtpServer SMTP server.
     */
    public void setSmtpServer(final String smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * Getter for smtpServer.
     *
     * @return SMTP server.
     */
    @Override
    public String getSmtpServer() {
        return smtpServer;
    }

    /**
     * Setter for mailDomain.
     *
     * @param mailDomain mail domain.
     */
    public void setMailDomain(final String mailDomain) {
        this.mailDomain = mailDomain;
    }

    /**
     * Getter for mailDomain.
     *
     * @return mail domain.
     */
    @Override
    public String getMailDomain() {
        return mailDomain;
    }

    /**
     * Setter for givenName.
     *
     * @param givenName given name.
     */
    public void setGivenName(final String givenName) {
        this.givenName = givenName;
    }

    /**
     * Getter for givenName.
     *
     * @return given name.
     */
    @Override
    public String getGivenName() {
        return givenName;
    }

    /**
     * Setter for sure name.
     *
     * @param sureName sure name.
     */
    public void setSurname(final String sureName) {
        this.surname = sureName;
    }

    /**
     * Getter for sure name.
     *
     * @return sure name.
     */
    @Override
    public String getSurname() {
        return surname;
    }

    /**
     * Setter for mail.
     *
     * @param mail Mail address.
     */
    public void setMail(final String mail) {
        this.mail = mail;
    }

    /**
     * Getter for mail.
     *
     * @return mail address.
     */
    @Override
    public String getMail() {
        return mail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAliases() {
        return new String[] { getMail() };
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes ;
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    /**
     * Setter for displayName.
     *
     * @param displayName Display name.
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for displayName.
     *
     * @return Display name.
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter for timeZone.
     *
     * @param timeZone Timezone.
     */
    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Getter for timeZone.
     *
     * @return Timezone.
     */
    @Override
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Setter for appointmentDays.
     *
     * @param appointmentDays Portal show appointments for this number of days.
     */
    public void setAppointmentDays(final int appointmentDays) {
        this.appointmentDays = appointmentDays;
    }

    /**
     * Getter for appointmentDays.
     *
     * @return Portal show appointments for this number of days.
     */
    public int getAppointmentDays() {
        return appointmentDays;
    }

    /**
     * Setter for taskDays.
     *
     * @param taskDays Portal show tasks for this number of days.
     */
    public void setTaskDays(final int taskDays) {
        this.taskDays = taskDays;
    }

    /**
     * Getter for taskDays.
     *
     * @return Portal show tasks for this number of days.
     */
    public int getTaskDays() {
        return taskDays;
    }

    /**
     * Setter for preferredLanguage.
     *
     * @param preferredLanguage Preferred language.
     */
    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    /**
     * Getter for preferredLanguage. The preferred language of the user. According to RFC 2798 and 2068 it should be something like de-de,
     * en-gb or en.
     *
     * @return Preferred Language.
     */
    @Override
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * Getter for groups.
     *
     * @return the groups this user is member of.
     */
    @Override
    public int[] getGroups() {
        return groups.clone();
    }

    /**
     * Setter for groups.
     *
     * @param groups the groups this user is member of.
     */
    public void setGroups(final int[] groups) {
        this.groups = addAllGroupsAndUsersGroup(groups);
    }

    /**
     * Adds the group identifier for all groups and users.
     *
     * @param groups groups of the user.
     * @return groups of the user and 0 will be added if it is missing.
     */
    private static int[] addAllGroupsAndUsersGroup(final int[] groups) {
        boolean contained = false;
        for (final int group : groups) {
            contained = group == GROUP_ALL;
        }
        if (contained) {
            return groups;
        }
        final int[] newgroups = new int[groups.length + 1];
        newgroups[0] = GROUP_ALL;
        System.arraycopy(groups, 0, newgroups, 1, groups.length);
        return newgroups;
    }

    /**
     * @return the contactId
     */
    @Override
    public int getContactId() {
        return contactId;
    }

    /**
     * @param contactId the contactId to set
     */
    public void setContactId(final int contactId) {
        this.contactId = contactId;
    }

    /**
     * @return the imapLogin
     */
    @Override
    public String getImapLogin() {
        return imapLogin;
    }

    public void setImapLogin(String imapLogin) {
        this.imapLogin = imapLogin;
    }

    @Override
    public String getPasswordMech() {
        return passwordMech;
    }

    public void setPasswordMech(final String passwordMech) {
        this.passwordMech = passwordMech;
    }

    public void setLoginInfo(final String loginInfo) {
        this.loginInfo = loginInfo;
    }

    @Override
    public String getLoginInfo() {
        return loginInfo;
    }

    @Override
    public Locale getLocale() {
        if (locale == null && preferredLanguage != null) {
            final String[] lang = preferredLanguage.split("_");
            if (lang.length == 2) {
                locale = new Locale(lang[0], lang[1]);
            } else {
                locale = new Locale(lang[0]);
            }
        }
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    @Override
    public int getCreatedBy() {
        return 0;
    }

    @Override
    public boolean isGuest() {
        return false;
    }

    @Override
    public int getFilestoreId() {
        return filestoreId;
    }

    /**
     * Sets the file storage identifier
     *
     * @param filestoreId The identifier
     */
    public void setFilestoreId(int filestoreId) {
        this.filestoreId = filestoreId;
    }

    @Override
    public String getFilestoreName() {
        return filestoreName;
    }

    /**
     * Sets the file storage name serving as appendeix to base URI.
     *
     * @param filestoreName The name
     */
    public void setFilestoreName(String filestoreName) {
        this.filestoreName = filestoreName;
    }

    /**
     * Sets the optional file storage credentials
     *
     * @param filestoreAuth The credentials
     */
    public void setFilestoreAuth(String[] filestoreAuth) {
        this.filestorageAuth = filestoreAuth;
    }

    @Override
    public String[] getFileStorageAuth() {
        return filestorageAuth.clone();
    }

    @Override
    public long getFileStorageQuota() {
        return fileStorageQuota;
    }

    /**
     * Sets the file storage quota
     *
     * @param fileStorageQuota The quota
     */
    public void setFileStorageQuota(long fileStorageQuota) {
        this.fileStorageQuota = fileStorageQuota;
    }

    @Override
    public int getFileStorageOwner() {
        return fileStorageOwner;
    }

    /**
     * Sets the file storage owner
     *
     * @param fileStorageOwner The file storage owner to set
     */
    public void setFileStorageOwner(int fileStorageOwner) {
        this.fileStorageOwner = fileStorageOwner;
    }

}
