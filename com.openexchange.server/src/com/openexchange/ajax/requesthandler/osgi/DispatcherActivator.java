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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.osgi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.Multiple;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.DefaultConverter;
import com.openexchange.ajax.requesthandler.DefaultDispatcher;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.converters.BasicTypeAPIResultConverter;
import com.openexchange.ajax.requesthandler.converters.DebugConverter;
import com.openexchange.ajax.requesthandler.converters.preview.HTMLPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.TextPreviewResultConverter;
import com.openexchange.ajax.requesthandler.customizer.ConversionCustomizer;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.JSONResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.StringResponseRenderer;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.server.osgiservice.SimpleRegistryListener;
import com.openexchange.tools.images.ImageScalingService;
import com.openexchange.tools.service.SessionServletRegistration;


/**
 * {@link DispatcherActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DispatcherActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();
        /*
         * Specify default converters
         */
        final DefaultConverter defaultConverter = new DefaultConverter();
        defaultConverter.addConverter(new DebugConverter());
        /*
         * Add basic converters
         */
        for(final ResultConverter converter : BasicTypeAPIResultConverter.CONVERTERS) {
        	defaultConverter.addConverter(converter);
        }
        /*
         * Add preview converters
         */
        defaultConverter.addConverter(new HTMLPreviewResultConverter());
        defaultConverter.addConverter(new TextPreviewResultConverter());

        track(ResultConverter.class, new SimpleRegistryListener<ResultConverter>() {

            @Override
            public void added(final ServiceReference<ResultConverter> ref, final ResultConverter thing) {
                defaultConverter.addConverter(thing);
            }

            @Override
            public void removed(final ServiceReference<ResultConverter> ref, final ResultConverter thing) {
                defaultConverter.removeConverter(thing);
            }

        });

        dispatcher.addCustomizer(new ConversionCustomizer(defaultConverter));

        final DispatcherServlet servlet = new DispatcherServlet(dispatcher, "/ajax/");
        Multiple.setDispatcher(dispatcher);

        DispatcherServlet.registerRenderer(new JSONResponseRenderer());
        final FileResponseRenderer fileRenderer = new FileResponseRenderer();
        DispatcherServlet.registerRenderer(fileRenderer);
        DispatcherServlet.registerRenderer(new StringResponseRenderer());

        track(ResponseRenderer.class, new SimpleRegistryListener<ResponseRenderer>() {

            @Override
            public void added(final ServiceReference<ResponseRenderer> ref, final ResponseRenderer thing) {
                DispatcherServlet.registerRenderer(thing);
            }

            @Override
            public void removed(final ServiceReference<ResponseRenderer> ref, final ResponseRenderer thing) {
                DispatcherServlet.unregisterRenderer(thing);
            }

        });


        track(AJAXActionServiceFactory.class, new Registerer(context, dispatcher, servlet));

        track(ImageScalingService.class, new SimpleRegistryListener<ImageScalingService>() {

            @Override
            public void added(final ServiceReference<ImageScalingService> ref, final ImageScalingService thing) {
                fileRenderer.setScaler(thing);
            }

            @Override
            public void removed(final ServiceReference<ImageScalingService> ref, final ImageScalingService thing) {
                fileRenderer.setScaler(null);
            }

        });

        openTrackers();
    }

    private final class Registerer implements SimpleRegistryListener<AJAXActionServiceFactory> {

        private final BundleContext rcontext;
        private final DefaultDispatcher dispatcher;
        private final DispatcherServlet servlet;

        private final Set<String> registrationGuardian = new HashSet<String>();

        private final Map<String, SessionServletRegistration> registrations = new HashMap<String, SessionServletRegistration>();
        private final Map<String, ServiceRegistration<AJAXActionServiceFactory>> serviceRegistrations = new HashMap<String, ServiceRegistration<AJAXActionServiceFactory>>();

        public Registerer(final BundleContext context, final DefaultDispatcher dispatcher, final DispatcherServlet servlet) {
            super();
            this.rcontext = context;
            this.dispatcher = dispatcher;
            this.servlet = servlet;
        }

        @Override
        public void added(final ServiceReference<AJAXActionServiceFactory> ref, final AJAXActionServiceFactory thing) {
            final String module = (String) ref.getProperty("module");
            dispatcher.register(module, thing);

            if (registrationGuardian.contains(module)) {
            	return;
            }
            registrationGuardian.add(module);

            final SessionServletRegistration registration = new SessionServletRegistration(rcontext, servlet, "/ajax/"+module);
            registrations.put(module, registration);
            rememberTracker(registration);
        }

        @Override
        public void removed(final ServiceReference<AJAXActionServiceFactory> ref, final AJAXActionServiceFactory thing) {
            final String module = (String) ref.getProperty("module");
            dispatcher.remove(module, thing);

            final SessionServletRegistration tracker = registrations.remove(module);
            if (null != tracker) {
                tracker.remove();
                forgetTracker(tracker);
            }
            final ServiceRegistration<AJAXActionServiceFactory> serviceRegistration = serviceRegistrations.remove(module);
            if (null != serviceRegistration) {
                serviceRegistration.unregister();
            }
        }

    }

    @Override
    public void forgetTracker(final ServiceTracker<?, ?> tracker) {
        super.forgetTracker(tracker);
    }

    @Override
    public void rememberTracker(final ServiceTracker<?, ?> tracker) {
        super.rememberTracker(tracker);
    }

}

