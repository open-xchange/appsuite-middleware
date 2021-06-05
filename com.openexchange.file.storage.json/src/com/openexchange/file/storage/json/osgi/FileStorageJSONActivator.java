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

package com.openexchange.file.storage.json.osgi;

import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.antivirus.AntiVirusResultEvaluatorService;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.composition.crypto.CryptographicAwareIDBasedFileAccessFactory;
import com.openexchange.file.storage.json.FileConverter;
import com.openexchange.file.storage.json.FileMetadataParser;
import com.openexchange.file.storage.json.actions.accounts.AccountActionFactory;
import com.openexchange.file.storage.json.actions.files.AliasFileActionFactory;
import com.openexchange.file.storage.json.actions.files.FileActionFactory;
import com.openexchange.file.storage.json.actions.services.ServiceActionFactory;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.file.storage.parse.FileMetadataParserService;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.preview.PreviewService;
import com.openexchange.rdiff.RdiffService;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.startup.ThreadControlService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.uploaddir.UploadDirService;

/**
 * {@link FileStorageJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageJSONActivator extends AJAXModuleActivator {

    //@formatter:off
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { FileStorageServiceRegistry.class, IDBasedFileAccessFactory.class, IDBasedFolderAccessFactory.class,
            AttachmentBase.class, FolderService.class, EventAdmin.class, ConfigurationService.class, ThreadPoolService.class,
            ThreadControlService.class, TimerService.class };
    }
    //@formatter:on

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);
            OSGiFileFieldCollector fieldCollector = new OSGiFileFieldCollector(context);
            Services.setFieldCollector(fieldCollector);
            rememberTracker(new ServiceTracker<>(context, AdditionalFileField.class.getName(), fieldCollector));

            trackService(ShareNotificationService.class);
            trackService(RdiffService.class);
            trackService(PreviewService.class);
            trackService(CryptographicAwareIDBasedFileAccessFactory.class);
            trackService(CryptographicServiceAuthenticationFactory.class);
            trackService(AntiVirusService.class);
            trackService(AntiVirusResultEvaluatorService.class);
            trackService(UploadDirService.class);
            openTrackers();
            // registerModule(AccountActionFactory.INSTANCE, "infostore");
            registerModule(FileActionFactory.INSTANCE, "infostore");
            registerModule(AliasFileActionFactory.ALIAS_INSTANCE, "files");
            registerModule(new AccountActionFactory(getService(FileStorageServiceRegistry.class)), "fileaccount");
            registerModule(new ServiceActionFactory(getService(FileStorageServiceRegistry.class)), "fileservice");
            registerService(FileMetadataParserService.class, FileMetadataParser.getInstance(), null);
            registerService(ResultConverter.class, new FileConverter(fieldCollector));
        } catch (Exception x) {
            org.slf4j.LoggerFactory.getLogger(FileStorageJSONActivator.class).error("", x);
            throw x;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        Services.setFieldCollector(null);
        super.stopBundle();
    }

}
