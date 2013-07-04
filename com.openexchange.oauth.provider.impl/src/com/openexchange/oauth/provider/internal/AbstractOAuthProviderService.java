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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal;

import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.oauth.OAuthServiceProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link AbstractOAuthProviderService} - The abstract class for OAuth providers.
 * <p>
 * <a href="http://wiki.oauth.net/w/page/12238543/ProblemReporting">OAuth error codes</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractOAuthProviderService implements OAuthProviderConstants {

    /**
     * The dummy object.
     */
    protected static final Object PRESENT = new Object();

    /**
     * The service look-up instance.
     */
    protected final ServiceLookup services;

    /**
     * The OAuth/OAuthv2 service provider.
     */
    protected final OAuthServiceProvider provider;

    /**
     * The secret string.
     */
    private final String secret;

    /**
     * The secret property names.
     */
    private static final Set<String> SECRET_PROP_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PROP_PASSWORD, PROP_LOGIN)));

    /**
     * Initializes a new {@link AbstractOAuthProviderService}.
     *
     * @throws OXException If service provider cannot be loaded from database
     */
    protected AbstractOAuthProviderService(final ServiceLookup services) throws OXException {
        super();
        this.services = services;
        // Load provider
        provider = loadServiceProvider();
        final ConfigurationService service = services.getService(ConfigurationService.class);
        secret = service.getProperty("com.openexchange.oauth.provider.secret", "f58c636e089745d4a79679d726aca8b5");
    }

    /**
     * Gets the property names of secret properties.
     *
     * @return The secret property names.
     */
    protected static Set<String> getSecretPropertyNames() {
        return SECRET_PROP_NAMES;
    }

    /**
     * Gets the <tt>OAuthServiceProvider</tt> instance
     *
     * @return The provider
     */
    public OAuthServiceProvider getProvider() {
        return provider;
    }

    /**
     * Loads the service provider.
     *
     * @return The service provider
     * @throws OXException If loading fails
     */
    private OAuthServiceProvider loadServiceProvider() throws OXException {
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        final Connection con = databaseService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                con.prepareStatement("SELECT requestTokenUrl, userAuthorizationUrl, accessTokenURL FROM oauthServiceProvider WHERE id=?");
            stmt.setInt(1, DEFAULT_PROVIDER);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw OAuthProviderExceptionCodes.PROVIDER_NOT_FOUND.create(Integer.valueOf(DEFAULT_PROVIDER));
            }
            return new OAuthServiceProvider(rs.getString(1), rs.getString(2), rs.getString(3));
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(con);
        }
    }

    /**
     * Gets available context identifier.
     *
     * @return The context identifiers
     * @throws OXException If identifiers cannot be loaded from configDB
     */
    protected TIntList getContextIds() throws OXException {
        return getContextIds(services.getService(DatabaseService.class));
    }

    /**
     * Gets available context identifier.
     *
     * @param optService The optional database service
     * @return The context identifiers
     * @throws OXException If identifiers cannot be loaded from configDB
     */
    protected TIntList getContextIds(final DatabaseService optService) throws OXException {
        final DatabaseService databaseService = null == optService ? services.getService(DatabaseService.class) : optService;
        final Connection con = databaseService.getReadOnly();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid FROM context");
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return new TIntLinkedList();
            }
            final TIntList ret = new TIntLinkedList();
            do {
                ret.add(rs.getInt(1));
            } while (rs.next());
            return ret;
        } catch (final SQLException e) {
            throw OAuthProviderExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(con);
        }
    }

    protected static Object valueOf(final String value) {
        if (isEmpty(value)) {
            return null;
        }
        /*
         * If it is true, false, or null, return the proper value.
         */
        final String s = value.trim();
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("null")) {
            return null;
        }
        /*
         * If it might be a number, try converting it. We support the 0- and 0x- conventions. If a number cannot be produced, then the value
         * will just be a string. Note that the 0-, 0x-, plus, and implied string conventions are non-standard. A JSON parser is free to
         * accept non-JSON forms as long as it accepts all correct JSON forms.
         */
        final char b = s.charAt(0);
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            if (b == '0') {
                if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                    try {
                        return Integer.valueOf(Integer.parseInt(s.substring(2), 16));
                    } catch (final Exception e) {
                        /* Ignore the error */
                    }
                } else {
                    try {
                        return Integer.valueOf(Integer.parseInt(s, 8));
                    } catch (final Exception e) {
                        /* Ignore the error */
                    }
                }
            }
            try {
                return Integer.valueOf(s);
            } catch (final Exception e) {
                try {
                    return Long.valueOf(s);
                } catch (final Exception f) {
                    try {
                        return Double.valueOf(s);
                    } catch (final Exception g) {
                        return s;
                    }
                }
            }
        }
        return s;
    }

    protected static String stringOf(final String value) {
        return isEmpty(value) ? null : value;
    }

    protected static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    protected static final boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equalsIgnoreCase(table));
        } finally {
            DBUtils.closeSQLStuff(rs);
        }
        return retval;
    }

    /**
     * Encrypts specified string.
     *
     * @param toEncrypt The string to encrypt
     * @return The encrypted string
     * @throws OXException If operation fails
     * @throws IllegalStateException If service is missing
     */
    protected String encrypt(final String toEncrypt) throws OXException {
        if (isEmpty(toEncrypt)) {
            return toEncrypt;
        }
        final CryptoService service = services.getService(CryptoService.class);
        if (null == service) {
            throw new IllegalStateException("Missing service: " + CryptoService.class);
        }
        return service.encrypt(toEncrypt, secret);
    }

    /**
     * Decrypts specified string.
     *
     * @param toDecrypt The string to decrypt
     * @return The decrypted string
     * @throws OXException If operation fails
     * @throws IllegalStateException If service is missing
     */
    protected String decrypt(final String toDecrypt) throws OXException {
        if (isEmpty(toDecrypt)) {
            return toDecrypt;
        }
        final CryptoService service = services.getService(CryptoService.class);
        if (null == service) {
            throw new IllegalStateException("Missing service: " + CryptoService.class);
        }
        return service.decrypt(toDecrypt, secret);
    }

}
