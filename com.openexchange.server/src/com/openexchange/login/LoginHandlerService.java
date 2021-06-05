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

package com.openexchange.login;

import com.openexchange.exception.OXException;

/**
 * {@link LoginHandlerService} - Handles a performed login.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface LoginHandlerService {

    /**
     * Handles the specified performed login.
     *
     * @param login The parameter object transporting all known values regarding this login process.
     * @throws OXException If an error occurs while handling the login
     */
    void handleLogin(LoginResult login) throws OXException;

    /**
     * Handles the specified performed logout.
     * This method is called in a very early step of the complete logout process and only handles the logout triggered by the user not any
     * other kinds of session terminations. It can be used to terminate resources bound to a session that are not required anymore in the
     * complete logout process.
     * If you need to perform any actions after the complete logout process then listen to the OSGi events of the session daemon under the
     * topic of removed sessions. The session object is passed in this event and your actions on it are performed as last steps right before
     * the session is finally gone. You need to implement the OSGi {@link org.osgi.service.event.EventHandler} and listen to the topics
     * defined in {@link com.openexchange.sessiond.SessiondEventConstants}.
     *
     * @param logout The parameter object transporting all known values regarding this logout process.
     * @throws OXException If an error occurs while handling the logout
     */
    void handleLogout(LoginResult logout) throws OXException;
}
