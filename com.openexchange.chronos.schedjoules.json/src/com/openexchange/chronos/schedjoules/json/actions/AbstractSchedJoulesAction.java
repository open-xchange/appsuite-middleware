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

package com.openexchange.chronos.schedjoules.json.actions;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.schedjoules.json.actions.parameter.SchedJoulesBrowseParameter;
import com.openexchange.chronos.schedjoules.json.actions.parameter.SchedJoulesCommonParameter;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractSchedJoulesAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractSchedJoulesAction {

    protected final ServiceLookup services;

    /**
     * Initialises a new {@link AbstractSchedJoulesAction}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    AbstractSchedJoulesAction(ServiceLookup services) {
        this.services = services;
    }

    /**
     * Gets the 'language' URL parameter from the request and returns it. If no
     * parameter is present, then the default language from the user's session
     * is returned.
     * 
     * @param requestData The {@link AJAXRequestData}
     * @param session The groupware session
     * @return The value of the 'language' URL parameter if present, or the user's language from the session
     */
    String getLanguage(AJAXRequestData requestData, ServerSession session) {
        String language = requestData.getParameter(SchedJoulesCommonParameter.LANGUAGE);
        if (Strings.isEmpty(language)) {
            language = session.getUser().getLocale().getLanguage().toLowerCase();
        }
        return language;
    }

    /**
     * Gets the 'country' URL parameter from the request and returns it. If no
     * parameter is present, then the default country from the user's session
     * is returned.
     * 
     * @param requestData The {@link AJAXRequestData}
     * @param session The groupware session
     * @return The value of the 'country' URL parameter if present, or the user's country from the session
     */
    String getCountry(AJAXRequestData requestData, ServerSession session) {
        String country = requestData.getParameter(SchedJoulesBrowseParameter.COUNTRY);
        if (Strings.isEmpty(country)) {
            country = session.getUser().getLocale().getCountry().toLowerCase();
        }
        return country;
    }
}
