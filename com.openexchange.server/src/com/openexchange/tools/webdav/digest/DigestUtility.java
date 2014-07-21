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

package com.openexchange.tools.webdav.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HeaderElement;
import org.apache.http.message.BasicHeaderValueParser;
import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserAttributeAccess;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.webdav.WebdavExceptionCode;

/**
 * {@link DigestUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DigestUtility {

    private static final DigestUtility instance = new DigestUtility();

    /**
     * Gets the {@link DigestUtility} instance.
     *
     * @return The instance
     */
    public static DigestUtility getInstance() {
        return instance;
    }

    /*
     * Member stuff
     */

    private final String nonceKey;

    private final MessageDigest md5Digest;

    /**
     * Initializes a new {@link DigestUtility}.
     */
    private DigestUtility() {
        super();
        nonceKey = UUIDs.getUnformattedString(UUID.randomUUID());
        try {
            md5Digest = MessageDigest.getInstance("md5");
        } catch (final NoSuchAlgorithmException e) {
            /*
             * Cannot occur
             */
            throw new IllegalStateException("No MD5 algorithm found", e);
        }
    }

    /**
     * Generates the server digest from given HTTP request (containing <code>"Authorization"</code> header) and given password.
     *
     * @param req The HTTP request
     * @param password The password
     * @return The generated digest
     */
    public String generateServerDigest(final HttpServletRequest req, final String password) {
        return generateServerDigest(req.getHeader("Authorization"), req.getMethod(), password);
    }

    private String generateServerDigest(final String auth, final String method, final String password) {
        if ((auth == null) || (!auth.startsWith("Digest ")) || (method == null) || (password == null)) {
            return null;
        }
        final Authorization localAuthorization = parseDigestAuthorization(auth);
        if (localAuthorization == null) {
            return null;
        }
        /*
         * Calculate first digest from: "<userName>" + ":" + "<realmName>" + ":" + "<passWord>"
         */
        final StringBuilder tmp = new StringBuilder(128);
        final String firstDigest =
            digest2HexString(md5Digest.digest(tmp.append(localAuthorization.user).append(':').append(localAuthorization.realm).append(':').append(
                password).toString().getBytes()));
        /*
         * Calculate second digest: "<method>" + ":" + "<URI>"
         */
        tmp.setLength(0);
        final String secondDigest =
            digest2HexString(md5Digest.digest((tmp.append(method).append(':').append(localAuthorization.uri).toString()).getBytes()));
        /*-
         * Calculate third digest dependent on "qop" value:
         * if qop is missing or not equal to "auth": digest1 + ":" + "<nonceValue>" + ":" + digest2
         * else: digest1 + ":" + "<nonceValue>" + ":" + "<ncValue>" + ":" + "<cnonceValue>" + ":" + "<qopValue>" + digest2
         */
        tmp.setLength(0);
        if ("auth".equals(localAuthorization.qop)) {
            tmp.append(firstDigest).append(':').append(localAuthorization.nOnce).append(':').append(localAuthorization.nc).append(':').append(
                localAuthorization.cnonce).append(':').append(localAuthorization.qop).append(':').append(secondDigest);
        } else {
            tmp.append(firstDigest).append(':').append(localAuthorization.nOnce).append(':').append(secondDigest);
        }
        return digest2HexString(md5Digest.digest(tmp.toString().getBytes()));
    }

    /**
     * Generates the HEX string from specified digest.
     *
     * @param digest The digest
     * @return The HEX string
     */
    public String digest2HexString(final byte[] digest) {
        final StringBuilder digestString = new StringBuilder(32);
        for (final byte b : digest) {
            digestString.append(Integer.toHexString(((b & 0xf0) >> 4))); // high
            digestString.append(Integer.toHexString((b & 0x0f))); // low
        }
        return digestString.toString();
    }

    /**
     * Generates the NOnce from given HTTP request.
     *
     * @param req The HTTP request
     * @return The NOnce
     */
    public String generateNOnce(final HttpServletRequest req) {
        final long currentTime = System.currentTimeMillis();
        final byte buffer[] =
            md5Digest.digest(new StringBuilder(req.getRemoteAddr()).append(':').append(currentTime).append(':').append(nonceKey).toString().getBytes());
        return digest2HexString(buffer);
    }

    /**
     * The regular expression to parse parameters
     */
    private static final Pattern PATTERN_DIGEST_LIST;

    static {
        final String paramNameRegex = "([\\p{ASCII}&&[^=\"\\s;]]+)";
        final String tokenRegex = "(?:[^\"][\\S&&[^\\s,;:\\\\\"/\\[\\]?()<>@]]*)";
        final String quotedStringRegex = "(?:\"(?:(?:\\\\\\\")|[^\"])+?\")"; // Grab '\"' char sequence or any non-quote character
        PATTERN_DIGEST_LIST =
            Pattern.compile("(?:\\s*,\\s*|\\s*)" + paramNameRegex + "(?: *= *(" + tokenRegex + '|' + quotedStringRegex + "))?");
    }

    private static Map<String, String> auth2map(final String auth) {
        HeaderElement[] elements = BasicHeaderValueParser.parseElements(auth, null);
        Map<String, String> map = new HashMap<String, String>(elements.length);
        for (HeaderElement element : elements) {
            map.put(element.getName(), element.getValue());
        }
        return map;
    }

    /**
     * Parses specified <code>"Authorization"</code> header value.
     *
     * @param authorization The <code>"Authorization"</code> header value
     * @return The parsed <code>"Authorization"</code> header value
     */
    public Authorization parseDigestAuthorization(final String authorization) {
        if (null == authorization) {
            return null;
        }
        final Map<String, String> map =
            auth2map(authorization.startsWith("Digest ") ? authorization.substring(7).trim() : authorization.trim());
        final Authorization ret = new Authorization();
        {
            final String tmp = map.get("username");
            if (null != tmp) {
                ret.user = tmp;
            }
        }
        {
            final String tmp = map.get("realm");
            if (null != tmp) {
                ret.realm = tmp;
            }
        }
        {
            final String tmp = map.get("nonce");
            if (null != tmp) {
                ret.nOnce = tmp;
            }
        }
        {
            final String tmp = map.get("nc");
            if (null != tmp) {
                ret.nc = tmp;
            }
        }
        {
            final String tmp = map.get("cnonce");
            if (null != tmp) {
                ret.cnonce = tmp;
            }
        }
        {
            final String tmp = map.get("qop");
            if (null != tmp) {
                ret.qop = tmp;
            }
        }
        {
            final String tmp = map.get("uri");
            if (null != tmp) {
                ret.uri = tmp;
            }
        }
        {
            final String tmp = map.get("response");
            if (null != tmp) {
                ret.response = tmp;
            }
        }
        {
            final String tmp = map.get("algorithm");
            if (null != tmp) {
                ret.algorithm = tmp;
            }
        }
        return ret;
    }

    /**
     * Gets the clear-text password by specified user name.
     *
     * @param userName The user name
     * @return The clear-text password or <code>null</code> if not available
     * @throws OXException If clear-text password cannot be returned
     */
    public String getPasswordByUserName(final String userName) throws OXException {
        if (null == userName) {
            return null;
        }
        final String[] splitted = split(userName);
        final ContextStorage contextStorage = ContextStorage.getInstance();
        final int ctxId = contextStorage.getContextId(splitted[0]);
        if (ContextStorage.NOT_FOUND == ctxId) {
            throw WebdavExceptionCode.RESOLVING_USER_NAME_FAILED.create(userName);
        }
        final Context ctx = contextStorage.getContext(ctxId);
        final int userId;
        final UserStorage userStorage = UserStorage.getInstance();
        try {
            userId = userStorage.getUserId(splitted[1], ctx);
        } catch (final OXException e) {
            throw WebdavExceptionCode.RESOLVING_USER_NAME_FAILED.create(userName);
        }
        final User user = userStorage.getUser(userId, ctx);
        /*
         * Lookup encrypted password in user attributes
         */
        final String passCrypt = UserAttributeAccess.getDefaultInstance().getAttribute("passcrypt", user, null);
        final ServerServiceRegistry serviceRegistry = ServerServiceRegistry.getInstance();
        final CryptoService cryptoService = serviceRegistry.getService(CryptoService.class, true);
        final String key = serviceRegistry.getService(ConfigurationService.class).getProperty("com.openexchange.passcrypt.key");
        return cryptoService.decrypt(passCrypt, key);
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     */
    private String[] split(final String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information seperated by an @ sign.
     * @param separator for spliting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws OXException if no seperator is found.
     */
    private String[] split(final String loginInfo, final char separator) {
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
