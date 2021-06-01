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

package com.openexchange.contact.osgi;

import org.osgi.framework.ServiceReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.admin.HideAdminService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.internal.ContactServiceImpl;
import com.openexchange.contact.internal.ContactServiceLookup;
import com.openexchange.contact.internal.FilteringContactService;
import com.openexchange.contact.storage.registry.ContactStorageRegistry;
import com.openexchange.context.ContextService;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.contact.ContactPictureURLService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.user.UserService;
import com.openexchange.user.interceptor.UserServiceInterceptor;
import com.openexchange.user.interceptor.UserServiceInterceptorRegistry;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ContactServiceActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactServiceActivator extends HousekeepingActivator {

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactServiceActivator.class);

    /**
     * Initializes a new {@link ContactServiceActivator}.
     */
    public ContactServiceActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContactStorageRegistry.class, ContextService.class, FolderService.class, ConfigurationService.class,
            UserConfigurationService.class, UserPermissionService.class, ThreadPoolService.class, UserService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: com.openexchange.contact.service");
            ContactServiceLookup.set(this);

            trackService(HideAdminService.class);
            final UserServiceInterceptorRegistry interceptorRegistry = new UserServiceInterceptorRegistry(context);
            track(UserServiceInterceptor.class, interceptorRegistry);
            track(ContactPictureURLService.class, new SimpleRegistryListener<ContactPictureURLService>() {

                @Override
                public void added(ServiceReference<ContactPictureURLService> ref, ContactPictureURLService service) {
                    ServerServiceRegistry.getInstance().addService(ContactPictureURLService.class, service);
                }

                @Override
                public void removed(ServiceReference<ContactPictureURLService> ref, ContactPictureURLService service) {
                    ServerServiceRegistry.getInstance().removeService(ContactPictureURLService.class);
                }
            });
            openTrackers();

            final ContactService contactService = new FilteringContactService(new ContactServiceImpl(interceptorRegistry), this);
            super.registerService(ContactService.class, contactService);
            ServerServiceRegistry.getInstance().addService(ContactService.class, contactService);
        } catch (Exception e) {
            LOG.error("error starting \"com.openexchange.contact.service\"", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.contact.service");
        ContactServiceLookup.set(null);
        super.stopBundle();
    }

}
