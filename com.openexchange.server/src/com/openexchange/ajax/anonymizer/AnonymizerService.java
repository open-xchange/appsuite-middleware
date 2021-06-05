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
import com.openexchange.session.Session;

/**
 * {@link AnonymizerService} - Performs needed modifications for a certain entity type in order to generate an anonymized instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface AnonymizerService<E> {

    /**
     * Gets the module this anonymizer is responsible for.
     *
     * @return The module
     */
    Module getModule();

    /**
     * Performs needed modifications in order to have an anonymized entity.
     *
     * @param entity The entity to anonymize
     * @param session The associated session
     * @return The anonymized entity
     * @throws OXException If anonymizing fails
     */
    E anonymize(E entity, Session session) throws OXException;

}
