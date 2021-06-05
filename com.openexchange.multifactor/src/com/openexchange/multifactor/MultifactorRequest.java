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

package com.openexchange.multifactor;

import java.util.Locale;
import java.util.Objects;
import com.openexchange.session.Session;

/**
 * {@link MultifactorRequest} holds general information to perform multi-factor authentication for a user
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorRequest {

    private final int userId;
    private final int contextId;
    private String login;
    private final String host;
    private Locale locale;

    /**
     * Initializes a new {@link MultifactorRequest}.
     *
     * @param contextId The contextId
     * @param userId The userId
     * @param host The host related to this session
     * @param locale The locale related to this session, or null if the locale is unknown
     */
    public MultifactorRequest(int contextId, int userId, String login, String host, Locale locale) {
        this.contextId = contextId;
        this.userId = userId;
        this.login = login;
        this.host = host;
        this.locale = locale;
    }


    /**
     * Initializes a new {@link MultifactorRequest}.
     *
     * @param session Server session
     * @param locale The host related to this session
     * @param providerParameters Provider specific parameters, or null if nor available/required
     */
    public MultifactorRequest(Session session, Locale locale) {
        this(session.getContextId(), session.getUserId(), session.getLogin(), (String) session.getParameter(Session.PARAM_HOST_NAME), locale);
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the contextId
     *
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the login
     *
     * @return The login
     */
    public String getLogin() {
        return login;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof MultifactorRequest)) {
            return false;
        }

        final MultifactorRequest m = (MultifactorRequest)obj;
        return this.userId == m.userId &&
               this.contextId == m.contextId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Integer.valueOf(userId), Integer.valueOf(contextId));
    }

    public String getHost() {
        return this.host;
    }

    /**
     * Gets the locale
     *
     * @return the locale, or null if the locale is unknown
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale
     *
     * @param locale The locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
