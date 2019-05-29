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
 *    trademarks of the OX Software GmbH. group of companies.
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
