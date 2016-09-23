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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns.transport.apn.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.apn.ApnOptions;
import com.openexchange.pns.transport.apn.ApnOptionsProvider;
import com.openexchange.pns.transport.apn.internal.ApnPushNotificationTransport;
import com.openexchange.pns.transport.apn.internal.DefaultApnOptionsProvider;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.strings.TimeSpanParser;


/**
 * {@link ApnPushNotificationTransportActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ApnPushNotificationTransportActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnPushNotificationTransportActivator.class);

    private static final String CONFIGFILE_APNS_OPTIONS = "pns-apns-options.yml";

    private ServiceRegistration<ApnOptionsProvider> optionsProviderRegistration;
    private ApnPushNotificationTransport apnTransport;
    private ScheduledTimerTask feedbackQueryTask;

    /**
     * Initializes a new {@link ApnPushNotificationTransportActivator}.
     */
    public ApnPushNotificationTransportActivator() {
        super();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            reinit(configService);
        } catch (Exception e) {
            LOG.error("Failed to re-initialize APNS transport", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder()
            .configFileNames(CONFIGFILE_APNS_OPTIONS)
            .propertiesOfInterest("com.openexchange.pns.transport.apn.ios.enabled", "com.openexchange.pns.transport.apn.ios.feedbackQueryInterval")
            .build();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, PushSubscriptionRegistry.class, PushMessageGeneratorRegistry.class, ConfigViewFactory.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        reinit(getService(ConfigurationService.class));

        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                ApnPushNotificationTransport.invalidateEnabledCache();
            }

            @Override
            public Interests getInterests() {
                return null;
            }
        });
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        ApnPushNotificationTransport apnTransport = this.apnTransport;
        if (null != apnTransport) {
            apnTransport.close();
            this.apnTransport = null;
        }
        ServiceRegistration<ApnOptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
        if (null != optionsProviderRegistration) {
            optionsProviderRegistration.unregister();
            this.optionsProviderRegistration = null;
        }
        ScheduledTimerTask feedbackQueryTask = this.feedbackQueryTask;
        if (null != feedbackQueryTask) {
            feedbackQueryTask.cancel();
            this.feedbackQueryTask = null;
        }
        super.stopBundle();
    }

    private synchronized void reinit(ConfigurationService configService) throws Exception {
        ApnPushNotificationTransport apnTransport = this.apnTransport;
        if (null != apnTransport) {
            apnTransport.close();
            this.apnTransport = null;
        }

        ServiceRegistration<ApnOptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
        if (null != optionsProviderRegistration) {
            optionsProviderRegistration.unregister();
            this.optionsProviderRegistration = null;
        }

        ScheduledTimerTask feedbackQueryTask = this.feedbackQueryTask;
        if (null != feedbackQueryTask) {
            feedbackQueryTask.cancel();
            this.feedbackQueryTask = null;
        }

        Object yaml = configService.getYaml(CONFIGFILE_APNS_OPTIONS);
        if (null != yaml && Map.class.isInstance(yaml)) {
            Map<String, Object> map = (Map<String, Object>) yaml;
            if (!map.isEmpty()) {
                Map<String, ApnOptions> options = parseApnOptions(map);
                if (null != options && !options.isEmpty()) {
                    Dictionary<String, Object> dictionary = new Hashtable<String, Object>(1);
                    dictionary.put(Constants.SERVICE_RANKING, Integer.valueOf(785));
                    optionsProviderRegistration = context.registerService(ApnOptionsProvider.class, new DefaultApnOptionsProvider(options), dictionary);
                }
            }
        }

        apnTransport = new ApnPushNotificationTransport(getService(PushSubscriptionRegistry.class), getService(PushMessageGeneratorRegistry.class), getService(ConfigViewFactory.class), context);
        apnTransport.open();
        this.apnTransport = apnTransport;

        String feedbackQueryInterval = configService.getProperty("com.openexchange.pns.transport.apn.ios.feedbackQueryInterval", "3600000");
        setupFeedbackQueries(apnTransport, feedbackQueryInterval);
    }

    private Map<String, ApnOptions> parseApnOptions(Map<String, Object> yaml) throws Exception {
        Map<String, ApnOptions> options = new LinkedHashMap<String, ApnOptions>(yaml.size());
        for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            String client = entry.getKey();

            // Check for duplicate
            if (options.containsKey(client)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Duplicate APNS options specified for client: " + client);
            }

            // Check values map
            if (false == Map.class.isInstance(entry.getValue())) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Invalid APNS options configuration specified for client: " + client);
            }

            // Parse values map
            Map<String, Object> values = (Map<String, Object>) entry.getValue();

            // Enabled?
            Boolean enabled = getBooleanOption("enabled", Boolean.TRUE, values);
            if (enabled.booleanValue()) {
                // Keystore name
                String keystoreName = getStringOption("keystore", values);
                if (null == keystoreName) {
                    LOG.info("Missing \"keystore\" APNS option for client {}. Ignoring that client's configuration.", client);
                }

                // Proceed if enabled for associated client
                if (Strings.isNotEmpty(keystoreName)) {
                    String password = getStringOption("password", values);
                    if (null == password) {
                        LOG.info("Missing \"password\" APNS option for client {}. Ignoring that client's configuration.", client);
                    } else {
                        Boolean production = getBooleanOption("production", Boolean.TRUE, values);
                        ApnOptions apnOptions = createOptions(keystoreName, password, production.booleanValue());
                        options.put(client, apnOptions);
                        LOG.info("Parsed APNS options for client {}.", client);
                    }
                }
            } else {
                LOG.info("APNS options for client {} is disabled.", client);
            }
        }
        return options;
    }

    private Boolean getBooleanOption(String name, Boolean def, Map<String, Object> values) {
        Object object = values.get(name);
        if (object instanceof Boolean) {
            return (Boolean) object;
        }
        return null == object ? def : Boolean.valueOf(object.toString());
    }

    private String getStringOption(String name, Map<String, Object> values) {
        Object object = values.get(name);
        if (null == object) {
            return null;
        }
        String str = object.toString();
        return Strings.isEmpty(str) ? null : str.trim();
    }

    private void setupFeedbackQueries(ApnPushNotificationTransport apnTransport, String feedbackQueryInterval) {
        if (Strings.isNotEmpty(feedbackQueryInterval)) {
            long interval = TimeSpanParser.parseTimespan(feedbackQueryInterval.trim()).longValue();
            if (60 * 1000 > interval) {
                LOG.warn("Ignoring too small value '{}' for APNS feedback query interval.", feedbackQueryInterval);
                return;
            }

            TimerService timerService = getService(TimerService.class);
            long shiftMillis = TimeUnit.MILLISECONDS.convert((long)(Math.random() * interval), TimeUnit.MILLISECONDS);
            long initialDelay = interval + shiftMillis;
            feedbackQueryTask = timerService.scheduleWithFixedDelay(createQueryFeedbackTask(apnTransport), initialDelay, interval);
            LOG.info("Starting APNS feedback query interval in {}ms (checking every {}ms)", Long.valueOf(initialDelay), Long.valueOf(interval));
        }
    }

    private Runnable createQueryFeedbackTask(final ApnPushNotificationTransport apnTransport) {
        return new Runnable() {

            @Override
            public void run() {
                apnTransport.queryFeedbackService();
            }
        };
    }

    private ApnOptions createOptions(String resourceName, String password, boolean production) throws Exception{
        KeyStore keyStore = null;
        InputStream resourceStream = null;
        try {
            resourceStream = new FileInputStream(new File(resourceName));
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(resourceStream, password.toCharArray());
            return new ApnOptions(keyStore, password, production);
        } finally {
            Streams.close(resourceStream);
        }
    }

}
