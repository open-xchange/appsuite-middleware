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

package com.openexchange.share.servlet.internal;

import java.util.Locale;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;
import com.openexchange.share.GuestInfo;


/**
 * {@link AbstractShareServlet}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AbstractShareServlet extends HttpServlet {

    private static final long serialVersionUID = 5459701824808419752L;

    /**
     * Determines the locale for translations based on the request parameters/headers and guest info.
     *
     * @param request The servlet request
     * @param guestInfo The guest info or <code>null</code>
     * @return The locale
     */
    public static Locale determineLocale(HttpServletRequest request, GuestInfo guestInfo) {
        Locale locale = null;
        if (Strings.isNotEmpty(request.getParameter("language"))) {
            locale = LocaleTools.getLocale(request.getParameter("language"));
        }
        if (null == locale && null != guestInfo) {
            locale = guestInfo.getLocale();
        }
        if (null == locale && Strings.isNotEmpty(request.getHeader("Accept-Language"))) {
            locale = request.getLocale();
        }
        return null != locale ? locale : LocaleTools.DEFAULT_LOCALE;
    }

}
