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
