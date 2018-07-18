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

package com.openexchange.pns.transport.apns_http2.osgi;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.apns_http2.ApnsHttp2Options;
import com.openexchange.pns.transport.apns_http2.ApnsHttp2Options.AuthType;
import com.openexchange.pns.transport.apns_http2.ApnsHttp2OptionsProvider;
import com.openexchange.pns.transport.apns_http2.internal.ApnsHttp2PushNotificationTransport;
import com.openexchange.pns.transport.apns_http2.internal.DefaultApnsHttp2OptionsProvider;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;


/**
 * {@link ApnsHttp2PushNotificationTransportActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ApnsHttp2PushNotificationTransportActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnsHttp2PushNotificationTransportActivator.class);

    private static final String CONFIGFILE_APNS_OPTIONS = "pns-apns_http2-options.yml";

    private ServiceRegistration<ApnsHttp2OptionsProvider> optionsProviderRegistration;
    private ApnsHttp2PushNotificationTransport apnHttp2Transport;
    private ScheduledTimerTask feedbackQueryTask;

    /**
     * Initializes a new {@link ApnsHttp2PushNotificationTransportActivator}.
     */
    public ApnsHttp2PushNotificationTransportActivator() {
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
            .propertiesOfInterest("com.openexchange.pns.transport.apns_http2.ios.enabled")
            .build();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, PushSubscriptionRegistry.class, PushMessageGeneratorRegistry.class, ConfigViewFactory.class, TimerService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        reinit(getService(ConfigurationService.class));

        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                ApnsHttp2PushNotificationTransport.invalidateEnabledCache();
            }

            @Override
            public Interests getInterests() {
                return null;
            }
        });

        registerService(Reloadable.class, this);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        ApnsHttp2PushNotificationTransport apnTransport = this.apnHttp2Transport;
        if (null != apnTransport) {
            apnTransport.close();
            this.apnHttp2Transport = null;
        }
        ServiceRegistration<ApnsHttp2OptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
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
        ApnsHttp2PushNotificationTransport apnHttp2Transport = this.apnHttp2Transport;
        if (null != apnHttp2Transport) {
            apnHttp2Transport.close();
            this.apnHttp2Transport = null;
        }

        ServiceRegistration<ApnsHttp2OptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
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
                Map<String, ApnsHttp2Options> options = parseApnOptions(map);
                if (null != options && !options.isEmpty()) {
                    Dictionary<String, Object> dictionary = new Hashtable<String, Object>(1);
                    dictionary.put(Constants.SERVICE_RANKING, Integer.valueOf(785));
                    optionsProviderRegistration = context.registerService(ApnsHttp2OptionsProvider.class, new DefaultApnsHttp2OptionsProvider(options), dictionary);
                    this.optionsProviderRegistration = optionsProviderRegistration;
                }
            }
        }

        apnHttp2Transport = new ApnsHttp2PushNotificationTransport(getService(PushSubscriptionRegistry.class), getService(PushMessageGeneratorRegistry.class), getService(ConfigViewFactory.class), context);
        apnHttp2Transport.open();
        this.apnHttp2Transport = apnHttp2Transport;
    }

    private Map<String, ApnsHttp2Options> parseApnOptions(Map<String, Object> yaml) throws Exception {
        Map<String, ApnsHttp2Options> options = new LinkedHashMap<String, ApnsHttp2Options>(yaml.size());
        NextClient: for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            String client = entry.getKey();

            // Check for duplicate
            if (options.containsKey(client)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Duplicate APNS HTTP/2 options specified for client: " + client);
            }

            // Check values map
            if (false == Map.class.isInstance(entry.getValue())) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Invalid APNS HTTP/2 options configuration specified for client: " + client);
            }

            // Parse values map
            Map<String, Object> values = (Map<String, Object>) entry.getValue();

            // Enabled?
            Boolean enabled = getBooleanOption("enabled", Boolean.TRUE, values);
            if (!enabled.booleanValue()) {
                LOG.info("APNS HTTP/2 options for client {} is disabled.", client);
                continue NextClient;
            }

            // Topic
            String topic = getStringOption("topic", values);
            if (null == topic) {
                LOG.info("Missing \"topic\" APNS HTTP/2 option for client {}. Ignoring that client's configuration.", client);
                continue NextClient;
            }

            // Auth type
            AuthType authType = AuthType.authTypeFor(getStringOption("authtype", values));
            if (null == authType) {
                LOG.info("Missing or invalid authentication type in APNS HTTP/2 options for client {}. Ignoring that client's configuration.", client);
                continue NextClient;
            }

            if (authType == AuthType.CERTIFICATE) {
                // Keystore name
                String keystoreName = getStringOption("keystore", values);
                if (null == keystoreName) {
                    LOG.info("Missing \"keystore\" APNS HTTP/2 option for client {}. Ignoring that client's configuration.", client);
                    continue NextClient;
                }

                // Proceed if enabled for associated client
                if (Strings.isNotEmpty(keystoreName)) {
                    String password = getStringOption("password", values);
                    if (null == password) {
                        LOG.info("Missing \"password\" APNS HTTP/2 option for client {}. Ignoring that client's configuration.", client);
                        continue NextClient;
                    }

                    Boolean production = getBooleanOption("production", Boolean.TRUE, values);
                    ApnsHttp2Options apnsHttp2Options = createOptions(client, keystoreName, password, production.booleanValue(), topic);
                    options.put(client, apnsHttp2Options);
                    LOG.info("Parsed APNS options for client {}.", client);
                }
            } else if (authType == AuthType.JWT) {
                String privateKey = getStringOption("privatekey", values);
                if (null == privateKey) {
                    LOG.info("Missing \"privatekey\" APNS HTTP/2 option for client {}. Ignoring that client's configuration.", client);
                    continue NextClient;
                }

                String keyId = getStringOption("keyid", values);
                if (null == keyId) {
                    LOG.info("Missing \"keyid\" APNS HTTP/2 option for client {}. Ignoring that client's configuration.", client);
                    continue NextClient;
                }

                String teamId = getStringOption("teamid", values);
                if (null == teamId) {
                    LOG.info("Missing \"teamid\" APNS HTTP/2 option for client {}. Ignoring that client's configuration.", client);
                    continue NextClient;
                }

                Boolean production = getBooleanOption("production", Boolean.TRUE, values);
                ApnsHttp2Options apnsHttp2Options = createOptions(client, privateKey, keyId, teamId, production.booleanValue(), topic);
                options.put(client, apnsHttp2Options);
                LOG.info("Parsed APNS options for client {}.", client);
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

    private ApnsHttp2Options createOptions(String clientId, String resourceName, String password, boolean production, String topic) {
        return new ApnsHttp2Options(clientId, new File(resourceName), password, production, topic);
    }

    private ApnsHttp2Options createOptions(String clientId, String privateKey, String keyId, String teamId, boolean production, String topic) throws Exception{
        return new ApnsHttp2Options(clientId, new File(privateKey), keyId, teamId, production, topic);
    }

}
