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

package com.openexchange.ajax.requesthandler.osgi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.Multiple;
import com.openexchange.ajax.osgi.AbstractSessionServletActivator;
import com.openexchange.ajax.requesthandler.AJAXActionAnnotationProcessor;
import com.openexchange.ajax.requesthandler.AJAXActionCustomizer;
import com.openexchange.ajax.requesthandler.AJAXActionCustomizerFactory;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXResultDecorator;
import com.openexchange.ajax.requesthandler.AJAXResultDecoratorRegistry;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.DefaultConverter;
import com.openexchange.ajax.requesthandler.DefaultDispatcher;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.DispatcherListener;
import com.openexchange.ajax.requesthandler.DispatcherListenerRegistry;
import com.openexchange.ajax.requesthandler.DispatcherNotesProcessor;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.cache.PreviewFilestoreLocationUpdater;
import com.openexchange.ajax.requesthandler.converters.BasicTypeAPIResultConverter;
import com.openexchange.ajax.requesthandler.converters.BasicTypeJsonConverter;
import com.openexchange.ajax.requesthandler.converters.Bean2JSONConverter;
import com.openexchange.ajax.requesthandler.converters.DebugConverter;
import com.openexchange.ajax.requesthandler.converters.Native2JSONConverter;
import com.openexchange.ajax.requesthandler.converters.NativeConverter;
import com.openexchange.ajax.requesthandler.converters.SecureContentResultConverter;
import com.openexchange.ajax.requesthandler.converters.cover.CoverExtractor;
import com.openexchange.ajax.requesthandler.converters.cover.CoverExtractorRegistry;
import com.openexchange.ajax.requesthandler.converters.cover.CoverResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.DownloadPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.FilteredHTMLPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.HTMLPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.MailFilteredHTMLPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.MailTextPreviewResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.PreviewImageResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.PreviewThumbResultConverter;
import com.openexchange.ajax.requesthandler.converters.preview.TextPreviewResultConverter;
import com.openexchange.ajax.requesthandler.customizer.ConversionCustomizer;
import com.openexchange.ajax.requesthandler.oauth.OAuthAnnotationProcessor;
import com.openexchange.ajax.requesthandler.oauth.OAuthDispatcherServlet;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.PreviewResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.SecureContentAPIResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.StringResponseRenderer;
import com.openexchange.ajax.response.IncludeStackTraceService;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.continuation.ContinuationRegistryService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.filestore.FileLocationHandler;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.mail.mime.utils.ImageMatcher;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.oauth.provider.resourceserver.OAuthResourceService;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link DispatcherActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DispatcherActivator extends AbstractSessionServletActivator {

    static final Object PRESENT = new Object();

    // ---------------------------------------------------------------------------------------------------

    final ConcurrentMap<String, Object> servlets = new ConcurrentHashMap<>(32, 0.9F, 1);
    String prefix;

    /**
     * Initializes a new {@link DispatcherActivator}.
     */
    public DispatcherActivator() {
        super();
    }

    @Override
    protected synchronized void startBundle() throws Exception {
    	DispatcherPrefixService dispatcherPrefixService = getService(DispatcherPrefixService.class);
        final String prefix = dispatcherPrefixService.getPrefix();
        this.prefix = prefix;
    	Dispatchers.setDispatcherPrefixService(dispatcherPrefixService);
    	Dispatcher.PREFIX.set(prefix);

        OSGiDispatcherListenerRegistry dispatcherListenerRegistry = new OSGiDispatcherListenerRegistry(context);
    	final DefaultDispatcher dispatcher = new DefaultDispatcher(dispatcherListenerRegistry);

    	/*
         * Specify default converters
         */
        final DefaultConverter defaultConverter = new DefaultConverter();
        registerService(Converter.class, defaultConverter);

        defaultConverter.addConverter(new DebugConverter());
        defaultConverter.addConverter(new NativeConverter());
        defaultConverter.addConverter(new Native2JSONConverter());
        /*
         * Add basic converters
         */
        for (final ResultConverter converter : BasicTypeAPIResultConverter.CONVERTERS) {
            defaultConverter.addConverter(converter);
        }
        for (final ResultConverter converter : BasicTypeJsonConverter.CONVERTERS) {
            defaultConverter.addConverter(converter);
        }

        defaultConverter.addConverter(new SecureContentResultConverter());
        defaultConverter.addConverter(new Bean2JSONConverter());
        /*
         * Add cover extractor converter
         */
        defaultConverter.addConverter(new CoverResultConverter());
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
            defaultConverter.addConverter(new PreviewThumbResultConverter(getOptionalService(ConfigurationService.class)));

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

        final BundleContext context = this.context;

        {
            final BodyParserRegistry registry = new BodyParserRegistry(context);
            rememberTracker(registry);
            AJAXRequestDataTools.setBodyParserRegistry(registry);
        }

        final OSGiCoverExtractorRegistry coverExtractorRegistry = new OSGiCoverExtractorRegistry(context);
        track(CoverExtractor.class, coverExtractorRegistry);
        registerService(CoverExtractorRegistry.class, coverExtractorRegistry);
        ServerServiceRegistry.getInstance().addService(CoverExtractorRegistry.class, coverExtractorRegistry);
        CoverExtractorRegistry.REGISTRY_REFERENCE.set(coverExtractorRegistry);

        final OSGiAJAXResultDecoratorRegistry decoratorRegistry = new OSGiAJAXResultDecoratorRegistry(context);
        track(AJAXResultDecorator.class, decoratorRegistry);
        registerService(AJAXResultDecoratorRegistry.class, decoratorRegistry);
        ServerServiceRegistry.getInstance().addService(AJAXResultDecoratorRegistry.class, decoratorRegistry);
        DecoratingAJAXActionCustomizer.REGISTRY_REF.set(decoratorRegistry);

        // Keep this order!!!
        dispatcher.addCustomizer(new ConversionCustomizer(defaultConverter));
        dispatcher.addCustomizer(new AJAXActionCustomizerFactory() {

            @Override
            public AJAXActionCustomizer createCustomizer(final AJAXRequestData request, final ServerSession session) {
                return DecoratingAJAXActionCustomizer.getInstance();
            }
        });

        Multiple.setDispatcher(dispatcher);
        DispatcherServlet.setDispatcher(dispatcher);
        APIResponseRenderer apiResponseRenderer = new APIResponseRenderer();
        DispatcherServlet.registerRenderer(apiResponseRenderer);
        DispatcherServlet.registerRenderer(new SecureContentAPIResponseRenderer(apiResponseRenderer));
        final FileResponseRenderer fileRenderer = new FileResponseRenderer();
        DispatcherServlet.registerRenderer(fileRenderer);
        DispatcherServlet.registerRenderer(new StringResponseRenderer());
        DispatcherServlet.registerRenderer(new PreviewResponseRenderer());

        registerService(AJAXActionAnnotationProcessor.class, new DispatcherNotesProcessor());
        registerService(AJAXActionAnnotationProcessor.class, new OAuthAnnotationProcessor());

        final DispatcherServlet dispatcherServlet = new DispatcherServlet(prefix);
        final OAuthDispatcherServlet oAuthDispatcherServlet = new OAuthDispatcherServlet(this, prefix);
        trackService(OAuthResourceService.class);
        trackService(ContextService.class);
        trackService(UserService.class);
        trackService(SessiondService.class);
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

        track(IncludeStackTraceService.class, new SimpleRegistryListener<IncludeStackTraceService>() {

            @Override
            public void added(final ServiceReference<IncludeStackTraceService> ref, final IncludeStackTraceService thing) {
                ResponseWriter.setIncludeStackTraceService(thing);
            }

            @Override
            public void removed(final ServiceReference<IncludeStackTraceService> ref, final IncludeStackTraceService thing) {
                ResponseWriter.setIncludeStackTraceService(null);
            }

        });

        track(AJAXActionServiceFactory.class, new SimpleRegistryListener<AJAXActionServiceFactory>() {

            @Override
            public void added(final ServiceReference<AJAXActionServiceFactory> ref, final AJAXActionServiceFactory service) {
                final String module = (String) ref.getProperty("module");
                dispatcher.register(module, service);
                if (null == servlets.putIfAbsent(module, PRESENT)) {
                    registerSessionServlet(prefix + module, dispatcherServlet);
                    if (service.getClass().isAnnotationPresent(OAuthModule.class)) {
                        registerSessionServlet(prefix + "oauth/modules/" + module, oAuthDispatcherServlet);
                    }
                }
            }

            @Override
            public void removed(final ServiceReference<AJAXActionServiceFactory> ref, final AJAXActionServiceFactory service) {
                final String module = (String) ref.getProperty("module");
                if (null != servlets.remove(module)) {
                    unregisterServlet(prefix + module);
                    if (service.getClass().isAnnotationPresent(OAuthModule.class)) {
                        unregisterServlet(prefix + "oauth/modules/" + module);
                    }
                }
                dispatcher.remove(module, service);
            }

        });

        track(AJAXActionAnnotationProcessor.class, new SimpleRegistryListener<AJAXActionAnnotationProcessor>() {

            @Override
            public void added(ServiceReference<AJAXActionAnnotationProcessor> ref, AJAXActionAnnotationProcessor service) {
                dispatcher.addAnnotationProcessor(service);
            }

            @Override
            public void removed(ServiceReference<AJAXActionAnnotationProcessor> ref, AJAXActionAnnotationProcessor service) {
                dispatcher.removeAnnotationProcessor(service);
            }

        });

        track(ImageTransformationService.class, new SimpleRegistryListener<ImageTransformationService>() {

            @Override
            public void added(final ServiceReference<ImageTransformationService> ref, final ImageTransformationService thing) {
                fileRenderer.setScaler(thing);
            }

            @Override
            public void removed(final ServiceReference<ImageTransformationService> ref, final ImageTransformationService thing) {
                fileRenderer.setScaler(null);
            }

        });

        track(ContinuationRegistryService.class, new SimpleRegistryListener<ContinuationRegistryService>() {

            @Override
            public void added(final ServiceReference<ContinuationRegistryService> ref, final ContinuationRegistryService thing) {
                ServerServiceRegistry.getInstance().addService(ContinuationRegistryService.class, thing);
            }

            @Override
            public void removed(final ServiceReference<ContinuationRegistryService> ref, final ContinuationRegistryService thing) {
                ServerServiceRegistry.getInstance().removeService(ContinuationRegistryService.class);
            }

        });

        track(AJAXActionCustomizerFactory.class, new SimpleRegistryListener<AJAXActionCustomizerFactory>() {

			@Override
			public void added(ServiceReference<AJAXActionCustomizerFactory> ref, AJAXActionCustomizerFactory service) {
				dispatcher.addCustomizer(service);
			}

			@Override
			public void removed(ServiceReference<AJAXActionCustomizerFactory> ref,
					AJAXActionCustomizerFactory service) {
				dispatcher.removeCustomizer(service);

			}
		});

        track(DispatcherListener.class, dispatcherListenerRegistry);

        track(UserAwareSSLConfigurationService.class, new SimpleRegistryListener<UserAwareSSLConfigurationService>() {

            @Override
            public void added(ServiceReference<UserAwareSSLConfigurationService> reference, UserAwareSSLConfigurationService service) {
                ServerServiceRegistry.getInstance().addService(UserAwareSSLConfigurationService.class, service);
            }

            @Override
            public void removed(ServiceReference<UserAwareSSLConfigurationService> reference, UserAwareSSLConfigurationService service) {
                ServerServiceRegistry.getInstance().removeService(UserAwareSSLConfigurationService.class);
            }

        });

        openTrackers();

        registerService(Dispatcher.class, dispatcher);
        registerService(DispatcherListenerRegistry.class, dispatcherListenerRegistry);

        /*
         * Register preview filestore updater for move context filestore
         */
        registerService(FileLocationHandler.class, new PreviewFilestoreLocationUpdater());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();
        DispatcherServlet.clearRenderer();
        DispatcherServlet.setDispatcher(null);
        ServerServiceRegistry.getInstance().removeService(AJAXResultDecoratorRegistry.class);
        DecoratingAJAXActionCustomizer.REGISTRY_REF.set(null);
        CoverExtractorRegistry.REGISTRY_REFERENCE.set(null);
        unregisterServlet(this.prefix);
        this.prefix = null;
        ServerServiceRegistry.getInstance().removeService(DispatcherPrefixService.class);
        ImageMatcher.setPrefixService(null);
        Multiple.setDispatcher(null);
        AJAXRequestDataTools.setBodyParserRegistry(null);
        Dispatchers.setDispatcherPrefixService(null);
        Dispatcher.PREFIX.set(DispatcherPrefixService.DEFAULT_PREFIX);
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
        return new Class<?>[] { DispatcherPrefixService.class };
    }

    @Override
    public void registerSessionServlet(String alias, HttpServlet servlet, String... configKeys) {
        super.registerSessionServlet(alias, servlet, configKeys);
    }

    @Override
    public void unregisterServlet(String alias) {
        super.unregisterServlet(alias);
    }

}

