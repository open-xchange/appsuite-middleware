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

package com.openexchange.gdpr.dataexport.json.osgi;

import javax.servlet.ServletException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.json.DataExportActionFactory;
import com.openexchange.gdpr.dataexport.json.converter.DataExportJSONResultConverter;
import com.openexchange.gdpr.dataexport.json.rest.DataExportRestServlet;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.osgi.service.http.HttpServices;

/**
 * {@link DataExportJSONActivator} - Activator for the GDPR data export JSON interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportJSONActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DataExportService.class, DispatcherPrefixService.class, CapabilityService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataExportJSONActivator.class);
        registerModule(new DataExportActionFactory(this), DataExportActionFactory.getModule());
        registerService(ResultConverter.class, new DataExportJSONResultConverter());

        DispatcherPrefixService dispatcherPrefixService = getService(DispatcherPrefixService.class);
        final String prefix = dispatcherPrefixService.getPrefix();
        track(HttpService.class, new SimpleRegistryListener<HttpService>() {

            @Override
            public void added(final ServiceReference<HttpService> ref, final HttpService service) {
                try {
                    service.registerServlet(prefix + "gdpr/dataexport", new DataExportRestServlet(prefix), null, null);
                } catch (final ServletException e) {
                    log.error("Servlet registration failed: {}", DataExportRestServlet.class.getName(), e);
                } catch (final NamespaceException e) {
                    log.error("Servlet registration failed: {}", DataExportRestServlet.class.getName(), e);
                }
            }

            @Override
            public void removed(final ServiceReference<HttpService> ref, final HttpService service) {
                HttpServices.unregister(prefix + "gdpr/dataexport", service);
            }
        });
        openTrackers();
        log.info("Bundle successfully started: com.openexchange.gdpr.dataexport.json");
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        org.slf4j.LoggerFactory.getLogger(DataExportJSONActivator.class).info("Bundle stopped: com.openexchange.gdpr.dataexport.json");
    }
}
