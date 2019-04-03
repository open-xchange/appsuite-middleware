/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mail.compose;

import java.util.List;
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
     * @return The identifier for the newly created composition space
     */
    CompositionSpace openCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc) throws OXException;

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
     * @return The composition space after changes are applied
     */
    CompositionSpace updateCompositionSpace(Session session, CompositionSpaceDescription compositionSpaceDesc) throws OXException;

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
