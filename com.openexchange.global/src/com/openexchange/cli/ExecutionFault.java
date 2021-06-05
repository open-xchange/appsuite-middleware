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

package com.openexchange.cli;

/**
 * {@link ExecutionFault} - A wrapper for exceptions caused by MBean/RMI invocation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ExecutionFault extends Exception {

    private static final long serialVersionUID = -435169182635746789L;

    /**
     * Initializes a new {@link ExecutionFault}.
     *
     * @param cause The cause
     */
    public ExecutionFault(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link ExecutionFault}.
     *
     * @param message The message
     * @param cause The cause
     */
    public ExecutionFault(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        return this;
    }

}
