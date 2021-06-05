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
import com.openexchange.apps.manifests.ManifestBuilder;
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
import com.openexchange.user.User;

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

        String hostname = null;
        if (hostNameService != null) {
            User user = session.getUser();
            if (null != user && user.isGuest()) {
                hostname = hostNameService.getGuestHostname(session.getUserId(), session.getContextId());
            } else {
                hostname = hostNameService.getHostname(session.getUserId(), session.getContextId());
            }
        }

        if (null == hostname) {
            HttpServletRequest servletRequest = requestData.optHttpServletRequest();
            hostname = null == servletRequest ? requestData.getHostname() : servletRequest.getServerName();
        }

        ServerConfig serverConfig = serverConfigService.getServerConfig(hostname, session);
        Map<String, Object> filteredConfig = serverConfig.forClient();

        try {

            JSONObject jsonConfig = asJSON(filteredConfig);
            String version = requestData.getParameter("version");
            jsonConfig.put("manifests", manifestBuilder.buildManifests(session, version));
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
        @SuppressWarnings("unchecked") Set<Capability> capabilities = readAttributes(serverConfig, "capabilities", Set.class);
        @SuppressWarnings("unchecked") List<SimpleEntry<String, String>> languages = readAttributes(serverConfig, "languages", List.class);

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
