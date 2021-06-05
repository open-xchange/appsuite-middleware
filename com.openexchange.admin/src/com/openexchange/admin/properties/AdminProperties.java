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

package com.openexchange.admin.properties;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.config.cascade.ConfigViews;

/**
 * This class will hold the properties setting from now on
 *
 * @author d7
 *
 */
public class AdminProperties {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AdminProperties.class);
    }

    /**
     * The properties for group
     *
     * @author d7
     *
     */
    public static class Group {

        public static final String CHECK_NOT_ALLOWED_CHARS = "CHECK_GROUP_UID_FOR_NOT_ALLOWED_CHARS";
        public static final String AUTO_LOWERCASE = "AUTO_TO_LOWERCASE_UID";
        public static final String CHECK_NOT_ALLOWED_NAMES = "CHECK_GROUP_UID_FOR_NOT_ALLOWED_NAMES";
        public static final String NOT_ALLOWED_NAMES = "NOT_ALLOWED_GROUP_UID_NAMES";
        public static final String GID_NUMBER_START = "GID_NUMBER_START";
    }

    /**
     * The general properties
     *
     * @author d7
     *
     */
    public static class Prop {

        public static final String SERVER_NAME = "SERVER_NAME";
        public static final String ADMINDAEMON_LOGLEVEL = "LOG_LEVEL";
        public static final String ADMINDAEMON_LOGFILE = "LOG";
    }

    /**
     * The properties for resources
     *
     * @author d7
     *
     */
    public static class Resource {

        public static final String CHECK_NOT_ALLOWED_CHARS = "CHECK_RES_UID_FOR_NOT_ALLOWED_CHARS";
        public static final String AUTO_LOWERCASE = "AUTO_TO_LOWERCASE_UID";
        public static final String CHECK_NOT_ALLOWED_NAMES = "CHECK_RES_UID_FOR_NOT_ALLOWED_NAMES";
        public static final String NOT_ALLOWED_NAMES = "NOT_ALLOWED_RES_UID_NAMES";
    }

    /**
     * The properties for RMI
     *
     * @author d7
     *
     */
    public static class RMI {

        public static final String RMI_PORT = "RMI_PORT";
    }

    /**
     * The properties for the user
     *
     * @author d7
     *
     */
    public static class User {

        public static final String UID_NUMBER_START = "UID_NUMBER_START";
        public static final String CHECK_NOT_ALLOWED_CHARS = "CHECK_USER_UID_FOR_NOT_ALLOWED_CHARS";
        public static final String AUTO_LOWERCASE = "AUTO_TO_LOWERCASE_UID";
        public static final String CHECK_NOT_ALLOWED_NAMES = "CHECK_USER_UID_FOR_NOT_ALLOWED_NAMES";
        public static final String NOT_ALLOWED_NAMES = "NOT_ALLOWED_USER_UID_NAMES";
        public static final String PRIMARY_MAIL_UNCHANGEABLE = "PRIMARY_MAIL_UNCHANGEABLE";
        public static final String USERNAME_CHANGEABLE = "USERNAME_CHANGEABLE";
        public static final String DEFAULT_PASSWORD_MECHANISM = "DEFAULT_PASSWORD_MECHANISM";
        public static final String DEFAULT_TIMEZONE = "DEFAULT_TIMEZONE";
        public static final String ENABLE_ADMIN_MAIL_CHECKS = "com.openexchange.admin.enableAdminMailChecks";
        public static final String ADDITIONAL_EMAIL_CHECK_REGEX = "com.openexchange.admin.additionalEmailCheckRegex";
        public static final String ADDITIONAL_CONFIG_CHECK_REGEX = "com.openexchange.admin.additionalConfigCheckRegex";
    }

    /**
     * Optionally gets the specified property in the specified scope.
     *
     * @param <T> The type to coerce the property to
     * @param propertyName The property name
     * @param propertyScope The property scope to apply
     * @param coerceTo The type to coerce the property to
     * @return The value of the property or <code>null</code> in any other case
     */
    public static <T> T optScopedProperty(String propertyName, PropertyScope propertyScope, Class<T> coerceTo) {
        com.openexchange.config.cascade.ConfigViewFactory viewFactory = AdminServiceRegistry.getInstance().getService(com.openexchange.config.cascade.ConfigViewFactory.class);
        if (null == viewFactory) {
            return null;
        }

        Optional<List<String>> optionalScopes = propertyScope.getScopes();
        try {
            // Obtain config-cascade view
            com.openexchange.config.cascade.ConfigView view = viewFactory.getView(propertyScope.getUserId(), propertyScope.getContextId());

            // Determine appropriate property value w/ or w/o given scopes
            if (optionalScopes.isPresent()) {
                // Scopes given
                for (String scope : optionalScopes.get()) {
                    com.openexchange.config.cascade.ConfigProperty<T> configProperty = view.property(scope, propertyName, coerceTo);
                    if (false == configProperty.isDefined()) {
                        continue;
                    }
                    return configProperty.get();
                }
            } else {
                // No scopes given. Follow regular search path
                return ConfigViews.getDefinedPropertyFrom(propertyName, view, coerceTo);
            }
        } catch (Exception e) {
            if (optionalScopes.isPresent()) {
                LoggerHolder.LOG.warn("Unable to get the value of the '{}' property for the '{}' scope(s)!", propertyName, optionalScopes.get(), e);
            } else {
                LoggerHolder.LOG.warn("Unable to get the value of the '{}' property!", propertyName, e);
            }
        }
        return null;
    }

}
