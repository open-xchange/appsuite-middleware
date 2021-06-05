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

package com.openexchange.i18n.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServiceHolder;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.i18n.impl.CompositeI18nTools;
import com.openexchange.i18n.impl.I18nImpl;
import com.openexchange.i18n.impl.I18nTranslatorFactory;
import com.openexchange.i18n.impl.POTranslationsDiscoverer;
import com.openexchange.i18n.impl.ResourceBundleDiscoverer;
import com.openexchange.i18n.impl.TranslationsI18N;
import com.openexchange.i18n.parsing.Translations;
import com.openexchange.osgi.BundleServiceTracker;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceHolderListener;

/**
 * {@link I18nActivator}
 *
 * @since 7.6.1
 */
public class I18nActivator extends HousekeepingActivator {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(I18nActivator.class);

    /**
     * {@link I18nServiceHolderListener} - Properly registers all I18n services defined through property <code>"i18n.language.path"</code>
     * when configuration service is available
     */
    private static final class I18nServiceHolderListener implements ServiceHolderListener<ConfigurationService> {

        private final BundleContext context;

        private final ConfigurationServiceHolder csh;

        private ServiceRegistration<?>[] serviceRegistrations;

        public I18nServiceHolderListener(final BundleContext context, final ConfigurationServiceHolder csh) {
            super();
            this.context = context;
            this.csh = csh;
        }

        @Override
        public void onServiceAvailable(final ConfigurationService service) throws Exception {
            unregisterAll();
            final ConfigurationService config = csh.getService();
            if (null == config) {
                throw ServiceExceptionCode.absentService(ConfigurationService.class);
            }
            try {
                serviceRegistrations = initI18nServices(context, config);
            } finally {
                csh.ungetService(config);
            }
        }

        @Override
        public void onServiceRelease() throws Exception {
            // Nope
        }

        /**
         * Unregisters all registered I18n services and resets them to <code>null</code>.
         */
        public void unregisterAll() {
            if (null == serviceRegistrations) {
                return;
            }
            for (int i = 0; i < serviceRegistrations.length; i++) {
                serviceRegistrations[i].unregister();
                serviceRegistrations[i] = null;
            }
            serviceRegistrations = null;
            LOG.info("All I18n services unregistered");
        }
    }

    /**
     * Reads in all I18n services configured through property <code>"i18n.language.path"</code>, registers them, and returns corresponding
     * service registrations for future unregistration.
     *
     * @param context The current valid bundle context
     * @return The corresponding service registrations of registered I18n services
     * @throws FileNotFoundException If directory referenced by <code>"i18n.language.path"</code> does not exist
     */
    protected static ServiceRegistration<?>[] initI18nServices(final BundleContext context, final ConfigurationService config) throws FileNotFoundException {
        final String value = config.getProperty("i18n.language.path");
        if (null == value) {
            final FileNotFoundException e = new FileNotFoundException("Configuration property 'i18n.language.path' is not defined.");
            LOG.error("", e);
            throw e;
        }
        final File dir = new File(value);

        final List<ResourceBundle> resourceBundles = new ResourceBundleDiscoverer(dir).getResourceBundles();
        final List<Translations> translations = new POTranslationsDiscoverer(dir).getTranslations();
        final List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<ServiceRegistration<?>>();

        final Map<Locale, List<I18nService>> locales = new HashMap<Locale, List<I18nService>>();

        for (final Translations tr : translations) {
            List<I18nService> list = locales.get(tr.getLocale());
            if (list == null) {
                list = new ArrayList<I18nService>();
                locales.put(tr.getLocale(), list);
            }

            list.add(new TranslationsI18N(tr));
        }

        for (final ResourceBundle rc : resourceBundles) {

            List<I18nService> list = locales.get(rc.getLocale());
            if (list == null) {
                list = new ArrayList<I18nService>();
                locales.put(rc.getLocale(), list);
            }

            list.add(new I18nImpl(rc));

            final Properties prop = new Properties();
            prop.put(I18nService.LANGUAGE, rc.getLocale());

        }

        for (final Map.Entry<Locale, List<I18nService>> localeEntry : locales.entrySet()) {
            final List<I18nService> list = localeEntry.getValue();

            final Dictionary<String, Object> prop = new Hashtable<String, Object>(1);
            prop.put(I18nService.LANGUAGE, localeEntry.getKey());

            final I18nService i18n;
            if (list.size() == 1) {
                i18n = list.get(0);
            } else {
                i18n = new CompositeI18nTools(list);
            }

            serviceRegistrations.add(context.registerService(I18nService.class, i18n, prop));

        }

        LOG.info("All I18n services registered");
        return serviceRegistrations.toArray(new ServiceRegistration[serviceRegistrations.size()]);
    }

    private ConfigurationServiceHolder csh;
    private I18nServiceHolderListener listener;

    /**
     * Initializes a new {@link I18nActivator}.
     */
    public I18nActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        LOG.debug("I18n Starting");



        try {
            ConfigurationServiceHolder csh = ConfigurationServiceHolder.newInstance();
            this.csh = csh;

            track(ConfigurationService.class, new BundleServiceTracker<ConfigurationService>(context, csh, ConfigurationService.class));

            I18nTranslatorFactory translatorFactory = new I18nTranslatorFactory(context);
            rememberTracker(translatorFactory);
            registerService(TranslatorFactory.class, translatorFactory);

            openTrackers();

            I18nServiceHolderListener listener = new I18nServiceHolderListener(context, csh);
            this.listener = listener;
            csh.addServiceHolderListener(listener);

        } catch (Throwable e) {
            throw e instanceof Exception ? (Exception) e : new Exception(e);
        }


        LOG.debug("I18n Started");
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        LOG.debug("Stopping I18n");

        try {
            I18nServiceHolderListener listener = this.listener;
            if (null != listener) {
                ConfigurationServiceHolder csh = this.csh;
                if (null != csh) {
                    csh.removeServiceHolderListenerByName(listener.getClass().getName());
                    csh = null;
                }
                /*
                 * Unregister through listener
                 */
                listener.unregisterAll();
                listener = null;
            }
            /*
             * Stop rest...
             */
            super.stopBundle();
        } catch (Throwable e) {
            LOG.error("I18nActivator: stop: ", e);
            throw e instanceof Exception ? (Exception) e : new Exception(e);
        }
        LOG.debug("I18n stopped");
    }

}
