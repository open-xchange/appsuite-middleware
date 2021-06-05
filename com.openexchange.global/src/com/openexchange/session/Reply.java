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

package com.openexchange.session;

/**
 * This enumeration represents the possible replies that a session handling can return.
 *
 * Based on the order that the Reply values are declared,
 * Reply.CONTINUE.compareTo(Reply.STOP) will return
 * a positive value.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public enum Reply {

    /**
     * The session handling requests to stop further processing.
     */
    STOP,
    /**
     * The session handling has no interest, therefore delegates to the next one in chain.
     */
    NEUTRAL,
    /**
     * The session handling signals to continue further handling.
     */
    CONTINUE;

}