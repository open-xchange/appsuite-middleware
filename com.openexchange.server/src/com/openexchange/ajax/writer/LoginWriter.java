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

package com.openexchange.ajax.writer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.login.ConfigurationProperty;
import com.openexchange.login.LoginResult;
import com.openexchange.login.multifactor.MultifactorChecker;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * JSON writer for login responses.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class LoginWriter {

    private static final Locale DEFAULT_LOCALE = Locale.US;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginWriter.class);

    /**
     * Initializes a new {@link LoginWriter}.
     */
    public LoginWriter() {
        super();
    }

    /**
     * Writes JSON login response.
     *
     * @param result The login result
     * @param json The JSON object to write to
     * @throws JSONException If writing to JSON object fails
     */
    public void writeLogin(final LoginResult result, final JSONObject json) throws JSONException {
        write(result, json);
    }

    /**
     * Writes JSON login response.
     *
     * @param session The session
     * @param json The JSON object to write to
     * @throws JSONException If writing to JSON object fails
     */
    public void writeLogin(final Session session, final JSONObject json) throws JSONException {
        write(session, json);
    }

    /**
     * Writes JSON login response.
     *
     * @param result The login result
     * @param json The JSON object to write to
     * @throws JSONException If writing to JSON object fails
     */
    public static void write(final LoginResult result, final JSONObject json) throws JSONException {
        write(result.getSession(), json, result.warnings(), result.getUser().getLocale());
    }

    /**
     * Writes JSON login response.
     *
     * @param session The session
     * @param json The JSON object to write to
     * @param warnings The (probably empty) warnings
     * @throws JSONException If writing to JSON object fails
     */
    public static void write(final Session session, final JSONObject json) throws JSONException {
        Locale locale = null;
        if (session instanceof ServerSession) {
            locale = ((ServerSession) session).getUser().getLocale();
        } else {
            try {
                locale = UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
            } catch (Exception e) {
                // Ignore
            }
        }
        write(session, json, Collections.<OXException> emptyList(), locale);
    }

    public static void write(Session session, JSONObject json, Locale locale) throws JSONException {
        write(session, json, Collections.<OXException> emptyList(), locale);
    }

    private static final String PARAMETER_USER_ID = AJAXServlet.PARAMETER_USER_ID;
    private static final String PARAMETER_CONTEXT_ID = "context_id";
    private static final String PARAMETER_USER = AJAXServlet.PARAMETER_USER;
    private static final String RANDOM_PARAM = LoginFields.RANDOM_PARAM;
    private static final String PARAMETER_SESSION = AJAXServlet.PARAMETER_SESSION;
    private static final String PARAMETER_LOCALE = "locale";
    private static final String PARAMETER_MULTIFACTOR = "requires_multifactor";

    private static volatile Boolean randomTokenEnabled;
    private static boolean randomTokenEnabled() {
        Boolean tmp = randomTokenEnabled;
        if (null == tmp) {
            synchronized (LoginWriter.class) {
                tmp = randomTokenEnabled;
                if (null == tmp) {
                    final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (configurationService == null) {
                        LOG.warn("Unable to get ConfigurationService, refusing to write random.");
                        return false;
                    }
                    tmp = Boolean.valueOf(configurationService.getBoolProperty(ConfigurationProperty.RANDOM_TOKEN.getPropertyName(), false));
                    randomTokenEnabled = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    /**
     * Invalidates cached setting for <code>"com.openexchange.ajax.login.randomToken"</code>.
     *
     * @see ConfigurationProperty#RANDOM_TOKEN
     */
    public static void invalidateRandomTokenEnabled() {
        randomTokenEnabled = null;
    }

    private static void write(final Session session, final JSONObject json, final Collection<OXException> warnings, final Locale locale) throws JSONException {
        // Random token
        if (randomTokenEnabled()) {
            json.put(RANDOM_PARAM, session.getRandomToken());
        }
        json.put(PARAMETER_SESSION, session.getSessionID());
        // Login
        json.put(PARAMETER_USER, prepareLogin(session.getLogin()));
        json.put(PARAMETER_USER_ID, session.getUserId());
        json.put(PARAMETER_CONTEXT_ID, session.getContextId());
        final Locale loc = locale == null ? resolveLocaleForUser(session, DEFAULT_LOCALE) : locale;
        if (MultifactorChecker.requiresMultifactor(session)) {  // Multifactor authentication required
            json.put(PARAMETER_MULTIFACTOR, true);
        }
        json.put(PARAMETER_LOCALE, loc.toString());
        if (null != warnings && !warnings.isEmpty()) {
            /*
             * Write warnings
             */
            final OXJSONWriter writer = new OXJSONWriter(json);
            final List<OXException> list = (warnings instanceof List) ? ((List<OXException>) warnings) : new ArrayList<OXException>(
                warnings);
            ResponseWriter.writeWarnings(list, writer, loc);
        }
    }


    private static String prepareLogin(final String login) {
        if (null == login) {
            return null;
        }
        // Possibly an E-Mail address
        try {
            return QuotedInternetAddress.toIDN(login);
        } catch (Exception x) {
            return login;
        }
    }

    private static Locale resolveLocaleForUser(final Session session, final Locale defaultLocale) {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        try {
            return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
        } catch (Exception e) {
            return defaultLocale;
        }
    }

}
