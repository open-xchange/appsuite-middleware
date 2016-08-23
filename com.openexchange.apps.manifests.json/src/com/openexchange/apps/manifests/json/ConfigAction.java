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

package com.openexchange.apps.manifests.json;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.apps.manifests.json.exception.ManifestsExceptionCodes;
import com.openexchange.capabilities.Capability;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ConfigAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Some clean-up
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@DispatcherNotes(noSession = true)
public class ConfigAction implements AJAXActionService {

    private final ServiceLookup services;
    private final ManifestBuilder manifestBuilder;

    public ConfigAction(ServiceLookup services, ManifestBuilder manifestBuilder) {
        super();
        this.services = services;
        this.manifestBuilder = manifestBuilder;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        ServerConfigService serverConfigService = services.getService(ServerConfigService.class);
        HostnameService hostNameService = services.getOptionalService(HostnameService.class);

        String hostname;

        if (hostNameService != null) {
            if (session.getUser().isGuest()) {
                hostname = hostNameService.getGuestHostname(session.getUserId(), session.getContextId());
            } else {
                hostname = hostNameService.getHostname(session.getUserId(), session.getContextId());
            }
        }
        HttpServletRequest servletRequest = requestData.optHttpServletRequest();
        if (null != servletRequest) {
            hostname = servletRequest.getServerName();
        } else {
            hostname = requestData.getHostname();
        }

        ServerConfig serverConfig = serverConfigService.getServerConfig(hostname, session);
        Map<String, Object> filteredConfig = serverConfig.forClient();

        try {

            JSONObject jsonConfig = asJSON(filteredConfig);
            jsonConfig.put("manifests", manifestBuilder.buildManifests(session));
            return new AJAXRequestResult(jsonConfig, "json");
        } catch (JSONException je) {
            throw AjaxExceptionCodes.JSON_ERROR.create(je.getMessage());
        }

    }

    /**
     * Converts the specified {@link Map} into a {@link JSONObject}
     * 
     * @param serverConfig The map with the server configuration
     * @return The converted {@link JSONObject} with the server configuration
     * @throws JSONException if a JSON error occurs
     * @throws OXException if an internal error occurs
     */
    private JSONObject asJSON(Map<String, Object> serverConfig) throws JSONException, OXException {
        Set<Capability> capabilities = readAttributes(serverConfig, "capabilities", Set.class);
        List<SimpleEntry<String, String>> languages = readAttributes(serverConfig, "languages", List.class);

        //coerce
        JSONObject serverConfigurationObject = (JSONObject) JSONCoercion.coerceToJSON(serverConfig);

        //add additional entries that can't simply be coerced
        serverConfigurationObject.put("capabilities", services.getService(SimpleConverter.class).convert("capability", "json", capabilities, null));

        final JSONArray allLanguages = new JSONArray(languages.size());
        for (SimpleEntry<String, String> language : languages) {
            allLanguages.put(new JSONArray(2).put(language.getKey()).put(language.getValue()));
        }
        serverConfigurationObject.put("languages", allLanguages);
        return serverConfigurationObject;
    }

    /**
     * Reads the specified attributes from the specified {@link Map} and casts them into a type {@link T}
     * 
     * @param serverConfig The {@link Map} containing the attributes
     * @param key The key of the attributes
     * @param clazz The type {@link T} to cast them
     * @return The attributes as type {@link T}
     * @throws OXException if the attributes are not present in the {@link Map}
     */
    private <T> T readAttributes(Map<String, Object> serverConfig, String key, Class<T> clazz) throws OXException {
        Object value = serverConfig.remove(key);
        if (value == null) {
            throw ManifestsExceptionCodes.MISSING_ATTRIBUTE_SET.create(key);
        }
        return clazz.cast(value);
    }
}
