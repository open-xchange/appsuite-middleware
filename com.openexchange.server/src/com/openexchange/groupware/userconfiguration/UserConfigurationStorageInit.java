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

        private final String alias;

        private final String impl;

        private final String bitImpl;

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


        } catch (final ClassNotFoundException e) {
            throw UserConfigurationCodes.CLASS_NOT_FOUND.create(e, classNameProp);
        } catch (final ClassCastException e) {
            throw UserConfigurationCodes.CLASS_NOT_FOUND.create(e, classNameProp);
        } catch (final InstantiationException e) {
            throw UserConfigurationCodes.CLASS_NOT_FOUND.create(e, classNameProp);
        } catch (final IllegalAccessException e) {
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
