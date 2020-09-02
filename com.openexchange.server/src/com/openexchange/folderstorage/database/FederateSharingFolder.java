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

package com.openexchange.folderstorage.database;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.filestorage.FileStorageId;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link FederateSharingFolder} - Handling for folders shared from other OX installations
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class FederateSharingFolder {

    private static final Logger LOG = LoggerFactory.getLogger(FederateSharingFolder.class);

    /**
     * Returns if there is at least one Federal Sharing account for the given session
     *
     * @param session The session to check
     * @return <code>True</code>, if there is at least one federated sharing account for the given session <code>false</code> otherwise.
     * @throws OXException
     */
    public static boolean hasFederalSharingAccount(final Session session) throws OXException {
        FileStorageServiceRegistry registry = ServerServiceRegistry.getInstance().getService(FileStorageServiceRegistry.class);
        List<FileStorageService> allServices = registry.getAllServices();
        for (FileStorageService service : allServices) {
            if (service instanceof AccountAware && service instanceof SharingFileStorageService) {
                List<FileStorageAccount> accounts = ((AccountAware) service).getAccounts(session);
                if (accounts.size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets a list of federated shared folders for the given {@link Session}
     *
     * @param parentFolder the ID of the parent folder to get the subfolders for
     * @param session The {@link Session} to get the federated shared folders for
     * @return The list of federated shared folders for the given session
     */
    public static SortableId[] getFolders(final int parentFolder, final Session session) {
        List<SortableId> ret = new ArrayList<>();
        int ordinal = 0;
        try {
            FileStorageServiceRegistry registry = ServerServiceRegistry.getInstance().getService(FileStorageServiceRegistry.class);
            List<FileStorageService> allServices = registry.getAllServices();
            for (FileStorageService service : allServices) {
                //TODO: Caching?
                if (service instanceof AccountAware && service instanceof SharingFileStorageService) {
                    List<FileStorageAccount> accounts = ((AccountAware) service).getAccounts(session);
                    for (FileStorageAccount account : accounts) {
                        try {
                            FileStorageAccountAccess access = account.getFileStorageService().getAccountAccess(account.getId(), session);
                            access.connect();
                            FileStorageFolderAccess folderAccess = access.getFolderAccess();
                            FileStorageFolder[] sharedFolders = folderAccess.getSubfolders(String.valueOf(parentFolder), true);
                            for (FileStorageFolder sharedFolder : sharedFolders) {
                                String folderId = IDMangler.mangle(service.getId(), account.getId(), sharedFolder.getId());
                                ret.add(new FileStorageId(folderId, ordinal++, sharedFolder.getName()));
                            }
                        }
                        catch(Exception e) {
                            LOG.error("Unable to list federate sharing account " + account.getId(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to list federate sharing folders", e);
        }
        return ret.toArray(new SortableId[ret.size()]);
    }
}
