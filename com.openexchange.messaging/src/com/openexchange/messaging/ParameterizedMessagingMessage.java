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

import java.util.Map;

/**
 * {@link ParameterizedMessagingMessage} - Extends {@link MessagingMessage} by the capability to carry parameters.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ParameterizedMessagingMessage extends MessagingMessage {

    /**
     * Gets all parameters of this message as a map.
     * <p>
     * Note: Any modifications applied to returned map will also be reflected in message's parameters.
     *
     * @return The parameters as a map
     */
    Map<String, Object> getParameters();

    /**
     * Gets the associated parameter value.
     *
     * @param name The parameter name
     * @return The parameter value or <code>null</code> if absent
     */
    Object getParameter(String name);

    /**
     * Puts specified parameter (and thus overwrites any existing parameter)
     *
     * @param name The parameter name
     * @param value The parameter value
     */
    void putParameter(String name, Object value);

    /**
     * Puts specified parameter if not already present.
     *
     * @param name The parameter name
     * @param value The parameter value
     * @return <code>true</code> if parameter has been put; otherwise <code>false</code> if already present
     */
    boolean putParameterIfAbsent(String name, Object value);

    /**
     * Clears all parameters associated with this message.
     */
    void clearParameters();

    /**
     * Checks if this message contains denoted parameter.
     *
     * @param name The parameter name
     * @return <code>true</code> if such a parameter exists; <code>false</code> if absent
     */
    boolean containsParameter(String name);

}
