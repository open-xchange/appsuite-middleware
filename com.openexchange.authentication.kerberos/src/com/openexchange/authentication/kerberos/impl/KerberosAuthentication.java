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

package com.openexchange.authentication.kerberos.impl;

import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING;
import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING;
import static com.openexchange.authentication.kerberos.impl.ConfigurationProperty.PROXY_DELIMITER;
import static com.openexchange.authentication.kerberos.impl.ConfigurationProperty.PROXY_USER;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.LoginException;
import com.openexchange.ajax.fields.Header;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.user.UserService;

/**
 * Implementes the authorization using a Kerberos backend. Two different inputs are possible:
 * - Login request contains a Negotiate authorization header
 * - Login and password are used to authorize the user against the Kerberos server.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class KerberosAuthentication implements AuthenticationService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(KerberosAuthentication.class);

    private final KerberosService kerberosService;
    private final ContextService contextService;
    private final UserService userService;
    private final String proxyDelimiter;
    private final String proxyUser;

    public KerberosAuthentication(KerberosService kerberosService, ContextService contextService, UserService userService, ConfigurationService configService) {
        super();
        this.kerberosService = kerberosService;
        this.contextService = contextService;
        this.userService = userService;
        proxyDelimiter = configService.getProperty(PROXY_DELIMITER.getName(), PROXY_DELIMITER.getDefault());
        proxyUser = configService.getProperty(PROXY_USER.getName(), PROXY_USER.getDefault());
    }

    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo) throws OXException {
        final String authHeader = parseAuthheader(loginInfo);
        final ClientPrincipal principal;
        String proxyAs = null;
        String uid = null;

        if (Authorization.checkForKerberosAuthorization(authHeader)) {
            final byte[] ticket = Base64.decode(authHeader.substring("Negotiate ".length()));
            principal = kerberosService.verifyAndDelegate(ticket);
        } else {
            uid = loginInfo.getUsername();
            if (null != proxyUser && null != proxyDelimiter && uid.contains(proxyDelimiter)) {
                proxyAs = uid.substring(uid.indexOf(proxyDelimiter) + proxyDelimiter.length(), uid.length());
                uid = uid.substring(0, uid.indexOf(proxyDelimiter));
                boolean foundProxy = false;
                for (final String pu : proxyUser.split(",")) {
                    if (pu.trim().equalsIgnoreCase(uid)) {
                        foundProxy = true;
                        break;
                    }
                }
                if (!foundProxy) {
                    LOG.error("none of the proxy user is matching");
                    throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                }
            }
            try {
                principal = kerberosService.authenticate(uid, loginInfo.getPassword());
            } catch (OXException e) {
                LOG.error(e.getMessage(), e);
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }
        }
        final String[] splitted = split(principal.getName());
        final int ctxId = contextService.getContextId(splitted[0]);
        if (ContextStorage.NOT_FOUND == ctxId) {
            throw INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING.create(splitted[0]);
        }
        final Context ctx = contextService.getContext(ctxId);
        final int userId;
        try {
            if (null != proxyAs) {
                userId = userService.getUserId(proxyAs, ctx);
            } else {
                userId = userService.getUserId(splitted[1], ctx);
            }
        } catch (OXException e) {
            if (LdapExceptionCode.USER_NOT_FOUND.equals(e)) {
                throw INVALID_CREDENTIALS_MISSING_USER_MAPPING.create(splitted[1]);
            }
            throw e;
        }
        userService.getUser(userId, ctx);
        if (null != proxyAs) {
            return new Authed(splitted[0], uid + proxyDelimiter + proxyAs, principal);
        }
        return new Authed(splitted[0], splitted[1], principal);
    }

    @Override
    public Authenticated handleAutoLoginInfo(LoginInfo loginInfo) {
        // Use this return to let the UI login screen be redirected to the /ajax/login/httpAuth for SPNEGO.
        // return new Redirect();
        return null;
    }

    private static String parseAuthheader(final LoginInfo loginInfo) {
        String retval = null;
        Object tmp = loginInfo.getProperties().get("headers");
        if (tmp instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> headers = (Map<String, List<String>>) tmp;
            tmp = headers.get(Header.AUTH_HEADER);
            if (tmp instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> values = (List<String>) tmp;
                if (values.size() > 0) {
                    retval = values.get(0);
                }
            }
        }
        return retval;
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     */
    private static String[] split(final String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @param separator for spliting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no seperator is found.
     */
    private static String[] split(final String loginInfo, final char separator) {
        final int pos = loginInfo.lastIndexOf(separator);
        final String[] splitted;
        if (-1 == pos) {
            splitted = new String[] { "defaultcontext", loginInfo };
        } else {
            splitted = new String[] { loginInfo.substring(pos + 1), loginInfo.substring(0, pos) };
        }
        return splitted;
    }
}
