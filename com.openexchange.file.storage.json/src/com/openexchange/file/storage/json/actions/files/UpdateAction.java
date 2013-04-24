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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.LinkedHashMap;
import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Actions;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedIgnorableVersionFileAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@Actions({
    @Action(method = RequestMethod.PUT, name = "update", description = "Update an infoitem via PUT", parameters = {
        @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
        @Parameter(name = "id", description = "Object ID of the updated infoitem."),
        @Parameter(name = "timestamp", description = "Timestamp of the updated infoitem. If the infoitem was modified after the specified timestamp, then the update must fail.")
    }, requestBody = "Infoitem object as described in Common object data and Detailed infoitem data. Only modified fields are present."),
    @Action(method = RequestMethod.POST, name = "update", description = "Update an infoitem via POST", parameters = {
        @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
        @Parameter(name = "id", description = "Object ID of the updated infoitem."),
        @Parameter(name = "timestamp", description = "Timestamp of the updated infoitem. If the infoitem was modified after the specified timestamp, then the update must fail."),
        @Parameter(name = "json", description = "Infoitem object as described in Common object data and Detailed infoitem data. The field id is not included."),
        @Parameter(name = "file", description = "File metadata as per <input type=\"file\" />")
    }, requestBody = "Body of content-type \"multipart/form-data\" or \"multipart/mixed\" containing the above mentioned fields and file-data.",
    responseDescription = "The response is sent as a HTML document (see introduction).")
})
public class UpdateAction extends AbstractWriteAction {

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        request.requireFileMetadata().require(Param.TIMESTAMP);
        final File file = request.getFile();
        if (file.getId() == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }

        final IDBasedFileAccess fileAccess = request.getFileAccess();
        if (request.hasUploads()) {
            final boolean ignoreVersion = request.getBoolParameter("ignoreVersion");
            if (ignoreVersion) {
                if (fileAccess instanceof IDBasedIgnorableVersionFileAccess) {
                    final IDBasedIgnorableVersionFileAccess ignorableVersionFileAccess = (IDBasedIgnorableVersionFileAccess) fileAccess;
                    final FileID id = new FileID(file.getId());
                    if (ignorableVersionFileAccess.supportsIgnorableVersion(id.getService(), id.getAccountId())) {
                        ignorableVersionFileAccess.saveDocument(file, request.getUploadedFileData(), request.getTimestamp(), request.getSentColumns(), true);
                    } else {
                        ignorableVersionFileAccess.saveDocument(file, request.getUploadedFileData(), request.getTimestamp(), request.getSentColumns());
                    }
                } else {
                    fileAccess.saveDocument(file, request.getUploadedFileData(), request.getTimestamp(), request.getSentColumns());
                }
            } else {
                fileAccess.saveDocument(file, request.getUploadedFileData(), request.getTimestamp(), request.getSentColumns());
            }
        } else {
            fileAccess.saveFileMetadata(file, request.getTimestamp(), request.getSentColumns());
        }

        final AJAXRequestResult result = success(file.getSequenceNumber());

        final EventAdmin eventAdmin = Services.getEventAdmin();
        if (null != eventAdmin) {
            final Map<String, Object> m = new LinkedHashMap<String, Object>(4);
            final ServerSession session = request.getSession();
            m.put("userId", Integer.valueOf(session.getUserId()));
            m.put("contextId", Integer.valueOf(session.getContextId()));
            m.put("fileId", file.getId());
            m.put("eTag", FileStorageUtility.getETagFor(fileAccess.getFileMetadata(file.getId(), FileStorageFileAccess.CURRENT_VERSION)));
            eventAdmin.postEvent(new Event("com/openexchange/infostore/update", m));
        }

        return result;
    }

}
