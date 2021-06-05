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

package com.openexchange.subscribe.internal;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.TargetFolderDefinition;


/**
 * {@link FolderUpdaterStrategy}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public interface FolderUpdaterStrategy<T> {

    /**
     * Whether this {@link FolderUpdaterStrategy} handles the given folder or not
     *
     * @param folder The folder to check
     * @return true if this {@link FolderUpdaterStrategy} handles this folder, otherwise false
     */
    boolean handles(FolderObject folder);

    /**
     * Creates a new session
     *
     * @param target The target folder
     * @return The session object
     * @throws OXException
     */
    Object startSession(TargetFolderDefinition target) throws OXException;

    /**
     * Gets the data from the subscription
     *
     * @param target The {@link TargetFolderDefinition}
     * @param session The session previously obtained from {@link #startSession(TargetFolderDefinition)}
     * @return A {@link Collection} of T
     * @throws OXException
     */
    Collection<T> getData(TargetFolderDefinition target, Object session) throws OXException;

    /**
     * Updates a data entry
     *
     * @param original The original entry
     * @param update The updated entry
     * @param session The session previously obtained from {@link #startSession(TargetFolderDefinition)}
     * @throws OXException
     */
    void update(T original, T update, Object session) throws OXException;

    /**
     * Saves a new entry
     *
     * @param newElement The new entry
     * @param session The session previously obtained from {@link #startSession(TargetFolderDefinition)}
     * @param errors A {@link Collection} of previous {@link OXException}
     * @throws OXException
     */
    void save(T newElement, Object session, Collection<OXException> errors) throws OXException;

    /**
     * Calculates the similarity score between two entries
     *
     * @param original The original entry
     * @param candidate The candidate
     * @param session The session previously obtained from {@link #startSession(TargetFolderDefinition)}
     * @return a similarity score
     * @throws OXException
     */
    int calculateSimilarityScore(T original, T candidate, Object session) throws OXException;

    /**
     * Gets the similarity threshold. Entries with a similarity score greater than this are to be considered to be the same entity
     *
     * @param session The session previously obtained from {@link #startSession(TargetFolderDefinition)}
     * @return The similarity threshold
     * @throws OXException
     */
    int getThreshold(Object session) throws OXException;

    /**
     * Closes the session previously obtained from {@link #startSession(TargetFolderDefinition)}
     *
     * @param session The session to close
     * @throws OXException
     */
    void closeSession(Object session) throws OXException;

}
