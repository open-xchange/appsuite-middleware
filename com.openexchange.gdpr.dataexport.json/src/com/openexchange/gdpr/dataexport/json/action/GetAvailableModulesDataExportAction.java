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

package com.openexchange.gdpr.dataexport.json.action;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.Module;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GetAvailableModulesDataExportAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class GetAvailableModulesDataExportAction extends AbstractDataExportAction {

    /**
     * Initializes a new {@link GetAvailableModulesDataExportAction}.
     *
     * @param services The service look-up
     */
    public GetAvailableModulesDataExportAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        // Acquire available modules
        DataExportService dataExportService = getDataExportService();
        DataExportConfig config = dataExportService.getConfig();
        List<Module> availableModules = dataExportService.getAvailableModules(session);

        // Add modules' configuration
        JSONObject jConfig = new JSONObject(availableModules.size() + 1);
        for (Module module : availableModules) {
            Optional<Map<String, Object>> optionalProps = module.getProperties();
            jConfig.put(module.getId(), optionalProps.isPresent() ? new JSONObject(optionalProps.get()) : JSONObject.EMPTY_OBJECT);
        }

        // Add default max. file size
        jConfig.put("maxFileSize", config.getDefaultMaxFileSize());

        return new AJAXRequestResult(jConfig, "json");
    }

}
