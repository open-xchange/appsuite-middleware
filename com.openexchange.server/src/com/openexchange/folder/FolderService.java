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

package com.openexchange.folder;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.server.impl.EffectivePermission;

/**
 * {@link FolderService} - The folder service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface FolderService {

    /**
     * Determines what storage to look-up.
     */
    public static enum Storage {
        /**
         * The working storage (possibly cached data).
         */
        WORKING,
        /**
         * The backup storage (possibly cached data).
         */
        BACKUP,
        /**
         * Performs live look-up on working table.
         */
        LIVE_WORKING,
        /**
         * Performs live look-up on backup table.
         */
        LIVE_BACKUP;
    }

    /**
     * Gets specified folder from given context.
     * <p>
     * First look-up is performed for {@link Storage#WORKING}. If a "folder not found" is indicated, then retry is performed for
     * {@link Storage#BACKUP}.
     *
     * @param folderId The folder ID
     * @param contextId The context ID
     * @return The folder object
     * @throws OXException If folder cannot be returned
     */
    public FolderObject getFolderObject(int folderId, int contextId) throws OXException;

    /**
     * Gets specified folder from given context.
     *
     * @param folderId The folder ID
     * @param contextId The context ID
     * @param working Whether to look-up working or backup table
     * @return The folder object
     * @throws OXException If folder cannot be returned
     */
    public FolderObject getFolderObject(int folderId, int contextId, boolean working) throws OXException;

    /**
     * Gets specified folder from given context.
     *
     * @param folderId The folder ID
     * @param contextId The context ID
     * @param storage What storage source to look-up
     * @return The folder object
     * @throws OXException If folder cannot be returned
     */
    public FolderObject getFolderObject(final int folderId, final int contextId, final Storage storage) throws OXException;

    /**
     * Determines specified user's effective permission on the folder matching given folder ID.
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param contextId The context ID
     * @return The user's effective permission
     * @throws OXException If effective permission cannot be determined
     */
    public EffectivePermission getFolderPermission(int folderId, int userId, int contextId) throws OXException;

    /**
     * Determines specified user's effective permission on the folder matching given folder ID.
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param contextId The context ID
     * @param working Whether to look-up working or backup table
     * @return The user's effective permission
     * @throws OXException If effective permission cannot be determined
     */
    public EffectivePermission getFolderPermission(int folderId, int userId, int contextId, boolean working) throws OXException;

}
