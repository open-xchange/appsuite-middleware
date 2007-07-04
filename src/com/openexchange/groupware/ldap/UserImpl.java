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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.Serializable;
import java.util.Locale;

import com.openexchange.server.OCLPermission;
import com.openexchange.tools.LocaleTools;

/**
 * This class implements the data container for the attributes of a user. This
 * class currently only contains the attributes of a user that are used oftenly
 * or stored in no.global and will be used from there.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UserImpl implements Serializable, User, Cloneable {

    /**
     * The group identifier for all groups and users.
     * 
     * @deprecated use {@link OCLPermission#ALL_GROUPS_AND_USERS} instead
     */
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
    private int id;

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
     * The hashed and base64 encoded password. The default value is
     * <code>"x"</code> to cause matches fail.
     */
    private String userPassword = "x";

    /**
     * Password encryption mechanism.
     */
    private String passwordMech = "{CRYPT}";

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
    private int[] groups;

    /**
     * Login information of this user.
     */
    private String loginInfo;

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
    }

    /**
     * {@inheritDoc}
     */
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
    public int getId() {
        return id;
    }

    /**
     * Setter for userPassword.
     * @param userPassword Password.
     */
    void setUserPassword(final String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMailEnabled() {
        return mailEnabled;
    }

    /**
     * Setter for mailEnabled.
     * @param mailEnabled <code>true</code> to enable user.
     */
    void setMailEnabled(final boolean mailEnabled) {
        this.mailEnabled = mailEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public int getShadowLastChange() {
        return shadowLastChange;
    }

    /**
     * Setter for shadowLastChange.
     * @param shadowLastChange Days since Jan 1, 1970 that password was last
     * changed.
     */
    void setShadowLastChange(final int shadowLastChange) {
        this.shadowLastChange = shadowLastChange;
    }

    /**
     * @param passwordMech password encryption mechanism.
     */
    void setPasswordMech(final String passwordMech) {
        this.passwordMech = passwordMech;
    }

    /**
     * Setter for imapServer.
     * @param imapServer IMAP server.
     */
    void setImapServer(final String imapServer) {
        this.imapServer = imapServer;
    }

    /**
     * {@inheritDoc}
     */
    public String getImapServer() {
        return imapServer;
    }

    /**
     * Setter for smtpServer.
     * @param smtpServer SMTP server.
     */
    void setSmtpServer(final String smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * {@inheritDoc}
     */
    public String getSmtpServer() {
        return smtpServer;
    }

    /**
     * Setter for mailDomain.
     * @param mailDomain mail domain.
     */
    void setMailDomain(final String mailDomain) {
        this.mailDomain = mailDomain;
    }

    /**
     * {@inheritDoc}
     */
    public String getMailDomain() {
        return mailDomain;
    }

    /**
     * Setter for givenName.
     * @param givenName given name.
     */
    void setGivenName(final String givenName) {
        this.givenName = givenName;
    }

    /**
     * {@inheritDoc}
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Setter for sure name.
     * @param sureName sure name.
     */
    void setSurname(final String sureName) {
        this.surname = sureName;
    }

    /**
     * {@inheritDoc}
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Setter for mail.
     * @param mail Mail address.
     */
    void setMail(final String mail) {
        this.mail = mail;
    }

    /**
     * {@inheritDoc}
     */
    public String getMail() {
        return mail;
    }

    /**
     * Setter for displayName.
     * @param displayName Display name.
     */
    void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * {@inheritDoc}
     */
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
		this.locale = LocaleTools.getLocale(preferredLanguage);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	/**
	 * {@inheritDoc}
	 */
	public Locale getLocale() {
		return locale;
	}

    /**
	 * {@inheritDoc}
	 */
    public int[] getGroups() {
        return groups.clone();
    }

    /**
     * Setter for groups.
     * @param groups the groups this user is member of.
     */
    void setGroups(final int[] groups) {
        this.groups = addAllGroupsAndUsersGroup(groups);
    }

    /**
     * Adds the group identifier for all groups and users.
     * @param groups groups of the user.
     * @return groups of the user and 0 will be added if it is missing.
     */
    private static int[] addAllGroupsAndUsersGroup(final int[] groups) {
        boolean contained = false;
        for (int group : groups) {
            contained = group == GROUP_ALL;
        }
        final int[] retval;
        if (contained) {
            retval = groups;
        } else {
            retval = new int[groups.length + 1];
            retval[0] = GROUP_ALL;
            System.arraycopy(groups, 0, retval, 1, groups.length);
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    public int getContactId() {
        return contactId;
    }

    /**
     * @param contactId the contactId to set
     */
    void setContactId(final int contactId) {
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
    public String getImapLogin() {
        return imapLogin;
    }

    /**
     * @param imapLogin the imapLogin to set
     */
    void setImapLogin(final String imapLogin) {
        this.imapLogin = imapLogin;
    }

    /**
     * @return the aliases
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * @param aliases the aliases to set
     */
    void setAliases(final String[] aliases) {
        this.aliases = aliases;
    }

    /**
     * {@inheritDoc}
     */
    public String getPasswordMech() {
        return passwordMech;
    }

    /**
     * {@inheritDoc}
     */
    public String getLoginInfo() {
        return loginInfo;
    }

    /**
     * @param loginInfo the login information.
     */
    void setLoginInfo(final String loginInfo) {
        this.loginInfo = loginInfo;
    }
}
