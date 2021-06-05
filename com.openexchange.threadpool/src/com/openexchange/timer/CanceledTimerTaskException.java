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

package com.openexchange.timer;

/**
 * {@link CanceledTimerTaskException} - A special runtime exception suitable to be thrown from within execution of a timer service to let
 * it terminate itself.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class CanceledTimerTaskException extends RuntimeException {

    private static final long serialVersionUID = -7214265124137166983L;

    /**
     * Initializes a new {@link CanceledTimerTaskException}.
     */
    public CanceledTimerTaskException() {
        super();
    }

    /**
     * Initializes a new {@link CanceledTimerTaskException}.
     *
     * @param message The detail message
     */
    public CanceledTimerTaskException(String message) {
        super(message);
    }

    /**
     * Initializes a new {@link CanceledTimerTaskException}.
     *
     * @param cause The cause
     */
    public CanceledTimerTaskException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link CanceledTimerTaskException}.
     *
     * @param message The detail message
     * @param cause The cause
     */
    public CanceledTimerTaskException(String message, Throwable cause) {
        super(message, cause);
    }

}
