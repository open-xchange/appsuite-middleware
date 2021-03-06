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
 * Thrown if an user doesn't exist in an operation.
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class NoSuchReasonException extends AbstractAdminRmiException {

    /**
     * For serializations
     */
    private static final long serialVersionUID = 8838129017619256228L;

    /**
     *
     */
    public NoSuchReasonException() {
        super("Reason does not exist");
    }

    /**
     * @param message
     */
    public NoSuchReasonException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public NoSuchReasonException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public NoSuchReasonException(String message, Throwable cause) {
        super(message, cause);
    }

}
