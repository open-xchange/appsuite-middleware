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

package com.openexchange.groupware.userconfiguration;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.SystemConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;

/**
 * {@link UserConfigurationStorageInit}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserConfigurationStorageInit implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserConfigurationStorageInit.class);

    private static enum UserConfigurationImpl {

        /**
         * Caching
         */
        CACHING("Caching", CachingUserConfigurationStorage.class.getName(), CachingUserPermissionBitsStorage.class.getName()),
        /**
         * Database
         */
        DB("DB", CapabilityUserConfigurationStorage.class.getName(), CachingUserPermissionBitsStorage.class.getName());

        final String alias;
        final String impl;
        final String bitImpl;

        private UserConfigurationImpl(final String alias, final String impl, final String bitImpl) {
            this.alias = alias;
            this.impl = impl;
            this.bitImpl = bitImpl;
        }
    }

    private static UserConfigurationStorageInit instance = new UserConfigurationStorageInit();

    /**
     * Gets the singleton instance of {@link UserConfigurationStorageInit}
     *
     * @return The singleton instance of {@link UserConfigurationStorageInit}
     */
    public static UserConfigurationStorageInit getInstance() {
        return instance;
    }

    private final AtomicBoolean started = new AtomicBoolean();

    /**
     * No instance
     */
    private UserConfigurationStorageInit() {
        super();
    }

    private static String getUserConfigurationImpl(final String alias) {
        final UserConfigurationImpl[] arr = UserConfigurationImpl.values();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].alias.equalsIgnoreCase(alias)) {
                return arr[i].impl;
            }
        }
        return null;
    }

    private static String getUserPermissionBitsImpl(final String alias) {
        final UserConfigurationImpl[] arr = UserConfigurationImpl.values();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].alias.equalsIgnoreCase(alias)) {
                return arr[i].bitImpl;
            }
        }
        return null;
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("{} already started", UserConfigurationStorageInit.class.getName());
            return;
        }
        // Initialize instance
        final String classNameProp = SystemConfig.getProperty(Property.USER_CONF_STORAGE);
        if (null == classNameProp) {
            throw UserConfigurationCodes.MISSING_SETTING.create(Property.USER_CONF_STORAGE.getPropertyName());
        }
        try {
            final String className = getUserConfigurationImpl(classNameProp);
            final Class<? extends UserConfigurationStorage> implementingClass = Class.forName(className == null ? classNameProp : className).asSubclass(
                UserConfigurationStorage.class);
            LOG.info("UserConfigurationStorage implementation: {}", implementingClass.getName());
            UserConfigurationStorage.setInstance(implementingClass.newInstance());

            // Now for the permission bits
            final String bitClassName = getUserPermissionBitsImpl(classNameProp);
            final Class<? extends UserPermissionBitsStorage> bitsImplementingClass = Class.forName(bitClassName).asSubclass(UserPermissionBitsStorage.class);

            LOG.info("UserPermissionBitsStorage implementation: {}", bitsImplementingClass.getName());

            UserPermissionBitsStorage.setInstance(bitsImplementingClass.newInstance());


        } catch (ClassNotFoundException e) {
            throw UserConfigurationCodes.CLASS_NOT_FOUND.create(e, classNameProp);
        } catch (ClassCastException e) {
            throw UserConfigurationCodes.CLASS_NOT_FOUND.create(e, classNameProp);
        } catch (InstantiationException e) {
            throw UserConfigurationCodes.CLASS_NOT_FOUND.create(e, classNameProp);
        } catch (IllegalAccessException e) {
            throw UserConfigurationCodes.CLASS_NOT_FOUND.create(e, classNameProp);
        }
    }

    @Override
    public void stop() throws OXException {
        if (!started.compareAndSet(true, false)) {
            LOG.error("{} cannot be stopped since it has not been started before", UserConfigurationStorageInit.class.getName());
            return;
        }
        // Release instance
        UserConfigurationStorage.releaseInstance();
        UserPermissionBitsStorage.releaseInstance();
    }

}
