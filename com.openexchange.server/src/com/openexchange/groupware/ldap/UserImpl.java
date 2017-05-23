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

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import javax.mail.internet.idn.IDNA;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.passwordmechs.IPasswordMech;

/**
 * This class implements the data container for the attributes of a user. This
 * class currently only contains the attributes of a user that are used oftenly
 * or stored in no.global and will be used from there.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UserImpl implements User, Cloneable {

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
    private int id;

    /**
     * The user id of this guest users creator.
     */
    private int createdBy = 0;

    /**
     * Unique identifier of the contact belonging to this user.
     */
    private int contactId;

    /**
     * E-Mail address.
     */
    private String mail;

    /**
     * E-Mail aliases.
     */
    private String[] aliases;

    /**
     * User attributes
     */
    private Map<String, String> attributes;

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
     * The preferred language of this user. According to RFC 2798 and 2068 it
     * should be something like de_DE, en_GB or en.
     */
    private String preferredLanguage;

    /**
     * The locale bound to preferred language of this user
     */
    private Locale locale;

    /**
     * Display name of the user.
     */
    private String displayName;

    /**
     * TODO: userPassword had been initialized with "x" in the past. This, however
     * does not work since we now update the password of a user in {@link RdbUserStorage}
     *
     * OLD comment:
     * The hashed and base64 encoded password. The default value is
     * <code>"x"</code> to cause matches fail.
     */
    private String userPassword = null;

    /**
     * Password encryption mechanism.
     */
    private String passwordMech = IPasswordMech.CRYPT;

    /**
     * Determines if the user is enabled or disabled.
     */
    private boolean mailEnabled;

    /**
     * Days since Jan 1, 1970 that password was last changed.
     */
    private int shadowLastChange = -1;

    /**
     * Groups this user is member of.
     */
    private int[] groups = new int[0];

    /**
     * Login information of this user.
     */
    private String loginInfo;

    private int filestoreId = -1;
    private String filestoreName;
    private String[] filestorageAuth;
    private long fileStorageQuota;
    private int fileStorageOwner;

    /**
     * Default constructor.
     */
    public UserImpl() {
        super();
    }

    /**
     * Copy constructor.
     * @param user object to copy.
     */
    public UserImpl(final User user) {
        super();
        givenName = user.getGivenName();
        surname = user.getSurname();
        id = user.getId();
        contactId = user.getContactId();
        mail = user.getMail();
        mailDomain = user.getMailDomain();
        imapServer = user.getImapServer();
        imapLogin = user.getImapLogin();
        smtpServer = user.getSmtpServer();
        timeZone = user.getTimeZone();
        preferredLanguage = user.getPreferredLanguage();
        displayName = user.getDisplayName();
        mailEnabled = user.isMailEnabled();
        passwordMech = user.getPasswordMech();
        shadowLastChange = user.getShadowLastChange();
        groups = user.getGroups().clone();
        createdBy = user.getCreatedBy();
        filestoreId = user.getFilestoreId();
        filestoreName = user.getFilestoreName();
        filestorageAuth = user.getFileStorageAuth();
        fileStorageQuota = user.getFileStorageQuota();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Setter for id.
     * @param id User identifier.
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCreatedBy() {
        return createdBy ;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean isGuest() {
        return createdBy > 0;
    }

    /**
     * Setter for userPassword.
     * @param userPassword Password.
     */
    public void setUserPassword(final String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMailEnabled() {
        return mailEnabled;
    }

    /**
     * Setter for mailEnabled.
     * @param mailEnabled <code>true</code> to enable user.
     */
    public void setMailEnabled(final boolean mailEnabled) {
        this.mailEnabled = mailEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getShadowLastChange() {
        return shadowLastChange;
    }

    /**
     * Setter for shadowLastChange.
     * @param shadowLastChange Days since Jan 1, 1970 that password was last
     * changed.
     */
    public void setShadowLastChange(final int shadowLastChange) {
        this.shadowLastChange = shadowLastChange;
    }

    /**
     * @param passwordMech password encryption mechanism.
     */
    public void setPasswordMech(final String passwordMech) {
        this.passwordMech = passwordMech;
    }

    /**
     * Setter for imapServer.
     * @param imapServer IMAP server.
     */
    public void setImapServer(final String imapServer) {
        this.imapServer = imapServer == null ? null : IDNA.toUnicode(imapServer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImapServer() {
        return imapServer;
    }

    /**
     * Setter for smtpServer.
     * @param smtpServer SMTP server.
     */
    public void setSmtpServer(final String smtpServer) {
        this.smtpServer = smtpServer == null ? null : IDNA.toUnicode(smtpServer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSmtpServer() {
        return smtpServer;
    }

    /**
     * Setter for mailDomain.
     * @param mailDomain mail domain.
     */
    public void setMailDomain(final String mailDomain) {
        this.mailDomain = mailDomain == null ? null : IDNA.toUnicode(mailDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMailDomain() {
        return mailDomain;
    }

    /**
     * Setter for givenName.
     * @param givenName given name.
     */
    public void setGivenName(final String givenName) {
        this.givenName = givenName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGivenName() {
        return givenName;
    }

    /**
     * Setter for sure name.
     * @param sureName sure name.
     */
    public void setSurname(final String sureName) {
        this.surname = sureName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSurname() {
        return surname;
    }

    /**
     * Setter for mail.
     * @param mail Mail address.
     */
    public void setMail(final String mail) {
        this.mail = mail == null ? mail : IDNA.toIDN(mail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMail() {
        return mail;
    }

    /**
     * Setter for displayName.
     * @param displayName Display name.
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter for timeZone.
     * @param timeZone Timezone.
     */
    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Setter for preferredLanguage. The user's locale is implicitely re-set,
     * too.
     *
     * @param preferredLanguage
     *            Preferred language.
     */
    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
        this.locale = LocaleTools.getSaneLocale(LocaleTools.getLocale(preferredLanguage));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getGroups() {
        return groups.clone();
    }

    /**
     * Setter for groups.
     * @param groups the groups this user is member of.
     */
    public void setGroups(final int[] groups) {
        this.groups = groups;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final UserImpl retval = (UserImpl) super.clone();
        retval.groups = groups.clone();
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getName() + ' ' + id;
    }

    /**
     * @return the imapLogin
     */
    @Override
    public String getImapLogin() {
        return imapLogin;
    }

    /**
     * @param imapLogin the imapLogin to set
     */
    public void setImapLogin(final String imapLogin) {
        this.imapLogin = imapLogin == null ? imapLogin : IDNA.toIDN(imapLogin);
    }

    /**
     * Gets this user's aliases.
     *
     * @return The aliases
     */
    @Override
    public String[] getAliases() {
        return aliases;
    }

    /**
     * Sets this user's aliases.
     *
     * @param aliases The aliases to set
     */
    public void setAliases(final String[] aliases) {
        if (null == aliases) {
            this.aliases = null;
            return;
        }
        final String[] thisAliases = this.aliases = new String[aliases.length];
        for (int i = 0; i < aliases.length; i++) {
            thisAliases[i] = IDNA.toIDN(aliases[i]);
        }
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = this.attributes;
        return null == attributes ? null : Collections.unmodifiableMap(attributes);
    }

    /**
     * Gets the map reference for attributes
     *
     * @return The attributes' map
     */
    public Map<String, String> getAttributesInternal() {
        return attributes;
    }

    /**
     * Sets the user attributes as an unmodifiable map.
     *
     * @param attributes The attributes to set as an unmodifiable map
     */
    public void setAttributes(final Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPasswordMech() {
        return passwordMech;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLoginInfo() {
        return loginInfo;
    }

    /**
     * @param loginInfo the login information.
     */
    public void setLoginInfo(String loginInfo) {
        this.loginInfo = loginInfo;
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
     * Sets the file storage name serving as appendix to base URI.
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
        return null != filestorageAuth ? filestorageAuth.clone() : null;
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
