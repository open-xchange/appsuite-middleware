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
import java.util.Map;
import net.sf.uadetector.OperatingSystem;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.ReadableDeviceCategory;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.ReadableDeviceCategory.Category;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.onboarding.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.uadetector.UserAgentParser;
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
     * @return The translated string or <code>null</code>
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
     * Gets the translation for referenced i18n string; returns translation for default value if such a property does not exist.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param defaultValue The default value to return
     * @param translateDefaultValue Whether specified default value is supposed to be translated
     * @param session The session from requesting user
     * @return The translated string or <code>defaultValue</code>
     * @throws OXException If translated string cannot be returned
     */
    public static String getTranslationFromProperty(String propertyName, String defaultValue, boolean translateDefaultValue, Session session) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return translateDefaultValue ? StringHelper.valueOf(getLocaleFor(session)).getString(defaultValue) : defaultValue;
        }

        String i18nString = property.get();
        if (Strings.isEmpty(i18nString)) {
            return translateDefaultValue ? StringHelper.valueOf(getLocaleFor(session)).getString(defaultValue) : defaultValue;
        }
        return StringHelper.valueOf(getLocaleFor(session)).getString(i18nString);
    }

    /**
     * Gets the translation for referenced i18n string; returns translation for default value if such a property does not exist.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param defaultValue The default value to return
     * @param translateDefaultValue Whether specified default value is supposed to be translated
     * @param session The session from requesting user
     * @return The translated string or <code>defaultValue</code>
     * @throws OXException If translated string cannot be returned
     */
    public static String getValueFromProperty(String propertyName, String defaultValue, Session session) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : value;
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

        return loadIconImageFromTemplate(imageName);
    }

    /**
     * Loads an icon image for referenced property.
     *
     * @param propertyName The name of the property for the icon image; e.g. <code>"com.openexchange.onbaording.apple.icon"</code>
     * @param defaultImageName The default name for the icon image; e.g. <code>"platform_icon_apple"</code>
     * @param session The session from requesting user
     * @return The loaded icon or <code>null</code>
     * @throws OXException If loading icon fails
     */
    public static Icon loadIconImageFromProperty(String propertyName, String defaultImageName, Session session) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return null;
        }

        String imageName = property.get();
        return Strings.isEmpty(imageName) ? loadIconImageFromTemplate(defaultImageName) : loadIconImageFromTemplate(imageName);
    }

    /**
     * Loads the named icon image from template path.
     *
     * @param imageName The image name; e.g. <code>"platform_icon_apple"</code>
     * @return The loaded icon image or <code>null</code>
     */
    public static Icon loadIconImageFromTemplate(String imageName) {
        String templatesPath = getTemplatesPath();
        if (Strings.isEmpty(templatesPath)) {
            return null;
        }

        try {
            File image = new File(new File(templatesPath), imageName);
            byte[] imageBytes = Streams.stream2bytes(new FileInputStream(image));

            MimeTypeMap mimeTypeMap = Services.getService(MimeTypeMap.class);
            return new DefaultIcon(imageBytes, null == mimeTypeMap ? null : mimeTypeMap.getContentType(imageName));
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

    /**
     * Constructs a URL to this server, injecting the host name and optionally the JVM route.
     *
     * <pre>
     *  &lt;protocol&gt; + "://" + &lt;hostname&gt; + "/" + &lt;path&gt; + &lt;jvm-route&gt; + "?" + &lt;query-string&gt;
     * </pre>
     *
     * @param hostData The host data
     * @param optProtocol The protocol to use (HTTP or HTTPS). If <code>null</code>, defaults to the protocol used for this request.
     * @param optPath The path on the server. If <code>null</code> no path is inserted
     * @param withRoute Whether to include the JVM route in the server URL or not
     * @param params The query string parameters. If <code>null</code> no query is included
     * @return A string builder with the URL so far, ready for meddling.
     */
    public static StringBuilder constructURLWithParameters(HostData hostData, String optProtocol, String optPath, boolean withRoute, Map<String, String> params) {
        final StringBuilder url = new StringBuilder(128);
        // Protocol/schema
        {
            String prot = optProtocol;
            if (prot == null) {
                prot = hostData.isSecure() ? "https://" : "http://";
            }
            url.append(prot);
            if (!prot.endsWith("://")) {
                url.append("://");
            }
        }
        // Host name
        url.append(hostData.getHost());
        {
            // ... and port
            int port = hostData.getPort();
            if ((hostData.isSecure() && port != 443) || (!hostData.isSecure() && port != 80)) {
                url.append(':').append(port);
            }
        }
        // Path
        if (optPath != null) {
            if (!optPath.startsWith("/")) {
                url.append('/');
            }
            url.append(optPath);
        }
        // JVM route
        if (withRoute) {
            url.append(";jsessionid=").append(hostData.getRoute());
        }
        // Query string
        if (params != null) {
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (!Strings.isEmpty(key)) {
                    if (first) {
                        url.append('?');
                        first = false;
                    } else {
                        url.append('&');
                    }
                    url.append(AJAXUtility.encodeUrl(key, true));
                    String value = entry.getValue();
                    if (!Strings.isEmpty(value)) {
                        url.append('=').append(AJAXUtility.encodeUrl(value, true));
                    }
                }
            }
        }
        // Return URL
        return url;
    }

    // --------------------------------------------- User-Agent parsing --------------------------------------------------------------

    public static boolean isIPad(ClientInfo clientInfo) {
        String userAgent = clientInfo.getUserAgent();
        if (null == userAgent) {
            return false;
        }

        UserAgentParser userAgentParser = Services.getService(UserAgentParser.class);
        ReadableUserAgent agent = userAgentParser.parse(userAgent);

        OperatingSystem operatingSystem = agent.getOperatingSystem();
        if (!OperatingSystemFamily.IOS.equals(operatingSystem.getFamily())) {
            return false;
        }

        ReadableDeviceCategory deviceCategory = agent.getDeviceCategory();
        return Category.TABLET.equals(deviceCategory.getCategory());
    }

    public static boolean isIPhone(ClientInfo clientInfo) {
        String userAgent = clientInfo.getUserAgent();
        if (null == userAgent) {
            return false;
        }

        UserAgentParser userAgentParser = Services.getService(UserAgentParser.class);
        ReadableUserAgent agent = userAgentParser.parse(userAgent);

        OperatingSystem operatingSystem = agent.getOperatingSystem();
        if (!OperatingSystemFamily.IOS.equals(operatingSystem.getFamily())) {
            return false;
        }

        ReadableDeviceCategory deviceCategory = agent.getDeviceCategory();
        return Category.SMARTPHONE.equals(deviceCategory.getCategory());
    }

}
