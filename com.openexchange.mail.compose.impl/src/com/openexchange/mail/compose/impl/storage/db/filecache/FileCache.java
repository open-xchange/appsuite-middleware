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

package com.openexchange.mail.compose.impl.storage.db.filecache;

import java.util.Optional;
import java.util.UUID;
import com.openexchange.exception.OXException;

/**
 * {@link FileCache} - A file cache storing very big message contents for database-backed composition space storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public interface FileCache {

    /** The file name prefix */
    public static final String FILE_NAME_PREFIX = "open-xchange-tmpcscontent-";

    /**
     * Gets the optional cached content for given arguments.
     *
     * @param compositionSpaceId The composition space identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The cached content or empty
     * @throws OXException If loading cached content fails fatally
     */
    Optional<String> getCachedContent(UUID compositionSpaceId, int userId, int contextId) throws OXException;

    /**
     * Stores given content in cache.
     *
     * @param content The content to store
     * @param compositionSpaceId The composition space identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if content has been successfully cached; otherwise <code>false</code>
     * @throws OXException If store attempt fails
     */
    boolean storeCachedContent(String content, UUID compositionSpaceId, int userId, int contextId) throws OXException;

    /**
     * Deletes cached content for given arguments.
     *
     * @param compositionSpaceId The composition space identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If deletion fails
     */
    void deleteCachedContent(UUID compositionSpaceId, int userId, int contextId) throws OXException;

    /**
     * Signals that application is going to be stopped.
     *
     * @throws OXException If operation fails fatally
     */
    void signalStop() throws OXException;

}
