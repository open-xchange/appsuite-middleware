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

package com.openexchange.ajax.login;

import static com.openexchange.java.Strings.isEmpty;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Charsets;
import com.openexchange.tools.encoding.Base64;

/**
 * {@link HashCalculator} - Calculates the hash string.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HashCalculator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HashCalculator.class);

    private static final String USER_AGENT = LoginFields.USER_AGENT;
    private static final String CLIENT_PARAM = LoginFields.CLIENT_PARAM;

    // -------------------------------    SINGLETON    ------------------------------------------------ //

    private static final HashCalculator SINGLETON = new HashCalculator();

    /**
     * Gets the singleton instance of {@link HashCalculator}.
     *
     * @return The instance
     */
    public static HashCalculator getInstance() {
        return SINGLETON;
    }

    // -------------------------------    MEMBER STUFF    -------------------------------------------- //

    private volatile String[] fields;
    private volatile byte[] salt;

    private HashCalculator() {
        super();
        fields = new String[0];
        salt = new byte[0];
    }

    /**
     * Configures this {@link HashCalculator} using specified configuration service.
     *
     * @param service The configuration service
     */
    public void configure(final ConfigurationService service) {
        if (null != service) {
            final String fieldList = service.getProperty("com.openexchange.cookie.hash.fields", "");
            fields = Pattern.compile("\\s*,\\s*").split(fieldList, 0);
            salt = service.getProperty("com.openexchange.cookie.hash.salt", "replaceMe1234567890").getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Gets the calculated hash string for specified request and client identifier.
     *
     * @param req The HTTP Servlet request
     * @param client The optional client identifier
     * @return The calculated hash string
     */
    public String getHash(final HttpServletRequest req, final String client) {
        return getHash(req, getUserAgent(req), client);
    }

    /**
     * Gets the calculated hash string for specified arguments.
     *
     * @param req The HTTP Servlet request
     * @param userAgent The optional <code>User-Agent</code> identifier
     * @param client The optional client identifier
     * @return The calculated hash string
     */
    public String getHash(final HttpServletRequest req, final String userAgent, final String client) {
        return getHash(req, userAgent, client, (String[])null);
    }

    /**
     * Gets the calculated hash string for specified arguments.
     *
     * @param req The HTTP Servlet request
     * @param userAgent The optional <code>User-Agent</code> identifier
     * @param client The optional client identifier
     * @param additionals Additional values to include in the hash, or <code>null</code> if not needed
     * @return The calculated hash string
     */
    public String getHash(final HttpServletRequest req, final String userAgent, final String client, String...additionals) {
        try {
            StringBuilder traceBuilder = LOG.isTraceEnabled() ? new StringBuilder("md5 (") : null;
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Update digest with User-Agent info
            {
                String effectiveUserAgent = null == userAgent ? parseClientUserAgent(req, "") : userAgent;
                md.update(effectiveUserAgent.getBytes(Charsets.UTF_8));
                if (null != traceBuilder) {
                    traceBuilder.append("effectiveUserAgent=").append(effectiveUserAgent);
                }
            }

            // Update digest with client info
            if (null != client) {
                md.update(client.getBytes(Charsets.UTF_8));
                if (null != traceBuilder) {
                    traceBuilder.append(", client=").append(client);
                }
            }

            // Update digest with additional info (if any)
            if (null != additionals) {
                for (String value : additionals) {
                    md.update(value.getBytes(Charsets.UTF_8));
                    if (null != traceBuilder) {
                        traceBuilder.append(", additional=").append(value);
                    }
                }
            }

            // Update digest with configured header info (if any)
            String[] fields = this.fields;
            if (null != fields) {
                for (final String field : fields) {
                    final String header = null == field || 0 == field.length() ? null : req.getHeader(field);
                    if (!isEmpty(header)) {
                        md.update(header.getBytes(Charsets.UTF_8));
                        if (null != traceBuilder) {
                            traceBuilder.append(", ").append(field).append('=').append(field);
                        }
                    }
                }
            }

            // Update digest with configured salt
            byte[] salt = this.salt;
            if (null != salt) {
                md.update(salt);
                if (null != traceBuilder) {
                    traceBuilder.append(", salt=***");
                }
            }

            // Calculate hash & create its string representation
            String hash = removeNonWordCharactersFrom(Base64.encode(md.digest()));
            if (null != traceBuilder) {
                traceBuilder.append(") -> ").append(hash);
                LOG.trace(traceBuilder.toString());
            }
            return hash;
        } catch (NoSuchAlgorithmException e) {
            LOG.error("", e);
        }
        return "";
    }

    /**
     * Gets the calculated hash string for specified request.
     *
     * @param req The HTTP Servlet request
     * @return The calculated hash string
     */
    public String getHash(final HttpServletRequest req) {
        return getHash(req, getClient(req));
    }

    /**
     * Gets the <code>"client"</code> request parameter or <code>"default"</code> if absent.
     *
     * @param req The HTTP Servlet request
     * @return The client identifier or <code>"default"</code> if absent
     */
    public static String getClient(final HttpServletRequest req) {
        final String parameter = req.getParameter(CLIENT_PARAM);
        return isEmpty(parameter) ? "default" : parameter;
    }

    /**
     * Gets the calculated hash string for user-agent only.
     *
     * @param req The HTTP Servlet request
     * @return The calculated hash string
     */
    public String getUserAgentHash(final HttpServletRequest req) {
        return getUserAgentHash(req, null);
    }

    /**
     * Gets the calculated hash string for user-agent only.
     *
     * @param req The HTTP Servlet request
     * @param userAgent The optional <code>User-Agent</code> identifier
     * @return The calculated hash string
     */
    public String getUserAgentHash(final HttpServletRequest req, final String userAgent) {
        final String md5 = com.openexchange.tools.HashUtility.getMD5(null == userAgent ? getUserAgent(req) : userAgent, "hex");
        return null == md5 ? "" : md5;
    }

    /**
     * Gets the <code>"clientUserAgent"</code> request parameter or given <code>defaultValue</code> if absent.
     *
     * @param req The request
     * @param defaultValue The default value
     * @return The <code>"clientUserAgent"</code> request parameter or given <code>defaultValue</code> if absent
     */
    private static String parseClientUserAgent(final HttpServletRequest req, final String defaultValue) {
        final String parameter = req.getParameter(USER_AGENT);
        return isEmpty(parameter) ? defaultValue : parameter;
    }

    /**
     * Gets the <code>"User-Agent"</code> request header or an empty String if absent.
     *
     * @param req The request
     * @return The <code>"User-Agent"</code> request header or an empty String if absent
     */
    public static String getUserAgent(final HttpServletRequest req) {
        String header = req.getHeader(Header.USER_AGENT);
        return header == null ? "" : header;
    }

    private static String removeNonWordCharactersFrom(String str) {
        if (null == str) {
            return null;
        }

        int length = str.length();
        if (length == 0) {
            return str;
        }

        StringBuilder sb = null;
        for (int i = 0, k = length; k-- > 0; i++) {
            char ch = str.charAt(i);
            if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9') && ch != '_') {
                // A non-word character
                if (null == sb) {
                    sb = new StringBuilder(length);
                    if (i > 0) {
                        sb.append(str, 0, i);
                    }
                }
            } else {
                // A word character
                if (null != sb) {
                    sb.append(ch);
                }
            }
        }

        return null == sb ? str : sb.toString();
    }

}
