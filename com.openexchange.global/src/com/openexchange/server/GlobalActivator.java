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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.classloader.osgi.DynamicClassLoaderActivator;
import com.openexchange.exception.internal.I18nCustomizer;
import com.openexchange.i18n.I18nService;
import com.openexchange.java.ConcurrentList;
import com.openexchange.log.LogFactory;
import com.openexchange.log.LogWrapperFactory;
import com.openexchange.tools.strings.BasicTypesStringParser;
import com.openexchange.tools.strings.CompositeParser;
import com.openexchange.tools.strings.DateStringParser;
import com.openexchange.tools.strings.StringParser;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link GlobalActivator} - Activator for global (aka kernel) bundle
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GlobalActivator implements BundleActivator {

    private static final Log LOG = LogFactory.getLog(GlobalActivator.class);

    private volatile Initialization initialization;
    private volatile ServiceTracker<StringParser,StringParser> parserTracker;
    private volatile ServiceRegistration<StringParser> parserRegistration;
    private volatile List<ServiceTracker<?,?>> trackers;
    private volatile DynamicClassLoaderActivator dynamicClassLoaderActivator;

    /**
     * Initializes a new {@link GlobalActivator}
     */
    public GlobalActivator() {
        super();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        try {
            final Initialization initialization = new com.openexchange.server.ServerInitialization();
            this.initialization = initialization;
            initialization.start();
            ServiceHolderInit.getInstance().start();
            initStringParsers(context);

            final List<ServiceTracker<?, ?>> trackers = new ArrayList<ServiceTracker<?, ?>>(4);
            this.trackers = trackers;
            trackers.add(new ServiceTracker<I18nService, I18nService>(context, I18nService.class, new I18nCustomizer(context)));

            final ServiceTracker<LogWrapperFactory, LogWrapperFactory> logWrapperTracker = new ServiceTracker<LogWrapperFactory, LogWrapperFactory>(context, LogWrapperFactory.class, null);
			LogFactory.FACTORY.set(new LogWrapperFactory() {

				@Override
                public Log wrap(final String name, final Log log) {
                    Log retval = log;
                    for (final LogWrapperFactory factory : logWrapperTracker.getTracked().values()) {
                        retval = factory.wrap(name, retval);
                    }
                    return retval;
                }
			});

            trackers.add(logWrapperTracker);

            for (final ServiceTracker<?,?> tracker : trackers) {
                tracker.open();
            }

            final DynamicClassLoaderActivator dynamicClassLoaderActivator = new DynamicClassLoaderActivator();
            dynamicClassLoaderActivator.start(context);
            this.dynamicClassLoaderActivator = dynamicClassLoaderActivator;

            LOG.info("Global bundle successfully started");
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private void initStringParsers(final BundleContext context) {
        final ServiceTracker<StringParser, StringParser> parserTracker;
        final ConcurrentList<StringParser> trackedParsers = new ConcurrentList<StringParser>();
        {
            final ServiceTrackerCustomizer<StringParser, StringParser> customizer = new ServiceTrackerCustomizer<StringParser, StringParser>() {

                @Override
                public void removedService(final ServiceReference<StringParser> reference, final StringParser service) {
                    trackedParsers.remove(service);
                    context.ungetService(reference);
                }

                @Override
                public void modifiedService(final ServiceReference<StringParser> reference, final StringParser service) {
                    // Ignore
                }

                @Override
                public StringParser addingService(final ServiceReference<StringParser> reference) {
                    final StringParser service = context.getService(reference);
                    if (trackedParsers.add(service)) {
                        return service;
                    }
                    context.ungetService(reference);
                    return null;
                }
            };
            parserTracker = new ServiceTracker<StringParser, StringParser>(context, StringParser.class, customizer);
            this.parserTracker = parserTracker;
        }

        final List<StringParser> standardParsers = new ArrayList<StringParser>(3);

        final StringParser standardParsersComposite = new CompositeParser() {

            @Override
            protected Collection<StringParser> getParsers() {
                return standardParsers;
            }
        };

        final StringParser allParsers = new CompositeParser() {

            @Override
            protected Collection<StringParser> getParsers() {
                final int size = trackedParsers.size();
                if (size <= 0) {
                    return Collections.singletonList(standardParsersComposite);
                }

                final List<StringParser> parsers = new ArrayList<StringParser>(size);
                for (final StringParser parser : trackedParsers) {
                    if (parser == this) {
                        // Yapp, it is safe to remove while iterating because it is a ConcurrentList
                        // and will therefore not throw a ConcurrentModificationException
                        trackedParsers.remove(parser);
                    } else {
                        parsers.add(parser);
                    }
                }

                parsers.add(standardParsersComposite);
                return parsers;
            }
        };

        standardParsers.add(new BasicTypesStringParser());
        standardParsers.add(new DateStringParser(allParsers));
        standardParsers.add(new TimeSpanParser());

        final Hashtable<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(Constants.SERVICE_RANKING, Integer.valueOf(100));

        parserTracker.open();

        parserRegistration = context.registerService(StringParser.class, allParsers, properties);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        try {
            final DynamicClassLoaderActivator dynamicClassLoaderActivator = this.dynamicClassLoaderActivator;
            if (null != dynamicClassLoaderActivator) {
                dynamicClassLoaderActivator.stop(context);
                this.dynamicClassLoaderActivator = null;
            }
            final List<ServiceTracker<?, ?>> trackers = this.trackers;
            if (null != trackers) {
                while (!trackers.isEmpty()) {
                    trackers.remove(0).close();
                }
                this.trackers = null;
            }
            ServiceHolderInit.getInstance().stop();
            final Initialization initialization = this.initialization;
            if (null != initialization) {
                initialization.stop();
                this.initialization = null;
            }
            shutdownStringParsers();
            LOG.debug("Global bundle successfully stopped");
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private void shutdownStringParsers() {
        final ServiceRegistration<StringParser> parserRegistration = this.parserRegistration;
        if (null != parserRegistration) {
            parserRegistration.unregister();
            this.parserRegistration = null;
        }
        final ServiceTracker<StringParser,StringParser> parserTracker = this.parserTracker;
        if (null != parserTracker) {
            parserTracker.close();
            this.parserTracker = null;
        }
    }
}
