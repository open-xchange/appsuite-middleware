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

package com.openexchange.mail.categories.impl;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang.Validate;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
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

    private static final String CONFIG_FILE_NAME = "mail-categories.properties";
    private final static String[] PROPERTIES = new String[] { "all properties in file" };
    private static final MailCategoriesConfigUtil INSTANCE = new MailCategoriesConfigUtil();

    public static MailCategoriesConfigUtil getInstance() {
        return INSTANCE;
    }

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
    public static void activateProperty(String category, boolean activate, Session session) throws OXException{
        ConfigViewFactory viewFactory = getConfigViewFactory();
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ConfigProperty<String> property = view.property("user", MailCategoriesConstants.MAIL_CATEGORIES_PREFIX+category+MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, String.class);
        property.set(String.valueOf(activate));

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

        ConfigProperty<String> confProperty = view.property("user", property, String.class);
        confProperty.set(value);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        // nothing to do
    }

    @Override
    public Map<String, String[]> getConfigFileNames() {
        return Collections.singletonMap(CONFIG_FILE_NAME, PROPERTIES);
    }

}
