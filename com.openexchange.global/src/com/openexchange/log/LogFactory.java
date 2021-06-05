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

package com.openexchange.log;

import org.apache.commons.logging.LogConfigurationException;

/**
 * {@link LogFactory} - The <code>org.apache.commons.logging.LogFactory</code> using {@link LogService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @deprecated Use slf4j logging
 */
@Deprecated
public class LogFactory {

	/**
     * Convenience method to return a named logger, without the application having to care about factories.
     *
     * @param clazz The class from which a log name will be derived
     * @throws LogConfigurationException If a suitable <code>Log</code> instance cannot be returned
     * @deprecated Use slf4j logging
     */
    @Deprecated
    public static org.apache.commons.logging.Log getLog(final Class<?> clazz) {
        return org.apache.commons.logging.LogFactory.getLog(clazz);
    }

    /**
     * Convenience method to return a named logger, without the application having to care about factories.
     *
     * @param name The logical name of the <code>Log</code> instance to be returned (the meaning of this name is only known to the
     *            underlying logging implementation that is being wrapped)
     * @throws LogConfigurationException If a suitable <code>Log</code> instance cannot be returned
     * @deprecated Use slf4j logging
     */
    @Deprecated
    public static org.apache.commons.logging.Log getLog(final String name) {
        return org.apache.commons.logging.LogFactory.getLog(name);
    }

}
