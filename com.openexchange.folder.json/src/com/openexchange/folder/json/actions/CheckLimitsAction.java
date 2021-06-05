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

package com.openexchange.folder.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.FileLimitService;
import com.openexchange.file.storage.limit.LimitFile;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * {@link CheckLimitsAction}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class CheckLimitsAction extends AbstractFolderAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CheckLimitsAction.class);

    protected static final String ACTION = "checklimits";

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData request, ServerSession session) throws OXException, JSONException {
        final String folderId = request.getParameter("id");
        if (Strings.isEmpty(folderId)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }
        final String type = request.getParameter("type");
        if (Strings.isEmpty(type)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("type");
        }

        FileLimitService limitsService = ServiceRegistry.getInstance().getService(FileLimitService.class, true);

        List<OXException> exceededLimits = limitsService.checkLimits(request.getSession(), folderId, getFiles(request), type);
        Locale locale = session.getUser().getLocale();
        JSONObject jsonObject = createResponse(exceededLimits, locale);
        return new AJAXRequestResult(jsonObject, new Date(), "json");
    }

    private List<LimitFile> getFiles(AJAXRequestData request) throws OXException {
        final JSONObject body = getBodyAsJSONObject(request);
        List<LimitFile> quotaCheckFiles;
        if (body.hasAndNotNull("files")) {
            quotaCheckFiles = getParsedFiles(body);
        } else {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
        return quotaCheckFiles;
    }

    private List<LimitFile> getParsedFiles(final JSONObject body) throws OXException {
        List<LimitFile> quotaCheckFiles = new ArrayList<>();
        try {
            JSONArray files = new JSONArray(body.getString("files"));
            for (int i = 0; i < files.length(); i++) {
                JSONObject jsonFile = files.getJSONObject(i);
                if (jsonFile == null || jsonFile == JSONObject.EMPTY_OBJECT || jsonFile == JSONObject.NULL) {
                    continue;
                }
                quotaCheckFiles.add(parse(jsonFile));
            }
        } catch (JSONException e) {
            LOG.error("Unable to get files from body: {}", e.getMessage(), e);
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
        return quotaCheckFiles;
    }

    private JSONObject getBodyAsJSONObject(AJAXRequestData request) throws OXException {
        Object data = request.getData();
        if (data == null) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        if (!(data instanceof JSONObject)) {
            throw AjaxExceptionCodes.ILLEGAL_REQUEST_BODY.create();
        }
        return (JSONObject) data;
    }

    private LimitFile parse(final JSONObject object) throws OXException {
        final LimitFile file = new LimitFile();
        if (object.hasAndNotNull("size")) {
            try {
                file.setSize(object.getLong("size"));
            } catch (JSONException e) {
                LOG.debug("Unable to set file size. Will ignore that file in checks.", e);
                file.setSize(0);
            }
        } else {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
        if (object.hasAndNotNull("name")) {
            try {
                file.setName(object.getString("name"));
            } catch (JSONException e) {
                LOG.debug("Unable to set file name.", e);
                file.setName("Unknown");
            }
        } else {
            file.setName("Unknown");
        }
        return file;
    }

    private JSONObject createResponse(final List<OXException> errors, Locale locale) throws JSONException {
        JSONArray jErrors;
        if (null == errors || errors.isEmpty()) {
            jErrors = JSONArray.EMPTY_ARRAY;
        } else {
            jErrors = new JSONArray(errors.size());
            for (OXException error : errors) {
                JSONObject jError = new JSONObject();
                ResponseWriter.addException(jError, error.setCategory(Category.CATEGORY_ERROR), locale);
                jErrors.put(jError);
            }
        }
        return new JSONObject(2).put("errors", jErrors);
    }

}
