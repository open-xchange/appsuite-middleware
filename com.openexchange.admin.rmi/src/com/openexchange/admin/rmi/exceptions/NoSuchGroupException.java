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

package com.openexchange.admin.rmi.exceptions;


/**
 * Is thrown if a user doesn't exist in an operation
 *
 * @author d7
 *
 */
public class NoSuchGroupException extends AbstractAdminRmiException {

    /**
     *
     */
    private static final long serialVersionUID = -994183866598925225L;

    /**
     *
     */
    public NoSuchGroupException() {
        super("User does not exist");
    }

    /**
     * @param message
     */
    public NoSuchGroupException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public NoSuchGroupException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public NoSuchGroupException(String message, Throwable cause) {
        super(message, cause);
    }

}
