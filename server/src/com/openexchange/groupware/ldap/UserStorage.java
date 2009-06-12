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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;

/**
 * This interface provides methods to read data from users in the directory
 * service. This class is implemented according the DAO design pattern.
 * @author <a href="mailto:marcus@open-xchange.de">Marcus Klein </a>
 */
public abstract class UserStorage {

    private static final Log LOG = LogFactory.getLog(UserStorage.class);

    /**
     * Attribute name of alias.
     */
    public static String ALIAS;

    /**
     * Attribute name of appointmentDays.
     */
    public static String APPOINTMENTDAYS;

    /**
     * Attribute name of the display name.
     */
    public static String DISPLAYNAME;

    /**
     * Attribute name of givenName.
     */
    public static String GIVENNAME;

    /**
     * Attribute name of the identifier.
     */
    public static String IDENTIFIER;

    /**
     * Attribute name of imapServer.
     */
    public static String IMAPSERVER;

    /**
     * Attribute name of preferredLanguage.
     */
    public static String LANGUAGE;

    /**
     * Attribute name of mail.
     */
    public static String MAIL;

    /**
     * Attribute name of mailDomain.
     */
    public static String MAILDOMAIN;

    /**
     * Attribute name of mailEnabled.
     */
    public static String MAILENABLED;

    /**
     * Value of mailEnabled if user is activated..
     */
    public static String MAILENABLED_OK;

    /**
     * Attribute name of modifyTimestamp.
     */
    public static String MODIFYTIMESTAMP;

    /**
     * Attribute name of shadowLastChange.
     */
    public static String SHADOWLASTCHANGE;

    /**
     * Attribute name of smtpServer.
     */
    public static String SMTPSERVER;

    /**
     * Attribute name of sureName.
     */
    public static String SURENAME;

    /**
     * Attribute name of taskDays.
     */
    public static String TASKDAYS;

    /**
     * Attribute name of timeZone.
     */
    public static String TIMEZONE;

    /**
     * Attribute name of uid.
     */
    public static String UID;

    /**
     * Attribute name of userPassword.
     */
    public static String USERPASSWORD;

    /**
     * The instance
     */
    private static UserStorage instance;

    /**
     * Default constructor.
     */
    protected UserStorage() {
        super();
    }

    /**
     * Searches for a user whose login matches the given uid.
     * @param loginInfo Login name of the user.
     * @param context The context.
     * @return The unique identifier of the user.
     * @throws LdapException if an error occurs while searching the user or the
     * user doesn't exist.
     */
    public abstract int getUserId(String loginInfo, Context context) throws LdapException;

    /**
     * Reads the data from a user from the underlying persistent data storage.
     * @param uid User identifier.
     * @return a user object.
     *  @param context The context.
     * @throws LdapException if an error occurs while reading from the
     * persistent storage or the user doesn't exist.
     */
    public abstract User getUser(int uid, Context context) throws LdapException;

    /**
     * This method updates some values of a user.
     * @param user user object with the updated values.
     * @param context The context.
     * @throws LdapException  if an error occurs.
     */
    public abstract void updateUser(User user, Context context) throws LdapException;

    /**
     * Searches a user by its email address. This is used for converting iCal to
     * appointments.
     * @param email the email address of the user.
     * @param context The context.
     * @return a User object if the user was found by its email address or
     * <code>null</code> if no user could be found.
     * @throws LdapException if an error occurs.
     */
    public abstract User searchUser(String email, Context context) throws LdapException;

    /**
     * Returns an array with all user identifier of the context.
     * @param context The context.
     * @return an array with all user identifier of the context.
     * @throws UserException if generating this list fails.
     */
    public abstract int[] listAllUser(Context context) throws UserException;

    /**
     * Searches for users whose IMAP login name matches the given login name.
     * @param imapLogin the IMAP login name to search for
     * @param context The context.
     * @return The unique identifiers of the users.
     * @throws UserException if an error occurs during the search.
     */
    public abstract int[] resolveIMAPLogin(String imapLogin, Context context) throws UserException;

    /**
     * Performs internal start-up
     * @throws UserException If internal start-up fails
     */
    protected abstract void startInternal() throws UserException;

    /**
     * Performs internal shut-down
     * @throws UserException If internal shut-down fails
     */
    protected abstract void stopInternal() throws UserException;

    /**
     * Searches users who where modified later than the given date.
     * @param modifiedSince Date after that the returned users are modified.
     * @param context The context.
     * @return a string array with the uids of the matching user.
     * @throws LdapException if an error occurs during the search.
     */
    public abstract int[] listModifiedUser(Date modifiedSince, Context context)
        throws LdapException;

    /**
     * Removes a user from the cache if caching is used.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @throws UserException if removing gives an exception.
     */
    public abstract void invalidateUser(final Context ctx, final int userId) throws UserException;

    public final void invalidateUser(Context ctx, int[] userIds) throws UserException {
        for (final int member : userIds) {
            invalidateUser(ctx, member);
        }
    }

    public static final boolean authenticate(final User user,
        final String password) throws UserException {
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
     * Initialization.
     * @throws ContextException if initialization of contexts fails.
     */
    public static void start() throws UserException {
        if (null != instance) {
            LOG.error("Duplicate initialization of UserStorage.");
            return;
        }
        try {
            loadProperties();
        } catch (final LdapException e) {
            throw new UserException(e);
        }
        instance = new CachingUserStorage(new RdbUserStorage());
        instance.startInternal();
    }

    /**
     * Shutdown.
     */
    public static void stop() throws UserException {
        if (null == instance) {
            LOG.error("Duplicate shutdown of UserStorage.");
            return;
        }
        instance.stopInternal();
        instance = null;
    }

    /**
     * Creates a new instance implementing the user storage interface.
     * @return an instance implementing the user storage interface.
     */
    public static UserStorage getInstance() {
        return instance;
    }

    /**
     * Reads the data from a user from the underlying persistent data storage.
     *
     * @param uid
     *            User identifier.
     * @param context
     *            Context.
     * @return a user object or <code>null</code> on exception.
     */
    public static User getStorageUser(final int uid, final Context context) {
        try {
            return getInstance().getUser(uid, context);
        } catch (final LdapException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Reads the data from a user from the underlying persistent data storage.
     *
     * @param uid User identifier.
     * @param contextId Context ID.
     * @return a user object or <code>null</code> on exception.
     */
    public static User getStorageUser(final int uid, final int contextId) {
        try {
            return getInstance().getUser(uid, ContextStorage.getStorageContext(contextId));
        } catch (final LdapException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } catch (final ContextException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    static void loadProperties() throws LdapException {
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
    }
}
