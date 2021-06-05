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

import java.util.Optional;
import org.json.ImmutableJSONObject;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExport;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.DataExportStatus;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GetDataExportAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class GetDataExportAction extends AbstractDataExportAction {

    private final JSONObject jNonExistent;

    /**
     * Initializes a new {@link GetDataExportAction}.
     *
     * @param services The service look-up
     */
    public GetDataExportAction(ServiceLookup services) {
        super(services);
        jNonExistent = ImmutableJSONObject.immutableFor(new JSONObject(2).putSafe("status", DataExportStatus.NONE.getId() ));
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        DataExportService dataExportService = getDataExportService();
        Optional<DataExport> optionalExport = dataExportService.getDataExport(session);
        if (!optionalExport.isPresent()) {
            return new AJAXRequestResult(jNonExistent, "json");
        }

        return new AJAXRequestResult(optionalExport.get(), "dataexport");
    }

}
