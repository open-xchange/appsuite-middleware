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

import java.util.Date;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UnixCrypt;

/**
 * This interface provides methods to read data from users in the directory
 * service. This class is implemented according the DAO design pattern.
 * @author <a href="mailto:marcus@open-xchange.de">Marcus Klein </a>
 */
public abstract class UserStorage {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UserStorage.class);

    /**
     * Attribute name of alias.
     */
    public static final String ALIAS;

    /**
     * Attribute name of appointmentDays.
     */
    public static final String APPOINTMENTDAYS;

    /**
     * Attribute name of the display name.
     */
    public static final String DISPLAYNAME;

    /**
     * Attribute name of givenName.
     */
    public static final String GIVENNAME;

    /**
     * Attribute name of the identifier.
     */
    public static final String IDENTIFIER;

    /**
     * Attribute name of imapServer.
     */
    public static final String IMAPSERVER;

    /**
     * Attribute name of preferredLanguage.
     */
    public static final String LANGUAGE;

    /**
     * Attribute name of mail.
     */
    public static final String MAIL;

    /**
     * Attribute name of mailDomain.
     */
    public static final String MAILDOMAIN;

    /**
     * Attribute name of mailEnabled.
     */
    public static final String MAILENABLED;

    /**
     * Value of mailEnabled if user is activated..
     */
    public static final String MAILENABLED_OK;

    /**
     * Attribute name of modifyTimestamp.
     */
    public static final String MODIFYTIMESTAMP;

    /**
     * Attribute name of shadowLastChange.
     */
    public static final String SHADOWLASTCHANGE;

    /**
     * Attribute name of smtpServer.
     */
    public static final String SMTPSERVER;

    /**
     * Attribute name of sureName.
     */
    public static final String SURENAME;

    /**
     * Attribute name of taskDays.
     */
    public static final String TASKDAYS;

    /**
     * Attribute name of timeZone.
     */
    public static final String TIMEZONE;

    /**
     * Attribute name of uid.
     */
    public static final String UID;

    /**
     * Attribute name of userPassword.
     */
    public static final String USERPASSWORD;

    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class< ? extends UserStorage> implementingClass;

    /**
     * Default constructor.
     */
    protected UserStorage() {
        super();
    }

    /**
     * Searches for a user whose login matches the given uid.
     * @param loginInfo Login name of the user.
     * @return The unique identifier of the user.
     * @throws LdapException if an error occurs while searching the user or the
     * user doesn't exist.
     */
    public abstract int getUserId(String loginInfo) throws LdapException;

    /**
     * Reads the data from a user from the underlying persistent data storage.
     * @param uid User identifier.
     * @return a user object.
     * @throws LdapException if an error occurs while reading from the
     * persistent storage or the user doesn't exist.
     */
    public abstract User getUser(int uid) throws LdapException;

    /**
     * This method updates some values of a user.
     * @param user user object with the updated values.
     * @throws LdapException  if an error occurs.
     */
    public abstract void updateUser(User user) throws LdapException;

    /**
     * Searches a user by its email address. This is used for converting iCal to
     * appointments.
     * @param email the email address of the user.
     * @return a User object if the user was found by its email address or
     * <code>null</code> if no user could be found.
     * @throws LdapException if an error occurs.
     */
    public abstract User searchUser(String email) throws LdapException;

    /**
     * Returns an array with all user identifier of the context.
     * @return an array with all user identifier of the context.
     * @throws UserException if generating this list fails.
     */
    public abstract int[] listAllUser() throws UserException;

    /**
     * Searches for a user whose IMAP login name matches the given login name.
     * @param imapLogin the IMAP login name to search for
     * @return The unique identifier of the user.
     * @throws UserException if an error occurs during the search. 
     */
    public abstract int resolveIMAPLogin(String imapLogin) throws UserException;

    /**
     * Searches users who where modified later than the given date.
     * @param modifiedSince Date after that the returned users are modified.
     * @return a string array with the uids of the matching user.
     * @throws LdapException if an error occurs during the search.
     */
    public abstract int[] listModifiedUser(Date modifiedSince)
        throws LdapException;

    public boolean authenticate(final User user, final String password)
        throws UserException {
        boolean retval = false;
        if ("{CRYPT}".equals(user.getPasswordMech())) {
            retval = UnixCrypt.matches(user.getUserPassword(), password);
        } else if ("{SHA}".equals(user.getPasswordMech())) {
            retval = UserTools.hashPassword(password).equals(user
                .getUserPassword());
        }
        return retval;
    }

    /**
     * Creates a new instance implementing the user storage interface.
     * @param context Context.
     * @return an instance implementing the user storage interface.
     * @throws LdapException if the instance can't be created.
     */
    public static UserStorage getInstance(final Context context)
        throws LdapException {
        final boolean caching = Boolean.parseBoolean(LdapUtility
            .findProperty(Names.CACHING));
        synchronized (UserStorage.class) {
            if (null == implementingClass) {
                if (caching) {
                    implementingClass = CachingUserStorage.class;
                } else {
                    final String className = LdapUtility.findProperty(Names.
                        USERSTORAGE_IMPL);
                    implementingClass = LdapUtility.getImplementation(
                        className, UserStorage.class);
                }
            }
        }
        return LdapUtility.getInstance(implementingClass, context);
    }

    /**
     * Reads the data from a user from the underlying persistent data storage.
     * 
     * @param uid User identifier.
     * @param context Context.
     * @return a user object or <code>null</code> on exception.
     */
    public static User getStorageUser(final int uid, final Context context) {
		try {
			return getInstance(context).getUser(uid);
		} catch (final LdapException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

    static {
        try {
            ALIAS = LdapUtility.findProperty(Names.USER_ATTRIBUTE_ALIAS);
            APPOINTMENTDAYS = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_APPOINTMENTDAYS);
            DISPLAYNAME = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_DISPLAYNAME);
            GIVENNAME = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_GIVENNAME);
            IDENTIFIER = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_IDENTIFIER);
            IMAPSERVER = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_IMAPSERVER);
            LANGUAGE = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_PREFERREDLANGUAGE);
            MAIL = LdapUtility.findProperty(Names.USER_ATTRIBUTE_MAIL);
            MAILDOMAIN = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_MAILDOMAIN);
            MAILENABLED = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_MAILENABLED);
            MAILENABLED_OK = LdapUtility.findProperty(Names.MAILENABLED_OK);
            MODIFYTIMESTAMP = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_MODIFYTIMESTAMP);
            SHADOWLASTCHANGE = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_SHADOWLASTCHANGE);
            SMTPSERVER = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_SMTPSERVER);
            SURENAME = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_SURENAME);
            TASKDAYS = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_TASKDAYS);
            TIMEZONE = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_TIMEZONE);
            UID = LdapUtility.findProperty(Names.USER_ATTRIBUTE_UID);
            USERPASSWORD = LdapUtility.findProperty(Names
                .USER_ATTRIBUTE_PASSWORD);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }
}
