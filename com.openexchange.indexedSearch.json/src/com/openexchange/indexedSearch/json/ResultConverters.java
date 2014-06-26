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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.indexedSearch.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONTokener;
import org.json.JSONValue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Java7ConcurrentLinkedQueue;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ResultConverters}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResultConverters {

    private volatile ServiceTracker<ResultConverter, ResultConverter> tracker;

    private final ConcurrentMap<String, Queue<ResultConverter>> converters;

    /**
     * Initializes a new {@link ResultConverters}.
     */
    public ResultConverters() {
        super();
        converters = new ConcurrentHashMap<String, Queue<ResultConverter>>(16);
    }

    /**
     * Starts-up registry.
     *
     * @param context The bundle context
     */
    public void start(final BundleContext context) {
        ServiceTracker<ResultConverter, ResultConverter> tracker = this.tracker;
        if (null == tracker) {
            final ServiceTrackerCustomizer<ResultConverter, ResultConverter> customizer =
                new ServiceTrackerCustomizer<ResultConverter, ResultConverter>() {

                    @Override
                    public ResultConverter addingService(final ServiceReference<ResultConverter> reference) {
                        final ResultConverter resultConverter = context.getService(reference);
                        if (add(resultConverter)) {
                            return resultConverter;
                        }
                        context.ungetService(reference);
                        return null;
                    }

                    @Override
                    public void modifiedService(final ServiceReference<ResultConverter> reference, final ResultConverter service) {
                        // Nope
                    }

                    @Override
                    public void removedService(final ServiceReference<ResultConverter> reference, final ResultConverter service) {
                        if (null != service) {
                            remove(service);
                            context.ungetService(reference);
                        }

                    }
                };
            tracker = this.tracker = new ServiceTracker<ResultConverter, ResultConverter>(context, ResultConverter.class, customizer);
            tracker.open();
        }
    }

    /**
     * Stops this registry.
     */
    public void stop() {
        final ServiceTracker<ResultConverter, ResultConverter> tracker = this.tracker;
        if (null != tracker) {
            tracker.close();
            this.tracker = null;
        }
    }

    private static final Set<String> ACCEPTED = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("json", "apiResponse")));

    /**
     * Adds given {@link ResultConverter converter}
     *
     * @param rc The converter to add
     */
    public boolean add(final ResultConverter rc) {
        if (ACCEPTED.contains(rc.getOutputFormat())) {
            final String inputFormat = rc.getInputFormat();
            Queue<ResultConverter> queue = converters.get(inputFormat);
            if (null == queue) {
                final Queue<ResultConverter> nq = new Java7ConcurrentLinkedQueue<ResultConverter>();
                queue = converters.putIfAbsent(inputFormat, nq);
                if (null == queue) {
                    queue = nq;
                }
            }
            queue.add(rc);
            return true;
        }
        return false;
    }

    /**
     * Removes given {@link ResultConverter converter}
     *
     * @param rc The converter to remove
     * @return <code>true</code> if removed; otherwise <code>false</code> if there was no such converter
     */
    public boolean remove(final ResultConverter rc) {
        final Queue<ResultConverter> queue = converters.get(rc.getInputFormat());
        if (null == queue) {
            return false;
        }
        return queue.remove(rc);
    }

    /**
     * Gets the {@link ResultConverter converter} for specified input format.
     *
     * @param inputFormat The input format
     * @return THe converter or <code>null</code>
     */
    public ResultConverter getFor(final String inputFormat) {
        final Queue<ResultConverter> queue = converters.get(inputFormat);
        if (null == queue) {
            return null;
        }
        ResultConverter fallback = null;
        for (final ResultConverter rc : queue) {
            if ("json".equals(rc.getOutputFormat())) {
                return rc;
            }
            fallback = rc;
        }
        return ApiResponseResultConverter.converterFor(fallback);
    }

    private static final class ApiResponseResultConverter implements ResultConverter {

        protected static ApiResponseResultConverter converterFor(final ResultConverter delegate) {
            if ((null == delegate) || (!"apiResponse".equals(delegate.getOutputFormat()) || "json".equals(delegate.getInputFormat()))) {
                return null;
            }
            return new ApiResponseResultConverter(delegate);
        }

        private final ResultConverter delegate;

        /**
         * Initializes a new {@link ResultConverters.ApiResponseResultConverter}.
         */
        private ApiResponseResultConverter(final ResultConverter delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public String getInputFormat() {
            return delegate.getInputFormat();
        }

        @Override
        public String getOutputFormat() {
            return "json";
        }

        @Override
        public Quality getQuality() {
            return delegate.getQuality();
        }

        @Override
        public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
            delegate.convert(requestData, result, session, converter);
            final Response response = (Response) result.getResultObject();
            result.setResultObject(asJSObject(response.getData()), "json");

        }

        private static Object asJSObject(final Object value) {
            if (value instanceof JSONValue) {
                return value;
            }
            if (null == value) {
                return null;
            }
            try {
                return new JSONTokener(value.toString()).nextValue();
            } catch (final Exception e) {
                return value;
            }
        }

    }

}
