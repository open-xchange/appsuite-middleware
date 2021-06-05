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

package com.openexchange.importexport.actions.exporter;

import static com.openexchange.java.Autoboxing.I2i;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.Exporter;
import com.openexchange.importexport.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.json.ExportRequest;
import com.openexchange.tools.session.ServerSession;

public abstract class AbstractExportAction implements AJAXActionService {

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        return perform(new ExportRequest(requestData, session));
    }

    public abstract Format getFormat();

    public abstract Exporter getExporter();

    private static final String PARAMETER_CONTENT_TYPE = "content_type";
    private static final String DELIVERY = AJAXServlet.PARAMETER_DELIVERY;
    private static final String SAVE_AS_TYPE = "application/octet-stream";
    private static final String DOWNLOAD = "download";

    private AJAXRequestResult perform(ExportRequest req) throws OXException {
        int[] fieldsToBeExported = req.getColumns() != null ? I2i(req.getColumns()) : null;
        Map<String, List<String>> batchIds = req.getBatchIds();
        Exporter exporter = getExporter();

        SizedInputStream sis;

        if (doBatchExport(batchIds)) {
            sis = exporter.exportBatchData(req.getSession(), getFormat(), batchIds, fieldsToBeExported, getOptionalParams(req));
        } else {
            sis = exporter.exportFolderData(req.getSession(), getFormat(), req.getFolder(), fieldsToBeExported, getOptionalParams(req));
        }

        if (null == sis) {
            // Streamed
            return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
        }

        final FileHolder fileHolder = new FileHolder(sis, sis.getSize(), sis.getFormat().getMimeType(), getExportFileName(req, sis.getFormat().getExtension()));
        fileHolder.setDisposition("attachment");
        req.getRequest().setFormat("file");
        return new AJAXRequestResult(fileHolder, "file");
    }

    private boolean doBatchExport(Map<String, List<String>> batchIds) {
        return null != batchIds && false == batchIds.isEmpty();
    }

    protected Map<String, Object> getOptionalParams(ExportRequest req) {
        AJAXRequestData request = req.getRequest();
        boolean responseAccess = request.isHttpServletResponseAvailable();
        if (!responseAccess) {
            return null;
        }

        Map<String, Object> optionalParams = new HashMap<>(4);
        optionalParams.put("__requestData", request);
        String contentType = request.getParameter(PARAMETER_CONTENT_TYPE);
        String delivery = request.getParameter(DELIVERY);
        if (SAVE_AS_TYPE.equals(contentType) || DOWNLOAD.equalsIgnoreCase(delivery)) {
            optionalParams.put("__saveToDisk", Boolean.TRUE);
        }

        return optionalParams;
    }

    private String getExportFileName(ExportRequest req, String extension) {
        Map<String, List<String>> batchIds = req.getBatchIds();
        if (null == batchIds || batchIds.isEmpty()) {
            return getExporter().getFolderExportFileName(req.getSession(), req.getFolder(), extension);
        }
        return getExporter().getBatchExportFileName(req.getSession(), batchIds, extension);
    }

}
