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

package com.openexchange.passwordchange;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginException;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.caching.CacheException;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.mail.cache.MailAccessCache;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.passwordchange.mechs.SHACrypt;
import com.openexchange.passwordchange.mechs.UnixCrypt;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.exception.SessiondException;

/**
 * {@link PasswordChangeService} - Performs changing a user's password
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class PasswordChangeService {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(PasswordChangeService.class);

    private static final String MECH_CRYPT = "{CRYPT}";

    private static final String MECH_SHA = "{SHA}";

    /**
     * Initializes a new {@link PasswordChangeService}
     */
    protected PasswordChangeService() {
        super();
    }

    /**
     * Performs the password update.
     * 
     * @param event The event containing the session of the user whose password shall be changed, the context, the new password, and the old
     *            password (needed for verification)
     * @throws UserException If password update fails
     */
    public final void perform(final PasswordChangeEvent event) throws UserException {
        allow(event);
        check(event);
        update(event);
        propagate(event);
    }

    /**
     * Checks permission
     * 
     * @param event The event containing the session of the user whose password shall be changed, the context, the new password, and the old
     *            password (needed for verification)
     * @throws UserException If permission is denied to update password
     */
    protected void allow(final PasswordChangeEvent event) throws UserException {
        /*
         * At the moment security service is not used for timing reasons but is ought to be used later on
         */
        try {
            if (!UserConfigurationStorage.getInstance().getUserConfiguration(event.getSession().getUserId(), event.getContext()).isEditPassword()) {
                throw new UserException(UserException.Code.PERMISSION, Integer.valueOf(event.getContext().getContextId()));
            }
        } catch (final UserConfigurationException e1) {
            throw new UserException(e1);
        }
        /*
         * TODO: Remove statements above and replace with commented call below
         */
        // checkBySecurityService();
    }

    /**
     * Check old password
     * 
     * @param event The event containing the session of the user whose password shall be changed, the context, the new password, and the old
     *            password (needed for verification)
     * @throws UserException If old password is invalid
     */
    protected void check(final PasswordChangeEvent event) throws UserException {
        /*
         * Verify old password prior to applying new one
         */
        final AuthenticationService authenticationService = Authentication.getService();
        if (authenticationService == null) {
            throw new UserException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, AuthenticationService.class.getName()));
        }
        try {
            /*
             * Loading user also verifies its existence
             */
            UserStorage.getStorageUser(event.getSession().getUserId(), event.getContext());
            final String login = event.getSession().getLogin();
            authenticationService.handleLoginInfo(new _LoginInfo(login, event.getOldPassword()));
        } catch (final LoginException e) {
            /*
             * Verification of old password failed
             */
            throw new UserException(e);
        }
        /*
         * No validation of new password since admin daemon does no validation, too
         */
        /*-
         * if (!validatePassword(event.getNewPassword())) {
         *     throw new UserException(UserException.Code.INVALID_PASSWORD);
         * }
         */
    }

    /**
     * Actually updates the password in affected resources
     * 
     * @param event The event containing the session of the user whose password shall be changed, the context, the new password, and the old
     *            password (needed for verification)
     * @see #getEncodedPassword(String, String)
     * @throws UserException If updating the password fails
     */
    protected abstract void update(final PasswordChangeEvent event) throws UserException;

    /**
     * Propagates changed password throughout system: invalidate caches, propagate to sub-systems like mail, etc.
     * 
     * @param event The event containing the session of the user whose password shall be changed, the context, the new password, and the old
     *            password (needed for verification)
     * @throws UserException If propagating the password change fails
     */
    protected void propagate(final PasswordChangeEvent event) throws UserException {
        /*
         * Remove possible session-bound cached default mail access
         */
        try {
            MailAccessCache.getInstance().removeMailAccess(event.getSession(), MailAccount.DEFAULT_ID);
        } catch (final CacheException e) {
            LOG.error("Removing cached mail access failed", e);
            throw new UserException(e);
        }
        /*
         * Invalidate user cache
         */
        UserStorage.getInstance().invalidateUser(event.getContext(), event.getSession().getUserId());
        /*
         * Update password in session
         */
        final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (sessiondService == null) {
            throw new UserException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, SessiondService.class.getName()));
        }
        try {
            sessiondService.changeSessionPassword(event.getSession().getSessionID(), event.getNewPassword());
        } catch (final SessiondException e) {
            LOG.error("Updating password in user session failed", e);
            throw new UserException(e);
        }
    }

    /**
     * Utility method to encode given <code>newPassword</code> according to specified encoding mechanism
     * 
     * @param mech The encoding mechanism; currently supported values: <code>&quot;{CRYPT}&quot;</code> and <code>&quot;{SHA}&quot;</code>
     * @param newPassword The new password to encode
     * @return The encoded password
     * @throws UserException If encoding the new password fails
     */
    protected static final String getEncodedPassword(final String mech, final String newPassword) throws UserException {
        if (MECH_CRYPT.equals(mech)) {
            try {
                return UnixCrypt.crypt(newPassword);
            } catch (final UnsupportedEncodingException e) {
                LOG.error("Error encrypting password according to CRYPT mechanism", e);
                throw new UserException(UserException.Code.UNSUPPORTED_ENCODING, e, e.getMessage());
            }
        } else if (MECH_SHA.equals(mech)) {
            try {
                return SHACrypt.makeSHAPasswd(newPassword);
            } catch (final NoSuchAlgorithmException e) {
                LOG.error("Error encrypting password according to SHA mechanism", e);
                throw new UserException(UserException.Code.UNSUPPORTED_ENCODING, e, e.getMessage());
            } catch (final UnsupportedEncodingException e) {
                LOG.error("Error encrypting password according to SHA mechanism", e);
                throw new UserException(UserException.Code.UNSUPPORTED_ENCODING, e, e.getMessage());
            }
        } else {
            throw new UserException(UserException.Code.MISSING_PASSWORD_MECH, mech == null ? "" : mech);
        }
    }

    /*-
     * +++++++++++++++ _LoginInfo +++++++++++++++
     */

    /**
     * {@link _LoginInfo} - Simple class that implements {@link LoginInfo}
     * 
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    protected static final class _LoginInfo implements LoginInfo {

        private final String pw;

        private final String loginInfo;

        /**
         * Initializes a new {@link _LoginInfo}
         * 
         * @param loginInfo The login info
         * @param pw The password
         */
        public _LoginInfo(final String loginInfo, final String pw) {
            super();
            this.loginInfo = loginInfo;
            this.pw = pw;
        }

        public String getPassword() {
            return pw;
        }

        public String getUsername() {
            return loginInfo;
        }

    }

    /*-
     * +++++++++++++++ Utility methods +++++++++++++++
     */

    private static final Pattern PATTERN_ALLOWED_CHARS = Pattern.compile("[ $@%\\.+a-zA-Z0-9_-]+");

    /**
     * Checks if specified password string contains invalid characters.
     * <p>
     * Valid characters are:<br>
     * &quot;<i>&nbsp; abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ -+.%$@<i>&quot;
     * 
     * @param password The password string to check
     * @return <code>true</code> if specified password string only consists of valid characters; otherwise <code>false</code>
     */
    protected static final boolean validatePassword(final String password) {
        /*
         * Check for allowed chars: abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@
         */
        return PATTERN_ALLOWED_CHARS.matcher(password).matches();
    }
}
