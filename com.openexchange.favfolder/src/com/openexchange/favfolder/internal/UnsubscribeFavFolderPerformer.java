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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.favfolder.internal;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UnsubscribeFavFolderPerformer}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnsubscribeFavFolderPerformer extends AbstractFavFolderPerformer {

    /**
     * Initializes a new {@link UnsubscribeFavFolderPerformer}.
     */
    public UnsubscribeFavFolderPerformer(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    /**
     * Unsubscribes specified folders
     * 
     * @param sourceTreeId The source tree identifier to subscribe from; default is {@link FolderStorage#REAL_TREE_ID}
     * @param folderIds The identifiers of the folders to subscribe
     * @param targetTreeId The identifier of the tree to subscribe to
     * @param targetParentId The identifier of the parent in target tree; default is {@link FolderStorage#ROOT_ID}
     * @param session The session providing user data
     * @throws OXException If subscribe fails for any reason
     */
    public void unsubscribeFolders(final int treeId, final Collection<String> folderIds, final ServerSession session) throws OXException {
        final FolderService folderService = getService(FolderService.class);
        final String treeId2 = treeId > 0 ? String.valueOf(treeId) : FolderStorage.REAL_TREE_ID;
        for (final String folderId : folderIds) {
            folderService.unsubscribeFolder(treeId2, folderId, session);
        }
    }

}
