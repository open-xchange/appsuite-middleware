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

import static com.openexchange.java.Autoboxing.I;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportDownload;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportService;
import com.openexchange.gdpr.dataexport.json.DataExportFileHolder;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DownloadDataExportResultFileAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DownloadDataExportResultFileAction extends AbstractDataExportAction {

    /**
     * Initializes a new {@link DownloadDataExportResultFileAction}.
     *
     * @param services The service look-up
     */
    public DownloadDataExportResultFileAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        DataExportService dataExportService = getDataExportService();
        String demandedTaskId = requestData.requireParameter("id");
        int number = Integer.parseInt(requestData.requireParameter("number"));

        DataExportDownload dataExportDownload = dataExportService.getDataExportDownload(number, session.getUserId(), session.getContextId());

        if (!demandedTaskId.equals(UUIDs.getUnformattedString(dataExportDownload.getTaskId()))) {
            throw DataExportExceptionCode.NO_SUCH_TASK.create(I(session.getUserId()), I(session.getContextId()));
        }

        requestData.setFormat("file");
        DataExportFileHolder fileHolder = new DataExportFileHolder(dataExportDownload);
        return new AJAXRequestResult(fileHolder, "file");
    }

}
