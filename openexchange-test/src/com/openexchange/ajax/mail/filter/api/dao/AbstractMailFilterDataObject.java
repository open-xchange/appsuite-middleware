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

package com.openexchange.ajax.mail.filter.api.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link AbstractMailFilterDataObject}. Defines the elements of a mail filter data object
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractMailFilterDataObject<A extends Argument> {

    private Map<A, Object> arguments;

    /**
     * Initialises a new {@link AbstractMailFilterDataObject}.
     */
    public AbstractMailFilterDataObject() {
        super();
        arguments = new HashMap<>(4);
    }

    /**
     * Adds an argument to this {@link MatchType}
     * 
     * @param argument The {@link Argument} to add
     * @param value The value of the {@link Argument}
     */
    protected void addArgument(A argument, Object value) {
        arguments.put(argument, value);
    }

    /**
     * Returns an unmodifiable map of {@link Argument}s
     * 
     * @return an unmodifiable map of {@link Argument}s
     */
    protected Map<A, Object> getArguments() {
        return Collections.unmodifiableMap(arguments);
    }
}
