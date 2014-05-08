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

package com.openexchange.apps.manifests.json;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.apps.manifests.ComputedServerConfigValueService;
import com.openexchange.apps.manifests.ServerConfigMatcherService;
import com.openexchange.apps.manifests.json.osgi.ServerConfigServicesLookup;
import com.openexchange.apps.manifests.json.values.Capabilities;
import com.openexchange.apps.manifests.json.values.Hosts;
import com.openexchange.apps.manifests.json.values.Languages;
import com.openexchange.apps.manifests.json.values.Manifests;
import com.openexchange.apps.manifests.json.values.ServerVersion;
import com.openexchange.apps.manifests.json.values.UIVersion;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ConfigAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Some clean-up
 */
@DispatcherNotes(noSession = true)
public class ConfigAction implements AJAXActionService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ConfigAction.class);

    private final ServiceLookup services;
    private final ServerConfigServicesLookup registry;
    private final ComputedServerConfigValueService[] computedValues;

    public ConfigAction(ServiceLookup services, JSONArray manifests, ServerConfigServicesLookup registry) {
        super();
        this.services = services;
        this.registry = registry;

        computedValues = new ComputedServerConfigValueService[] {
            new Manifests(services, manifests),
            new Capabilities(services),
            new Hosts(),
            new ServerVersion(),
            new Languages(services),
            new UIVersion()
        };
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            JSONObject serverconfig = getFromConfiguration(requestData, session);

            addComputedValues(serverconfig, requestData, session);
            mixInConfigurationValues(serverconfig, session);

            return new AJAXRequestResult(serverconfig, "json");
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x, x.toString());
        }
    }

    protected void mixInConfigurationValues(JSONObject serverconfig, ServerSession session) throws OXException, JSONException {
        if (session.isAnonymous()) {
            return;
        }

        ConfigView view = services.getService(ConfigViewFactory.class).getView(session.getUserId(), session.getContextId());
        for(Map.Entry<String,ComposedConfigProperty<String>> entry: view.all().entrySet()) {
            if (entry.getKey().startsWith("com.openexchange.appsuite.server")) {
                String name = entry.getKey().substring(32);
                serverconfig.put(name, entry.getValue().get());
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getFromConfiguration(AJAXRequestData requestData, ServerSession session) throws JSONException, OXException {
        // Get configured brands/server configurations
        Map<String, Object> configurations = (Map<String, Object>) services.getService(ConfigurationService.class).getYaml("as-config.yml");
        debugOut("as-config.yml", configurations);

        // The resulting brand/server configuration
        Map<String, Object> serverConfiguration = new HashMap<String, Object>(4);

        // Check for default brands/server configurations
        {
            Map<String, Object> defaults = (Map<String, Object>) services.getService(ConfigurationService.class).getYaml("as-config-defaults.yml");
            if (defaults != null) {
                serverConfiguration.putAll((Map<String, Object>) defaults.get("default"));
                debugOut("as-config-defaults.yml", defaults);
            }
        }

        // Find other applicable brands/server configurations
        if (configurations != null) {
            boolean empty = true;
            LinkedList<Map<String, Object>> applicableConfigs = new LinkedList<Map<String,Object>>();
            for (Map.Entry<String, Object> configEntry : configurations.entrySet()) {
                Map<String, Object> possibleConfiguration = (Map<String, Object>) configEntry.getValue();
                if (looksApplicable(possibleConfiguration, requestData, session)) {
                    // ensure that "all"-host-wildcards are applied first
                    if ("all".equals(possibleConfiguration.get("host"))) {
                        applicableConfigs.addFirst(possibleConfiguration);
                    } else {
                        applicableConfigs.add(possibleConfiguration);
                    }
                    empty = false;
                } else {
                    final String configName = configEntry.getKey();
                    if (null == possibleConfiguration) {
                        LOGGER.debug("Empty configuration \"{}\" is not applicable", configName);
                    } else {
                        LOGGER.debug("Configuration \"{}\" is not applicable: {}", configName, prettyPrint(configName, possibleConfiguration));
                    }
                }
            }
            if (!empty) {
                for (Map<String, Object> config : applicableConfigs) {
                    serverConfiguration.putAll(config);
                }
            }
        }

        // Return its JSON representation
        return (JSONObject) JSONCoercion.coerceToJSON(serverConfiguration);
    }

    protected boolean looksApplicable(Map<String, Object> possibleConfiguration, AJAXRequestData requestData, ServerSession session) throws OXException {
        if (possibleConfiguration == null) {
            return false;
        }

        // Check "host"
        {
            final String host = (String) possibleConfiguration.get("host");
            if (host != null) {
                if ("all".equals(host)) {
                    return true;
                }

                final String hostName = requestData.getHostname();
                if (host.equals(hostName)) {
                    return true;
                }

                // Not applicable according to host check
                LOGGER.debug("Host '{}' does not apply to {}", host, hostName);
            }
        }

        // Check "hostRegex"
        {
            final String keyHostRegex = "hostRegex";
            final String hostRegex = (String) possibleConfiguration.get(keyHostRegex);
            if (hostRegex != null) {
                try {
                    final Pattern pattern = Pattern.compile(hostRegex);

                    final String hostName = requestData.getHostname();
                    if (pattern.matcher(hostName).find()) {
                        return true;
                    }

                    // Not applicable according to hostRegex check
                    LOGGER.debug("Host-Regex '{}' does not match {}", hostRegex, hostName);
                } catch (final PatternSyntaxException e) {
                    // Ignore. Treat as absent.
                    LOGGER.debug("Invalid regex pattern for {}: {}", keyHostRegex, hostRegex, e);
                }
            }
        }

        // Check by matchers
        {
            final List<ServerConfigMatcherService> matchers = registry.getMatchers();
            for (final ServerConfigMatcherService matcher : matchers) {
                if (matcher.looksApplicable(possibleConfiguration, requestData, session)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void addComputedValues(JSONObject serverconfig, AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        for (ComputedServerConfigValueService computed : computedValues) {
            computed.addValue(serverconfig, requestData, session);
        }
        for (ComputedServerConfigValueService computed : registry.getComputed()) {
            computed.addValue(serverconfig, requestData, session);
        }
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
            LOGGER.debug("Read configurations from \"{}\": {}", ymlName, str);
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

    String prettyPrint(final String configName, Map<String, Object> configuration) {
        if (null == configuration) {
            return "<not-set>";
        }

        final StringBuilder sb = new StringBuilder(configuration.size() << 4);
        final String indent = "    ";
        final String sep = Strings.getLineSeparator();

        sb.append(sep);
        prettyPrint(configName, configuration, indent, sep, sb);
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

}
