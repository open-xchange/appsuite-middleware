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

package com.openexchange.importexport.actions.importer;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.EnqueuableAJAXActionService;
import com.openexchange.ajax.requesthandler.jobqueue.JobKey;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.data.conversion.ical.TruncationInfo;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.importexport.Format;
import com.openexchange.importexport.Importer;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.importers.ImportResults;
import com.openexchange.importexport.json.ImportRequest;
import com.openexchange.importexport.json.ImportWriter;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

@DispatcherNotes(enqueueable = true)
public abstract class AbstractImportAction implements EnqueuableAJAXActionService {

    private static final String PARAM_UPLOAD_PARSED = "importexport.upload.parsed";

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractImportAction}.
     *
     * @param services The service look-up
     */
    protected AbstractImportAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the format of this import action.
     *
     * @return The format
     */
    protected abstract Format getFormat();

    /**
     * Gets the importer to use.
     *
     * @return The importer
     */
    protected abstract Importer getImporter();

    @Override
    public Result isEnqueueable(AJAXRequestData request, ServerSession session) throws OXException {
        JSONObject jKeyDesc = new JSONObject(3)
            .putSafe("module", "import")
            .putSafe("action", getFormat().getConstantName())
            .putSafe("folder", request.getParameter(AJAXServlet.PARAMETER_FOLDERID))
        ;
        return EnqueuableAJAXActionService.resultFor(true, new JobKey(session.getUserId(), session.getContextId(), jKeyDesc.toString()), this);
    }

    @Override
    public void prepareForEnqueue(AJAXRequestData request, ServerSession session) throws OXException {
        // Initiate & parse upload prior to submitting to job queue
        long maxSize = sysconfMaxUpload();
        if (!request.hasUploads(-1, maxSize > 0 ? maxSize : -1L)) {
            throw ImportExportExceptionCodes.NO_FILE_UPLOADED.create();
        }
        if (request.getFiles(-1, maxSize > 0 ? maxSize : -1L).size() > 1) {
            throw ImportExportExceptionCodes.ONLY_ONE_FILE.create();
        }
        request.putParameter(PARAM_UPLOAD_PARSED, "true");
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        // Initiate & parse upload (if not already done)
        UploadFile uploadFile;
        String sUploadParsed = requestData.getParameter(PARAM_UPLOAD_PARSED);
        if (sUploadParsed == null || !"true".equals(sUploadParsed)) {
            long maxSize = sysconfMaxUpload();
            if (!requestData.hasUploads(-1, maxSize > 0 ? maxSize : -1L)){
                throw ImportExportExceptionCodes.NO_FILE_UPLOADED.create();
            }
            List<UploadFile> uploadFiles = requestData.getFiles(-1, maxSize > 0 ? maxSize : -1L);
            if (uploadFiles.size() > 1){
                throw ImportExportExceptionCodes.ONLY_ONE_FILE.create();
            }
            uploadFile = uploadFiles.get(0);
        } else {
            uploadFile = requestData.getFiles().get(0);
        }
        return perform(new ImportRequest(uploadFile, requestData, session));
    }

    private AJAXRequestResult perform(ImportRequest req) throws OXException {
        try {
            ImportResults importResults = getImporter().importData(req.getSession(), getFormat(), req.getImportFileAsStream(), req.getFolders(), req.getOptionalParams());
            OXJSONWriter jsonWriter = new OXJSONWriter();
            try {
                new ImportWriter(jsonWriter, req.getSession()).writeObjects(importResults.getImportResults());
            } catch (JSONException e1) {
                final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractImportAction.class);
                logger.error("JSON error", e1);
            }

            AJAXRequestResult result = new AJAXRequestResult(jsonWriter.getObject());
            int num = 0;
            for (ImportResult res : importResults.getImportResults()) {
                if (res.getWarnings() != null && res.getWarnings().size() > 0) {
                    num += res.getWarnings().size();
                }
            }

            TruncationInfo truncationInfo = importResults.getTruncationInfo();
            if (num > 0) {
                if (null != truncationInfo && truncationInfo.isTruncated()) {
                    result.setException(ImportExportExceptionCodes.WARNINGS_AND_TRUNCATED_RESULTS.create(I(num), I(truncationInfo.getLimit())));
                } else {
                    result.setException(ImportExportExceptionCodes.WARNINGS.create(I(num)));
                }
            } else if (null != truncationInfo && truncationInfo.isTruncated()) {
                result.setException(ImportExportExceptionCodes.TRUNCATED_RESULTS.create(I(truncationInfo.getLimit())));
            }

            return result;
        } finally {
            final AJAXRequestData ajaxRequestData = req.getRequest();
            if (null != ajaxRequestData) {
                try {
                    ajaxRequestData.cleanUploads();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    private static long sysconfMaxUpload() {
        String sizeS = ServerConfig.getProperty(ServerConfig.Property.MAX_UPLOAD_SIZE);
        return null == sizeS ? 0 : Long.parseLong(sizeS);
    }

}
