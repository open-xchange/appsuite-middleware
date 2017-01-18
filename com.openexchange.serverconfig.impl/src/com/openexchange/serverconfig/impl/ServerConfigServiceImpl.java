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

package com.openexchange.serverconfig.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.serverconfig.ServerConfigServicesLookup;
import com.openexchange.session.Session;


/**
 * {@link ServerConfigServiceImpl}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class ServerConfigServiceImpl implements ServerConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(ServerConfigService.class);

    private final ServiceLookup serviceLookup;
    private final ServerConfigServicesLookup serverConfigServicesLookup;

    /**
     * Initializes a new {@link ServerConfigServiceImpl}.
     */
    public ServerConfigServiceImpl(ServiceLookup serviceLookup, ServerConfigServicesLookup serverConfigServicesLookup) {
        super();
        this.serviceLookup = serviceLookup;
        this.serverConfigServicesLookup = serverConfigServicesLookup;
    }

    @Override
    public ServerConfig getServerConfig(String hostName, int userID, int contextID) throws OXException {
        return createNewServerConfig0(hostName, userID, contextID, null);
    }

    @Override
    public ServerConfig getServerConfig(String hostName, Session session) throws OXException {
        return createNewServerConfig0(hostName, session.getUserId(), session.getContextId(), session);
    }

    @SuppressWarnings("unchecked")
    private ServerConfig createNewServerConfig0(String hostName, int userID, int contextID, Session session) throws OXException {
        ConfigurationService configService = serviceLookup.getService(ConfigurationService.class);

        // The resulting brand/server configuration
        Map<String, Object> serverConfiguration = new HashMap<String, Object>(32, 0.9F);

        Map<String, Object> defaults = (Map<String, Object>) configService.getYaml("as-config-defaults.yml");
        if (defaults != null) {
            serverConfiguration.putAll((Map<String, Object>) defaults.get("default"));
            debugOut("as-config-defaults.yml", defaults);
        }

        LinkedList<Map<String, Object>> applicableConfigs = configService.getCustomHostConfigurations(hostName, userID, contextID, serviceLookup.getService(ConfigViewFactory.class));
        
        for (Map<String, Object> config : applicableConfigs) {
            serverConfiguration.putAll(config);
        }
        
        /*
         * Add computed values after configview values
         */
        for (ComputedServerConfigValueService computed : serverConfigServicesLookup.getComputed()) {
            try {
                computed.addValue(serverConfiguration, hostName, userID, contextID, session);
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                LOG.error("Failed to add value from '{}' to server configuration", computed.getClass().getName(), t);
            }
        }

        serverConfiguration = correctLanguageConfiguration(serverConfiguration);

        return new ServerConfigImpl(serverConfiguration, serverConfigServicesLookup.getClientFilters());
    }
    

    /**
     * The languages config item in the as-config.yml can be:
     *  - A String "all" which then gets replaced with an ArrayList containing all installed languages
     *  - A Hash as shown in Bug 24171, 41992 specifying a set of languages
     *  - Missing which gets replaced with an ArrayList containing all installed languages
     *
     * What we have to deliver is an ArrayList of entries like [{de_DE=Deutsch},{en_US=English}]. As customers might be using this kind
     * of configuration already we have to stay backwards compatible and thus rewrite the entry as workaround.
     *
     * @param serverConfiguration The configuration that might contain the languages entry.
     * @return The configuration with the adjusted languages entry.
     */
    private Map<String, Object> correctLanguageConfiguration(Map<String, Object> serverConfiguration) {
        Object languagesConfig = serverConfiguration.get("languages");

        if (languagesConfig instanceof Map) {
            ArrayList<SimpleEntry<String, String>> languageList = new ArrayList<SimpleEntry<String, String>>();
            Map<String, String> languageMap = (Map<String, String>) languagesConfig;
            for (Entry<String, String> entry : languageMap.entrySet()) {
                languageList.add(new SimpleEntry<>(entry));
            }
            serverConfiguration.put("languages", languageList);
        }
        return serverConfiguration;
    }


    // ---------------------------------------------------- DEBUG STUFF --------------------------------------------------------------- //

    /**
     * Output pretty-printed configuration to debug log.
     *
     * @param ymlName The name of the YML file
     * @param configurations The read configurations from YML file
     */
    private void debugOut(final String ymlName, final Map<String, Object> configurations) {
        if (null != configurations) {
            final Object str = new Object() {

                @Override
                public String toString() {
                    return prettyPrintConfigurations(configurations);
                }
            };
            LOG.debug("Read configurations from \"{}\": {}", ymlName, str);
        }
    }

    String prettyPrintConfigurations(Map<String, Object> configurations) {
        if (null == configurations) {
            return "<not-set>";
        }

        final StringBuilder sb = new StringBuilder(configurations.size() << 4);
        final String indent = "    ";
        final String sep = Strings.getLineSeparator();
        boolean first = true;

        for (final Entry<String, Object> configurationEntry : configurations.entrySet()) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> configuration = (Map<String, Object>) configurationEntry.getValue();
            if (null != configuration) {
                if (first) {
                    sb.append(sep);
                    first = false;
                }

                prettyPrint(configurationEntry.getKey(), configuration, indent, sep, sb);
            }
        }

        if (first) {
            return "<not-set>";
        }

        return sb.toString();
    }

    void prettyPrint(final String configName, Map<String, Object> configuration, final String indent, final String sep, final StringBuilder sb) {
        if (null != configuration) {
            sb.append(indent).append(configName).append(':').append(sep);

            for (final Entry<String, Object> entry : configuration.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                sb.append(indent).append(indent).append(key).append(": ");
                if (value instanceof String) {
                    sb.append('\'').append(value).append('\'');
                } else {
                    sb.append(value);
                }
                sb.append(sep);
            }
        }
    }

    @Override
    public ServerConfigServicesLookup getServerConfigServicesLookup() {
        return this.serverConfigServicesLookup;
    }

}
