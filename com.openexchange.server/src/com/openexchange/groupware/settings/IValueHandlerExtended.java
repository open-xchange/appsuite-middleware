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
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;

/**
 * {@link IValueHandlerExtended} - Extends {@link IValueHandler} by {@link #isAvailable(Session, UserConfiguration)} to allow session-based
 * availability checks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface IValueHandlerExtended extends IValueHandler {

    /**
     * Checks if the associated setting is available to session-associated user.
     *
     * @param session The session providing user data
     * @param userConfig The user configuration
     * @return <code>true</code> if this setting is available; otherwise <code>false</code>
     * @throws OXException if an error occurs.
     */
    boolean isAvailable(Session session, UserConfiguration userConfig) throws OXException;

    /**
     * Checks if the setting can be written.
     *
     * @param session The session providing user data
     * @return <code>true</code> if the setting can be written; otherwise <code>false</code>
     * @throws OXException if an error occurs.
     */
    boolean isWritable(Session session) throws OXException;

}
