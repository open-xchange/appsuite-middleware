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

package com.openexchange.find.json.osgi;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.ResultConverter;


/**
 * {@link ResultConverterRegistry} - A registry for registered {@link ResultConverter}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResultConverterRegistry implements ServiceTrackerCustomizer<ResultConverter, ResultConverter> {

    private final ConcurrentMap<String, ResultConverter> converters;
    private final BundleContext context;

    /**
     * Initializes a new {@link ResultConverterRegistry}.
     *
     * @param context The bundle context
     */
    public ResultConverterRegistry(final BundleContext context) {
        super();
        this.context = context;
        converters = new ConcurrentHashMap<String, ResultConverter>(16, 0.9f, 1);
    }

    /**
     * Gets the tracked converters.
     *
     * @return The converters
     */
    public Map<String, ResultConverter> getConverters() {
        return Collections.unmodifiableMap(converters);
    }

    /**
     * Gets the tracked converter.
     *
     * @param inputFormat The desired converter's input format
     * @return The converter or <code>null</code>
     */
    public ResultConverter getConverter(final String inputFormat) {
        return converters.get(inputFormat);
    }

    @Override
    public ResultConverter addingService(final ServiceReference<ResultConverter> reference) {
        final ResultConverter resultConverter = context.getService(reference);
        if ("json".equals(resultConverter.getOutputFormat()) && null == converters.putIfAbsent(resultConverter.getInputFormat(), resultConverter)) {
            return resultConverter;
        }
        // Not of interest or duplicate input format
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<ResultConverter> reference, final ResultConverter resultConverter) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<ResultConverter> reference, final ResultConverter resultConverter) {
        converters.remove(resultConverter.getInputFormat());
        context.ungetService(reference);
    }

}
