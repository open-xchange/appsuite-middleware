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

package com.openexchange.control.internal;

/**
 * {@link BundleNotFoundException} - Indicates that a bundle could not be found.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class BundleNotFoundException extends Exception {

    private static final long serialVersionUID = 3798426359092953192L;

    /**
     * Initializes a new {@link BundleNotFoundException}.
     */
    public BundleNotFoundException() {
        super();
    }

    /**
     * Initializes a new {@link BundleNotFoundException}.
     *
     * @param message The exception message
     */
    public BundleNotFoundException(final String message) {
        super(message);
    }

    /**
     * Initializes a new {@link BundleNotFoundException}.
     *
     * @param message The exception message
     * @param exc The cause
     */
    public BundleNotFoundException(final String message, final Exception exc) {
        super(message, exc);
    }

    /**
     * Initializes a new {@link BundleNotFoundException}.
     *
     * @param exc The cause
     */
    public BundleNotFoundException(final Exception exc) {
        super(exc);
    }
}
