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

package com.openexchange.groupware.settings.impl;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * This class defines the interface to the storage for user specific settings.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class SettingStorage {

    /**
     * Default constructor.
     */
    protected SettingStorage() {
        super();
    }

    /**
     * This method stores a specific setting.
     * @param setting the setting to store.
     * @throws OXException if an error occurs while saving the setting.
     */
    public abstract void save(Setting setting) throws OXException;

    /**
     * This method stores a specific setting.
     * @param con writable database connection.
     * @param setting the setting to store.
     * @throws OXException if an error occurs while saving the setting.
     */
    public abstract void save(Connection con, Setting setting) throws
        OXException;

    /**
     * This method reads the setting and its subsettings from the database.
     * @param setting setting to read.
     * @throws OXException if an error occurs while reading the setting.
     */
    public abstract void readValues(Setting setting)
        throws OXException;

    /**
     * This method reads the setting and its subsettings from the database.
     * @param con database connection.
     * @param setting setting to read.
     * @throws OXException if an error occurs while reading the setting.
     */
    public abstract void readValues(Connection con, Setting setting)
        throws OXException;

    /**
     * @param session Session.
     * @return an instance implementing this storage interface.
     */
    public static SettingStorage getInstance(final Session session) {
        try {
            return new RdbSettingStorage(session);
        } catch (OXException e) {
            throw new RuntimeException(e);
        }
    }

    public static SettingStorage getInstance(final Session session,
        final Context ctx, final User user, final UserConfiguration userConfig) {
        return new RdbSettingStorage(session, ctx, user, userConfig);
    }

    /**
     * @param contextId unique identifier of the context.
     * @param userId unique identifier of the user.
     * @return an instance implementing this storage interface.
     */
    public static SettingStorage getInstance(final int contextId,
        final int userId) {
        return new RdbSettingStorage(contextId, userId);
    }
}
