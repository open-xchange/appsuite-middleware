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

package com.openexchange.folder.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.File;
import com.openexchange.file.storage.limit.FileLimitService;
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
        JSONObject jsonObject = createResponse(exceededLimits);
        return new AJAXRequestResult(jsonObject, new Date(), "json");
    }

    private List<File> getFiles(AJAXRequestData request) throws OXException {
        final JSONObject body = getBodyAsJSONObject(request);
        List<File> quotaCheckFiles;
        if (body.hasAndNotNull("files")) {
            quotaCheckFiles = getParsedFiles(body);
        } else {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
        return quotaCheckFiles;
    }

    private List<File> getParsedFiles(final JSONObject body) throws OXException {
        List<File> quotaCheckFiles = new ArrayList<>();
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

    private File parse(final JSONObject object) throws OXException {
        final File file = new File();
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

    private JSONObject createResponse(final List<OXException> errors) throws JSONException {
        JSONObject json = new JSONObject();
        if (null == errors || errors.isEmpty()) {
            json.put("errors", new JSONArray());
            return json;
        }
        final JSONArray jsonArray = new JSONArray(errors.size());
        for (final OXException error : errors) {
            final JSONObject jsonError = new JSONObject();
            ResponseWriter.addException(jsonError, error.setCategory(Category.CATEGORY_ERROR), false);
            jsonArray.put(jsonError);
        }
        json.put("errors", jsonArray);
        return json;
    }

}
