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

package com.openexchange.java.delegate;


/**
 * {@link DelegationException}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DelegationException extends RuntimeException {

    private static final long serialVersionUID = 5659167653266376586L;

    /**
     * Initializes a new {@link DelegationException}.
     */
    public DelegationException() {
        super();
    }

    /**
     * Initializes a new {@link DelegationException}.
     *
     * @param message The detail message
     */
    public DelegationException(String message) {
        super(message);
    }

    /**
     * Initializes a new {@link DelegationException}.
     *
     * @param cause The initial cause
     */
    public DelegationException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link DelegationException}.
     *
     * @param message The detail message
     * @param cause The initial cause
     */
    public DelegationException(String message, Throwable cause) {
        super(message, cause);
    }

}
