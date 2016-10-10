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
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdateAction extends AbstractWriteAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        // Some checks and useful variables
        request.requireFileMetadata().require(Param.TIMESTAMP);
        File file = request.getFile();
        if (file.getId() == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }
        FileID id = new FileID(file.getId());
        IDBasedFileAccess fileAccess = request.getFileAccess();
        boolean ignoreWarnings = AJAXRequestDataTools.parseBoolParameter("ignoreWarnings", request.getRequestData(), false);

        String newId;
        List<Field> columns = request.getSentColumns();
        boolean notify = request.notifyPermissionEntities() && columns.contains(File.Field.OBJECT_PERMISSIONS) && file.getObjectPermissions() != null && file.getObjectPermissions().size() > 0;
        File original = null;
        if (notify) {
            original = fileAccess.getFileMetadata(file.getId(), FileStorageFileAccess.CURRENT_VERSION);
        }
        if (request.hasUploads()) {
            int offset = AJAXRequestDataTools.parseIntParameter(request.getParameter("offset"), 0);
            if (0 < offset) {
                // Append file metadata with binary payload at given offset
                newId = fileAccess.saveDocument(file, request.getUploadedFileData(), request.getTimestamp(), columns, offset);
            } else {
                // Save file metadata with binary payload
                boolean ignoreVersion = request.getBoolParameter("ignoreVersion") && fileAccess.supports(id.getService(), id.getAccountId(), FileStorageCapability.IGNORABLE_VERSION);
                newId = fileAccess.saveDocument(file, request.getUploadedFileData(), request.getTimestamp(), columns, ignoreVersion, ignoreWarnings, false);
            }
        } else {
            // Save file metadata without binary payload
            newId = fileAccess.saveFileMetadata(file, request.getTimestamp(), columns, ignoreWarnings, false);
        }

        List<OXException> warnings = new ArrayList<>(fileAccess.getAndFlushWarnings());
        if (notify && null != newId) {
            File modified = fileAccess.getFileMetadata(newId, FileStorageFileAccess.CURRENT_VERSION);
            warnings.addAll(sendNotifications(request.getNotificationTransport(), request.getNotifiactionMessage(), original, modified, request.getSession(), request.getRequestData().getHostData()));
        }
        // Construct detailed response as requested including any warnings, treat as error if not forcibly ignored by client
        AJAXRequestResult result;
        if (null != newId && request.extendedResponse()) {
            result = result(fileAccess.getFileMetadata(newId, FileStorageFileAccess.CURRENT_VERSION), request);
        } else {
            result = new AJAXRequestResult(newId, new Date(file.getSequenceNumber()));
        }

        result.addWarnings(warnings);
        if (null == newId && null != warnings && false == warnings.isEmpty() && false == ignoreWarnings) {
            result.setException(FileStorageExceptionCodes.FILE_UPDATE_ABORTED.create(getFilenameSave(file, id, fileAccess), id.toUniqueID()));
        }
        return result;
    }

}
