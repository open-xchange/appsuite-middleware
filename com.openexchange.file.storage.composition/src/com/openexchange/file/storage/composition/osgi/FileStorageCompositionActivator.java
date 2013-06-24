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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.composition.osgi;

import java.util.List;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FileStreamHandlerRegistry;
import com.openexchange.file.storage.composition.FolderAware;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.composition.internal.AbstractCompositingIDBasedFileAccess;
import com.openexchange.file.storage.composition.internal.AbstractCompositingIDBasedFolderAccess;
import com.openexchange.file.storage.composition.internal.FileStreamHandlerRegistryImpl;
import com.openexchange.file.storage.composition.internal.Services;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link FileStorageCompositionActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileStorageCompositionActivator extends HousekeepingActivator {

    private final class CompositingIDBasedFileAccessImpl extends AbstractCompositingIDBasedFileAccess implements FolderAware {

        /**
         * Initializes a new {@link CompositingIDBasedFileAccessImpl}.
         */
        CompositingIDBasedFileAccessImpl(Session session) {
            super(session);
        }

        @Override
        protected List<FileStorageService> getAllFileStorageServices() throws OXException {
            return getService(FileStorageServiceRegistry.class).getAllServices();
        }

        @Override
        protected FileStorageService getFileStorageService(final String serviceId) throws OXException {
            return getService(FileStorageServiceRegistry.class).getFileStorageService(serviceId);
        }

        @Override
        protected EventAdmin getEventAdmin() {
            return getService(EventAdmin.class);
        }

        @Override
        public FileStoragePermission optOwnPermission(final String id) throws OXException {
            final FileID fileID = new FileID(id);
            final String folderId = fileID.getFolderId();
            if (null == folderId) {
                return null;
            }
            return getFolderAccess(fileID.getService(), fileID.getAccountId()).getFolder(folderId).getOwnPermission();
        }

        @Override
        public FileStorageFolder optFolder(String id) throws OXException {
            final FileID fileID = new FileID(id);
            final String folderId = fileID.getFolderId();
            if (null == folderId) {
                return null;
            }
            return getFolderAccess(fileID.getService(), fileID.getAccountId()).getFolder(folderId);
        }
    }

    private final class CompositingIDBasedFolderAccessImpl extends AbstractCompositingIDBasedFolderAccess {

        protected CompositingIDBasedFolderAccessImpl(Session session) {
            super(session);
        }

        @Override
        protected FileStorageService getFileStorageService(String serviceId) throws OXException {
            return getService(FileStorageServiceRegistry.class).getFileStorageService(serviceId);
        }

        protected List<FileStorageService> getAllFileStorageServices() throws OXException {
            return getService(FileStorageServiceRegistry.class).getAllServices();
        }

        @Override
        protected EventAdmin getEventAdmin() {
            return getService(EventAdmin.class);
        }

    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[]{ FileStorageServiceRegistry.class, EventAdmin.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        registerService(IDBasedFileAccessFactory.class, new IDBasedFileAccessFactory() {

            @Override
            public IDBasedFileAccess createAccess(final Session session) {
                return new CompositingIDBasedFileAccessImpl(session);
            }

        });
        registerService(IDBasedFolderAccessFactory.class, new IDBasedFolderAccessFactory() {

            @Override
            public CompositingIDBasedFolderAccessImpl createAccess(Session session) {
                return new CompositingIDBasedFolderAccessImpl(session);
            }

        });
        // Start-up & register FileStreamHandlerRegistry
        final FileStreamHandlerRegistryImpl registry = new FileStreamHandlerRegistryImpl(context);
        AbstractCompositingIDBasedFileAccess.setHandlerRegistry(registry);
        rememberTracker(registry);
        openTrackers();
        registerService(FileStreamHandlerRegistry.class, registry);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        AbstractCompositingIDBasedFileAccess.setHandlerRegistry(null);
        Services.setServiceLookup(null);
    }

}
