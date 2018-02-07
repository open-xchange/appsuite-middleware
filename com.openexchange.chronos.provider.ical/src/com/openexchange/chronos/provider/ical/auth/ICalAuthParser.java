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

package com.openexchange.chronos.provider.ical.auth;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.auth.info.AuthType;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccountAttribute;
import com.openexchange.chronos.provider.ical.auth.AdvancedAuthInfo.Builder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * 
 * {@link ICalAuthParser}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalAuthParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalAuthParser.class);

    private static final ICalAuthParser INSTANCE = new ICalAuthParser();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static ICalAuthParser getInstance() {
        return INSTANCE;
    }

    private ICalAuthParser() {
        super();
    }

    public AdvancedAuthInfo getEncryptedFromDecrypted(Session session, JSONObject config) throws OXException {
        return getAuthInfoFromUnstructured(session, config, true);
    }

    public AdvancedAuthInfo getDecryptedFromEncyrpted(Session session, JSONObject config) throws OXException {
        return getAuthInfoFromUnstructured(session, config, false);
    }

    private AdvancedAuthInfo getAuthInfoFromUnstructured(Session session, JSONObject config, boolean encrypt) throws OXException {
        JSONObject userConfiguration = new JSONObject(config);
        try {
            Set<CalendarAccountAttribute> availableAttributes = parse(userConfiguration);
            Set<CalendarAccountAttribute> authAttributes = validate(availableAttributes);
            AdvancedAuthInfo authInfo = generateAuthInfo(session, userConfiguration, authAttributes, encrypt);
            return authInfo;
        } catch (JSONException e) {
            LOG.error("Unable to recognize auth information. Will try with no auth.", e);
        }
        return AdvancedAuthInfo.NONE_ADVANCED;
    }

    /**
     * Parses the attributes from the configuration and adapts them to be valid for further processing
     *
     * @param configuration
     * @return Set of {@link CalendarAccountAttribute}s contained within the configuration map
     */
    private Set<CalendarAccountAttribute> parse(final JSONObject configuration) throws JSONException {
        final Set<CalendarAccountAttribute> attributes = new HashSet<CalendarAccountAttribute>();

        String login = CalendarAccountAttribute.LOGIN_LITERAL.getName();
        if (configuration.has(login)) {
            attributes.add(CalendarAccountAttribute.LOGIN_LITERAL);
        }

        String id = CalendarAccountAttribute.ID_LITERAL.getName();
        if (configuration.has(id)) {
            attributes.add(CalendarAccountAttribute.ID_LITERAL);
        }

        String password = CalendarAccountAttribute.PASSWORD_LITERAL.getName();
        if (configuration.has(password)) {
            configuration.put(password, parseString(configuration, password));
            attributes.add(CalendarAccountAttribute.PASSWORD_LITERAL);
        }

        String token = CalendarAccountAttribute.TOKEN_LITERAL.getName();
        if (configuration.has(token)) {
            attributes.add(CalendarAccountAttribute.TOKEN_LITERAL);
        }
        return attributes;
    }

    private static String parseString(final JSONObject configuration, final String name) throws JSONException {
        String retval = null;
        if (configuration.has(name)) {
            final String test = configuration.getString(name);
            if (0 != test.length()) {
                retval = test;
            }
        }
        return retval;
    }

    private Set<CalendarAccountAttribute> validate(Set<CalendarAccountAttribute> attributes) throws OXException {
        Set<CalendarAccountAttribute> copy = new HashSet<>(attributes);
        copy.remove(CalendarAccountAttribute.LOGIN_LITERAL);
        copy.remove(CalendarAccountAttribute.ID_LITERAL);

        if (copy.size() > 1) {
            throw CalendarExceptionCodes.BAD_AUTH_CONFIGURATION.create(copy.toString());
        }
        return copy;
    }

    private AdvancedAuthInfo generateAuthInfo(Session session, JSONObject configuration, Set<CalendarAccountAttribute> authAttributes, boolean encrypt) throws JSONException, OXException {
        AdvancedAuthInfo authInfo = null;
        Builder builder = AdvancedAuthInfo.builder();
        if (authAttributes.contains(CalendarAccountAttribute.TOKEN_LITERAL)) {
            authInfo = builder.setAuthType(AuthType.TOKEN).setToken((String) configuration.get(CalendarAccountAttribute.TOKEN_LITERAL.getName())).build();
        } else if (authAttributes.contains(CalendarAccountAttribute.LOGIN_LITERAL) || authAttributes.contains(CalendarAccountAttribute.PASSWORD_LITERAL)) {
            builder.setAuthType(AuthType.BASIC);
            String login = configuration.optString(CalendarAccountAttribute.LOGIN_LITERAL.getName());
            if (Strings.isNotEmpty(login)) {
                builder.setLogin(login);
            }
            String feedPassword = configuration.optString(CalendarAccountAttribute.PASSWORD_LITERAL.getName());
            if (Strings.isNotEmpty(feedPassword)) {
                if (!encrypt) {
                    builder.setPassword(ICalAuthParser.decrypt(feedPassword, session.getPassword()));
                    builder.setEncryptedPassword(feedPassword);
                }
                if (encrypt) {
                    builder.setPassword(feedPassword);
                    builder.setEncryptedPassword(ICalAuthParser.encrypt(feedPassword, session.getPassword()));
                }
            }
            authInfo = builder.build();
        } else {
            authInfo = builder.setAuthType(AuthType.NONE).build();
        }
        return authInfo;
    }

    public static void encrypt(JSONObject userConfiguration, String password) throws OXException {
        if (userConfiguration.has("password")) {
            try {
                String encrypt = encrypt(parseString(userConfiguration, "password"), password);
                userConfiguration.put("password", encrypt);
            } catch (JSONException e) {
                LOG.error("Unable to encrypt password in user configuration.", e);
            }
        }
    }

    private static String encrypt(String passwordToEncrypt, String passwordToEncryptWith) throws OXException {
        return PasswordUtil.encrypt(passwordToEncrypt, passwordToEncryptWith);
    }

    private static String decrypt(String passwordToEncrypt, String passwordToEncryptWith) throws OXException {
        return PasswordUtil.decrypt(passwordToEncrypt, passwordToEncryptWith);
    }
}
