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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    private final ServiceLookup services;
    private final ServerConfigServicesLookup registry;
    private final ComputedServerConfigValueService[] computedValues;

    public ConfigAction(ServiceLookup services, JSONArray manifests, ServerConfigServicesLookup registry) {
        super();
        this.services = services;
        this.registry = registry;

        computedValues = new ComputedServerConfigValueService[]{
            new Manifests(services, manifests), new Capabilities(services),
            new Hosts(),
            new ServerVersion(),
            new Languages(services),
            new UIVersion()
        };
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData,
        ServerSession session) throws OXException {
        try {
            JSONObject serverconfig = getFromConfiguration(requestData, session);

            addComputedValues(serverconfig, requestData, session);
            mixInConfigurationValues(serverconfig, session);
            
            return new AJAXRequestResult(serverconfig, "json");
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x.toString());
        }
    }

    private void mixInConfigurationValues(JSONObject serverconfig, ServerSession session) throws OXException, JSONException {
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
    private JSONObject getFromConfiguration(AJAXRequestData requestData, ServerSession session) throws JSONException, OXException {

        Map<String, Object> serverConfigs = (Map<String, Object>) services.getService(ConfigurationService.class).getYaml("as-config.yml");

        Map<String, Object> serverConfig = new HashMap<String, Object>();

        Map<String, Object> defaults = (Map<String, Object>) services.getService(ConfigurationService.class).getYaml("as-config-defaults.yml");
        if (defaults != null) {
            serverConfig.putAll((Map<String, Object>) defaults.get("default"));
        }

        // Find other applicable configurations
        if (serverConfigs != null) {
            for (Object value : serverConfigs.values()) {
                if (looksApplicable((Map<String, Object>) value, requestData, session)) {
                    serverConfig.putAll((Map<String, Object>) value);
                }
            }
        }

        return (JSONObject) JSONCoercion.coerceToJSON(serverConfig);
    }

    private boolean looksApplicable(Map<String, Object> value, AJAXRequestData requestData, ServerSession session) throws OXException {
        if (value == null) {
            return false;
        }
        String host = (String) value.get("host");
        if (host != null) {
            if (host.equals(requestData.getHostname()) || "all".equals(host)) {
                return true;
            }
        }

        String hostRegex = (String) value.get("hostRegex");
        if (hostRegex != null) {
            try {
                Pattern pattern = Pattern.compile(hostRegex);
                if (pattern.matcher(requestData.getHostname()).find()) {
                    return true;
                }
            } catch (final PatternSyntaxException e) {
                // Ignore. Treat as absent.
            }
        }

        List<ServerConfigMatcherService> matchers = registry.getMatchers();
        for (ServerConfigMatcherService matcher : matchers) {
            if (matcher.looksApplicable(value, requestData, session)) {
                return true;
            }
        }

        return false;
    }

    private void addComputedValues(JSONObject serverconfig, AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        for (ComputedServerConfigValueService computed : computedValues) {
            computed.addValue(serverconfig, requestData, session);
        }
        for (ComputedServerConfigValueService computed : registry.getComputed()) {
            computed.addValue(serverconfig, requestData, session);
        }
    }

}
