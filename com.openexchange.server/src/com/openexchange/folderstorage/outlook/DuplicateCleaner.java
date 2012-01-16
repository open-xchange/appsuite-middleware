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

package com.openexchange.folderstorage.outlook;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.internal.Tools;
import com.openexchange.folderstorage.outlook.sql.Duplicate;
import com.openexchange.session.Session;


/**
 * {@link DuplicateCleaner}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DuplicateCleaner {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DuplicateCleaner.class));

    /**
     * Initializes a new {@link DuplicateCleaner}.
     */
    private DuplicateCleaner() {
        super();
    }

    /**
     * Cleans duplicates from virtual tree.
     * 
     * @param treeId The virtual tree identifier
     * @param storageParameters The storage parameters
     * @throws OXException If cleaning fails
     */
    public static void cleanDuplicates(final String treeId, final StorageParameters storageParameters) throws OXException {
        cleanDuplicates(treeId, storageParameters, null);
    }

    /**
     * Cleans duplicates from virtual tree.
     * 
     * @param treeId The virtual tree identifier
     * @param storageParameters The storage parameters
     * @param lookUp The optional look-up identifier possibly contained in deleted duplicates
     * @return <code>true</code> if look-up is contained in deleted IDs; otherwise <code>false</code>
     * @throws OXException If cleaning fails
     */
    public static boolean cleanDuplicates(final String treeId, final StorageParameters storageParameters, final String lookUp) throws OXException {
        final OutlookFolderStorage outlookFolderStorage = OutlookFolderStorage.getInstance();
        final FolderStorageDiscoverer folderStorageRegistry = outlookFolderStorage.folderStorageRegistry;
        final String realTreeId = outlookFolderStorage.realTreeId;

        final Session session = storageParameters.getSession();
        final int tree = Tools.getUnsignedInteger(treeId);

        final Map<String, List<String>> name2ids = Duplicate.lookupDuplicateNames(session.getContextId(), tree, session.getUserId());
        if (name2ids.isEmpty()) {
            return false;
        }
        boolean retval = false;
        for (final List<String> folderIds : name2ids.values()) {
            for (final String folderId : folderIds) {
                if (!retval && null != lookUp) {
                    retval = lookUp.equals(folderId);
                }
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
                final boolean started = folderStorage.startTransaction(storageParameters, true);
                try {
                    folderStorage.deleteFolder(realTreeId, folderId, storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                } catch (final OXException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    LOG.warn("Deleting folder "+folderId+" failed for tree " + treeId, e);
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    LOG.warn("Deleting folder "+folderId+" failed for tree " + treeId, e);
                }
            }
        }
        return retval;
    }

}
