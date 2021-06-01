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

package gnu.trove.procedure;


/**
 * {@link AbstractErrorAware}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
abstract class AbstractErrorAware<E extends Exception> {

    /**
     * The exception reference.
     */
    protected E exception;

    /**
     * Initializes a new {@link AbstractErrorAware}.
     */
    protected AbstractErrorAware() {
        super();
    }

    /**
     * Throws the exception if not <code>null</code>
     *
     * @throws E The expected exception
     */
    public void throwIfNotNull() throws E {
        final E exception = this.exception;
        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Gets the exception possibly thrown during iteration.
     *
     * @return The exception or <code>null</code>
     */
    public E getException() {
        return exception;
    }

    /**
     * Gets the expected exception from specified instance.
     *
     * @param e The exception instance
     * @return The expected exception
     * @throws IllegalStateException If exception is not of expected type
     */
    protected E valueOf(final Exception e) {
        try {
            return (E) e;
        } catch (ClassCastException cce) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
