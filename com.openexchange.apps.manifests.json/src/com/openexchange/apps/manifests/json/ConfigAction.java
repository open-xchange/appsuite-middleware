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
import com.openexchange.capabilities.CapabilityFilter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ConfigAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@DispatcherNotes(noSession = true)
public class ConfigAction implements AJAXActionService {

	private final ServiceLookup services;
	private final ServerConfigServicesLookup registry;
	private final ComputedServerConfigValueService[] computedValues;

	public ConfigAction(ServiceLookup services, JSONArray manifests, ServerConfigServicesLookup registry, final CapabilityFilter capabilityFilter) {
		super();
		this.services = services;
		this.registry = registry;

		computedValues = new ComputedServerConfigValueService[]{
				new Manifests(services, manifests, capabilityFilter),
				new Capabilities(services),
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

			return new AJAXRequestResult(serverconfig, "json");
		} catch (JSONException x) {
			throw AjaxExceptionCodes.JSON_ERROR.create(x.toString());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JSONObject getFromConfiguration(AJAXRequestData requestData,
			ServerSession session) throws JSONException, OXException {

		Map serverConfigs = (Map) services.getService(ConfigurationService.class).getYaml("as-config.yml");

		Map serverConfig = new HashMap();

		Map defaults = (Map) services.getService(ConfigurationService.class).getYaml("as-config-defaults.yml");
		if (defaults != null) {
			serverConfig.putAll((Map) defaults.get("default"));
		}



		// Find other applicable configs
		if (serverConfigs != null) {
			for(Object value: serverConfigs.values()) {
				if (looksApplicable((Map) value, requestData, session)) {
					serverConfig.putAll((Map) value);
				}
			}
		}

		return (JSONObject) JSONCoercion.coerceToJSON(serverConfig);
	}

	private boolean looksApplicable(Map value, AJAXRequestData requestData,
			ServerSession session) throws OXException {

		if (value == null) {
			return false;
		}
		String host = (String) value.get("host");
		if (host != null) {
			if (requestData.getHostname().equals(host) || host.equals("all")) {
				return true;
			}
		}

		String hostRegex = (String) value.get("hostRegex");

		if (hostRegex != null) {
			Pattern pattern = Pattern.compile(hostRegex);
			if (pattern.matcher(requestData.getHostname()).find()) {
				return true;
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
