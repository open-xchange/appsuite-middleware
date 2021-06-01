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

package com.openexchange.mail.categories.impl;

import org.apache.commons.lang.Validate;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.categories.MailCategoriesConstants;
import com.openexchange.mail.categories.MailCategoriesExceptionCodes;
import com.openexchange.mail.categories.impl.osgi.Services;
import com.openexchange.session.Session;

/**
 * {@link MailCategoriesConfigUtil} - A utility class for mail categories aka. tabbed primary Inbox.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailCategoriesConfigUtil implements Reloadable {

    private static final MailCategoriesConfigUtil INSTANCE = new MailCategoriesConfigUtil();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static MailCategoriesConfigUtil getInstance() {
        return INSTANCE;
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Prevent initialization.
     */
    private MailCategoriesConfigUtil() {
        super();
    }

    private static ConfigViewFactory getConfigViewFactory() throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class);
        }
        return viewFactory;
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
        ConfigViewFactory viewFactory = getConfigViewFactory();
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
        ConfigViewFactory viewFactory = getConfigViewFactory();
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
        ConfigViewFactory viewFactory = getConfigViewFactory();
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
        ConfigViewFactory viewFactory = getConfigViewFactory();
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
    public static boolean getBoolFromProperty(String propertyName, boolean defaultValue, Session session) throws OXException {
        Validate.notNull(session, "session must not be null");
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : ("true".equalsIgnoreCase(value.trim()) ? true : ("false".equalsIgnoreCase(value.trim()) ? false : defaultValue));
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
    public static boolean getBoolFromProperty(String propertyName, boolean defaultValue, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(userId, contextId);

        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : ("true".equalsIgnoreCase(value.trim()) ? true : ("false".equalsIgnoreCase(value.trim()) ? false : defaultValue));
    }

    /**
     * Activates or deactivates the given category
     *
     * @param category The category identifier
     * @param activate Flag indicating if the category should be activated or deactivated
     * @param session The user session
     * @throws OXException
     */
    public static void activateProperty(String category, boolean activate, Session session) throws OXException {
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        String propertyName = MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE;
        ConfigProperty<String> property = view.property(ConfigViewScope.USER.getScopeName(), propertyName, String.class);
        property.set(String.valueOf(activate));

        Reloadables.propagatePropertyChange(propertyName);
    }

    /**
     * Sets a user attribute
     *
     * @param property The name of the property
     * @param value The new value of the property
     * @param session The user session
     * @throws OXException
     */
    public static void setProperty(String property, String value, Session session) throws OXException{
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ConfigProperty<String> confProperty = view.property(ConfigViewScope.USER.getScopeName(), property, String.class);
        confProperty.set(value);

        Reloadables.propagatePropertyChange(property);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        // nothing to do
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForFiles("mail-categories.properties");
    }

}
