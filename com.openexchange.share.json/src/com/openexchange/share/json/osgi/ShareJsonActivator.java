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

package com.openexchange.share.json.osgi;

import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.quota.QuotaService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.json.ShareActionFactory;
import com.openexchange.share.json.fields.ExtendedFolderPermissionsField;
import com.openexchange.share.json.fields.ExtendedObjectPermissionsField;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.subscription.ShareSubscriptionRegistry;
import com.openexchange.user.UserService;

/**
 * {@link ShareJsonActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareJsonActivator extends AJAXModuleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareJsonActivator.class);

    /**
     * Initializes a new {@link ShareJsonActivator}.
     */
    public ShareJsonActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ShareService.class, UserService.class, ContextService.class, GroupService.class, ContactService.class,
            CapabilityService.class, SessiondService.class, ShareNotificationService.class, ModuleSupport.class, QuotaService.class, 
            ContactUserStorage.class, ShareSubscriptionRegistry.class, ConversionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.share.json\"");
        trackService(IDBasedFileAccessFactory.class);
        trackService(FolderService.class);
        trackService(TranslatorFactory.class);
        trackService(DispatcherPrefixService.class);
        trackService(HostnameService.class);
        openTrackers();

        registerModule(new ShareActionFactory(this), "share/management");
        registerService(AdditionalFolderField.class, new ExtendedFolderPermissionsField(this));
        registerService(AdditionalFileField.class, new ExtendedObjectPermissionsField(this));
    }

}
