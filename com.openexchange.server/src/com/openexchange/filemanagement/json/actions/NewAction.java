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

package com.openexchange.filemanagement.json.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.POST, name = "new", description = "Uploading a file", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "module", description = "The module for which the file is uploaded to determine proper upload quota constraints (e.g. \"mail\", \"infostore\", etc.)."),
    @Parameter(name = "type", description = "The file type filter to define which file types are allowed during upload. Currently supported filters are: file=all, text=text/*, media=image OR audio OR video, image=image/*, audio=audio/*, video=video/*, application=application/*")
}, requestBody = "A common POST request body of MIME type \"multipart/*\" which holds the file(s) to upload",
responseDescription = "A JSON array containing the IDs of the uploaded files. The files are accessible through the returned IDs for future use.")
public final class NewAction implements AJAXActionService {

    /**
     * Initializes a new {@link NewAction}.
     */
    public NewAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        long maxSize = sysconfMaxUpload();
        if (!requestData.hasUploads(-1, maxSize > 0 ? maxSize : -1L)) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("Not an upload request.");
        }
        final UploadEvent upload = requestData.getUploadEvent();
        final String moduleParam = requestData.getParameter(AJAXServlet.PARAMETER_MODULE);
        if (moduleParam == null) {
            throw UploadException.UploadCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_MODULE);
        }
        final String fileTypeFilter = requestData.getParameter(AJAXServlet.PARAMETER_TYPE);
        if (fileTypeFilter == null) {
            throw UploadException.UploadCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_TYPE);
        }
        /*
         * Iterate & store uploaded files
         */
        ManagedFileManagement management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
        JSONArray jArray = new JSONArray();
        boolean error = true;
        try {
            for (UploadFile uploadFile : upload.getUploadFiles()) {
                // Check file item
                ContentType ct = new ContentType(uploadFile.getContentType());
                if (!checkFileType(fileTypeFilter, ct)) {
                    throw UploadException.UploadCode.INVALID_FILE_TYPE.create(uploadFile.getContentType(), fileTypeFilter);
                }
                if (DownloadUtility.isIllegalUpload(uploadFile)) {
                    throw UploadException.UploadCode.INVALID_FILE.create();
                }
                jArray.put(processFileItem(uploadFile, session, management));
            }
            AJAXRequestResult result = new AJAXRequestResult(jArray, "json");
            error = false;
            return result;
        } finally {
            if (error) {
                for (Object id : jArray) {
                    removeSafe(id, management);
                }
            }
        }
    }

    private void removeSafe(Object id, ManagedFileManagement management) {
        try {
            management.removeByID(id.toString());
        } catch (Exception e) {
            // Ignore...
        }
    }

    private static long sysconfMaxUpload() {
        final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        if (null == sizeS) {
            return 0;
        }
        return Long.parseLong(sizeS);
    }

    private static String processFileItem(UploadFile fileItem, ServerSession session, ManagedFileManagement management) throws OXException {
        try {
            final ManagedFile managedFile = management.createManagedFile(new FileInputStream(fileItem.getTmpFile()));
            managedFile.setFileName(fileItem.getPreparedFileName());
            managedFile.setContentType(fileItem.getContentType());
            managedFile.setSize(fileItem.getSize());
            managedFile.setAffiliation(session.getSessionID());
            return managedFile.getID();
        } catch (final FileNotFoundException e) {
            throw ManagedFileExceptionErrorMessage.FILE_NOT_FOUND.create(e, e.getMessage());
        }
    }

    private static final String FILE_TYPE_ALL = "file";

    private static final String FILE_TYPE_TEXT = "text";

    private static final String FILE_TYPE_MEDIA = "media";

    private static final String FILE_TYPE_IMAGE = "image";

    private static final String FILE_TYPE_AUDIO = "audio";

    private static final String FILE_TYPE_VIDEO = "video";

    private static final String FILE_TYPE_APPLICATION = "application";

    private static boolean checkFileType(final String filter, final ContentType fileContentType) {
        if (FILE_TYPE_ALL.equalsIgnoreCase(filter)) {
            return true;
        } else if (FILE_TYPE_TEXT.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("text/");
        } else if (FILE_TYPE_MEDIA.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("image/") || fileContentType.startsWith("audio/") || fileContentType.startsWith("video/");
        } else if (FILE_TYPE_IMAGE.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("image/");
        } else if (FILE_TYPE_AUDIO.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("audio/");
        } else if (FILE_TYPE_VIDEO.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("video/");
        } else if (FILE_TYPE_APPLICATION.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("application/");
        }
        return false;
    }

}
