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

package com.openexchange.groupware.settings;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * Interface for settings that are shared between GUI and server.
 *
 * @see IValueHandlerExtended
 */
public interface IValueHandler {

    int NO_ID = -1;
    Object UNDEFINED = new Object();

    /**
     * @param session Session.
     * @param userConfig user configuration.
     * @param setting the value should be set in this setting object.
     * @throws OXException if an error occurs.
     */
    void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException;

    /**
     * @param session Session.
     * @return <code>true</code> if this setting is available due to {@link UserConfiguration}.
     * @see IValueHandlerExtended
     */
    boolean isAvailable(final UserConfiguration userConfig);

    /**
     * @return <code>true</code> if the setting can be written by the GUI.
     */
    boolean isWritable();

    /**
     * Write a new value to the setting.
     * @param session Session.
     * @param ctx Context.
     * @param user user object.
     * @param setting contains the value for the setting.
     * @throws OXException if the setting can't be written or an error occurs while writing the value.
     */
    void writeValue(Session session, Context ctx, User user, Setting setting) throws OXException;

    /**
     * If the value should be written simply to the database and read from there a unique identifier must be returned instead of
     * implementing methods {@link #getValue(Session, Context, User, UserConfiguration, Setting)} and
     * {@link #writeValue(Context, User, Setting)}.
     *
     * @return the unique identifier of the value in the database.
     */
    int getId();
}
