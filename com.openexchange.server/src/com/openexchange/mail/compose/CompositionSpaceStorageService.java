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

package com.openexchange.mail.compose;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link CompositionSpaceStorageService} - Responsible for create, read, update and delete operations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:martin.herfurthn@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
@SingletonService
public interface CompositionSpaceStorageService {

    /**
     * Checks if the content of specified composition space is stored encrypted
     *
     * @param session The session providing user data
     * @param id The composition space identifier
     * @return <code>true</code> if content is encrypted; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean isContentEncrypted(Session session, UUID id) throws OXException;

    /**
     * Checks if such a composition space associated with given identifier exists.
     *
     * @param session The session providing user data
     * @param id The composition space identifier
     * @return <code>true</code> if such a composition space exists; otherwise <code>false</code>
     * @throws OXException If composition space cannot be returned
     */
    boolean existsCompositionSpace(Session session, UUID id) throws OXException;

    /**
     * Gets the composition space associated with given identifier.
     *
     * @param session The session providing user data
     * @param id The composition space identifier
     * @return The composition space or <code>null</code> if no such composition space exists
     * @throws OXException If composition space cannot be returned
     */
    CompositionSpace getCompositionSpace(Session session, UUID id) throws OXException;

    /**
     * Gets all available composition spaces associated with given session.
     *
     * @param session The session
     * @param fields The fields to set in returned composition spaces
     * @return The identifiers of the composition spaces
     * @throws OXException If identifiers of the composition spaces cannot be returned
     */
    List<CompositionSpace> getCompositionSpaces(Session session, MessageField[] fields) throws OXException;

    /**
     * Creates a new composition space with just the identifier set.
     *
     * @param session The session providing user information
     * @param compositionSpaceDesc The composition space, which shall be opened
     * @param optionalEncrypt The optional encryption flag. If present and <code>true</code> the attachment to save is supposed to be
     *                        encrypted according to caller. If present and<code>false</code>  the attachment to save is <b>not</b> supposed
     *                        to be encrypted according to caller. If absent, encryption is automatically determined.
     * @return The identifier for the newly created composition space
     */
    CompositionSpace openCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc, Optional<Boolean> optionalEncrypt) throws OXException;

    /**
     * Updates a composition space.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * In case given <code>CompositionSpaceDescription</code> argument specifies
     * {@link CompositionSpaceDescription#setLastModifiedDate(java.util.Date) a last-modified date}, a
     * {@link CompositionSpaceErrorCode#CONCURRENT_UPDATE} error might be thrown.
     * </div>
     *
     * @param session The session providing user information
     * @param compositionSpaceDesc The description providing the changes to apply
     * @param optionalOriginalSpace The optional original compositon space that is supposed to be updated
     * @return The composition space after changes are applied
     */
    CompositionSpace updateCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc, Optional<CompositionSpace> optionalOriginalSpace) throws OXException;

    /**
     * Closes a composition space, which effectively removes it from the storage.
     *
     * @param session The session providing user information
     * @param id The id to remove
     * @return <code>true</code> if such a composition space has been successfully closed; otherwise <code>false</code>
     */
    boolean closeCompositionSpace(Session session, UUID id) throws OXException;

    /**
     * Deletes those composition spaces associated with given session, which are idle for longer than given max. idle time.
     *
     * @param session The session
     * @param maxIdleTimeMillis The max. idle time in milliseconds
     * @return The identifiers of the composition spaces that were deleted
     * @throws OXException If composition spaces cannot be deleted
     */
    List<UUID> deleteExpiredCompositionSpaces(Session session, long maxIdleTimeMillis) throws OXException;

}
