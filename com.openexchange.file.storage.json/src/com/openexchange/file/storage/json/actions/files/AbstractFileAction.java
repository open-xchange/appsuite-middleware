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

package com.openexchange.file.storage.json.actions.files;

import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.json.FileMetadataWriter;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractFileAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractFileAction implements AJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractFileAction.class);

    /**
     * The parameter enumeration.
     */
    public static enum Param {
        /** The <code>"id"</code> parameter */
        ID("id"),
        /** The <code>"folder"</code> parameter */
        FOLDER_ID("folder"),
        /** The <code>"version"</code> parameter */
        VERSION("version"),
        /** The <code>"columns"</code> parameter */
        COLUMNS("columns"),
        /** The <code>"sort"</code> parameter */
        SORT("sort"),
        /** The <code>"order"</code> parameter */
        ORDER("order"),
        /** The <code>"timezone"</code> parameter */
        TIMEZONE("timezone"),
        /** The <code>"timestamp"</code> parameter */
        TIMESTAMP("timestamp"),
        /** The <code>"ignore"</code> parameter */
        IGNORE("ignore"),
        /** The <code>"diff"</code> parameter */
        DIFF("diff"),
        /** The <code>"attached"</code> parameter */
        ATTACHED_ID("attached"),
        /** The <code>"module"</code> parameter */
        MODULE("module"),
        /** The <code>"attachment"</code> parameter */
        ATTACHMENT("attachment");

        private final String name;

        private Param(final String name) {
            this.name = name;
        }

        /**
         * Gets the parameter name
         *
         * @return The name
         */
        public String getName() {
            return name;
        }
    } // End of enum Param

    /**
     * Handles the given request
     *
     * @param request The request to handle
     * @return The result
     * @throws OXException If handling the request fails
     */
    protected abstract AJAXRequestResult handle(InfostoreRequest request) throws OXException;

    /**
     * Creates an appropriate result for a single file.
     *
     * @param file The file result
     * @param request The request
     * @return The AJAX result for a single file
     * @throws OXException If result cannot be created
     */
    protected AJAXRequestResult result(final File file, final InfostoreRequest request) throws OXException {
        return new AJAXRequestResult(file, new Date(file.getSequenceNumber()), "infostore");
    }

    /**
     * Creates an appropriate result for a single file.
     *
     * @param file The file result
     * @param request The request
     * @param saveAction Action performed when saving the file
     * @return The AJAX result for a single file
     * @throws OXException If result cannot be created
     */
    protected AJAXRequestResult result(final File file, final AJAXInfostoreRequest request, String saveAction) throws OXException {
        try {
            JSONObject json = new JSONObject();
            json.put("save_action", saveAction);
            json.put("file", new FileMetadataWriter(Services.getFieldCollector()).write(request, file));
            return new AJAXRequestResult(json);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    protected AJAXRequestResult result(final List<String> ids, final InfostoreRequest request) throws OXException {
        try {
            JSONArray array = new JSONArray(ids.size());
            for (String id : ids) {
                JSONObject object = new JSONObject(4);
                object.put("id", id);
                object.put("folder", request.getFolderForID(id));
                array.put(object);
            }
            return new AJAXRequestResult(array);
        } catch (final JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create( x.getMessage());
        }
    }

    public AJAXRequestResult result(final String[] versions, final long sequenceNumber, final InfostoreRequest request) throws OXException {
        JSONArray array = new JSONArray(versions.length);
        for (String i : versions) {
            array.put(i);
        }
        return new AJAXRequestResult(array, new Date(sequenceNumber));
    }

    public AJAXRequestResult success(final long sequenceNumber) {
        return new AJAXRequestResult(Boolean.TRUE, new Date(sequenceNumber));
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        AJAXInfostoreRequest req = new AJAXInfostoreRequest(requestData, session);
        try {
            before(req);
            AJAXRequestResult result = handle(req);
            success(req, result);
            return result;
        } catch (OXException x) {
            failure(req, x);
            throw x;
        } catch (NullPointerException e) {
            failure(req, e);
            LOG.error("", e);
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, "Null dereference.");
        } finally {
            after(req);

            // Delete tmp files
            requestData.cleanUploads();
        }
    }

    /**
     * Invoked after the request has been handled (either successfully or not).
     *
     * @param req The request
     */
    protected void after(final AJAXInfostoreRequest req) {
        IDBasedFileAccess fileAccess = req.optFileAccess();
        if (null != fileAccess) {
            try {
                fileAccess.finish();
            } catch (Exception e) {
                // Ignore
            }
        }
        IDBasedFolderAccess folderAccess = req.optFolderAccess();
        if (null != folderAccess) {
            try {
                folderAccess.finish();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Invoked if handling the request failed
     *
     * @param req The failed request
     * @param throwable The associated error
     * @throws OXException If call fails
     */
    protected void failure(final AJAXInfostoreRequest req, final Throwable throwable) throws OXException{
        // Nothing to do
    }

    /**
     * Invoked if handling the request succeeded
     *
     * @param req The succeeded request
     * @param result The associated result
     * @throws OXException If call fails
     */
    protected void success(final AJAXInfostoreRequest req, final AJAXRequestResult result) throws OXException{
        // Nothing to do
    }

    /**
     * Invoked before the request is about to be handled.
     *
     * @param req The request
     * @throws OXException If the call fails
     */
    protected void before(final AJAXInfostoreRequest req) throws OXException {
        // Nothing to do
    }

    /**
     * Gets the size threshold for ZIP archives
     *
     * @return The size threshold
     */
    protected static long threshold() {
        return FileStorageUtility.threshold();
    }

    /**
     * Gets the configured value for "com.openexchange.infostore.zipDocumentsCompressionLevel".
     *
     * @return The configured compression level
     * @throws OXException
     */
    public static int getZipDocumentsCompressionLevel() throws OXException {
        return FileStorageUtility.getZipDocumentsCompressionLevel();
    }

}
