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

package com.openexchange.push;

import java.util.List;
import com.openexchange.exception.OXException;


/**
 * {@link PushManagerExtendedService} - Extends {@link PushManagerService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface PushManagerExtendedService extends PushManagerService {

    /**
     * Whether permanent listeners are supported.
     * <p>
     * If not supported, calling {@link #startPermanentListener(PushUser)} will return the {@link PushExceptionCodes#PERMANENT_NOT_SUPPORTED} error.
     *
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If checking for support fails
     */
    boolean supportsPermanentListeners();

    /**
     * Starts a permanent listener for specified push user.
     * <p>
     * The push manager is supposed to keep track of started listeners; e.g. only one listener per session or per user-context-pair exists.
     *
     * @param pushUser The associated push user
     * @return A newly started permanent listener or <code>null</code> if a listener could not be started
     * @throws OXException If permanent listener cannot be started due to an error
     */
    PushListener startPermanentListener(PushUser pushUser) throws OXException;

    /**
     * Stops the permanent listener for specified session.
     *
     * @param pushUser The associated push user
     * @param tryToReconnect <code>true</code> to signal that a reconnect attempt should be performed; otherwise <code>false</code>
     * @return <code>true</code> if permanent listener has been successfully stopped; otherwise <code>false</code>
     * @throws OXException If permanent listener cannot be stopped due to an error
     */
    boolean stopPermanentListener(PushUser pushUser, boolean tryToReconnect) throws OXException;

    /**
     * Gets currently available push users
     *
     * @return The currently available push users
     * @throws OXException If push users cannot be returned
     */
    List<PushUserInfo> getAvailablePushUsers() throws OXException;

}
