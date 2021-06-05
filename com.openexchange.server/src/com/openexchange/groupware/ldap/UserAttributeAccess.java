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

package com.openexchange.groupware.ldap;

import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.User;

/**
 * {@link UserAttributeAccess} - Provides utility methods to access/update a user's attributes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserAttributeAccess {

    private static volatile UserAttributeAccess instance;

    /**
     * Gets the default instance.
     *
     * @return The default instance
     */
    public static UserAttributeAccess getDefaultInstance() {
        UserAttributeAccess tmp = instance;
        if (null == tmp) {
            synchronized (UserAttributeAccess.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = instance = new UserAttributeAccess();
                }
            }
        }
        return tmp;
    }

    /*-
     * Member stuff
     */

    private final UserStorage userStorage;

    /**
     * Initializes a new {@link UserAttributeAccess}.
     */
    private UserAttributeAccess() {
        this(UserStorage.getInstance());
    }

    /**
     * Initializes a new {@link UserAttributeAccess}.
     */
    public UserAttributeAccess(final UserStorage userStorage) {
        super();
        this.userStorage = userStorage;
    }

    /**
     * Gets the specified <code>boolean</code> property from configuration service read from appropriate properties files.
     *
     * @param name The property's name
     * @param defaultValue The default <code>boolean</code> value to return if property is missing
     * @return The <code>boolean</code> value
     */
    public boolean getBooleanProperty(final String name, final boolean defaultValue) {
        final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == service) {
            return defaultValue;
        }
        return service.getBoolProperty(name, defaultValue);
    }

    /**
     * Gets the specified <code>boolean</code> attribute from given user.
     * <p>
     * This methods assumes that at most one value is associated with specified attribute name.
     *
     * @param name The name of the <code>boolean</code> attribute
     * @param userId The identifier of the user
     * @param contextId The identifier of user's context
     * @param defaultValue The default value to return if user has no attribute of specified name
     * @return The value of the <code>boolean</code> attribute
     * @throws OXException
     */
    public boolean getBooleanAttribute(final String name, final int userId, final int contextId, final boolean defaultValue) throws OXException {
        return getBooleanAttribute(name, UserStorage.getInstance().getUser(userId, contextId), defaultValue);
    }

    /**
     * Gets the specified <code>boolean</code> attribute from given user.
     * <p>
     * This methods assumes that at most one value is associated with specified attribute name.
     *
     * @param name The name of the <code>boolean</code> attribute
     * @param user The user
     * @param defaultValue The default value to return if user has no attribute of specified name
     * @return The value of the <code>boolean</code> attribute
     */
    public boolean getBooleanAttribute(final String name, final User user, final boolean defaultValue) {
        return Boolean.parseBoolean(getAttribute(name, user, String.valueOf(defaultValue)));
    }

    /**
     * Gets the specified attribute from given user.
     * <p>
     * This methods assumes that at most one value is associated with specified attribute name.
     *
     * @param name The name of the attribute
     * @param user The user
     * @param defaultValue The default value to return if user has no attribute of specified name
     * @return The value of the attribute
     */
    public String getAttribute(final String name, final User user, final String defaultValue) {
        final Map<String, String> attributes = user.getAttributes();
        if (null == attributes) {
            return defaultValue;
        }

        String bset = attributes.get(name);
        return null == bset ? defaultValue : bset;
    }

    /**
     * Set specified <code>boolean</code> attribute for given user.
     *
     * @param name The attribute name
     * @param value The attribute <code>boolean</code> value
     * @param user The user
     * @param context The context
     * @throws OXException If setting <code>boolean</code> attribute fails
     */
    public void setBooleanAttribute(final String name, final boolean value, final int userId, final int contextId) throws OXException {
        Context context = null;
        try {
            context = ContextStorage.getStorageContext(contextId);
        } catch (@SuppressWarnings("unused") final OXException e) {
            // Occurs if a context's admin is created
            context = new ContextImpl(contextId);
        }
        setBooleanAttribute(name, value, userStorage.getUser(userId, context), context);
    }

    /**
     * Set specified <code>boolean</code> attribute for given user.
     *
     * @param name The attribute name
     * @param value The attribute <code>boolean</code> value
     * @param user The user
     * @param context The context
     * @throws OXException If setting <code>boolean</code> attribute fails
     */
    public void setBooleanAttribute(final String name, final boolean value, final User user, final Context context) throws OXException {
        setAttribute(name, String.valueOf(value), user, context);
    }

    /**
     * Set specified attribute for given user.
     *
     * @param name The attribute name
     * @param value The attribute value
     * @param user The user
     * @param context The context
     * @throws OXException If setting attribute fails
     */
    public void setAttribute(final String name, final String value, final User user, final Context context) throws OXException {
        userStorage.setAttribute(name, value, user.getId(), context);
    }
}
