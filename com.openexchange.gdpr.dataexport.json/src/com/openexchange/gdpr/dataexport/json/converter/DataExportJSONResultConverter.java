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

package com.openexchange.gdpr.dataexport.json.converter;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExport;
import com.openexchange.gdpr.dataexport.DataExportResultFile;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.DataExportWorkItem;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DataExportJSONResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportJSONResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link DataExportJSONResultConverter}.
     */
    public DataExportJSONResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "dataexport";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        try {
            final Object resultObject = result.getResultObject();
            if (resultObject instanceof DataExport) {
                DataExport export = (DataExport) resultObject;
                result.setResultObject(convertDataExport(export), "json");
                return;
            }
            /*
             * Collection of tasks
             */
            if (resultObject == null) {
                return;
            }
            @SuppressWarnings("unchecked") Collection<DataExport> exports = (Collection<DataExport>) resultObject;
            final JSONArray jArray = new JSONArray(exports.size());
            for (DataExport export : exports) {
                jArray.put(convertDataExport(export));
            }
            result.setResultObject(jArray, "json");
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject convertDataExport(DataExport export) throws JSONException {
        JSONObject json = convertDataExportTask(export.getTask());

        Optional<List<DataExportResultFile>> optionalResultFiles = export.getResultFiles();
        if (optionalResultFiles.isPresent()) {
            json.put("duration", export.getTask().getDuration());
            Optional<Date> optionalAvailableUntil = export.getAvailableUntil();
            if (optionalAvailableUntil.isPresent()) {
                json.put("availableUntil", optionalAvailableUntil.get().getTime());
            }
            List<DataExportResultFile> list = optionalResultFiles.get();
            json.put("results", convertDataExportResultFiles(list));
        }

        return json;
    }

    private JSONObject convertDataExportTask(DataExportTask task) throws JSONException {
        JSONObject json = new JSONObject(6);
        json.put("id", UUIDs.getUnformattedString(task.getId()));
        json.put("status", task.getStatus());
        json.put("creationTime", task.getCreationTime().getTime());
        if (task.getStartTime() != null) {
            json.put("startTime", task.getStartTime().getTime());
        }
        json.put("workItems", convertDataExportWorkItems(task.getWorkItems()));
        return json;
    }

    private JSONArray convertDataExportWorkItems(List<DataExportWorkItem> workItems) throws JSONException {
        JSONArray jItems = new JSONArray(workItems.size());
        for (DataExportWorkItem workItem : workItems) {
            jItems.put(convertDataExportWorkItem(workItem));
        }
        return jItems;
    }

    private JSONObject convertDataExportWorkItem(DataExportWorkItem workItem) throws JSONException {
        JSONObject json = new JSONObject(6);
        json.put("id", UUIDs.getUnformattedString(workItem.getId()));
        json.put("module", workItem.getModuleId());
        json.put("status", workItem.getStatus());
        json.putOpt("info", workItem.getInfo());
        return json;
    }

    private JSONArray convertDataExportResultFiles(List<DataExportResultFile> resultFiles) throws JSONException {
        JSONArray jItems = new JSONArray(resultFiles.size());
        for (DataExportResultFile resultFile : resultFiles) {
            jItems.put(convertDataExportResultFile(resultFile));
        }
        return jItems;
    }

    private JSONObject convertDataExportResultFile(DataExportResultFile resultFile) throws JSONException {
        JSONObject json = new JSONObject(6);
        json.put("fileInfo", resultFile.getFileName());
        json.put("number", resultFile.getNumber());
        json.put("contentType", resultFile.getContentType());
        json.put("taskId", UUIDs.getUnformattedString(resultFile.getTaskId()));
        return json;
    }

}
