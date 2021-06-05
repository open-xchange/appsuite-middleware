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

package com.openexchange.ajax.requesthandler;

import java.util.List;


/**
 * {@link AJAXResultDecoratorRegistry} - A registry for {@link AJAXResultDecorator}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface AJAXResultDecoratorRegistry {

    /**
     * Gets the decorator for specified identifier.
     *
     * @param identifier The decorator's identifier
     * @return The identifier or <code>null</code> if there is no such decorator
     */
    AJAXResultDecorator getDecorator(String identifier);

    /**
     * Gets the decorators from this registry.
     *
     * @return The decorators kept in a list
     */
    List<AJAXResultDecorator> getDecorators();

}
