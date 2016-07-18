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
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.apn.ApnOptions;
import com.openexchange.pns.transport.apn.ApnPushNotificationTransport;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.strings.TimeSpanParser;


/**
 * {@link ApnPushNotificationTransportActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ApnPushNotificationTransportActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnPushNotificationTransportActivator.class);

    private BundleContext _bundleContext;
    private ServiceRegistration<PushNotificationTransport> transportRegistration;

    /**
     * Initializes a new {@link ApnPushNotificationTransportActivator}.
     */
    public ApnPushNotificationTransportActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, PushSubscriptionRegistry.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        _bundleContext = context;
        reinit();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        ServiceRegistration<PushNotificationTransport> transportRegistration = this.transportRegistration;
        if (null != transportRegistration) {
            transportRegistration.unregister();
            this.transportRegistration = null;
        }
        _bundleContext = null;
        super.stopBundle();
    }

    private synchronized void reinit() throws Exception {
        ServiceRegistration<PushNotificationTransport> transportRegistration = this.transportRegistration;
        if (null != transportRegistration) {
            transportRegistration.unregister();
        }

        ConfigurationService configService = getService(ConfigurationService.class);

        if (!configService.getBoolProperty("com.openxchange.mobilepush.events.apn.ios.enabled", false)) {
            LOG.info("APN push notification transport is disabled per configuration");
            return;
        }

        String resourceName = configService.getProperty("com.openxchange.mobilepush.events.apn.ios.keystore");
        if (Strings.isEmpty(resourceName)) {
            LOG.error("Missing key store for APN push notification transport.");
            return;
        }

        String password = configService.getProperty("com.openxchange.mobilepush.events.apn.ios.password");
        boolean production = configService.getBoolProperty("com.openxchange.mobilepush.events.apn.ios.production", false);
        ApnOptions options = createOptions(resourceName, password, production);
        ApnPushNotificationTransport apnTransport = new ApnPushNotificationTransport(options, getService(PushSubscriptionRegistry.class));

        String feedbackQueryInterval = configService.getProperty("com.openxchange.mobilepush.events.apn.ios.feedbackQueryInterval", (String)null);
        setupFeedbackQueries(apnTransport, feedbackQueryInterval);

        this.transportRegistration = _bundleContext.registerService(PushNotificationTransport.class, apnTransport, null);
    }

    private void setupFeedbackQueries(ApnPushNotificationTransport apnTransport, String feedbackQueryInterval) {
        if (Strings.isNotEmpty(feedbackQueryInterval)) {
            long interval = TimeSpanParser.parseTimespan(feedbackQueryInterval.trim()).longValue();
            if (60 * 1000 > interval) {
                LOG.warn("Ignoring too small value '{}' for APN feedback query interval.", feedbackQueryInterval);
                return;
            }

            TimerService timerService = getService(TimerService.class);
            long shiftMillis = TimeUnit.MILLISECONDS.convert((long)(Math.random() * interval), TimeUnit.MILLISECONDS);
            long initialDelay = interval + shiftMillis;
            timerService.scheduleWithFixedDelay(createQueryFeedbackTask(apnTransport), initialDelay, interval);
            LOG.info("Starting APN feedback query interval in {}ms (checking every {}ms)", Long.valueOf(initialDelay), Long.valueOf(interval));
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
