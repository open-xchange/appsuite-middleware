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

package com.openexchange.test;

import org.junit.Assert;

/**
 * {@link AbstractAssertTool} - Base help class for assertion tools
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractAssertTool extends Assert {

    /**
     * Asserts the expected and actual objects
     *
     * @param message The message if the assertion fails
     * @param expect The expected object
     * @param value The actual object
     */
    public static void assertEqualsAndNotNull(final String message, final Object expect, final Object value) {
        if (expect == null) {
            return;
        }

        assertNotNull(message + " is null", value);
        assertEquals(message, expect, value);
    }
}
