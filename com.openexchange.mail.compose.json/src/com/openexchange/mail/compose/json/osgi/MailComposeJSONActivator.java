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

package com.openexchange.mail.compose.json.osgi;

import javax.servlet.ServletException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.mail.compose.CompositionSpaceServiceFactoryRegistry;
import com.openexchange.mail.compose.json.MailComposeActionFactory;
import com.openexchange.mail.compose.json.converter.AttachmentJSONResultConverter;
import com.openexchange.mail.compose.json.converter.CompositionSpaceJSONResultConverter;
import com.openexchange.mail.compose.json.rest.MailComposeRestServlet;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link MailComposeJSONActivator} - Activator for the mail compose JSON interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailComposeJSONActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ThreadPoolService.class, DispatcherPrefixService.class, IDBasedFileAccessFactory.class, CompositionSpaceServiceFactoryRegistry.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailComposeJSONActivator.class);
        
        registerModule(new MailComposeActionFactory(this), MailComposeActionFactory.getModule());
        registerService(ResultConverter.class, new CompositionSpaceJSONResultConverter());
        registerService(ResultConverter.class, new AttachmentJSONResultConverter());

        DispatcherPrefixService dispatcherPrefixService = getService(DispatcherPrefixService.class);
        final String prefix = dispatcherPrefixService.getPrefix();
        track(HttpService.class, new SimpleRegistryListener<HttpService>() {

            @Override
            public void added(final ServiceReference<HttpService> ref, final HttpService service) {
                try {
                    service.registerServlet(prefix + "mail/compose", new MailComposeRestServlet(prefix), null, null);
                } catch (ServletException e) {
                    log.error("Servlet registration failed: {}", MailComposeRestServlet.class.getName(), e);
                } catch (NamespaceException e) {
                    log.error("Servlet registration failed: {}", MailComposeRestServlet.class.getName(), e);
                }
            }

            @Override
            public void removed(final ServiceReference<HttpService> ref, final HttpService service) {
                HttpServices.unregister(prefix + "mail/compose", service);
            }
        });
        trackService(CapabilityService.class);
        openTrackers();
        log.info("Bundle successfully started: com.openexchange.mail.compose.json");
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        org.slf4j.LoggerFactory.getLogger(MailComposeJSONActivator.class).info("Bundle stopped: com.openexchange.mail.compose.json");
    }
}
