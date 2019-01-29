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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableList;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder.InputStreamClosure;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.EnqueuableAJAXActionService;
import com.openexchange.antivirus.AntiVirusResult;
import com.openexchange.antivirus.AntiVirusResultEvaluatorService;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.antivirus.exceptions.AntiVirusServiceExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.json.FileMetadataWriter;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.id.IDMangler;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractFileAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractFileAction implements AJAXActionService, EnqueuableAJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractFileAction.class);

    private static final List<File.Field> FILE_FIELDS = ImmutableList.of(File.Field.ID, File.Field.FILENAME, File.Field.FILE_SIZE, File.Field.FILE_MD5SUM);

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
        ATTACHMENT("attachment"),
        /** The <code>"attachment_module"</code> parameter */
        ATTACHMENT_MODULE("attachment_module");

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
            throw AjaxExceptionCodes.JSON_ERROR.create(x.getMessage());
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
            parsePushTokenParameter(req);
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
        } catch (RuntimeException e) {
            failure(req, e);
            LOG.error("", e);
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
    protected void failure(final AJAXInfostoreRequest req, final Throwable throwable) throws OXException {
        // Nothing to do
    }

    /**
     * Invoked if handling the request succeeded
     *
     * @param req The succeeded request
     * @param result The associated result
     * @throws OXException If call fails
     */
    protected void success(final AJAXInfostoreRequest req, final AJAXRequestResult result) throws OXException {
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

    @Override
    public Result isEnqueueable(AJAXRequestData request, ServerSession session) throws OXException {
        AJAXInfostoreRequest req = new AJAXInfostoreRequest(request, session);
        return isEnqueueable(req);
    }

    /**
     * @param request The {@link InfostoreRequest}
     * @throws OXException
     */
    protected Result isEnqueueable(InfostoreRequest request) throws OXException {
        return EnqueuableAJAXActionService.resultFor(false);
    }

    /**
     * Parses the folder/file pairs from the specified {@link JSONArray}
     * 
     * @param jPairs The pairs to parse
     * @return A {@link List} with all the parsed pairs
     * @throws JSONException if a JSON error is occurred
     * @throws OXException if an invalid resource identifier is encountered
     */
    protected List<IdVersionPair> parsePairs(JSONArray jPairs) throws JSONException, OXException {
        int len = jPairs.length();
        List<IdVersionPair> idVersionPairs = new ArrayList<IdVersionPair>(len);
        for (int i = 0; i < len; i++) {
            JSONObject tuple = jPairs.getJSONObject(i);

            // Identifier
            String id = tuple.optString(Param.ID.getName(), null);
            // Folder
            String folderId = tuple.optString(Param.FOLDER_ID.getName(), null);
            // Version
            String version = tuple.optString(Param.VERSION.getName(), FileStorageFileAccess.CURRENT_VERSION);

            // Check validity
            if (null == id && null == folderId) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("body", "Invalid resource identifier: " + tuple);
            }

            idVersionPairs.add(new IdVersionPair(id, version, folderId));
        }
        return idVersionPairs;
    }

    /**
     * Performs a scan (if a scan is requested by the specified {@link InfostoreRequest}, i.e. via the <code>scan</code>
     * URL parameter) of the specified InputStreamClosure.
     * 
     * @param request The {@link InfostoreRequest}
     * @param isClosure The stream
     * @param metadata The metadata of the stream
     * @throws OXException if the file is too large, or if the {@link AntiVirusService} is absent,
     *             or if the file is infected, or if a timeout or any other error is occurred
     */
    protected boolean scan(InfostoreRequest request, IFileHolder fileHolder, File metadata) throws OXException {
        if (false == mayScan(request)) {
            return false;
        }
        AntiVirusResult result = getAntiVirusService().scan(fileHolder, getUniqueId(metadata, request.getSession().getContextId()));
        getAntiVirusResultEvaluatorService().evaluate(result, metadata.getFileName());
        return result.isStreamScanned();
    }

    /**
     * Performs a scan (if a scan is requested by the specified {@link InfostoreRequest}, i.e. via the <code>scan</code>
     * URL parameter) of the specified InputStreamClosure.
     * 
     * @param request The {@link InfostoreRequest}
     * @param isClosure The stream
     * @param metadata The attachment metadata of the stream
     * @throws OXException if the file is too large, or if the {@link AntiVirusService} is absent,
     *             or if the file is infected, or if a timeout or any other error is occurred
     */
    protected void scan(InfostoreRequest request, InputStreamClosure isClosure, AttachmentMetadata metadata) throws OXException {
        if (false == mayScan(request)) {
            return;
        }
        scan(isClosure, metadata.getFilename(), metadata.getFileId(), metadata.getFilesize());
    }
    
    /**
     * Retrieves the optional pushToken parameter from the request and adds it to the session
     *
     * @param request The request of the action call
     */
    private void parsePushTokenParameter(AJAXInfostoreRequest request) {
        String pushToken = request.getParameter("pushToken");
        if (Strings.isNotEmpty(pushToken)) {
            request.getSession().setParameter(Session.PARAM_PUSH_TOKEN, pushToken);
        }
    }
    
    /**
     * Scans the items that are denoted with the specified {@link IdVersionPair}s (if a scan is requested by the specified
     * {@link InfostoreRequest}, i.e. via the <code>scan</code> URL parameter). If the <code>recursive</code> flag
     * is enabled and one (or more) of the {@link IdVersionPair}s denote a folder, then the scan is performed recursively
     * for all sub-folders.
     * 
     * @param request The {@link InfostoreRequest}
     * @param versionPairs The version pairs that denote files and/or folders
     * @param fileAccess The {@link IDBasedFileAccess}
     * @param folderAccess The {@link IDBasedFolderAccess}
     * @param recursive <code>true</code> to scan recursively, <code>false</code> otherwise
     * @throws OXException if the file is too large, or if the {@link AntiVirusService} is absent,
     *             or if the file is infected, or if a timeout or any other error is occurred
     */
    protected void scan(InfostoreRequest request, List<IdVersionPair> versionPairs, IDBasedFileAccess fileAccess, IDBasedFolderAccess folderAccess, boolean recursive) throws OXException {
        if (false == mayScan(request)) {
            return;
        }
        for (IdVersionPair pair : versionPairs) {
            if (pair.getIdentifier() == null) {
                scanFolder(request, pair.getFolderId(), fileAccess, folderAccess, recursive);
            } else {
                File file = fileAccess.getFileMetadata(pair.getIdentifier(), pair.getVersion());
                scanFile(request, () -> fileAccess.getDocument(getUniqueId(file, request.getSession().getContextId()), FileStorageFileAccess.CURRENT_VERSION), file);
            }
        }
    }

    /**
     * Checks whether to perform an Anti-Virus scan by examining:<br/>
     * a) the 'scan' URL parameter of the request (if absent defaults to <code>false</code>)<br/>
     * b) the capability 'antivirus' is enabled for the specified user<br/>
     * 
     * @param request The {@link InfostoreRequest}
     * @return <code>true</code> if scanning is enabled, <code>false</code> otherwise
     * @throws OXException if the 'antivirus' capability is disabled
     */
    private boolean mayScan(InfostoreRequest request) throws OXException {
        String scan = request.getParameter("scan");
        Boolean s = Strings.isEmpty(scan) ? Boolean.FALSE : Boolean.valueOf(scan);
        if (false == s.booleanValue()) {
            LOG.debug("No anti-virus scanning was performed.");
            return false;
        }
        AntiVirusService antiVirusService = Services.getAntiVirusService();
        if (antiVirusService == null) {
            throw AntiVirusServiceExceptionCodes.ANTI_VIRUS_SERVICE_ABSENT.create();
        }
        return antiVirusService.isEnabled(request.getSession());
    }

    /**
     * Scans the folder with the specified identifier (recursively)
     * 
     * @param request The {@link InfostoreRequest}
     * @param folderId The folder identifier
     * @param fileAccess The {@link IDBasedFileAccess}
     * @param folderAccess The {@link IDBasedFolderAccess}
     * @param recursive <code>true</code> to scan recursively, <code>false</code> otherwise
     * @throws OXException if the file is too large, or if the {@link AntiVirusService} is absent,
     *             or if the file is infected, or if a timeout or any other error is occurred
     */
    private void scanFolder(InfostoreRequest request, String folderId, IDBasedFileAccess fileAccess, IDBasedFolderAccess folderAccess, boolean recursive) throws OXException {
        List<File> files = SearchIterators.asList(fileAccess.getDocuments(folderId, FILE_FIELDS).results());
        for (File file : files) {
            scanFile(request, () -> fileAccess.getDocument(file.getId(), FileStorageFileAccess.CURRENT_VERSION), file);
        }
        if (false == recursive) {
            return;
        }
        for (FileStorageFolder folder : folderAccess.getSubfolders(folderId, false)) {
            scanFolder(request, folder.getId(), fileAccess, folderAccess, recursive);
        }
    }

    /**
     * Checks whether the {@link InputStream} in the specified closure should be scanned
     * 
     * @param request The {@link InfostoreRequest}
     * @param isClosure The InputStreamClosure
     * @param metadata The metadata of the input stream
     * @throws OXException if the file is too large, or if the {@link AntiVirusService} is absent,
     *             or if the file is infected, or if a timeout or any other error is occurred
     */
    private void scanFile(InfostoreRequest request, InputStreamClosure isClosure, File metadata) throws OXException {
        scan(isClosure, metadata.getFileName(), getUniqueId(metadata, request.getSession().getContextId()), metadata.getFileSize());
    }

    /**
     * Checks whether the {@link InputStream} in the specified closure should be scanned
     * 
     * @param closure The InputStreamClosure
     * @param uniqueId the unique identifier of the stream
     * @param filesize The file size of the stream
     * @throws OXException if the file is too large, or if the {@link AntiVirusService} is absent,
     *             or if the file is infected, or if a timeout or any other error is occurred
     */
    private void scan(InputStreamClosure closure, String filename, String uniqueId, long filesize) throws OXException {
        AntiVirusResult result = getAntiVirusService().scan(closure, uniqueId, filesize);
        getAntiVirusResultEvaluatorService().evaluate(result, filename);
    }

    /**
     * Retrieves the Anti-Virus service if available
     * 
     * @return The anti-virus service
     * @throws OXException if the service is absent
     */
    private AntiVirusService getAntiVirusService() throws OXException {
        AntiVirusService avService = Services.getAntiVirusService();
        if (avService == null) {
            throw ServiceExceptionCode.serviceUnavailable(AntiVirusService.class);
        }
        return avService;
    }

    /**
     * Retrieves the Anti-Virus service if available
     * 
     * @return The anti-virus service
     * @throws OXException if the service is absent
     */
    private AntiVirusResultEvaluatorService getAntiVirusResultEvaluatorService() throws OXException {
        AntiVirusResultEvaluatorService service = Services.getAntiVirusResultEvaluatorService();
        if (service == null) {
            throw ServiceExceptionCode.serviceUnavailable(AntiVirusResultEvaluatorService.class);
        }
        return service;
    }

    /**
     * Gets the identifier that uniquely identifies the specified {@link File}, being
     * either the MD5 checksum, or the file identifier (in that order). If none is present
     * then the fall-back identifier is returned
     * 
     * @param file The {@link File}
     * @param contextId The context identifier
     * @return The unique identifier, never <code>null</code>
     */
    private String getUniqueId(File file, int contextId) {
        String id = file.getFileMD5Sum();
        if (Strings.isNotEmpty(id)) {
            return id;
        }
        return IDMangler.mangle(Integer.toString(contextId), file.getId(), file.getVersion(), Long.toString(file.getSequenceNumber()));
    }
}
