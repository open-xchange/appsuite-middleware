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

package com.openexchange.server.osgi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.ResultConverterRegistry;

/**
 * {@link CommonResultConverterRegistry} - Tracks every {@link ResultConverter}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class CommonResultConverterRegistry implements ServiceTrackerCustomizer<ResultConverter, ResultConverter>, ResultConverterRegistry {

    private final BundleContext context;

    private final ConcurrentHashMap<String, Map<String, ResultConverter>> converters;

    /**
     * Initializes a new {@link CommonResultConverterRegistry}.
     *
     * @param context The {@link BundleContext}
     *
     */
    public CommonResultConverterRegistry(BundleContext context) {
        super();
        this.context = context;
        this.converters = new ConcurrentHashMap<String, Map<String, ResultConverter>>(16, 0.9f, 1);
    }

    @Override
    public ResultConverter addingService(ServiceReference<ResultConverter> reference) {
        ResultConverter resultConverter;
        if (null != reference && null != (resultConverter = context.getService(reference))) {
            return addResultConverter(resultConverter);
        }
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<ResultConverter> reference, ResultConverter service) {
        // Igonre
    }

    @Override
    public void removedService(ServiceReference<ResultConverter> reference, ResultConverter service) {
        if (null != service) {
            Map<String, ResultConverter> map = converters.get(service.getInputFormat());
            if (null != map) {
                if (map.containsKey(service.getOutputFormat())) {
                    map.remove(service.getOutputFormat());
                }
                converters.put(service.getInputFormat(), map);
                context.ungetService(reference);
            }
        }
    }

    @Override
    public ResultConverter getResultConverter(String inputFormat, String outputFormat) {
        Map<String, ResultConverter> map = converters.get(inputFormat);
        return null != map ? map.get(outputFormat) : null;
    }

    /**
     * Add a {@link ResultConverter}
     *
     * @param resultConverter The converter to add
     * @return The {@link ResultConverter} if successfully added, else <code>null</code>
     */
    public ResultConverter addResultConverter(ResultConverter resultConverter) {
        if (null != resultConverter) {
            Map<String, ResultConverter> map = converters.get(resultConverter.getInputFormat());
            if (null == map) {
                Map<String, ResultConverter> newmap = new ConcurrentHashMap<String, ResultConverter>(16, 0.9f, 1);
                map = converters.putIfAbsent(resultConverter.getInputFormat(), newmap);
                if (null == map) {
                    map = newmap;
                }
            }
            map.put(resultConverter.getOutputFormat(), resultConverter);
            return resultConverter;
        }
        return null;
    }

}
