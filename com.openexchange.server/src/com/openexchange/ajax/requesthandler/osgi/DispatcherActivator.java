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

package com.openexchange.ajax.requesthandler.osgi;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.Multiple;
import com.openexchange.ajax.osgi.AbstractSessionServletActivator;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.DefaultConverter;
import com.openexchange.ajax.requesthandler.DefaultDispatcher;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.converters.BasicTypeAPIResultConverter;
import com.openexchange.ajax.requesthandler.converters.DebugConverter;
import com.openexchange.ajax.requesthandler.converters.preview.DownloadPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.FilteredHTMLPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.HTMLPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.MailFilteredHTMLPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.MailTextPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.PreviewImageResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.TextPreviewResultConverter;
import com.openexchange.ajax.requesthandler.customizer.ConversionCustomizer;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.PreviewResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.StringResponseRenderer;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.tools.images.ImageScalingService;


/**
 * {@link DispatcherActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DispatcherActivator extends AbstractSessionServletActivator {

    private final Set<String> servlets = new HashSet<String>();

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
        {
            final TextPreviewResultConverter textPreviewResultConverter = new TextPreviewResultConverter();
            final FilteredHTMLPreviewResultConverter filteredHTMLPreviewResultConverter = new FilteredHTMLPreviewResultConverter();
            /*
             * File converters
             */
            defaultConverter.addConverter(new HTMLPreviewResultConverter());
            defaultConverter.addConverter(textPreviewResultConverter);
            defaultConverter.addConverter(filteredHTMLPreviewResultConverter);
            defaultConverter.addConverter(new DownloadPreviewResultConverter());
            defaultConverter.addConverter(new PreviewImageResultConverter());
            /*-
             * TODO: Mail converters
             *
             * Might throw: java.lang.IllegalArgumentException: Can't find path from mail to apiResponse
             */
            defaultConverter.addConverter(new MailTextPreviewResultConverter(textPreviewResultConverter));
            defaultConverter.addConverter(new MailFilteredHTMLPreviewResultConverter(filteredHTMLPreviewResultConverter));
        }

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

        final DispatcherServlet servlet = new DispatcherServlet();
        DispatcherServlet.setDispatcher(dispatcher);
        DispatcherServlet.setPrefix("/ajax/");
        Multiple.setDispatcher(dispatcher);

        DispatcherServlet.registerRenderer(new APIResponseRenderer());
        final FileResponseRenderer fileRenderer = new FileResponseRenderer();
        DispatcherServlet.registerRenderer(fileRenderer);
        DispatcherServlet.registerRenderer(new StringResponseRenderer());
        DispatcherServlet.registerRenderer(new PreviewResponseRenderer());

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

        track(AJAXActionServiceFactory.class, new SimpleRegistryListener<AJAXActionServiceFactory>() {

            @Override
            public void added(ServiceReference<AJAXActionServiceFactory> ref, AJAXActionServiceFactory service) {
                String module = (String) ref.getProperty("module");
                dispatcher.register(module, service);
                if (!servlets.contains(module)) {
                    servlets.add(module);
                    registerSessionServlet("/ajax/" + module, servlet);
                }
            }

            @Override
            public void removed(ServiceReference<AJAXActionServiceFactory> ref, AJAXActionServiceFactory service) {
                String module = (String) ref.getProperty("module");
                if (servlets.contains(module)) {
                    unregisterServlet("/ajax/" + module);
                    servlets.remove(module);
                }
            }
            
        });

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

        registerService(Dispatcher.class, dispatcher);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        DispatcherServlet.clearRenderer();
        DispatcherServlet.setDispatcher(null);
        DispatcherServlet.setPrefix(null);
        unregisterServlet("/ajax");
        Multiple.setDispatcher(null);
    }

    @Override
    public void forgetTracker(final ServiceTracker<?, ?> tracker) {
        super.forgetTracker(tracker);
    }

    @Override
    public void rememberTracker(final ServiceTracker<?, ?> tracker) {
        super.rememberTracker(tracker);
    }

    @Override
    protected Class<?>[] getAdditionalNeededServices() {
        return EMPTY_CLASSES;
    }

}

