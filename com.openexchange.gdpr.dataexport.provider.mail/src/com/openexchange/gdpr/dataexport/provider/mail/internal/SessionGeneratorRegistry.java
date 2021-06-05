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

package com.openexchange.gdpr.dataexport.provider.mail.internal;

import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.provider.mail.generator.SessionGenerator;
import com.openexchange.session.Session;

/**
 * {@link SessionGeneratorRegistry} - The registry for available session generators.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface SessionGeneratorRegistry {

    /**
     * Gets the suitable generator for given arguments.
     *
     * @param session The session
     * @return The generator
     * @throws OXException If a suitable generator cannot be returned
     */
    SessionGenerator getGenerator(Session session) throws OXException;

    /**
     * Gets the generator by given identifier.
     *
     * @param generatorId The generator identifier
     * @return The generator
     * @throws OXException If there is no such generator
     */
    SessionGenerator getGeneratorById(String generatorId)  throws OXException;

}
