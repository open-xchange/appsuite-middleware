/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.authentication.kerberos.impl;

import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_CONTEXT_MAPPING;
import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING;
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
            @SuppressWarnings("unchecked") Map<String, List<String>> headers = (Map<String, List<String>>) tmp;
            List<String> values = headers.get(Header.AUTH_HEADER);
            if (values != null && values.size() > 0) {
                retval = values.get(0);
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
