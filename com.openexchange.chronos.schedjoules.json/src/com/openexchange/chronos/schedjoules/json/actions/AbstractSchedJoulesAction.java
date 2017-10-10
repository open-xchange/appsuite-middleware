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
 *     Copyright (C) 2017-2020 OX Software GmbH
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
