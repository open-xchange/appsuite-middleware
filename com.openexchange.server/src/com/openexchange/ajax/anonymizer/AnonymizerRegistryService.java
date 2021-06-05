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

package com.openexchange.ajax.anonymizer;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;


/**
 * {@link AnonymizerRegistryService} - The registry for anonymizer.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface AnonymizerRegistryService {

    /**
     * Gets the anonymizer for given module name
     *
     * @param name The module name
     * @return The anonymizer or <code>null</code> if there is no such anonymizer
     * @throws OXException If anonymizer cannot be returned
     */
    <E> AnonymizerService<E> getAnonymizerFor(String name) throws OXException;

    /**
     * Gets the anonymizer for given module
     *
     * @param name The module
     * @return The anonymizer or <code>null</code> if there is no such anonymizer
     * @throws OXException If anonymizer cannot be returned
     */
    <E> AnonymizerService<E> getAnonymizerFor(Module module) throws OXException;

}
