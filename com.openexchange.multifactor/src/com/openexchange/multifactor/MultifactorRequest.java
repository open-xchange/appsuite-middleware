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
    public MultifactorRequest(int contextId, int userId, String host, Locale locale) {
        this.contextId = contextId;
        this.userId = userId;
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
        this(session.getContextId(), session.getUserId(), (String) session.getParameter(Session.PARAM_HOST_NAME), locale);
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

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }

        if(!(obj instanceof MultifactorRequest)) {
            return false;
        }

        final MultifactorRequest m = (MultifactorRequest)obj;
        return this.userId == m.userId &&
               this.contextId == m.contextId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, contextId);
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
