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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Actions;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@Actions({@Action(method = RequestMethod.PUT, name = "new", description = "Create an infoitem via PUT", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module.")
}, requestBody = "Infoitem object as described in Common object data and Detailed infoitem data. The field id is not included.",
responseDescription = "FolderID/ObjectID of the newly created infoitem."),
@Action(method = RequestMethod.POST, name = "new", description = "Create an infoitem via POST", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "json", description = "Infoitem object as described in Common object data and Detailed infoitem data. The field id is not included."),
    @Parameter(name = "file", description = "File metadata as per <input type=\"file\" />")
}, requestBody = "Body of content-type \"multipart/form-data\" or \"multipart/mixed\" containing the above mentioned fields and file-data.",
responseDescription = "FolderID/ObjectID of the newly created infoitem. The response is sent as a HTML document (see introduction).")})
public class NewAction extends AbstractWriteAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        request.requireFileMetadata();

        IDBasedFileAccess fileAccess = request.getFileAccess();
        File file = request.getFile();
        String originalFileName = file.getFileName();

        // Check folder
        if (Strings.isEmpty(file.getFolderId())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("folder");
        }

        file.setId(FileStorageFileAccess.NEW);
        boolean ignoreWarnings = AJAXRequestDataTools.parseBoolParameter("ignoreWarnings", request.getRequestData(), false);
        boolean tryAddVersion = AJAXRequestDataTools.parseBoolParameter("try_add_version", request.getRequestData(), false);

        String newId;
        if (request.hasUploads()) {
            // Save file metadata with binary payload
            newId = fileAccess.saveDocument(file, request.getUploadedFileData(), FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, request.getSentColumns(), false, ignoreWarnings, tryAddVersion);
        } else {
            // Save file metadata without binary payload
            newId = fileAccess.saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, request.getSentColumns(), ignoreWarnings, tryAddVersion);
        }

        List<OXException> warnings = new ArrayList<>(fileAccess.getAndFlushWarnings());
        if (request.notifyPermissionEntities() && file.getObjectPermissions() != null && file.getObjectPermissions().size() > 0) {
            File modified = fileAccess.getFileMetadata(newId, FileStorageFileAccess.CURRENT_VERSION);
            warnings.addAll(sendNotifications(request.getNotificationTransport(), request.getNotifiactionMessage(), null, modified, request.getSession(), request.getRequestData().getHostData()));
        }

        // Construct detailed response as requested including any warnings, treat as error if not forcibly ignored by client
        AJAXRequestResult result;
        String saveAction = fileAccess.getAndFlushSaveActions(newId);
        if (null == saveAction || Strings.isEmpty(saveAction)) {
            saveAction = "none";
        }
        if (null != newId && request.extendedResponse()) {
            File metadata = fileAccess.getFileMetadata(newId, FileStorageFileAccess.CURRENT_VERSION);
            if (!metadata.getFileName().equals(originalFileName)) {
                saveAction = "rename";
            }
            result = result(metadata, (AJAXInfostoreRequest) request, saveAction);
        } else {
            result = new AJAXRequestResult(newId, new Date(file.getSequenceNumber()));
        }

        result.addWarnings(warnings);
        if (null == newId && null != warnings && false == warnings.isEmpty() && false == ignoreWarnings) {
            String name = getFilenameSave(file, null, fileAccess);
            result.setException(FileStorageExceptionCodes.FILE_SAVE_ABORTED.create(name, name));
        }
        return result;
    }

}
