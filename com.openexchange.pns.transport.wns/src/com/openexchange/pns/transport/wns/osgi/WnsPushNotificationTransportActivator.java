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

package com.openexchange.pns.transport.wns.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.wns.WnsOptions;
import com.openexchange.pns.transport.wns.WnsOptionsProvider;
import com.openexchange.pns.transport.wns.internal.DefaultWnsOptionsProvider;
import com.openexchange.pns.transport.wns.internal.WnsPushNotificationTransport;


/**
 * {@link WnsPushNotificationTransportActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WnsPushNotificationTransportActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WnsPushNotificationTransportActivator.class);

    private static final String CONFIGFILE_WNS_OPTIONS = "pns-wns-options.yml";

    private ServiceRegistration<WnsOptionsProvider> optionsProviderRegistration;
    private WnsPushNotificationTransport wnsTransport;

    /**
     * Initializes a new {@link ApnPushNotificationTransportActivator}.
     */
    public WnsPushNotificationTransportActivator() {
        super();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            reinit(configService);
        } catch (Exception e) {
            LOG.error("Failed to re-initialize WNS transport", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder()
            .configFileNames(CONFIGFILE_WNS_OPTIONS)
            .propertiesOfInterest("com.openexchange.pns.transport.wns.enabled")
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
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        WnsPushNotificationTransport wnsTransport = this.wnsTransport;
        if (null != wnsTransport) {
            wnsTransport.close();
            this.wnsTransport = null;
        }
        ServiceRegistration<WnsOptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
        if (null != optionsProviderRegistration) {
            optionsProviderRegistration.unregister();
            this.optionsProviderRegistration = null;
        }
        super.stopBundle();
    }

    private synchronized void reinit(ConfigurationService configService) throws Exception {
        WnsPushNotificationTransport wnsTransport = this.wnsTransport;
        if (null != wnsTransport) {
            wnsTransport.close();
            this.wnsTransport = null;
        }

        ServiceRegistration<WnsOptionsProvider> optionsProviderRegistration = this.optionsProviderRegistration;
        if (null != optionsProviderRegistration) {
            optionsProviderRegistration.unregister();
            this.optionsProviderRegistration = null;
        }

        if (!configService.getBoolProperty("com.openexchange.pns.transport.wns.enabled", false)) {
            LOG.info("WNS push notification transport is disabled per configuration");
            return;
        }

        Object yaml = configService.getYaml(CONFIGFILE_WNS_OPTIONS);
        if (null != yaml && Map.class.isInstance(yaml)) {
            Map<String, Object> map = (Map<String, Object>) yaml;
            if (!map.isEmpty()) {
                Map<String, WnsOptions> options = parseWnsOptions(map);
                if (null != options && !options.isEmpty()) {
                    Dictionary<String, Object> dictionary = new Hashtable<String, Object>(1);
                    dictionary.put(Constants.SERVICE_RANKING, Integer.valueOf(785));
                    optionsProviderRegistration = context.registerService(WnsOptionsProvider.class, new DefaultWnsOptionsProvider(options), dictionary);
                }
            }
        }

        wnsTransport = new WnsPushNotificationTransport(getService(PushSubscriptionRegistry.class), getService(PushMessageGeneratorRegistry.class), getService(ConfigViewFactory.class), context);
        wnsTransport.open();
        this.wnsTransport = wnsTransport;
    }

    private Map<String, WnsOptions> parseWnsOptions(Map<String, Object> yaml) throws Exception {
        Map<String, WnsOptions> options = new LinkedHashMap<String, WnsOptions>(yaml.size());
        for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            String client = entry.getKey();

            // Check for duplicate
            if (options.containsKey(client)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Duplicate WNS options specified for client: " + client);
            }

            // Check values map
            if (false == Map.class.isInstance(entry.getValue())) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Invalid WNS options configuration specified for client: " + client);
            }

            // Parse values map
            Map<String, Object> values = (Map<String, Object>) entry.getValue();

            // Enabled?
            Boolean enabled = getBooleanOption("enabled", Boolean.TRUE, values);
            if (enabled.booleanValue()) {
                // Key
                String sid = getStringOption("sid", values);
                String secret = getStringOption("secret", values);
                if (null == sid || null == secret) {
                    LOG.info("Missing \"sid\" or \"secret\" WNS option for client {}. Ignoring that client's configuration.", client);
                } else {
                    WnsOptions wnsOptions = new WnsOptions(sid, secret);
                    options.put(client, wnsOptions);
                    LOG.info("Parsed WNS options for client {}.", client);
                }
            } else {
                LOG.info("WNS options for client {} is disabled.", client);
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

}
