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

package com.openexchange.file.storage.json.services;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.service.event.EventAdmin;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.antivirus.AntiVirusResultEvaluatorService;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.composition.crypto.CryptographicAwareIDBasedFileAccessFactory;
import com.openexchange.file.storage.json.FileFieldCollector;
import com.openexchange.file.storage.json.osgi.OSGiFileFieldCollector;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.preview.PreviewService;
import com.openexchange.rdiff.RdiffService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.startup.ThreadControlService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.uploaddir.UploadDirService;

/**
 * {@link Services}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Services {

    private static AtomicReference<ServiceLookup> LOOKUP_REF = new AtomicReference<>();
    private static AtomicReference<OSGiFileFieldCollector> FIELD_COLLECTOR = new AtomicReference<>();

    /**
     * Sets the file field collector instance.
     *
     * @param fieldCollector The field collector to set
     */
    public static void setFieldCollector(OSGiFileFieldCollector fieldCollector) {
        FIELD_COLLECTOR.set(fieldCollector);
    }

    /**
     * Sets the service look-up instance.
     *
     * @param serviceLookup The service look-up instance
     */
    public static void setServiceLookup(final ServiceLookup serviceLookup) {
        LOOKUP_REF.set(serviceLookup);
    }

    /**
     * Gets the file field collector instance.
     *
     * @return The field collector, or <code>null</code> if not initialized
     */
    public static FileFieldCollector getFieldCollector() {
        return FIELD_COLLECTOR.get();
    }

    public static ConfigurationService getConfigurationService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(ConfigurationService.class);
    }

    public static EventAdmin getEventAdmin() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(EventAdmin.class);
    }

    public static IDBasedFileAccessFactory getFileAccessFactory() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(IDBasedFileAccessFactory.class);
    }

    public static CryptographicAwareIDBasedFileAccessFactory getCryptographicFileAccessFactory() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(CryptographicAwareIDBasedFileAccessFactory.class);
    }

    public static CryptographicServiceAuthenticationFactory getCryptographicServiceAuthenticationFactory() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(CryptographicServiceAuthenticationFactory.class);
    }

    public static IDBasedFolderAccessFactory getFolderAccessFactory() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(IDBasedFolderAccessFactory.class);
    }

    public static AttachmentBase getAttachmentBase() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(AttachmentBase.class);
    }

    public static RdiffService getRdiffService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(RdiffService.class);
    }

    public static FolderService getFolderService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(FolderService.class);
    }

    public static ShareNotificationService getShareNotificationService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(ShareNotificationService.class);
    }

    public static ThreadPoolService getThreadPoolService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(ThreadPoolService.class);
    }

    public static PreviewService getPreviewService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(PreviewService.class);
    }

    public static ThreadControlService getThreadControlService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(ThreadControlService.class);
    }

    public static AntiVirusService getAntiVirusService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(AntiVirusService.class);
    }

    public static AntiVirusResultEvaluatorService getAntiVirusResultEvaluatorService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(AntiVirusResultEvaluatorService.class);
    }

    public static UploadDirService getUploadDirService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(UploadDirService.class);
    }

    public static TimerService getTimerService() {
        final ServiceLookup lookup = LOOKUP_REF.get();
        return null == lookup ? null : lookup.getService(TimerService.class);
    }

}
