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
import com.openexchange.exception.internal.I18nCustomizer;
import com.openexchange.i18n.I18nService;
import com.openexchange.java.ConcurrentList;
import com.openexchange.passwordmechs.PasswordMech;
import com.openexchange.passwordmechs.PasswordMechFactory;
import com.openexchange.passwordmechs.PasswordMechFactoryImpl;
import com.openexchange.server.Initialization;
import com.openexchange.server.ServiceHolderInit;
import com.openexchange.session.inspector.SessionInspectorChain;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.inspector.internal.ServiceSet;
import com.openexchange.session.inspector.internal.SessionInspectorChainImpl;
import com.openexchange.startup.CloseableControlService;
import com.openexchange.startup.ThreadControlService;
import com.openexchange.startup.impl.ThreadLocalCloseableControl;
import com.openexchange.startup.impl.ThreadControl;
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

    private volatile Initialization initialization;
    private volatile ServiceTracker<StringParser,StringParser> parserTracker;
    private volatile ServiceRegistration<StringParser> parserRegistration;
    private volatile ServiceRegistration<SessionInspectorChain> inspectorChainRegistration;
    private volatile ServiceRegistration<ThreadControlService> threadControlRegistration;
    private volatile ServiceRegistration<CloseableControlService> closeableControlRegistration;
    private volatile List<ServiceTracker<?,?>> trackers;


    /**
     * Initializes a new {@link GlobalActivator}
     */
    public GlobalActivator() {
        super();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalActivator.class);
        try {
            final Initialization initialization = new ServerInitialization();
            this.initialization = initialization;
            initialization.start();
            ServiceHolderInit.getInstance().start();
            initStringParsers(context);

            final List<ServiceTracker<?, ?>> trackers = new ArrayList<ServiceTracker<?, ?>>(4);
            this.trackers = trackers;
            trackers.add(new ServiceTracker<I18nService, I18nService>(context, I18nService.class, new I18nCustomizer(context)));

            OXExceptionInterceptorRegistration.initInstance();
            trackers.add(new ServiceTracker<OXExceptionInterceptor, OXExceptionInterceptor>(context, OXExceptionInterceptor.class, new OXExceptionInterceptorTracker(context)));

            // Session inspector chain
            {
                ServiceSet<SessionInspectorService> serviceSet = new ServiceSet<SessionInspectorService>(context);
                trackers.add(new ServiceTracker<SessionInspectorService, SessionInspectorService>(context, SessionInspectorService.class, serviceSet));
                SessionInspectorChainImpl chainImpl = new SessionInspectorChainImpl(serviceSet);
                inspectorChainRegistration = context.registerService(SessionInspectorChain.class, chainImpl, null);
            }

            for (final ServiceTracker<?,?> tracker : trackers) {
                tracker.open();
            }
            PasswordMechFactoryImpl passwordMechFactoryImpl = new PasswordMechFactoryImpl();
            passwordMechFactoryImpl.register(PasswordMech.BCRYPT, PasswordMech.CRYPT, PasswordMech.SHA);
            context.registerService(PasswordMechFactory.class, passwordMechFactoryImpl, null);

            threadControlRegistration = context.registerService(ThreadControlService.class, ThreadControl.getInstance(), null);
            closeableControlRegistration = context.registerService(CloseableControlService.class, ThreadLocalCloseableControl.getInstance(), null);

            logger.info("Global bundle successfully started");
        } catch (final Exception e) {
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
    public void stop(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalActivator.class);
        try {
            final List<ServiceTracker<?, ?>> trackers = this.trackers;
            if (null != trackers) {
                while (!trackers.isEmpty()) {
                    trackers.remove(0).close();
                }
                this.trackers = null;
            }
            ServiceHolderInit.getInstance().stop();

            Initialization initialization = this.initialization;
            if (null != initialization) {
                this.initialization = null;
                initialization.stop();
            }
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

            OXExceptionInterceptorRegistration.dropInstance();

            logger.info("Global bundle successfully stopped");
        } catch (final Exception e) {
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
