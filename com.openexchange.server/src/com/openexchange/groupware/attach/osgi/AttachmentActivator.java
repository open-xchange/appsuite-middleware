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

package com.openexchange.groupware.attach.osgi;

import org.osgi.framework.ServiceRegistration;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.antivirus.AntiVirusResultEvaluatorService;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.attach.AttachmentFilestoreLocationUpdater;
import com.openexchange.groupware.attach.impl.AttachmentBaseImpl;
import com.openexchange.groupware.attach.impl.AttachmentQuotaProvider;
import com.openexchange.groupware.attach.json.AttachmentActionFactory;
import com.openexchange.groupware.filestore.FileLocationHandler;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;

/**
 * {@link AttachmentActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class AttachmentActivator extends AJAXModuleActivator {

    public AttachmentActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[0];
    }

    @Override
    protected void startBundle() throws Exception {
        /*
         * register attachment filestore location updater for move context filestore
         */
        registerService(FileLocationHandler.class, new AttachmentFilestoreLocationUpdater());
        registerModule(new AttachmentActionFactory(new ExceptionOnAbsenceServiceLookup(this)), "attachment");
        DependentServiceRegisterer<QuotaProvider> quotaProviderRegisterer = new DependentServiceRegisterer<QuotaProvider>(
            context,
            QuotaProvider.class,
            AttachmentQuotaProvider.class,
            null,
            DatabaseService.class,
            ContextService.class,
            ConfigViewFactory.class,
            QuotaFileStorageService.class) {

            @Override
            protected void register() {
                super.register();
                AttachmentBaseImpl.setQuotaProvider((AttachmentQuotaProvider) registeredService);
            }

            @Override
            protected void unregister(ServiceRegistration<?> unregister, Object service) {
                AttachmentBaseImpl.setQuotaProvider(null);
                super.unregister(unregister, service);
            }
        };
        track(quotaProviderRegisterer.getFilter(), quotaProviderRegisterer);
        trackService(AntiVirusService.class);
        trackService(AntiVirusResultEvaluatorService.class);
        openTrackers();
    }

}
