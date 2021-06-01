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

package com.openexchange.find.basic;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link Services}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class Services {

    private static final AtomicReference<ServiceLookup> LOOKUP = new AtomicReference<ServiceLookup>();

    private Services() {
        super();
    }

    public static FolderService getFolderService() throws OXException {
        return requireService(FolderService.class);
    }

    public static MailService getMailService() throws OXException {
        return requireService(MailService.class);
    }

    public static MailAccountStorageService getMailAccountStorageService() throws OXException {
        return requireService(MailAccountStorageService.class);
    }

    public static ThreadPoolService getThreadPoolService() throws OXException {
        return requireService(ThreadPoolService.class);
    }

    public static IDBasedFileAccessFactory getIdBasedFileAccessFactory() throws OXException {
        return requireService(IDBasedFileAccessFactory.class);
    }

    public static IDBasedFolderAccessFactory getIdBasedFolderAccessFactory() throws OXException {
        return requireService(IDBasedFolderAccessFactory.class);
    }

    public static IDBasedContactsAccessFactory getIdBasedContactsAccessFactory() throws OXException {
        return requireService(IDBasedContactsAccessFactory.class);
    }

    public static ConfigurationService getConfigurationService() throws OXException {
        return requireService(ConfigurationService.class);
    }

    public static LeanConfigurationService getLeanConfigurationService() throws OXException {
        return requireService(LeanConfigurationService.class);
    }

    public static InfostoreSearchEngine getInfostoreSearchEngine() throws OXException {
        return requireService(InfostoreSearchEngine.class);
    }

    public static FileStorageServiceRegistry getFileStorageServiceRegistry() throws OXException {
        return requireService(FileStorageServiceRegistry.class);
    }

    public static void setServiceLookup(ServiceLookup lookup) {
        LOOKUP.set(lookup);
    }

    public static <T> T requireService(Class<T> clazz) throws OXException {
        T service = getServiceLookup().getService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.absentService(clazz);
        }
        return service;
    }

    public static <T> T optionalService(Class<T> clazz) {
        return getServiceLookup().getOptionalService(clazz);
    }

    private static ServiceLookup getServiceLookup() {
        ServiceLookup serviceLookup = LOOKUP.get();
        if (serviceLookup == null) {
            throw new IllegalStateException("ServiceLookup was null!");
        }

        return serviceLookup;
    }

}
