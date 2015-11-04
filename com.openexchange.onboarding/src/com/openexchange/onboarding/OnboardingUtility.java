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

package com.openexchange.onboarding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.onboarding.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link OnboardingUtility} - Utility class for on-boarding module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingUtility {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OnboardingUtility.class);

    /**
     * Initializes a new {@link OnboardingUtility}.
     */
    private OnboardingUtility() {
        super();
    }

    /**
     * Gets the locale for session-associated user
     *
     * @param session The session
     * @return The locale
     * @throws OXException If locale cannot be returned
     */
    public static Locale getLocaleFor(Session session) throws OXException {
        if (null == session) {
            return null;
        }

        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        UserService service = Services.getService(UserService.class);
        return service.getUser(session.getUserId(), session.getContextId()).getLocale();
    }

    /**
     * Gets the translation for specified i18n string
     *
     * @param i18nString The i18n string to translate
     * @param session The session from requesting user
     * @return The translated string
     * @throws OXException If translated string cannot be returned
     */
    public static String getTranslationFor(String i18nString, Session session) throws OXException {
        return StringHelper.valueOf(getLocaleFor(session)).getString(i18nString);
    }

    /**
     * Gets the translation for referenced i18n string.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param session The session from requesting user
     * @return The translated string
     * @throws OXException If translated string cannot be returned
     */
    public static String getTranslationFromProperty(String propertyName, Session session) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return null;
        }

        String i18nString = property.get();
        if (Strings.isEmpty(i18nString)) {
            return null;
        }
        return StringHelper.valueOf(getLocaleFor(session)).getString(i18nString);
    }

    /**
     * Loads an icon image for referenced property.
     *
     * @param propertyName The name of the property for the icon image; e.g. <code>"platform_icon_apple"</code>
     * @param session The session from requesting user
     * @return The loaded icon or <code>null</code>
     * @throws OXException If loading icon fails
     */
    public static Icon loadIconImageFromProperty(String propertyName, Session session) throws OXException {
        MimeTypeMap mimeTypeMap = Services.getService(MimeTypeMap.class);

        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return null;
        }

        String imageName = property.get();
        if (Strings.isEmpty(imageName)) {
            return null;
        }

        String templatesPath = getTemplatesPath();
        if (Strings.isEmpty(templatesPath)) {
            return null;
        }

        try {
            File image = new File(new File(templatesPath), imageName);
            byte[] imageBytes = Streams.stream2bytes(new FileInputStream(image));
            return new DefaultIcon(imageBytes, mimeTypeMap.getContentType(imageName));
        } catch (IOException e) {
            LOG.warn("Could not load icon image {} from path {}.", imageName, templatesPath, e);
            return null;
        }
    }

    /**
     * Gets the template path
     *
     * @return The template path
     */
    public static String getTemplatesPath() {
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        return configService.getProperty("com.openexchange.templating.path");
    }

}
