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

package com.openexchange.tools.session;

import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Session;
import com.openexchange.session.UserContextSession;

/**
 * {@link ServerSession} - Extends common {@link Session} interface by additional getter methods for common used objects like context, user,
 * etc.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ServerSession extends UserContextSession {

    /**
     * Gets the user configuration object.
     *
     * @return The user configuration object.
     * @throws IllegalStateException If user configuration object could not be loaded
     */
    UserConfiguration getUserConfiguration();

    /**
     * Gets the user mail settings.
     *
     * @return The user mail settings.
     */
    UserSettingMail getUserSettingMail();

    /**
     * Determines if this session is not authenticated and therefore anonymous.
     *
     * @return <code>true</code> if this session is anonymous; otherwise <code>false</code>
     */
    boolean isAnonymous();

    /**
     * Gets the user permission bits.
     *
     * @return The user permission bits
     * @throws IllegalStateException If user permission bits could not be loaded
     */
    UserPermissionBits getUserPermissionBits();
}
