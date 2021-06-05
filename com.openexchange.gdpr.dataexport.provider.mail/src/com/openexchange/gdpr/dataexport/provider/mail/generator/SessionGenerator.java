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

package com.openexchange.gdpr.dataexport.provider.mail.generator;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.GeneratedSession;
import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;

/**
 * {@link SessionGenerator} - Generates the session to use for accessing mail system.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
@Service
public interface SessionGenerator {

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Checks if this generator is applicable.
     *
     * @param session The session in use while submitting data export task
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If applicability cannot be checked
     */
    boolean isApplicable(Session session) throws OXException;

    /**
     * Crafts the appropriate properties for given arguments that are supposed to be added to existent module properties.
     * <p>
     * Those extended properties are required in order to craft a session later on through {@link #generateSession(int, int, Map)} call.
     *
     * @param session The session in use while submitting data export task
     * @return The extended properties or an empty map
     * @throws OXException If extended properties cannot be returned
     */
    Map<String, Object> craftExtendedProperties(Session session) throws OXException;

    /**
     * Generates the session to use for accessing mail system.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param moduleProperties The module properties (with extended properties previously generated through {@link #craftExtendedProperties(Session)})
     * @return The session
     * @throws OXException If session cannot be generated
     */
    GeneratedSession generateSession(int userId, int contextId, Map<String, Object> moduleProperties) throws OXException;

    /**
     * Invoked in case mail system cannot be accessed due to an authentication failure.
     *
     * @param e The exception reflecting the authentication failure
     * @param session The session used to access mail system
     * @param moduleProperties The used module properties (with extended properties previously generated through {@link #craftExtendedProperties(Session)})
     * @return The result after examination of passed authentication failure; e.g. {@link FailedAuthenticationResult#retry() perform a retry}
     * @throws OXException If there is no remedy for authentication failure
     */
    FailedAuthenticationResult onFailedAuthentication(OXException e, GeneratedSession session, Map<String, Object> moduleProperties) throws OXException;

}
