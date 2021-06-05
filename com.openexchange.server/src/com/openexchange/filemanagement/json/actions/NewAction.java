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

package com.openexchange.filemanagement.json.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileExceptionErrorMessage;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.imagetransformation.ImageTransformationDeniedIOException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NewAction implements AJAXActionService {

    /**
     * Initializes a new {@link NewAction}.
     */
    public NewAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        ManagedFileManagement management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
        if (null == management) {
            throw ServiceExceptionCode.absentService(ManagedFileManagement.class);
        }
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
        List<UploadFile> uploadFiles = upload.getUploadFiles();
        JSONArray jArray = new JSONArray(uploadFiles.size());
        boolean error = true;
        try {
            for (UploadFile uploadFile : uploadFiles) {
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
        } catch (ImageTransformationDeniedIOException e) {
            throw UploadException.UploadCode.INVALID_FILE.create(e);
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
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
        } catch (FileNotFoundException e) {
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
