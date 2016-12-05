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

package com.openexchange.client.onboarding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.client.onboarding.osgi.Services;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
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
     * Gets the configured product name
     *
     * @param hostName The host name
     * @param session The session providing user information
     * @return The product name
     * @throws OXException If product name cannot be returned
     */
    public static String getProductName(String hostName, Session session) throws OXException {
        return getProductName(hostName, session.getUserId(), session.getContextId());
    }

    /**
     * Gets the configured product name
     *
     * @param hostName The host name
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The product name
     * @throws OXException If product name cannot be returned
     */
    public static String getProductName(String hostName, int userId, int contextId) throws OXException {
        ServerConfigService serverConfigService = Services.getService(ServerConfigService.class);
        if (null == serverConfigService) {
            throw ServiceExceptionCode.absentService(ServerConfigService.class);
        }

        ServerConfig serverConfig = serverConfigService.getServerConfig(hostName, userId, contextId);
        return serverConfig.getProductName();
    }

    /**
     * Crafts a UUID for specified scenario for given user.
     *
     * @param identifier The identifier to craft from
     * @param session The session providing user data
     * @return The crafted UUID
     */
    public static UUID craftUUIDFrom(String identifier, Session session) {
        return craftUUIDFrom(identifier, session.getUserId(), session.getContextId());
    }

    /**
     * Crafts a UUID for specified scenario for given user.
     *
     * @param identifier The identifier to craft from
     * @param userId The user id
     * @param contextId The context id
     * @return The crafted UUID
     */
    public static UUID craftUUIDFrom(String identifier, int userId, int contextId) {
        return new UUID(longFor(identifier.hashCode(), "open-xchange".hashCode()), longFor(userId, contextId));
    }

    private static long longFor(int x, int y) {
        return ((((long) x) << 32) | (y & 0xffffffffL));
    }

    /**
     * Parses the specified composite identifier; e.g. <code>"apple.iphone/davsync"</code>.
     *
     * @param compositeId The composite identifier; e.g. <code>"apple.iphone/davsync"</code>
     * @return The parsed device/scenario pair
     * @throws OXException If parse attempt fails
     */
    public static CompositeId parseCompositeId(String compositeId) throws OXException {
        if (null == compositeId) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create("null");
        }

        char delim = '/';

        int off = 0;
        int pos = compositeId.indexOf(delim, off);
        if (pos < 0) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create(compositeId);
        }

        Device device = Device.deviceFor(compositeId.substring(off, pos));
        if (null == device) {
            throw OnboardingExceptionCodes.INVALID_COMPOSITE_ID.create(compositeId);
        }

        off = pos + 1;
        String scenarioId = compositeId.substring(off);

        return new CompositeId(device, scenarioId);
    }

    /**
     * Checks availability of the "no reply" transport for specified session's context.
     *
     * @param session The session denoting the context
     * @return <code>true</code> if available; otherwise <code>false</code>
     */
    public static boolean hasNoReplyTransport(Session session) {
        return null != session && hasNoReplyTransport(session.getContextId());
    }

    /**
     * Checks availability of the "no reply" transport for specified context.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if available; otherwise <code>false</code>
     */
    public static boolean hasNoReplyTransport(int contextId) {
        TransportProvider transportProvider = TransportProviderRegistry.getTransportProvider("smtp");
        try {
            transportProvider.createNewNoReplyTransport(contextId);
            return true;
        } catch (OXException e) {
            LOG.debug("\"no reply\" transport is not available for context {}", contextId, e);
            return false;
        }
    }

    /**
     * Checks if specified permission is set for session-associated user.
     *
     * @param permission The permission to check
     * @param session The session
     * @return <code>true</code> if permission is set; otherwise <code>false</code>
     * @throws OXException If check fails
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static boolean hasPermission(Permission permission, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        return null == permission ? false : hasCapability(permission.getCapabilityName(), session);
    }

    /**
     * Checks if specified permission is set for given user.
     *
     * @param permission The permission to check
     * @param userId The user id
     * @param contextId The context id
     * @return <code>true</code> if permission is set; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public static boolean hasPermission(Permission permission, int userId, int contextId) throws OXException {
        return null == permission ? false : hasCapability(permission.getCapabilityName(), userId, contextId);
    }

    /**
     * Checks if specified capability is set for session-associated user.
     *
     * @param capability The capability to check
     * @param session The session
     * @return <code>true</code> if capability is set; otherwise <code>false</code>
     * @throws OXException If check fails
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static boolean hasCapability(String capability, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");

        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (serverSession.isAnonymous() || serverSession.getUser().isGuest()) {
            return false;
        }

        CapabilityService service = Services.getService(CapabilityService.class);
        return null == capability ? false : service.getCapabilities(serverSession).contains(capability);
    }

    /**
     * Checks if specified capability is set for given user.
     *
     * @param capability The capability to check
     * @param userId The user id
     * @param contextId The context id
     * @return <code>true</code> if capability is set; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public static boolean hasCapability(String capability, int userId, int contextId) throws OXException {
        CapabilityService service = Services.getService(CapabilityService.class);
        return null == capability ? false : service.getCapabilities(userId, contextId).contains(capability);
    }

    /**
     * Checks if specified scenario is enabled for session-associated user.
     *
     * @param scenarioId The scenario identifier to check
     * @param session The session
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If check fails
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static boolean isScenarioEnabled(String scenarioId, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        if (Strings.isEmpty(scenarioId)) {
            return false;
        }

        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        ComposedConfigProperty<String> property = view.property("com.openexchange.client.onboarding.enabledScenarios", String.class);
        if (null == property || !property.isDefined()) {
            // Nothing enabled...
            return false;
        }

        String[] ids = Strings.splitByComma(Strings.asciiLowerCase(property.get()));
        Set<String> set = new HashSet<String>(ids.length, 0.9F);
        set.addAll(Arrays.asList(ids));
        return set.contains(Strings.asciiLowerCase(scenarioId));
    }

    /**
     * Checks if specified scenario is enabled for given user.
     *
     * @param scenarioId The scenario identifier to check
     * @param userId The user id
     * @param contextId The context id
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public static boolean isScenarioEnabled(String scenarioId, int userId, int contextId) throws OXException {
        if (Strings.isEmpty(scenarioId)) {
            return false;
        }

        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<String> property = view.property("com.openexchange.client.onboarding.enabledScenarios", String.class);
        if (null == property || !property.isDefined()) {
            // Nothing enabled...
            return false;
        }

        String[] ids = Strings.splitByComma(Strings.asciiLowerCase(property.get()));
        Set<String> set = new HashSet<String>(ids.length, 0.9F);
        set.addAll(Arrays.asList(ids));
        return set.contains(Strings.asciiLowerCase(scenarioId));
    }

    /**
     * Gets the locale for session-associated user.
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
     * Gets the locale for given user.
     *
     * @param userId The user id
     * @param contextId The context id
     * @return The locale
     * @throws OXException If locale cannot be returned
     */
    public static Locale getLocaleFor(int userId, int contextId) throws OXException {
        UserService service = Services.getService(UserService.class);
        return service.getUser(userId, contextId).getLocale();
    }

    /**
     * Gets the value for specified <code>boolean</code> property.
     *
     * @param propertyName The name of the <code>boolean</code> property
     * @param defaultValue The default <code>boolean</code> value
     * @param session The session from requesting user
     * @return The <code>boolean</code> value or <code>defaultValue</code> (if absent)
     * @throws OXException If <code>boolean</code> value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static boolean getBoolValue(String propertyName, boolean defaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<Boolean> property = view.property(propertyName, boolean.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        return property.get().booleanValue();
    }

    /**
     * Gets the value for specified <code>boolean</code> property.
     *
     * @param propertyName The name of the <code>boolean</code> property
     * @param defaultValue The default <code>boolean</code> value
     * @param userId The user id
     * @param contextId The context id
     * @return The <code>boolean</code> value or <code>defaultValue</code> (if absent)
     * @throws OXException If <code>boolean</code> value cannot be returned
     */
    public static boolean getBoolValue(String propertyName, boolean defaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<Boolean> property = view.property(propertyName, boolean.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        return property.get().booleanValue();
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
        return null == i18nString ? null : StringHelper.valueOf(getLocaleFor(session)).getString(i18nString);
    }

    /**
     * Gets the translation for specified i18n string
     *
     * @param i18nString The i18n string to translate
     * @param userId The user id
     * @param contextId The context id
     * @return The translated string
     * @throws OXException If translated string cannot be returned
     */
    public static String getTranslationFor(String i18nString, int userId, int contextId) throws OXException {
        return null == i18nString ? null : StringHelper.valueOf(getLocaleFor(userId, contextId)).getString(i18nString);
    }

    /**
     * Gets the translation for referenced i18n string.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param session The session from requesting user
     * @return The translated string or <code>null</code>
     * @throws OXException If translated string cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static String getTranslationFromProperty(String propertyName, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
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
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static String getTranslationFromProperty(String propertyName, String defaultValue, boolean translateDefaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
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
     * @param userId The user id
     * @param contextId The context id
     * @return The translated string or <code>defaultValue</code>
     * @throws OXException If translated string cannot be returned
     */
    public static String getTranslationFromProperty(String propertyName, String defaultValue, boolean translateDefaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return translateDefaultValue ? StringHelper.valueOf(getLocaleFor(userId, contextId)).getString(defaultValue) : defaultValue;
        }

        String i18nString = property.get();
        if (Strings.isEmpty(i18nString)) {
            return translateDefaultValue ? StringHelper.valueOf(getLocaleFor(userId, contextId)).getString(defaultValue) : defaultValue;
        }
        return StringHelper.valueOf(getLocaleFor(userId, contextId)).getString(i18nString);
    }

    /**
     * Gets the value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param defaultValue The default value to return
     * @param session The session from requesting user
     * @return The value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static String getValueFromProperty(String propertyName, String defaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
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
     * Gets the value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param defaultValue The default value to return
     * @param userId The user id
     * @param contextId The context id
     * @return The value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     */
    public static String getValueFromProperty(String propertyName, String defaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : value;
    }

    /**
     * Gets the integer value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param defaultValue The default value to return
     * @param session The session from requesting user
     * @return The integer value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static Integer getIntFromProperty(String propertyName, Integer defaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        try {
            String value = property.get();
            return Strings.isEmpty(value) ? defaultValue : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets the integer value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param defaultValue The default value to return
     * @param userId The user id
     * @param contextId The context id
     * @return The integer value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     */
    public static Integer getIntFromProperty(String propertyName, Integer defaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        try {
            String value = property.get();
            return Strings.isEmpty(value) ? defaultValue : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets the boolean value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param defaultValue The default value to return
     * @param session The session from requesting user
     * @return The boolean value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static Boolean getBoolFromProperty(String propertyName, Boolean defaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : ("true".equalsIgnoreCase(value.trim()) ? Boolean.TRUE : ("false".equalsIgnoreCase(value.trim()) ? Boolean.FALSE : defaultValue));
    }

    /**
     * Gets the boolean value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name for the i18n string to translate
     * @param defaultValue The default value to return
     * @param userId The user id
     * @param contextId The context id
     * @return The boolean value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     */
    public static Boolean getBoolFromProperty(String propertyName, Boolean defaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : ("true".equalsIgnoreCase(value.trim()) ? Boolean.TRUE : ("false".equalsIgnoreCase(value.trim()) ? Boolean.FALSE : defaultValue));
    }

    /**
     * Loads an icon image for referenced property.
     *
     * @param propertyName The name of the property for the icon image; e.g. <code>"com.openexchange.client.onboarding.apple.icon"</code>
     * @param session The session from requesting user
     * @return The loaded icon or <code>null</code>
     * @throws OXException If loading icon fails
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static Icon loadIconImageFromProperty(String propertyName, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
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

        return loadIconImage(imageName);
    }

    /**
     * Loads an icon image for referenced property.
     *
     * @param propertyName The name of the property for the icon image; e.g. <code>"com.openexchange.client.onboarding.apple.icon"</code>
     * @param defaultImageName The default name for the icon image; e.g. <code>"platform_icon_apple.png"</code>
     * @param session The session from requesting user
     * @return The loaded icon or <code>null</code>
     * @throws OXException If loading icon fails
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static Icon loadIconImageFromProperty(String propertyName, String defaultImageName, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return null;
        }

        String imageName = property.get();
        return Strings.isEmpty(imageName) ? loadIconImage(defaultImageName) : loadIconImage(imageName);
    }

    /**
     * Loads the named icon image from template path.
     *
     * @param imageName The image name; e.g. <code>"platform_icon_apple.png"</code>
     * @return The loaded icon image or <code>null</code>
     * @throws IllegalArgumentException If imageName is <code>null</code>
     */
    public static Icon loadIconImage(String imageName) {
        Validate.notNull(imageName, "imageName must not be null");

        String templatesPath = getTemplatesPath();
        if (Strings.isEmpty(templatesPath)) {
            return null;
        }

        try {
            File image = new File(new File(templatesPath), imageName);
            byte[] imageBytes = Streams.stream2bytes(new FileInputStream(image));

            MimeTypeMap mimeTypeMap = Services.getService(MimeTypeMap.class);
            return new DefaultIcon(imageBytes, null == mimeTypeMap ? null : mimeTypeMap.getContentType(imageName));
        } catch (java.io.FileNotFoundException e) {
            LOG.warn("Icon image {} does not exist in path {}.", imageName, templatesPath);
            return null;
        } catch (IOException e) {
            LOG.warn("Could not load icon image {} from path {}.", imageName, templatesPath, e);
            return null;
        }
    }

    /**
     * Gets the template file information for specified file name.
     *
     * @param name The file name; e.g. <code>"platform_icon_apple.png"</code>
     * @return The template file
     * @throws IllegalArgumentException If name is <code>null</code>
     */
    public static FileInfo getTemplateFileInfo(String name) {
        File templateFile = getTemplateFile(name);
        MimeTypeMap mimeTypeMap = Services.getService(MimeTypeMap.class);
        String mimeType = null == mimeTypeMap ? "application/octet-stream" : mimeTypeMap.getContentType(name);
        return new FileInfo(templateFile, mimeType);
    }

    /**
     * Gets the template file for specified file name.
     *
     * @param name The file name; e.g. <code>"platform_icon_apple.png"</code>
     * @return The template file
     * @throws IllegalArgumentException If name is <code>null</code>
     */
    public static File getTemplateFile(String name) {
        Validate.notNull(name, "name must not be null");

        String templatesPath = getTemplatesPath();
        if (Strings.isEmpty(templatesPath)) {
            return null;
        }

        return new File(new File(templatesPath), name);
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

    /**
     * Retrieves the primary mail address of a user.
     *
     * @param userId The user id
     * @param contextId The context id
     * @return The users primary mail address
     * @throws OXException
     */
    public static String getUserMail(int userId, int contextId) throws OXException {
        UserService userService = Services.getService(UserService.class);
        if (userService == null) {
            LOG.error("UserService is unavailable!");
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create("UserService is unavailable");
        }
        return userService.getUser(userId, contextId).getMail();
    }

    /**
     * Retrieves the primary mail address of a user.
     *
     * @param userId The user id
     * @param contextId The context id
     * @return The users login name
     * @throws OXException
     */
    public static String getUserLogin(int userId, int contextId) throws OXException {
        UserService userService = Services.getService(UserService.class);
        if (userService == null) {
            LOG.error("UserService is unavailable!");
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create("UserService is unavailable");
        }
        Map<String, String> attributes = userService.getUser(userId, contextId).getAttributes();
        if (!attributes.containsKey("loginnamerecorder/user_login")) {
            LOG.warn("No login user attribute for user %s in context %s.", userId, contextId);
            return null;
        }
        return attributes.get("loginnamerecorder/user_login");
    }
}
