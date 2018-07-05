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
