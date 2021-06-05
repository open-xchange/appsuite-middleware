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

package com.openexchange.conversion.simple.osgi;

import org.osgi.framework.ServiceReference;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.conversion.simple.impl.AJAXConverterAdapter;
import com.openexchange.conversion.simple.impl.PayloadConverterAdapter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link SimpleConverterActivator} To register a simple conversion service to
 * convert Object "data" from a certain format into another certain format.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class SimpleConverterActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { Converter.class };
    }

    /*
     * Register the SimpleConverterService and listen for registrations of new
     * SimplePayloadConverters. When new SimplePayloadConverters are added wrap
     * them in a PayloadConverterAdapter and register them as ResultConverter
     * service so they can be added to the DefaultConverter (as the
     * DispatcherActivator is listening for new ResultConverter services)
     */
    @Override
    protected void startBundle() throws Exception {
        //Get the Default converter that is able to convert AJAXRequestResults from..to
        Converter converter = getService(Converter.class);

        final AJAXConverterAdapter rtConverter = new AJAXConverterAdapter(converter);
        registerService(SimpleConverter.class, rtConverter);

        track(SimplePayloadConverter.class, new SimpleRegistryListener<SimplePayloadConverter>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void added(ServiceReference<SimplePayloadConverter> ref, SimplePayloadConverter service) {
                registerService(ResultConverter.class, new PayloadConverterAdapter(service, rtConverter));
            }

            @Override
            public void removed(ServiceReference<SimplePayloadConverter> ref, SimplePayloadConverter service) {
                // TODO: Figure out something here
            }
        });

        openTrackers();
    }
}
