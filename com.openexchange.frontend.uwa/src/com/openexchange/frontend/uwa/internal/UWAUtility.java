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

package com.openexchange.frontend.uwa.internal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import com.openexchange.exception.OXException;
import com.openexchange.frontend.uwa.UWAWidget;
import com.openexchange.frontend.uwa.UWAWidgetExceptionCodes;
import com.openexchange.java.Strings;

/**
 * {@link UWAUtility} - A utility class for UAW module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UWAUtility {

    /**
     * Initializes a new {@link UWAUtility}.
     */
    private UWAUtility() {
        super();
    }

    /**
     * Checks given UWA widget's URL string for syntactical correctness.
     *
     * @param uwaWidget The UWA widget
     * @throws OXException If UWA widget's URL string is invalid
     */
    public static void checkUrl(final UWAWidget uwaWidget) throws OXException {
        if (null == uwaWidget) {
            return;
        }
        checkUrl(uwaWidget.getURL());
    }

    /**
     * Checks given URL string for syntactical correctness.
     *
     * @param sUrl The URL string
     * @throws OXException If URL string is invalid
     */
    public static void checkUrl(final String sUrl) throws OXException {
        if (Strings.isEmpty(sUrl)) {
            // Nothing to check
            return;
        }
        try {
            final java.net.URL url = new java.net.URL(sUrl);
            final String protocol = url.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                throw new MalformedURLException("Only http & https protocols supported.");
            }
        } catch (final MalformedURLException e) {
            final String message = e.getMessage();
            if (null == message || !message.startsWith("no protocol: ") || !isUri(sUrl)) {
                throw UWAWidgetExceptionCodes.INVALID_URL.create(e, new Object[0]);
            }
            if (!sUrl.startsWith("../")) {
                throw UWAWidgetExceptionCodes.INVALID_URL.create(e, new Object[0]);
            }
            try {
                final String dummyUrl = "http://localhost" + sUrl.substring(2);
                new java.net.URL(dummyUrl);
            } catch (final MalformedURLException e1) {
                throw UWAWidgetExceptionCodes.INVALID_URL.create(e, new Object[0]);
            }
        }
    }

    private static boolean isUri(final String s) {
        try {
            new URI(s);
            return true;
        } catch (final URISyntaxException e) {
            return false;
        }
    }

}
