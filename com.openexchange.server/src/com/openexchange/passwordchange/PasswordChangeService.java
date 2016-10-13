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

package com.openexchange.passwordchange;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.BasicAuthenticationService;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.user.UserService;

/**
 * {@link PasswordChangeService} - Performs changing a user's password
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public abstract class PasswordChangeService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeService.class);

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
     * @throws OXException If password update fails
     */
    public final void perform(final PasswordChangeEvent event) throws OXException {
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
     * @throws OXException If permission is denied to update password
     */
    protected void allow(final PasswordChangeEvent event) throws OXException {
        /*
         * At the moment security service is not used for timing reasons but is ought to be used later on
         */
        final Context context = event.getContext();
        if (!UserConfigurationStorage.getInstance().getUserConfiguration(event.getSession().getUserId(), context).isEditPassword()) {
            throw UserExceptionCode.PERMISSION.create(Integer.valueOf(context.getContextId()));
        }
        /*
         * TODO: Remove statements above and replace with commented call below
         */
        // checkBySecurityService();
    }

    /**
     * Specifies whether the old password is supposed to be checked prior to changing a user's password.
     * <p>
     * Default is true.
     *
     * @param event The event containing the session of the user whose password shall be changed, the context, the new password, and the old password (needed for verification)
     * @return <code>true</code> if the old password is supposed to be checked; otherwise <code>false</code>
     */
    protected boolean checkOldPassword(PasswordChangeEvent event) {
        return true;
    }

    /**
     * Check old password
     *
     * @param event The event containing the session of the user whose password shall be changed, the context, the new password, and the old
     *            password (needed for verification)
     * @throws OXException If old password is invalid
     */
    protected void check(final PasswordChangeEvent event) throws OXException {
        User user;
        try {
            /*
             * Check whether to verify old password prior to applying new one
             */
            boolean checkOldPassword = checkOldPassword(event);
            final AuthenticationService authenticationService = Authentication.getService();
            if (checkOldPassword && authenticationService == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AuthenticationService.class.getName());
            }
            UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
            if (null == userService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
            }
            /*
             * Loading user also verifies its existence
             */
            final Session session = event.getSession();
            user = userService.getUser(session.getUserId(), session.getContextId());
            /*
             * verify mandatory parameters
             */
            if (checkOldPassword && Strings.isEmpty(event.getOldPassword()) && false == user.isGuest()) {
                throw UserExceptionCode.MISSING_CURRENT_PASSWORD.create();
            }
            if (Strings.isEmpty(event.getNewPassword()) && false == user.isGuest()) {
                throw UserExceptionCode.MISSING_NEW_PASSWORD.create();
            }

            if (checkOldPassword) {
                Map<String, Object> properties = new LinkedHashMap<String, Object>(2);
                {
                    Map<String, List<String>> headers = event.getHeaders();
                    if (headers != null) {
                        properties.put("headers", headers);
                    }
                    com.openexchange.authentication.Cookie[] cookies = event.getCookies();
                    if (null != cookies) {
                        properties.put("cookies", cookies);
                    }
                }
                if (!user.isGuest()) {
                    Authentication.login(new _LoginInfo(session.getLogin(), event.getOldPassword(), properties), authenticationService);
                } else {
                    BasicAuthenticationService basicService = Authentication.getBasicService();
                    if (basicService == null) {
                        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(BasicAuthenticationService.class.getName());
                    }
                    basicService.handleLoginInfo(user.getId(), session.getContextId(), event.getOldPassword());
                }
            }
        } catch (final OXException e) {
            if (e.equalsCode(6, "LGI")) {
                /*
                 * Verification of old password failed
                 */
                throw UserExceptionCode.INCORRECT_CURRENT_PASSWORD.create(e);
            }
            throw e;
        }
        /*
         * Check min/max length restrictions
         */
        if (false == user.isGuest()) {
            final int len = event.getNewPassword().length();
            final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            int property = service.getIntProperty("com.openexchange.passwordchange.minLength", 4);
            if (property > 0 && len < property) {
                throw UserExceptionCode.INVALID_MIN_LENGTH.create(Integer.valueOf(property));
            }
            property = service.getIntProperty("com.openexchange.passwordchange.maxLength", 0);
            if (property > 0 && len > property) {
                throw UserExceptionCode.INVALID_MAX_LENGTH.create(Integer.valueOf(property));
            }
            /*
             * Check against "allowed" pattern if defined
             */
            String allowedPattern = service.getProperty("com.openexchange.passwordchange.allowedPattern");
            if (false == Strings.isEmpty(allowedPattern)) {
                try {
                    if (false == Pattern.matches(allowedPattern, event.getNewPassword())) {
                        String allowedPatternHint = service.getProperty("com.openexchange.passwordchange.allowedPatternHint");
                        throw UserExceptionCode.NOT_ALLOWED_PASSWORD.create(allowedPatternHint);
                    }
                } catch (PatternSyntaxException e) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "com.openexchange.passwordchange.allowedPattern");
                }
            }
            /*
             * No validation of new password since admin daemon does no validation, too
             */
            /*-
             * if (!validatePassword(event.getNewPassword())) {
             *     throw new OXException(OXException.Code.INVALID_PASSWORD);
             * }
             */
        }
    }

    /**
     * Actually updates the password in affected resources
     *
     * @param event The event containing the session of the user whose password shall be changed, the context, the new password, and the old
     *            password (needed for verification)
     * @see #getEncodedPassword(String, String)
     * @throws OXException If updating the password fails
     */
    protected abstract void update(final PasswordChangeEvent event) throws OXException;

    /**
     * Propagates changed password throughout system: invalidate caches, propagate to sub-systems like mail, etc.
     *
     * @param event The event containing the session of the user whose password shall be changed, the context, the new password, and the old
     *            password (needed for verification)
     * @throws OXException If propagating the password change fails
     */
    protected void propagate(final PasswordChangeEvent event) throws OXException {
        /*
         * Remove possible session-bound cached default mail access
         */
        final Session session = event.getSession();
        MailAccess.getMailAccessCache().removeMailAccess(session, MailAccount.DEFAULT_ID);
        /*
         * Invalidate user cache
         */
        final int userId = session.getUserId();
        UserStorage.getInstance().invalidateUser(event.getContext(), userId);
        final ServerServiceRegistry serviceRegistry = ServerServiceRegistry.getInstance();
        /*
         * Update password in session
         */
        final SessiondService sessiondService = serviceRegistry.getService(SessiondService.class);
        if (sessiondService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }
        try {
            sessiondService.changeSessionPassword(session.getSessionID(), event.getNewPassword());
        } catch (final OXException e) {
            LOG.error("Updating password in user session failed", e);
            throw e;
        }
        /*
         * post event
         */
        final EventAdmin eventAdmin = serviceRegistry.getService(EventAdmin.class);
        final int contextId = session.getContextId();
        if (null != eventAdmin) {
            final Map<String, Object> properties = new HashMap<String, Object>(5);
            properties.put("com.openexchange.passwordchange.contextId", Integer.valueOf(contextId));
            properties.put("com.openexchange.passwordchange.userId", Integer.valueOf(userId));
            properties.put("com.openexchange.passwordchange.session", session);
            properties.put("com.openexchange.passwordchange.oldPassword", event.getOldPassword());
            properties.put("com.openexchange.passwordchange.newPassword", event.getNewPassword());
            properties.put(CommonEvent.PUBLISH_MARKER, Boolean.TRUE);
            eventAdmin.postEvent(new Event("com/openexchange/passwordchange", properties));
        }
        /*
         * Invalidate caches manually
         */
        final CacheService cacheService = serviceRegistry.getService(CacheService.class);
        if (null != cacheService) {
            try {
                final CacheKey key = cacheService.newCacheKey(contextId, userId);
                Cache jcs = cacheService.getCache("User");
                jcs.remove(key);

                jcs = cacheService.getCache("UserPermissionBits");
                jcs.remove(key);

                jcs = cacheService.getCache("UserConfiguration");
                jcs.remove(key);

                jcs = cacheService.getCache("UserSettingMail");
                jcs.remove(key);

                jcs = cacheService.getCache("MailAccount");
                jcs.remove(cacheService.newCacheKey(contextId, String.valueOf(0), String.valueOf(userId)));
                jcs.remove(cacheService.newCacheKey(contextId, Integer.toString(userId)));
                jcs.invalidateGroup(Integer.toString(contextId));
            } catch (final OXException e) {
                // Ignore
            }
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
        private final Map<String, Object> properties;

        /**
         * Initializes a new {@link _LoginInfo}
         *
         * @param loginInfo The login info
         * @param pw The password
         */
        public _LoginInfo(String loginInfo, String pw, Map<String, Object> properties) {
            super();
            this.loginInfo = loginInfo;
            this.pw = pw;
            this.properties = properties;
        }

        @Override
        public String getPassword() {
            return pw;
        }

        @Override
        public String getUsername() {
            return loginInfo;
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
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
