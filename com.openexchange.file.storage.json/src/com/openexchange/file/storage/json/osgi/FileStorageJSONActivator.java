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

package com.openexchange.file.storage.json.osgi;

import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
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
import com.openexchange.i18n.I18nService;
import com.openexchange.preview.PreviewService;
import com.openexchange.rdiff.RdiffService;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.startup.ThreadControlService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link FileStorageJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageJSONActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { FileStorageServiceRegistry.class, IDBasedFileAccessFactory.class, IDBasedFolderAccessFactory.class,
            AttachmentBase.class, FolderService.class, EventAdmin.class, ConfigurationService.class, ThreadPoolService.class,
            ThreadControlService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);
            rememberTracker(new ServiceTracker<I18nService, I18nService>(context, I18nService.class.getName(), new I18nServiceCustomizer(context)));
            FileFieldCollector fieldCollector = new FileFieldCollector(context);
            Services.setFieldCollector(fieldCollector);
            rememberTracker(new ServiceTracker<AdditionalFileField, AdditionalFileField>(context, AdditionalFileField.class.getName(), fieldCollector));

            trackService(ShareNotificationService.class);
            trackService(RdiffService.class);
            trackService(PreviewService.class);
            trackService(CryptographicAwareIDBasedFileAccessFactory.class);
            trackService(CryptographicServiceAuthenticationFactory.class);
            openTrackers();
            // registerModule(AccountActionFactory.INSTANCE, "infostore");
            registerModule(FileActionFactory.INSTANCE, "infostore");
            registerModule(AliasFileActionFactory.ALIAS_INSTANCE, "files");
            registerModule(new AccountActionFactory(getService(FileStorageServiceRegistry.class)), "fileaccount");
            registerModule(new ServiceActionFactory(getService(FileStorageServiceRegistry.class)), "fileservice");
            registerService(FileMetadataParserService.class, FileMetadataParser.getInstance(), null);
            registerService(ResultConverter.class, new FileConverter(fieldCollector));
        } catch (final Exception x) {
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
