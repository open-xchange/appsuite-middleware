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

package com.openexchange.monitoring.sockets.exceptions;

/**
 * {@link BlackListException} - Thrown in case a logger cannot be registered since it is already black-listed.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class BlackListException extends Exception {

    private static final long serialVersionUID = 4369004781883696987L;

    /**
     * Initializes a new {@link BlackListException}.
     *
     * @param loggerName The logger's actual name
     * @param registeredName The registered name
     */
    public BlackListException(String loggerName, String registeredName) {
        super(new StringBuilder(128).append("The logger '").append(loggerName).append("' with registered name '").append(registeredName).append("' is blacklisted.").toString());
    }

}
