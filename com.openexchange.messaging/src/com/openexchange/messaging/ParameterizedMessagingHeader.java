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

package com.openexchange.messaging;

import java.util.Iterator;

/**
 * {@link ParameterizedMessagingHeader} - A {@link MessagingHeader header} which is capable to hold parameters.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface ParameterizedMessagingHeader extends MessagingHeader {

    /**
     * Adds specified value to given parameter name. If existing, the parameter is treated as a contiguous parameter according to RFC2231.
     *
     * @param key The parameter name
     * @param value The parameter value to add
     */
    public void addParameter(String key, String value);

    /**
     * Sets the given parameter. Existing value is overwritten.
     *
     * @param key The parameter name
     * @param value The parameter value
     */
    public void setParameter(String key, String value);

    /**
     * Gets specified parameter's value
     *
     * @param key The parameter name
     * @return The parameter's value or <code>null</code> if not existing
     */
    public String getParameter(String key);

    /**
     * Removes specified parameter and returns its value
     *
     * @param key The parameter name
     * @return The parameter's value or <code>null</code> if not existing
     */
    public String removeParameter(String key);

    /**
     * Checks if parameter is present
     *
     * @param key the parameter name
     * @return <code>true</code> if parameter is present; otherwise <code>false</code>
     */
    public boolean containsParameter(String key);

    /**
     * Gets all parameter names wrapped in an {@link Iterator}
     *
     * @return All parameter names wrapped in an {@link Iterator}
     */
    public Iterator<String> getParameterNames();

}
