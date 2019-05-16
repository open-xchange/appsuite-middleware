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
 *    trademarks of the OX Software GmbH group of companies.
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
