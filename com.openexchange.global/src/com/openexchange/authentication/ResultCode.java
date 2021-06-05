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

package com.openexchange.authentication;

/**
 * This enum signals the three possible return values of an autologin authentication process.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public enum ResultCode {

    /**
     * Signals that the incoming autologin request does not contain any information about a web services session.
     */
    FAILED,

    /**
     * Signals that the autologin request contains a web services session and the returned {@link Authenticated} contains information
     * about the OX session to create.
     */
    SUCCEEDED,

    /**
     * Signals that the client should be redirected to another URL instead of showing the user the OX login interface.
     */
    REDIRECT;

}
