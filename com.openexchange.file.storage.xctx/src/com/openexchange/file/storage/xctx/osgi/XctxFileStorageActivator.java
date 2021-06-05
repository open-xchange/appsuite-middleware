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

package com.openexchange.file.storage.xctx.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.xctx.XctxFileStorageService;
import com.openexchange.file.storage.xctx.subscription.XctxContactPictureFinder;
import com.openexchange.file.storage.xctx.subscription.XctxShareSubscriptionProvider;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.share.ShareService;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.share.subscription.XctxSessionManager;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link XctxFileStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFileStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link XctxFileStorageActivator}.
     */
    public XctxFileStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        // @formatter:off
        return new Class[] { XctxSessionManager.class, ContextService.class, UserService.class, GroupService.class, ShareService.class, 
            FileStorageAccountManagerLookupService.class, FolderService.class, InfostoreFacade.class, InfostoreSearchEngine.class, 
            DispatcherPrefixService.class, CapabilityService.class, UserPermissionService.class, ContactPictureService.class
        }; 
        // @formatter:on
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(XctxFileStorageActivator.class).info("starting bundle {}", context.getBundle());
            /*
             * register cross-context file storage service
             */
            XctxFileStorageService fileStorageService = new XctxFileStorageService(this);
            registerService(FileStorageService.class, fileStorageService);
            registerService(ShareSubscriptionProvider.class, new XctxShareSubscriptionProvider(this, fileStorageService));
            registerService(ContactPictureFinder.class, new XctxContactPictureFinder(this, fileStorageService));
        } catch (Exception e) {
            getLogger(XctxFileStorageActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(XctxFileStorageActivator.class).info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
