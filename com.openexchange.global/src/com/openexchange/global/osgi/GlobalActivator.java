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

package com.openexchange.global.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.interception.OXExceptionInterceptor;
import com.openexchange.exception.interception.internal.OXExceptionInterceptorRegistration;
import com.openexchange.exception.interception.internal.OXExceptionInterceptorTracker;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.i18n.internal.I18nServiceRegistryImpl;
import com.openexchange.java.ConcurrentList;
import com.openexchange.server.ServiceHolderInit;
import com.openexchange.session.inspector.SessionInspectorChain;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.inspector.internal.ServiceSet;
import com.openexchange.session.inspector.internal.SessionInspectorChainImpl;
import com.openexchange.startup.CloseableControlService;
import com.openexchange.startup.SignalStartedService;
import com.openexchange.startup.ThreadControlService;
import com.openexchange.startup.impl.ThreadControl;
import com.openexchange.startup.impl.ThreadLocalCloseableControl;
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

    private ServiceTracker<StringParser,StringParser> parserTracker;
    private ServiceRegistration<StringParser> parserRegistration;
    private ServiceRegistration<SessionInspectorChain> inspectorChainRegistration;
    private ServiceRegistration<ThreadControlService> threadControlRegistration;
    private ServiceRegistration<CloseableControlService> closeableControlRegistration;
    private ServiceRegistration<I18nServiceRegistry> i18nRegistryRegistration;
    private List<ServiceTracker<?,?>> trackers;


    /**
     * Initializes a new {@link GlobalActivator}
     */
    public GlobalActivator() {
        super();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalActivator.class);
        try {
            ServiceHolderInit.getInstance().start();
            initStringParsers(context);

            final List<ServiceTracker<?, ?>> trackers = new ArrayList<ServiceTracker<?, ?>>(4);
            this.trackers = trackers;

            OXExceptionInterceptorRegistration.initInstance();
            trackers.add(new ServiceTracker<OXExceptionInterceptor, OXExceptionInterceptor>(context, OXExceptionInterceptor.class, new OXExceptionInterceptorTracker(context)));

            // Session inspector chain
            {
                ServiceSet<SessionInspectorService> serviceSet = new ServiceSet<SessionInspectorService>(context);
                trackers.add(new ServiceTracker<SessionInspectorService, SessionInspectorService>(context, SessionInspectorService.class, serviceSet));
                SessionInspectorChainImpl chainImpl = new SessionInspectorChainImpl(serviceSet);
                inspectorChainRegistration = context.registerService(SessionInspectorChain.class, chainImpl, null);
            }

            trackers.add(new ServiceTracker<>(context, I18nService.class, new I18nServiceTracker(I18nServiceRegistryImpl.getInstance(), context)));
            trackers.add(new ServiceTracker<>(context, SignalStartedService.class, new SignalStartedTracker(context)));

            for (final ServiceTracker<?,?> tracker : trackers) {
                tracker.open();
            }

            i18nRegistryRegistration = context.registerService(I18nServiceRegistry.class, I18nServiceRegistryImpl.getInstance(), null);

            threadControlRegistration = context.registerService(ThreadControlService.class, ThreadControl.getInstance(), null);
            closeableControlRegistration = context.registerService(CloseableControlService.class, ThreadLocalCloseableControl.getInstance(), null);

            logger.info("Global bundle successfully started");
        } catch (Exception e) {
            logger.error("", e);
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
    public synchronized void stop(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalActivator.class);
        try {
            final List<ServiceTracker<?, ?>> trackers = this.trackers;
            if (null != trackers) {
                this.trackers = null;
                while (!trackers.isEmpty()) {
                    trackers.remove(0).close();
                }
            }
            ServiceHolderInit.getInstance().stop();

            shutdownStringParsers();

            ServiceRegistration<CloseableControlService> closeableControlRegistration = this.closeableControlRegistration;
            if (null != closeableControlRegistration) {
                this.closeableControlRegistration = null;
                closeableControlRegistration.unregister();
            }

            ServiceRegistration<ThreadControlService> threadControlRegistration = this.threadControlRegistration;
            if (null != threadControlRegistration) {
                this.threadControlRegistration = null;
                threadControlRegistration.unregister();
            }

            ServiceRegistration<SessionInspectorChain> inspectorChainRegistration = this.inspectorChainRegistration;
            if (null != inspectorChainRegistration) {
                this.inspectorChainRegistration = null;
                inspectorChainRegistration.unregister();
            }

            ServiceRegistration<I18nServiceRegistry> i18nRegistryRegistration = this.i18nRegistryRegistration;
            if (null != i18nRegistryRegistration) {
                this.i18nRegistryRegistration = null;
                i18nRegistryRegistration.unregister();
            }

            OXExceptionInterceptorRegistration.dropInstance();

            logger.info("Global bundle successfully stopped");
        } catch (Exception e) {
            logger.error("", e);
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
